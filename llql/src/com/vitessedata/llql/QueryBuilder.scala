package com.vitessedata.llql

import akka.io.Udp.SO.Broadcast
import com.google.protobuf.ByteString
import com.vitessedata.llql.llql_proto.LLQLQuery.Relation
import com.vitessedata.llql.llql_proto._

class QueryBuilder() {
  val qb = LLQLQuery.Query.newBuilder()

  var nextRelId :Int = 0
  private val relMaps = scala.collection.mutable.Map[Int, Relation]()
  def setRoot(root: Int): Unit = {
    qb.setRoot(root)
  }

  def build(): LLQLQuery.Query = {
    for ( relid <- 0 to nextRelId) {
      if (relMaps.contains(relid)) {
        val rel = relMaps(relid)
        qb.addRels(rel)
      }
    }

    qb.build()
  }

  //
  // Data types.
  //
  def buildBinaryType() = LLQLData.DataType.newBuilder()
    .setTypeInfo(
      LLQLData.DataType.TypeInfo.newBuilder()
        .setNs(LLQLNs.Namespace.LLQL)
        .setTypeId(LLQLData.DataType.BuiltinType.BINARY_VALUE)
        .setLen(-1).setAlign(1)
        .build()
    ).build()

  def buildBooleanType() =  LLQLData.DataType.newBuilder()
    .setTypeInfo(
      LLQLData.DataType.TypeInfo.newBuilder()
        .setNs(LLQLNs.Namespace.LLQL)
        .setTypeId(LLQLData.DataType.BuiltinType.BOOLEAN_VALUE)
        .setLen(1).setAlign(1)
        .build()
    ).build()

  def buildDateType() =  LLQLData.DataType.newBuilder()
    .setTypeInfo(
      LLQLData.DataType.TypeInfo.newBuilder()
        .setNs(LLQLNs.Namespace.LLQL)
        .setTypeId(LLQLData.DataType.BuiltinType.DATE_VALUE)
        .setLen(4).setAlign(4)
        .build()
    ).build()

  def buildUnlimitedNumericType() =  LLQLData.DataType.newBuilder()
    .setTypeInfo(
      LLQLData.DataType.TypeInfo.newBuilder()
        .setNs(LLQLNs.Namespace.LLQL)
        .setTypeId(LLQLData.DataType.BuiltinType.NUMERIC_VALUE)
        .setLen(-1).setAlign(1)
        .build()
    ).build()

  def buildNumericType(pre :Int, sca :Int) =  LLQLData.DataType.newBuilder()
    .setTypeInfo(
      LLQLData.DataType.TypeInfo.newBuilder()
        .setNs(LLQLNs.Namespace.LLQL)
        .setTypeId(LLQLData.DataType.BuiltinType.NUMERIC_VALUE)
        .setLen(-1).setAlign(1)
        .setOpt1(pre).setOpt2(sca)
        .build()
    ).build()

  def buildDoubleType() =  LLQLData.DataType.newBuilder()
    .setTypeInfo(
      LLQLData.DataType.TypeInfo.newBuilder()
        .setNs(LLQLNs.Namespace.LLQL)
        .setTypeId(LLQLData.DataType.BuiltinType.FLOAT64_VALUE)
        .setLen(8).setAlign(8)
        .build()
    ).build()

  def buildFloatType() =  LLQLData.DataType.newBuilder()
    .setTypeInfo(
      LLQLData.DataType.TypeInfo.newBuilder()
        .setNs(LLQLNs.Namespace.LLQL)
        .setTypeId(LLQLData.DataType.BuiltinType.FLOAT32_VALUE)
        .setLen(4).setAlign(4)
        .build()
    ).build()

  def buildInt32Type() =  LLQLData.DataType.newBuilder()
    .setTypeInfo(
      LLQLData.DataType.TypeInfo.newBuilder()
        .setNs(LLQLNs.Namespace.LLQL)
        .setTypeId(LLQLData.DataType.BuiltinType.INT32_VALUE)
        .setLen(4).setAlign(4)
        .build()
    ).build()

  def buildInt64Type() =  LLQLData.DataType.newBuilder()
    .setTypeInfo(
      LLQLData.DataType.TypeInfo.newBuilder()
        .setNs(LLQLNs.Namespace.LLQL)
        .setTypeId(LLQLData.DataType.BuiltinType.INT64_VALUE)
        .setLen(8).setAlign(8)
        .build()
    ).build()

  def buildStringType() =  LLQLData.DataType.newBuilder()
    .setTypeInfo(
      LLQLData.DataType.TypeInfo.newBuilder()
        .setNs(LLQLNs.Namespace.LLQL)
        .setTypeId(LLQLData.DataType.BuiltinType.STRING_VALUE)
        .setLen(-1).setAlign(1)
        .build()
    ).build()

  def buildTimestampType() = LLQLData.DataType.newBuilder()
    .setTypeInfo(
      LLQLData.DataType.TypeInfo.newBuilder()
        .setNs(LLQLNs.Namespace.LLQL)
        .setTypeId(LLQLData.DataType.BuiltinType.TIMESTAMP_VALUE)
        .setLen(8).setAlign(8)
        .build()
    ).build()

  //
  // Spark OpenHashSet seems just support one element type, but, I don't know
  // if spark will allow a tuple as the element type -- will see, but for now,
  // lets just handle set of one element type.  C-Quark side, by design, should
  // be able to handle any nesting of tuple/set/array.
  //
  def buildSetType(et: LLQLData.DataType) = {
    val b = LLQLData.DataType.newBuilder()
      .setTypeInfo(
        LLQLData.DataType.TypeInfo.newBuilder()
          .setNs(LLQLNs.Namespace.LLQL)
          .setTypeId(LLQLData.DataType.BuiltinType.SET_VALUE)
          .setLen(8).setAlign(8)
          .build())

    // for (et <- ets) {
      b.addElemTypes(et)
    // }

    b.build()
  }

  //
  // Exprs.
  //
  def checkExpr(e: LLQLExpr.Expr) {
    var cnt :Int = 0
    if (e.hasConstVal()) {
      cnt += 1
    }
    if (e.hasColref()) {
      cnt += 1
    }
    if (e.hasFunc()) {
      cnt += 1
    }

    if (cnt != 1) {
      throw new Exception("ConvertExpr failed for expression " + e)
    }
  }

  def buildColRef(alias :String, inputrels :Array[Int]) :LLQLExpr.Expr = {
    for (relid <- inputrels) {
      val k = (alias, relid)
      if (colrefAliases.contains(k)) {
        val colid = colrefAliases(k)
        val rel = relMaps(relid)
        val dt = rel.getSchema().getColumns(colid).getColType()
        val colref = LLQLExpr.Expr.ColRef.newBuilder().setRelId(relid).setColId(colid).build()
        return LLQLExpr.Expr.newBuilder().setDt(dt).setColref(colref).build()
      }
    }
    throw new Exception("Vitesse: Try to bulid Invalid ColRef. " + alias)
  }


  def buildConst(dt: LLQLData.DataType, sval :String) = {
    LLQLExpr.Expr.newBuilder()
      .setDt(dt)
      .setConstVal(
        LLQLExpr.Expr.Const.newBuilder()
        .setIsNull(sval == "null")
        .setStrVal(sval)
        .build()
      )
      .build()
  }

  // Everything else is a function.
  def buildUnaryFunction(dt: LLQLData.DataType, fnid :Int, arg1: LLQLExpr.Expr) = {
    checkExpr(arg1)
    LLQLExpr.Expr.newBuilder()
      .setDt(dt)
      .setFunc(
        LLQLExpr.Expr.Func.newBuilder()
          .setNs(LLQLNs.Namespace.LLQL)
          .setFuncId(fnid)
          .setIsVolatile(false)
          .setIsStrict(true)
          .setIsDistinct(false)
          .addArgs(arg1)
          .build()
      )
      .build()
  }

  def buildBinaryFunction(dt: LLQLData.DataType, fnid :Int, arg1: LLQLExpr.Expr, arg2: LLQLExpr.Expr) = {
    checkExpr(arg1)
    checkExpr(arg2)
    LLQLExpr.Expr.newBuilder()
      .setDt(dt)
      .setFunc(
        LLQLExpr.Expr.Func.newBuilder()
          .setNs(LLQLNs.Namespace.LLQL)
          .setFuncId(fnid)
          .setIsVolatile(false)
          .setIsStrict(true)
          .setIsDistinct(false)
          .addArgs(arg1)
          .addArgs(arg2)
          .build()
      )
      .build()
  }

  def buildFuncArg3(dt: LLQLData.DataType, fnid :Int, arg1: LLQLExpr.Expr, arg2: LLQLExpr.Expr, arg3: LLQLExpr.Expr) = {
    checkExpr(arg1)
    checkExpr(arg2)
    checkExpr(arg3)
    LLQLExpr.Expr.newBuilder()
      .setDt(dt)
      .setFunc(
        LLQLExpr.Expr.Func.newBuilder()
          .setNs(LLQLNs.Namespace.LLQL)
          .setFuncId(fnid)
          .setIsVolatile(false)
          .setIsStrict(true)
          .setIsDistinct(false)
          .addArgs(arg1)
          .addArgs(arg2)
          .addArgs(arg3)
          .build()
      )
      .build()
  }

  def buildAggregateFunction(dt: LLQLData.DataType, fnid :Int, args: Array[LLQLExpr.Expr]) = {
    val fb = LLQLExpr.Expr.Func.newBuilder()
           .setNs(LLQLNs.Namespace.LLQL)
           .setFuncId(fnid)
           .setIsVolatile(false)
           .setIsStrict(true)
           .setIsDistinct(false)

    for (arg <- args) {
      checkExpr(arg)
      fb.addArgs(arg)
    }

    LLQLExpr.Expr.newBuilder()
      .setDt(dt)
      .setFunc( fb.build() )
      .build()
  }

  def buildDistinctAggregateFunction(dt: LLQLData.DataType, fnid :Int, arg1: LLQLExpr.Expr) = {
    checkExpr(arg1)
     LLQLExpr.Expr.newBuilder()
      .setDt(dt)
      .setFunc(
         LLQLExpr.Expr.Func.newBuilder()
           .setNs(LLQLNs.Namespace.LLQL)
           .setFuncId(fnid)
           .setIsVolatile(false)
           .setIsStrict(true)
           .setIsDistinct(true)
           .addArgs(arg1)
           .build()
       )
      .build()
  }

  // Several special expressions.
  def buildCaseWhen(dt: LLQLData.DataType, pred: Array[LLQLExpr.Expr], thenval: Array[LLQLExpr.Expr],
                    elseval: LLQLExpr.Expr) = {
    val f =  LLQLExpr.Expr.Func.newBuilder()
      .setNs(LLQLNs.Namespace.LLQL)
      .setFuncId(LLQLExpr.Expr.Func.BuiltIn.FUNC_CASE_VALUE)
      .setIsVolatile(false)
      .setIsStrict(true)
      .setIsDistinct(false)

    val ncase = pred.length
    for (i <- 0 until ncase) {
      f.addArgs(pred(0))
      f.addArgs(thenval(0))
    }

    if (elseval != null) {
      f.addArgs(elseval)
    }

    LLQLExpr.Expr.newBuilder()
      .setDt(dt)
      .setFunc(f.build())
      .build()
  }

  def buildCoalesce(dt: LLQLData.DataType, args: Array[LLQLExpr.Expr]) = {
    val f = LLQLExpr.Expr.Func.newBuilder().setNs(LLQLNs.Namespace.LLQL)
      .setFuncId(LLQLExpr.Expr.Func.BuiltIn.FUNC_COALESCE_VALUE)
      .setIsVolatile(false).setIsStrict(true).setIsDistinct(false)
    for (a <- args) {
      f.addArgs(a)
    }

    val b = LLQLExpr.Expr.newBuilder()
      .setDt(dt)
      .setFunc(f.build())

    b.build()
  }

  def buildIn(dt: LLQLData.DataType, value: LLQLExpr.Expr, inlist: Array[LLQLExpr.Expr]) = {
    val f =  LLQLExpr.Expr.Func.newBuilder()
      .setNs(LLQLNs.Namespace.LLQL)
      .setFuncId(LLQLExpr.Expr.Func.BuiltIn.FUNC_IN_VALUE)
      .setIsVolatile(false)
      .setIsStrict(true)
      .setIsDistinct(false)
      .addArgs(value)

    for (inval <- inlist) {
      f.addArgs(inval)
    }

    LLQLExpr.Expr.newBuilder()
      .setDt(dt)
      .setFunc(f.build())
      .build()
  }

  def buildInSet(dt: LLQLData.DataType, value: LLQLExpr.Expr, inlist: Array[String]) = {
     val f =  LLQLExpr.Expr.Func.newBuilder()
      .setNs(LLQLNs.Namespace.LLQL)
      .setFuncId(LLQLExpr.Expr.Func.BuiltIn.FUNC_IN_VALUE)
      .setIsVolatile(false)
      .setIsStrict(true)
      .setIsDistinct(false)
      .addArgs(value)

     for (v <- inlist) {
       val cv = buildConst(value.getDt(), v)
       f.addArgs(cv)
     }

     LLQLExpr.Expr.newBuilder()
      .setDt(dt)
      .setFunc(f.build())
      .build()
  }


  // SortExpr is an expr with sort info (desc and nullfirst)
  def buildSortExpr(expr: LLQLExpr.Expr, desc: Boolean, nfirst: Boolean): LLQLExpr.SortExpr =  {
    LLQLExpr.SortExpr.newBuilder()
    .setExpr(expr)
    .setDesc(desc)
    .setNullFirst(nfirst)
    .build()
  }

  //
  // Query nodes.
  //
  def buildRelationPre(inputs :Array[Int], schema :LLQLData.Schema) = {
    val relid = nextRelId
    nextRelId += 1

    val rb = LLQLQuery.Relation.newBuilder()
      .setRelId(relid)
      .setSchema(schema)

    if (inputs != null) {
      for (c <- inputs) {
        rb.addChildren(c)
      }
    }
    rb
  }

  def buildRelationPost(rb: LLQLQuery.Relation.Builder) = {
    val r = rb.build()
    val relid = r.getRelId()
    relMaps(relid) = r
    relid
  }

  def buildDistinct(inputs: Array[Int], schema :LLQLData.Schema, partial :Boolean) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val d = LLQLQuery.Distinct.newBuilder()
            .setIsPartial(partial)
    rb.setDistinct(d.build())
    buildRelationPost(rb)
  }

  def buildExcept(inputs: Array[Int], schema :LLQLData.Schema) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val setop = LLQLQuery.SetOp.newBuilder()
      .setSetopType(LLQLQuery.SetOp.SetOpType.EXCEPT)
    rb.setSetop(setop.build())
    buildRelationPost(rb)
  }

  def buildFilter(inputs: Array[Int], schema :LLQLData.Schema, pred: LLQLExpr.Expr) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val f = LLQLQuery.Filter.newBuilder()
      .setPredicate(pred)
    rb.setFilter(f.build())
    buildRelationPost(rb)
  }

  def buildGroupBy(inputs: Array[Int], schema: LLQLData.Schema, partial :Boolean,
                   grpExprs :Array[LLQLExpr.Expr], projExprs :Array[LLQLExpr.Expr]) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val g = LLQLQuery.HashAgg.newBuilder()
      .setIsPartial(partial).setIsTop(false)

    for (ge <- grpExprs) {
      g.addGrpExprs(ge)
    }
    for (pe <- projExprs) {
      g.addProjExprs(pe)
    }

    rb.setHashagg(g.build())
    buildRelationPost(rb)
  }

  def buildHashJoin(inputs: Array[Int], schema :LLQLData.Schema,
                    lks :Array[LLQLExpr.Expr], rks :Array[LLQLExpr.Expr],
                    bs :Int, jt :LLQLQuery.JoinType, p :LLQLExpr.Expr, broadcast: Boolean) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val hj = LLQLQuery.HashJoin.newBuilder()
      .setBuildSide(bs)
      .setJoinType(jt)

    if (p != null) {
      hj.setExtraPredicate(p)
    }

    if (lks.length == 0 || lks.length != rks.length) {
      throw new Exception("Vitesse: LLQL HJ busted")
    }

    for (lk <- lks) {
      hj.addLeftKeys(lk)
    }

    for (rk <- rks) {
      hj.addRightKeys(rk)
    }

    hj.setBroadcast(broadcast)

    rb.setHashjoin(hj.build())
    buildRelationPost(rb)
  }

  def buildIntersect(inputs: Array[Int], schema :LLQLData.Schema) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val setop = LLQLQuery.SetOp.newBuilder()
      .setSetopType(LLQLQuery.SetOp.SetOpType.INTERSECT)

    rb.setSetop(setop.build())
    buildRelationPost(rb)
  }

  def buildLimit(inputs: Array[Int], schema :LLQLData.Schema, limit :Long, offset :Long) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val op = LLQLQuery.Limit.newBuilder()
        .setLimit(limit)
        .setOffset(offset)

    rb.setLimit(op.build())
    buildRelationPost(rb)
  }

  def buildNestedLoopJoin(inputs :Array[Int], schema :LLQLData.Schema,
                          bs :Int, jt :LLQLQuery.JoinType, p :LLQLExpr.Expr, broadcast :Boolean) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val nlj = LLQLQuery.NestedLoopJoin.newBuilder()
      .setBuildSide(bs)
      .setJoinType(jt)

    if (p != null) {
      nlj.setPredicate(p)
    }

    nlj.setBroadcast(broadcast)

    rb.setNestedloopjoin(nlj.build())

    buildRelationPost(rb)
  }

  def buildParquet(paths :Array[String], schema :LLQLData.Schema) :Int = {
    val rb = buildRelationPre(null, schema)
    val scan = LLQLQuery.ExtScan.newBuilder()
      .setDsType(LLQLQuery.ExtScan.DSType.PARQUET)
      .setCols(schema)

    for (path <- paths) {
      scan.addUrls(path)
    }

    rb.setExtscan(scan.build())
    buildRelationPost(rb)
  }

  def buildPartition(inputs: Array[Int], schema: LLQLData.Schema, xt :String,
                     np :Int, exprs :Array[LLQLExpr.Expr], soexprs :Array[LLQLExpr.SortExpr]) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val ex = LLQLQuery.Exchange.newBuilder()
      .setXPart(np)

    val xtype = xt match {
      case "broadcast" => {
        ex.setXType(LLQLQuery.Exchange.ExchangeType.BROADCAST)
      }
      case "hash" => {
        ex.setXType(LLQLQuery.Exchange.ExchangeType.HASH)
        if (exprs != null) {
          for (e <- exprs) {
            ex.addXExprs(e)
          }
        }
      }
      case "range" => {
        ex.setXType(LLQLQuery.Exchange.ExchangeType.RANGE)
        if (soexprs != null) {
          for (e <- soexprs) {
            ex.addXSortexprs(e)
          }
        }
      }
      case _ => throw new Exception("Unknown partition type " + xt)
    }

    rb.setExchange(ex.build())
    buildRelationPost(rb)
  }

  def buildProject(inputs: Array[Int], schema: LLQLData.Schema, exprs :Array[LLQLExpr.Expr]) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val op = LLQLQuery.Project.newBuilder()
    for (ex <- exprs) {
      op.addExprs(ex)
    }
    rb.setProject(op.build())
    buildRelationPost(rb)
  }

  def buildSample(inputs: Array[Int], schema :LLQLData.Schema, frac :Double, withRepl :Boolean, seed :Long) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val op = LLQLQuery.Sample.newBuilder()
      .setFraction(frac)
      .setWithReplacement(withRepl)
      .setSeed(seed)
    rb.setSample(op.build())
    buildRelationPost(rb)
  }

  def buildSort(inputs: Array[Int], schema :LLQLData.Schema, sos :Array[LLQLExpr.SortExpr]) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val s = LLQLQuery.Sort.newBuilder()
    for (so <- sos) {
      s.addSortExprs(so)
    }
    rb.setSort(s.build())
    buildRelationPost(rb)
  }

  def buildSortLimit(inputs: Array[Int], schema :LLQLData.Schema, sos :Array[LLQLExpr.SortExpr], limit :Long, offset :Long) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val s = LLQLQuery.SortLimit.newBuilder()
      .setLimit(limit)
      .setOffset(offset)

    for (so <- sos) {
      s.addSortExprs(so)
    }
    rb.setSortlimit(s.build())
    buildRelationPost(rb)
  }

  def buildUnion(inputs: Array[Int], schema :LLQLData.Schema) :Int = {
    val rb = buildRelationPre(inputs, schema)
    val setop = LLQLQuery.SetOp.newBuilder()
      .setSetopType(LLQLQuery.SetOp.SetOpType.UNION)
    rb.setSetop(setop.build())
    buildRelationPost(rb)
  }

  private val colrefAliases = scala.collection.mutable.Map[(String, Int), Int]()
  def addColRefAlias(s :String, relid :Int, col :Int) = {
    val k = (s, relid)
    colrefAliases(k) = col
  }
}