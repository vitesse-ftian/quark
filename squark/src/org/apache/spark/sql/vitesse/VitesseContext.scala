package org.apache.spark.sql.vitesse

import java.util.UUID

import com.vitessedata.llql.llql_proto.LLQLQuery
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.catalyst.plans.physical.ClusteredDistribution

import org.apache.spark.{SparkEnv, SparkFiles, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.execution.SparkPlan

//
// XXX:
// 	VitesseContext is the execution context so that we can hijack Spark
// 	query execution.  The "right" way to do it, seems to be having our own
// 	QueryExecution class, then override bunch of method. 	But feels like an overkill.
//
//  Do it with direct, brutal force, with a thin wrapper Context and VitesseDataFrame.
// 	Real work happens in VitesseRDD.
//
class VitesseContext(sc: SparkContext) extends SQLContext(sc) {

	@transient
	val gluon :Gluon = {

		// logInfo("Vitesse: Start gluon: (app:session) is (" + sc.appName + ":" + sc.applicationId + ")")
		// logInfo("Vitesse: Start gluon: SparkFiles local dir is " + SparkFiles.getRootDirectory())
		// logInfo("Vitesse: Spart executorId is " + SparkEnv.get.executorId)

		val conf = GluonConf(
			getConf("vitesse.quark.cquark", "/nx/work/vitessedata.ql/llql/build/cquark"),
			sc.appName,
			getConf("vitesse.quark.sessions", UUID.randomUUID().toString()),
			true,
			getConf("vitesse.quark.master", "localhost"),
			getConf("vitesse.quark.master_port", "0").toInt,
			// getConf("vitesse.quark.local_dir", SparkFiles.getRootDirectory()),
			// Use /nx/cquark, so that dev/debug is easier.   Using SparkFiles will achieve 0-deployment
			getConf("vitesse.quark.local_dir", "/nx/cquark"),
			getConf("vitesse.quark.shared_store", ""),
			getConf("vitesse.quark.max_actors", "4096").toInt,
			getConf("vitesse.quark.master_port", "0").toInt
		)

		val g =  new Gluon(conf)
		g.connect()
		g
	}

	def vitesseRead = new VitesseDataFrameReader(this)

	override def sql(sqlText: String): DataFrame = {
    logWarning("Vitesse: context intercept running sql ..." + sqlText)
		val plan = parseSql(sqlText)
		val df = new VitesseDataFrame(this, plan)

		if (getConf("spark.sql.dialect", "vitesse-sql") == "sql") {
      logWarning("Vitesse running sql in sql context.")
			return df
		}

		try {
      logWarning("Vitesse running sql using vitesse context.")

			// XXX XXX
			//
			// See SQLConf.scala, but it is not exposed.  Why?
			// private[spark] def numShufflePartitions: Int = getConf(SHUFFLE_PARTITIONS, "200").toInt
			//
			// Number of partitions: there is a sql.shuffle.partitions, and there is a spark.cores.max.
			// We pick the smaller one.
			//
			// Also by default, spark refuse to schedule more partitions than totalCores -- but Quark need
			// have all partitions running at the same time.  TotalCores is captured by the defaultParallelism
			// but I don't know if this will stay true in later spark distributions.
			//
			// In short -- I don't know what is the right way of computing nPart.   This is what we come up
			// for now.
			val nShuffleConf = "spark.sql.shuffle.partitions"
			val nShuffle = getConf(nShuffleConf, "1000000").toInt
			val coresMaxConf = "spark.cores.max"
			val coresMax = getConf(coresMaxConf, "1000000").toInt
			val nCores = sc.defaultParallelism

			val nPart = math.min(math.min(nShuffle, coresMax), nCores)

			logInfo("Vitesse: Schedule SQL ViteseseRel, nShuffle: " + nShuffle
				        + ", coresMax: " + coresMax + ", nCores " + nCores + ", nPart " + nPart)

			val qryId = UUID.randomUUID().toString()

			val llql = df.llqlQuery().toByteArray()
			gluon.registerQuery(qryId, llql)
			return df.toVitesseRel(gluon.conf.master_host, gluon.portInUse, gluon.conf.sessionKey, qryId, nPart, llql)
		} catch {
			case ex: Throwable => logWarning("Vitesse cannot execute query.  UP CALL.   Exception is " + ex)
		}

		return df
	}

	def debugLogInfo(msg: String, ex: Throwable): Unit = {
		logInfo("Vitesse: Debug info " + msg)
	}
	def debugLogWarning(msg: String): Unit = {
		logWarning("Vitesse: Debug warning " + msg)
	}
	def debugLogError(msg: String): Unit = {
		logError("Vitesse: !!!!!!! DEBUG ERROR !!!!! .  Should BOMBED. " + msg)
	}
	def debugLogError(msg: String, ex: Throwable): Unit = {
		logError("Vitesse: !!!!!!! DEBUG ERROR !!!!! .  Should BOMBED. " + msg, ex)
	}
}

class VitesseDataFrame(sc: SQLContext, plan: LogicalPlan)
	extends DataFrame(sc, plan) {
	def vitesseCtxt = sqlContext.asInstanceOf[VitesseContext]

	def llqlQuery() = VitesseRelation.createLLQLPlan(
		queryExecution.optimizedPlan,
		queryExecution.executedPlan
	)

	def toVitesseRel(master: String, masterPort: Int, sessionKey: String, queryId: String, npart: Int, query: Array[Byte]) = {
		// SparkPlan is still available as executedPlan, but seems we do not need it
		// any more.
		val rel = new VitesseRelation(master, masterPort, sessionKey, queryId, npart, query, queryExecution.executedPlan.schema)(vitesseCtxt)
		sqlContext.baseRelationToDataFrame(rel)
	}
}
