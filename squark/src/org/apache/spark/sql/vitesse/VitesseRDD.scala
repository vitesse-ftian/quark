package org.apache.spark.sql.vitesse

import com.vitessedata.llql.llql_proto.LLQLQuery.Relation
import com.vitessedata.llql.llql_proto.{LLQLExpr, LLQLNs, LLQLData, LLQLQuery}
import org.apache.spark.sql.parquet.ParquetRelation2
import org.apache.spark.{Logging, Partition, SparkContext, TaskContext}

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.plans._
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.plans.physical.{SinglePartition, RangePartitioning, HashPartitioning}
import org.apache.spark.sql.catalyst.expressions._
import org.apache.spark.sql.execution.joins._

import scala.collection.mutable.ArrayBuffer

import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.sql.sources._
import org.apache.spark.sql.execution._

import com.vitessedata.llql._

object VitesseRDD extends Logging {
  //
  // All these super OO/FP machinery, just to make sure the RDD is serializable.
  // See JDBCRDD.
  //
  def getGluonF(gConf: GluonConf) :(VitessePartition) => Gluon = {
    (vpart: VitessePartition) => {

      val conf = GluonConf(
        gConf.binary,
        gConf.appName,
        vpart.sessionKey,
        false,
        vpart.master,
        vpart.masterPort,
        gConf.localDir,
        gConf.sharedStore,
        gConf.maxActors,
        gConf.port
      )
      val g = new Gluon(conf)
      g.connect()
      g
    }
  }

  def scanTable(sc: SparkContext,
                queryId: String,
                requiredCols :Array[String],
                filters: Array[sources.Filter],
                parts: Array[Partition]) = {

    val scConf = sc.getConf

    val gconf = GluonConf(
      scConf.get("vitesse.quark.cquark", "/nx/work/vitessedata.ql/llql/build/cquark"),
      sc.appName,
      "",
      false, // isMaster, slave set it to false.
      "",
      0,
      // getConf("vitesse.quark.local_dir", SparkFiles.getRootDirectory()),
      // Use /nx/cquark, so that dev/debug is easier.   Using SparkFiles will achieve 0-deployment
      scConf.get("vitesse.quark.local_dir", "/nx/cquark"),
      scConf.get("vitesse.quark.shared_store", ""),
      scConf.get("vitesse.quark.max_actors", "4096").toInt,
      scConf.get("vitesse.quark.slave_port", "0").toInt
    )

    new VitesseRDD(sc, getGluonF(gconf), queryId, requiredCols, filters, parts)
  }
}

//
// Maybe, the best example is the JDBCRDD.
//
class VitesseRDD (sc: SparkContext,
                  getGluon: (VitessePartition) => Gluon,
                  val queryId: String,
                  val requiredCols: Array[String],
                  val filters: Array[sources.Filter],
                  val parts: Array[Partition]) extends RDD[Row] (sc, Nil)
{
  override def getPartitions: Array[Partition] = parts

  override def compute(part: Partition, tc: TaskContext) = new Iterator[Row] {
    logInfo("Vitesse: RDD scheduled, this is where we should schedule a task with Gluon")
    logInfo("Vitesse: Task info stageId " + tc.stageId() + ":" + tc.attemptNumber() + ":" + tc.taskAttemptId())

    tc.addTaskCompletionListener{ tc => close() }

    val vpart = part.asInstanceOf[VitessePartition]
    logInfo("Vitesse: RDD partition, master " + vpart.master + ":" + vpart.masterPort + " session " + vpart.sessionKey)
    logInfo("Vitesse: RDD partition, query id " + vpart.queryId + " partition (" + vpart.idx + ":" + vpart.totalPartition + ")")

    var closed = false
    var finished = false
    var gotNext = false
    var nextValue: Row = null

    var currResult : llql_proto.LLQLQuark.RelData = null
    var nextTup: Int = -1

    val gluon = getGluon(vpart)
    gluon.executePartition(vpart, tc.stageId())

    val mutableRow = new SpecificMutableRow(vpart.schema.fields.map(x => x.dataType))

    def buildNextValue(rs: llql_proto.LLQLQuark.RelData, idx :Int) :Row = {
      for (i <- 0 until rs.getNcol()) {
        val dt = vpart.schema.fields(i)
        val colvec = rs.getCols(i)
        if (colvec.getIsnull(idx)) {
          mutableRow.setNullAt(idx)
        } else {
          dt.dataType match {
            case t :BinaryType => mutableRow.update(i, colvec.getBytesval(idx))
            case t :BooleanType => mutableRow.update(i, colvec.getBoolval(idx))
            case t :DateType => mutableRow.update(i, colvec.getI32Val(idx))
              // Decimal NYI
            case t :DoubleType => mutableRow.update(i, colvec.getDoubleval(idx))
            case t :FloatType => mutableRow.update(i, colvec.getFloatval(idx))
            case t :IntegerType => mutableRow.update(i, colvec.getI32Val(idx))
            case t :LongType => mutableRow.update(i, colvec.getI64Val(idx))
              // String, gluon use bytes.
            case t :StringType => mutableRow.update(i, colvec.getBytesval(idx).toStringUtf8())
              // Timestmap NYI
            case x => {
              throw new Exception("Vitesse data convert type NYI")
            }
          }
        }
      }

      mutableRow
    }

    def getNext(): Row = {
      if (currResult == null) {
        if (finished) {
          return null.asInstanceOf[Row]
        }
        currResult = gluon.getQueryResult()
        nextTup = -1
      }

      nextTup += 1
      if (nextTup < currResult.getNtuple()) {
        return buildNextValue(currResult, nextTup)
      } else {
        if (currResult.getLast()) {
          finished = true
        }
        currResult = null
      }

      return getNext()
    }

    def close(): Unit = {
      gluon.disconnect()
    }

    override def hasNext: Boolean = {
      if (!finished) {
        if (!gotNext) {
          nextValue = getNext()
          if (finished) {
            close()
          }
          gotNext = true
        }
      }
      !finished
    }

    override def next(): Row = {
      if (!hasNext) {
        throw new NoSuchElementException("End of Stream")
      }
      gotNext = false
      nextValue
    }
  }
}

case class VitessePartition(master: String, masterPort :Int, sessionKey: String,
                            queryId: String, query :Array[Byte], totalPartition: Int, idx: Int, schema :StructType)
  extends Partition {
  override def index: Int = idx
}

object VitesseRelation extends Logging {
  def partition(master: String, masterPort: Int, sessionKey: String,
                queryId: String, npart: Int, query: Array[Byte], schema: StructType): Array[Partition] = {
    logInfo("Vitesse: create " + npart + " partitions.")
    var ans = new ArrayBuffer[Partition]()
    for (idx <- 0 until npart) {
      ans += VitessePartition(master, masterPort, sessionKey, queryId, query, npart, idx, schema)
    }
    ans.toArray
  }

  def convertType(qb: QueryBuilder, dt: DataType): LLQLData.DataType = dt match {
    case t: BinaryType => qb.buildBinaryType()
    case t: BooleanType => qb.buildBooleanType()
    case t: DateType => qb.buildDateType()
    case t: DecimalType => {
      t.precisionInfo match {
        case Some(PrecisionInfo(pre, sca)) => qb.buildNumericType(pre, sca)
        case None => qb.buildUnlimitedNumericType()
      }
    }
    case t: DoubleType => qb.buildDoubleType()
    case t: FloatType => qb.buildFloatType()
    case t: IntegerType => qb.buildInt32Type()
    case t: LongType => qb.buildInt64Type()
    case t: StringType => qb.buildStringType()
    case t: TimestampType => qb.buildTimestampType()
    case t: OpenHashSetUDT => {
      val et = convertType(qb, t.elementType)
      qb.buildSetType(et)
    }
    case x => {
      throw new Exception("Vitesse Unknown type " + dt)
    }
  }

  def convertCol(qb: QueryBuilder, f: StructField): LLQLData.Schema.Col = {
    LLQLData.Schema.Col.newBuilder()
      .setColType(convertType(qb, f.dataType))
      .setName(f.name)
      .setNullable(f.nullable)
      .build()
  }

  def convertSchema(qb: QueryBuilder, s: StructType): LLQLData.Schema = {
    val llqlschema = LLQLData.Schema.newBuilder()
    for (f <- s.fields) {
      llqlschema.addColumns(convertCol(qb, f))
    }
    llqlschema.build()
  }

  def attrAlias(a: Attribute) = s"${a.name}#${a.exprId.id}"

  def convertOneExpr(qb: QueryBuilder, inputrels: Array[Int], e: Expression, args: Array[LLQLExpr.Expr]): LLQLExpr.Expr = {
    val dt = VitesseRelation.convertType(qb, e.dataType)

    e match {
      // Special exprs.
      case a: AttributeReference => qb.buildColRef(attrAlias(a.toAttribute), inputrels)
      case lit: Literal => qb.buildConst(dt, lit.toString())
      case a: Alias => args(0)

      // Everything else is a function.
      case a: Abs => qb.buildUnaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_ABS_VALUE, args(0))
      case a: Add => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_ADD_VALUE, args(0), args(1))
      case a: org.apache.spark.sql.catalyst.expressions.And => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_AND_VALUE, args(0), args(1))
      case a: Average => qb.buildAggregateFunction(dt, LLQLExpr.Expr.Func.BuiltIn.AGG_AVG_VALUE, args)
      case b: BitwiseAnd => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_BIT_AND_VALUE, args(0), args(1))
      case b: BitwiseNot => qb.buildUnaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_BIT_NOT_VALUE, args(0))
      case b: BitwiseOr => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_BIT_OR_VALUE, args(0), args(1))
      case b: BitwiseXor => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_BIT_XOR_VALUE, args(0), args(1))
      case c: CaseWhen => {
        // See CaseWhen, the following are copied, because these methods are declared private.
        val predicates = c.branches.sliding(2, 2).collect { case Seq(cond, _) => convertExpr(qb, inputrels, cond) }.toArray
        val values = c.branches.sliding(2, 2).collect { case Seq(_, value) => convertExpr(qb, inputrels, value) }.toArray
        val elsevalue = if ((c.branches.length % 2) == 0) {
          null
        } else {
          convertExpr(qb, inputrels, c.branches.last)
        }
        qb.buildCaseWhen(dt, predicates, values, elsevalue)
      }
      case c: Cast => qb.buildUnaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_CAST_VALUE, args(0))
      case c: Coalesce => qb.buildCoalesce(dt, args)
      case c: CollectHashSet => qb.buildAggregateFunction(dt, LLQLExpr.Expr.Func.BuiltIn.AGG_COLLECT_SET_VALUE, args)
      case c: CombineSum => qb.buildAggregateFunction(dt, LLQLExpr.Expr.Func.BuiltIn.AGG_SUM_VALUE, args)
      case c: CombineSetsAndCount => qb.buildAggregateFunction(dt, LLQLExpr.Expr.Func.BuiltIn.AGG_UNION_AND_COUNT_VALUE, args)
      case c: Contains => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_STR_CONTAINS_VALUE, args(0), args(1))
      case c: Count => qb.buildAggregateFunction(dt, LLQLExpr.Expr.Func.BuiltIn.AGG_COUNT_VALUE, args)
      case c: CountDistinct => qb.buildDistinctAggregateFunction(dt, LLQLExpr.Expr.Func.BuiltIn.AGG_COUNT_VALUE, args(0))
      case d: Divide => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_DIVIDE_VALUE, args(0), args(1))
      case e: EndsWith => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_END_WITH_VALUE, args(0), args(1))
      case e: org.apache.spark.sql.catalyst.expressions.EqualTo => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_EQ_VALUE, args(0), args(1))
      case f: First => qb.buildAggregateFunction(dt, LLQLExpr.Expr.Func.BuiltIn.AGG_FIRST_VALUE, args)
      case g: org.apache.spark.sql.catalyst.expressions.GreaterThan => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_GT_VALUE, args(0), args(1))
      case g: org.apache.spark.sql.catalyst.expressions.GreaterThanOrEqual => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_GE_VALUE, args(0), args(1))
      case i: If => {
        val pred = convertExpr(qb, inputrels, i.predicate)
        val tval = convertExpr(qb, inputrels, i.trueValue)
        val fval = convertExpr(qb, inputrels, i.falseValue)
        qb.buildFuncArg3(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_IF_VALUE, pred, tval, fval)
      }
      case i: org.apache.spark.sql.catalyst.expressions.In => {
        val expr = convertExpr(qb, inputrels, i.value)
        val listexprs = i.list.map(VitesseRelation.convertExpr(qb, inputrels, _)).toArray
        qb.buildIn(dt, expr, listexprs)
      }
      case i: InSet => {
        val expr = convertExpr(qb, inputrels, i.value)
        val setexprs = i.hset.toArray[Any].map(_.toString)
        qb.buildInSet(dt, args(0), setexprs)
      }
      case i: org.apache.spark.sql.catalyst.expressions.IsNotNull => qb.buildUnaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_ISNOTNULL_VALUE, args(0))
      case i: org.apache.spark.sql.catalyst.expressions.IsNull => qb.buildUnaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_ISNULL_VALUE, args(0))
      case l: Last => qb.buildAggregateFunction(dt, LLQLExpr.Expr.Func.BuiltIn.AGG_LAST_VALUE, args)
      case l: org.apache.spark.sql.catalyst.expressions.LessThan => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_LT_VALUE, args(0), args(1))
      case l: org.apache.spark.sql.catalyst.expressions.LessThanOrEqual => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_LE_VALUE, args(0), args(1))
      case l: Like => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_LIKE_VALUE, args(0), args(1))
      case l: Lower => qb.buildUnaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_LOWER_VALUE, args(0))
      case m: Max => qb.buildAggregateFunction(dt, LLQLExpr.Expr.Func.BuiltIn.AGG_MAX_VALUE, args)
      case m: Min => qb.buildAggregateFunction(dt, LLQLExpr.Expr.Func.BuiltIn.AGG_MIN_VALUE, args)
      case m: Multiply => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_MULTIPLY_VALUE, args(0), args(1))
      case n: org.apache.spark.sql.catalyst.expressions.Not => qb.buildUnaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_NOT_VALUE, args(0))
      case n: org.apache.spark.sql.catalyst.expressions.Or => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_OR_VALUE, args(0), args(1))
      case r: Remainder => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_REMAINDER_VALUE, args(0), args(1))
      case r: RLike => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_REGEXP_MATCH_VALUE, args(0), args(1))
      case s: Sqrt => qb.buildUnaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_SQRT_VALUE, args(0))
      case s: StartsWith => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_START_WITH_VALUE, args(0), args(1))
      case s: Substring => qb.buildFuncArg3(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_SUBSTRING_VALUE, args(0), args(1), args(2))
      case s: Subtract => qb.buildBinaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_SUBTRACT_VALUE, args(0), args(1))
      case s: Sum => qb.buildAggregateFunction(dt, LLQLExpr.Expr.Func.BuiltIn.AGG_SUM_VALUE, args)
      case s: SumDistinct => qb.buildDistinctAggregateFunction(dt, LLQLExpr.Expr.Func.BuiltIn.AGG_SUM_VALUE, args(0))
      case u: UnaryMinus => qb.buildUnaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_UMINUS_VALUE, args(0))
      case u: Upper => qb.buildUnaryFunction(dt, LLQLExpr.Expr.Func.BuiltIn.FUNC_UPPER_VALUE, args(0))

      case _ => {
        logError("Vitesse: Cannot convert expr " + e.getClass().toString)
        throw new Exception("Vitesse: Convert expression NYI. " + e.getClass().toString)
      }
    }
  }

  def convertExpr(qb: QueryBuilder, inputrels: Array[Int], e: Expression): LLQLExpr.Expr = {
    var inputs = new ArrayBuffer[LLQLExpr.Expr]()
    for (c <- e.children) {
      inputs += convertExpr(qb, inputrels, c)
    }
    convertOneExpr(qb, inputrels, e, inputs.toArray)
  }


  def convertSortOrder(qb: QueryBuilder, inputs: Array[Int], so: SortOrder): LLQLExpr.SortExpr = {
    val expr = convertExpr(qb, inputs, so.child)
    qb.buildSortExpr(expr, so.direction == Descending, false)
  }

  def convertJoinType(jt: JoinType): LLQLQuery.JoinType = {
    jt match {
      case Inner => LLQLQuery.JoinType.INNER
      case LeftOuter => LLQLQuery.JoinType.LEFT_OUTER
      case RightOuter => LLQLQuery.JoinType.RIGHT_OUTER
      case FullOuter => LLQLQuery.JoinType.FULL_OUTER
      case LeftSemi => LLQLQuery.JoinType.LEFT_SEMI
    }
  }

  def convertJoinBuildSide(bs: BuildSide): Int = bs match {
    case BuildLeft => 0
    case BuildRight => 1
  }

  case class TableInfo(tbltype: String, cols: String, paths: String)

  class LogicalTreeWalker(val qb: QueryBuilder, val tbls: scala.collection.mutable.Map[String, Array[String]]) {
    def buildNode(p: LogicalPlan) {
      for (c <- p.children) {
        buildNode(c)
      }
      buildOneNode(p)
    }

    def buildOneNode(p: LogicalPlan): Unit =  {
      if (p.children.size != 0) {
        logInfo("Vitesse: Logical tree walker ignore non leaf " + p)
      } else {
        p match {
          case r @LogicalRelation(rel)=> {
            rel match {
              case pq :ParquetRelation2 => {
                logInfo("Vitesse: Found parquet relation " + r + " " + rel)
                for (col <- r.output) {
                  logInfo("Vitesse: Adding col to path map for paquet table " + col.toString() + "->" + pq.paths)
                  tbls(col.toString()) = pq.paths
                }
              }
              case _ => {
                throw new Exception("Logical reation type " + rel + " NYI")
              }
            }
          }
          case _ => {
            logWarning("Vitesse: scan NYI" + p)
            throw new Exception("Scan NYI")
          }
        }
      }
    }
  }

  class PhysicalTreeWalker(val qb: QueryBuilder, val tbls: scala.collection.mutable.Map[String, Array[String]]) {
    var nextTbl: Int = 0

    def buildOneNode(p: SparkPlan, inputs: Array[Int]): Int = {
      var relid: Int = 0
      val llqlSchema = VitesseRelation.convertSchema(qb, p.schema)

      p match {

        case agg@Aggregate(partial, grpExprs, aggExprs, _) => {
          val llqlGrpExprs = grpExprs.map(VitesseRelation.convertExpr(qb, inputs, _)).toArray
          val llqlAggExprs = aggExprs.map(VitesseRelation.convertExpr(qb, inputs, _)).toArray
          relid = qb.buildGroupBy(inputs, llqlSchema, partial, llqlGrpExprs, llqlAggExprs)
        }

        case hj@BroadcastHashJoin(lk, rk, buildside, l, r) => {
          val lks = lk.map(VitesseRelation.convertExpr(qb, inputs, _)).toArray
          val rks = rk.map(VitesseRelation.convertExpr(qb, inputs, _)).toArray
          relid = qb.buildHashJoin(inputs, llqlSchema, lks, rks,
            convertJoinBuildSide(buildside),
            LLQLQuery.JoinType.INNER, null,
            true
          )
        }

        case hj@BroadcastNestedLoopJoin(l, r, buildside, jt, cond) => {
          relid = qb.buildNestedLoopJoin(inputs, llqlSchema,
            convertJoinBuildSide(buildside),
            convertJoinType(jt),
            cond match {
              case Some(x) => VitesseRelation.convertExpr(qb, inputs, x)
              case _ => null
            },
            true
          )
        }
        case c@CartesianProduct(l, r) => {
          relid = qb.buildNestedLoopJoin(inputs, llqlSchema,
            0, LLQLQuery.JoinType.INNER, null, true)
        }

        case d@Distinct(partial, child) => {
          relid = qb.buildDistinct(inputs, llqlSchema, partial)
        }

        case e@Except(l, r) => {
          relid = qb.buildExcept(inputs, llqlSchema)
        }

        case ex@Exchange(partitioning, ordering, child) => {
          partitioning match {
            case HashPartitioning(exprs, numpart) => {
              val xprs = exprs.map(VitesseRelation.convertExpr(qb, inputs, _)).toArray
              relid = qb.buildPartition(inputs, llqlSchema, "hash", numpart, xprs, null)
            }
            case RangePartitioning(exprs, numpart) => {
              val soxprs = exprs.map(VitesseRelation.convertSortOrder(qb, inputs, _)).toArray
              relid = qb.buildPartition(inputs, llqlSchema, "range", numpart, null, soxprs)
            }
            case SinglePartition => {
              relid = qb.buildPartition(inputs, llqlSchema, "hash", 1, null, null)
            }
            case _ => {
              throw new Exception("Vitesse: Unknown exchange partition type " + partitioning)
            }
          }
        }

        case s@ExternalSort(so, global, child) => {
          val sos = so.map(VitesseRelation.convertSortOrder(qb, inputs, _)).toArray
          relid = qb.buildSort(inputs, llqlSchema, sos)
        }

        case fil@Filter(cond, child) => {
          val condExpr = VitesseRelation.convertExpr(qb, inputs, cond)
          relid = qb.buildFilter(inputs, llqlSchema, condExpr)
        }

        case hj@HashOuterJoin(lk, rk, jt, cond, l, r) => {
          val lks = lk.map(VitesseRelation.convertExpr(qb, inputs, _)).toArray
          val rks = rk.map(VitesseRelation.convertExpr(qb, inputs, _)).toArray
          relid = qb.buildHashJoin(inputs, llqlSchema, lks, rks, 0,
            convertJoinType(jt), null, false)
        }

        case i@Intersect(l, r) => {
          relid = qb.buildIntersect(inputs, llqlSchema)
        }

        case lj@LeftSemiJoinBNL(s, b, cond) => {
          relid = qb.buildNestedLoopJoin(inputs, llqlSchema, 0, LLQLQuery.JoinType.LEFT_SEMI,
            cond match {
              case Some(x) => VitesseRelation.convertExpr(qb, inputs, x)
              case _ => null
            },
            true
          )
        }

        case lj@LeftSemiJoinHash(lk, rk, l, r) => {
          val lks = lk.map(VitesseRelation.convertExpr(qb, inputs, _)).toArray
          val rks = rk.map(VitesseRelation.convertExpr(qb, inputs, _)).toArray
          relid = qb.buildHashJoin(inputs, llqlSchema, lks, rks, 0, LLQLQuery.JoinType.LEFT_SEMI, null, false)
        }

        case lim@Limit(limit, child) => {
          relid = qb.buildLimit(inputs, llqlSchema, limit, 0)
        }

        case o@OutputFaker(out, c) => {
          relid = qb.buildUnion(inputs, llqlSchema)
        }

        case proj@Project(projList, child) => {
          val projExprs = projList.map(VitesseRelation.convertExpr(qb, inputs, _)).toArray
          relid = qb.buildProject(inputs, llqlSchema, projExprs)
        }

        case s@Sample(l, u, withRepl, seed, child) => {
          relid = qb.buildSample(inputs, llqlSchema, u - l, withRepl, seed)
        }

        case s@ShuffledHashJoin(lk, rk, bs, l, r) => {
          val lks = lk.map(VitesseRelation.convertExpr(qb, inputs, _)).toArray
          val rks = rk.map(VitesseRelation.convertExpr(qb, inputs, _)).toArray
          relid = qb.buildHashJoin(inputs, llqlSchema, lks, rks,
            convertJoinBuildSide(bs),
            LLQLQuery.JoinType.INNER, null, false)

        }

        case s@Sort(so, global, child) => {
          val sos = so.map(VitesseRelation.convertSortOrder(qb, inputs, _)).toArray
          relid = qb.buildSort(inputs, llqlSchema, sos)
        }


        case sortlim@TakeOrdered(limit, so, child) => {
          val sos = so.map(VitesseRelation.convertSortOrder(qb, inputs, _)).toArray
          relid = qb.buildSortLimit(inputs, llqlSchema, sos, limit, 0)
        }

        case u@Union(children) => {
          relid = qb.buildUnion(inputs, llqlSchema)
        }

        case r@PhysicalRDD(output, rdd) => {
          val k = output(0).toString()
          logInfo("Vitesse: Checking for scan tbls with col " + k)
          if (tbls.contains(k)) {
            relid = qb.buildParquet(tbls(k), llqlSchema)
          } else {
            throw new Exception("Vitesse buildOneNode physical RDD :" + r + "rdd : " + rdd)
          }
        }

        case _ => {
          throw new Exception("Vitesse buildOneNode Unknown table type :" + p)
        }
      }

      for ((attr, idx) <- p.output.zipWithIndex) {
        val alias = attrAlias(attr)
        logInfo(s"Vitesse: Adding col ref $alias, $relid, $idx")
        qb.addColRefAlias(alias, relid, idx)
      }

      relid
    }

    def buildNode(p: SparkPlan): Int = {
      var inputs = new ArrayBuffer[Int]()
      for (c <- p.children) {
        inputs += buildNode(c)
      }
      buildOneNode(p, inputs.toArray)
    }
  }

  //
  // The following rant may not apply any more, because we moved our package from com.vitesse
  // into org.apache.spark, so, package private may not be a problem any more.
  //
  // XXX XXX (ftian): Terrible Terrible HACK.
  //
  //  Spark is heavily Scala OO, to the degree that it is very hard to comprehend what
  //  the plan is.  The exePlan will scan a PhysicalRDD, which for now, will be a parquet
  //  file.  However, the exePlan is physical, means it is difficult to figure out what
  //  it really scans.
  //
  //  So we took the optPlan, which is logical.  I assume that the plan has been optimized,
  //  so that the shape of the tree won't change.  ExePlan may add Exchange, decorate a hashjoin
  //  with Broadcast or Hash distribution, but, if we walk the both optPlan and exePlan, we should
  //  visit base relations in same order.
  //
  //  But, the optPlan is package private.  Cannot access it.  Doh!
  //
  //  So we deparse the string, using a Regex.  This is just OO goes totally crazy.  Or there
  //  are better ways to do this, but I gave up, for now.
  //
  //  Also note that we indeed, lost valuable information here.   Spark did Parition Pruning on
  //  parquet table.  While later we can do partition pruning ourself, it may create more skew.
  //  so we need to either replicate Spark's logic Vitesse, or, dig deeper and pull out the Parquet
  //  partition info.   Good news is that, the partition scheme is rather simple, it is encoded in
  //  the group of paths, like below.
  //    {{{
  //       hdfs://<host>:<port>/path/to/partition/a=1/b=hello/c=3.14
  //       hdfs://<host>:<port>/path/to/partition/a=2/b=world/c=6.28
  //    }}}
  //

  def createLLQLPlan(optPlan: LogicalPlan, exePlan: SparkPlan): LLQLQuery.Query = {
    val qb = new QueryBuilder()
    val tbls = scala.collection.mutable.Map[String, Array[String]]()
    val lw = new LogicalTreeWalker(qb, tbls)
    lw.buildNode(optPlan)

    val pw = new PhysicalTreeWalker(qb, tbls)

    val root = pw.buildNode(exePlan)
    qb.setRoot(root)
    qb.build()
  }
}

case class VitesseRelation(master: String, masterPort: Int, sessionKey: String,
                           queryId: String, npart: Int, query :Array[Byte], val sparkSchema :StructType)
                          (@transient val vitesseCtxt: VitesseContext)
extends BaseRelation with PrunedFilteredScan
{
  override val schema = sparkSchema
  override val sqlContext = vitesseCtxt
  override def buildScan(requiredCols: Array[String], filters: Array[sources.Filter]) = {
    val parts = VitesseRelation.partition(master, masterPort, sessionKey, queryId, npart, query, schema)
    VitesseRDD.scanTable(sqlContext.sparkContext, queryId, requiredCols, filters, parts)
  }
}

