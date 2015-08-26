package org.apache.spark.sql.vitesse

import java.io._
import java.net.{UnknownHostException, Socket}
import java.nio.file.{Paths, Files}
import java.util.Properties

import com.google.protobuf.ByteString
import com.vitessedata.llql.QueryBuilder
import com.vitessedata.llql.llql_proto.{LLQLError, LLQLQuark, LLQLQuery}

import scala.sys.process.Process

import org.apache.spark.{Logging, SparkContext}
/**
 * Created by ftian on 4/13/15.
 */

case class GluonConf (binary: String,
                      appName: String,
                      sessionKey: String,
                      isMaster: Boolean,
                      master_host: String,
                      master_port: Int,
                      localDir: String,
                      sharedStore: String,
                      maxActors: Int,
                      port: Int)
{
  def masterOrSalve = if (isMaster) "master" else "slave"
  def sessionDir = localDir + "/" + sessionKey + "." + masterOrSalve
  def lockFile = sessionDir + "/" + "quark.lock"
  def confFile = sessionDir + "/" + "quark.properties"

  def commandLine = List(
    binary,
    "--app_name=" + appName,
    "--session=" + sessionKey,
    "--ismaster=" + isMaster,
    "--master_host=" + master_host,
    "--master_port=" + master_port,
    "--local_dir=" + localDir,
    "--shared_store=" + sharedStore,
    "--max_actors=" + maxActors,
    "--port=" + port
  )
}

class Gluon(val conf: GluonConf) extends Logging {
  var portInUse :Int = 0
  var socket: Socket = _
  var inStream: DataInputStream = _
  var outStream: DataOutputStream = _

  def connect() = {
    if (portInUse == 0) {
      // If we do not have a valid port, we may need to start Gluon.  Check if someone else
      // have started it for me.
      if (!Files.exists(Paths.get(conf.lockFile))) {
        startGluon()
      }

      // Load the portInUse.  Note that portInUse may be different from the port in conf.
      val p = new Properties()
      val pf = new FileInputStream(conf.confFile)
      p.load(pf)
      portInUse = p.getProperty("vitesse.quark.port_in_use").toInt
      logInfo("Vitesse connect to Gluon at localhost:" + portInUse)
      try {
        socket = new Socket("localhost", portInUse)
        inStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()))
        outStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))
      } catch {
        case e: UnknownHostException => logError("Vitesse: cannot connect to localhost")
        case e: IOException => logError("Vitesse: gluon socket connection failure" + e)
      }
    }
  }

  def disconnect(): Unit = {
    socket.close
    portInUse = 0
  }

  def startGluon(): Unit = {
    for (i <- 1 to 10) {
      if (!Files.exists(Paths.get(conf.lockFile))) {
        val pb = Process(conf.commandLine)
        // This crazy symbol means run to finish.  This is the right thing, because we daemonize cquark.
        pb.!

        // How to block on a file creation?
        // Yeah, sleep is bad.  But need to give the process some time to start, anyway.
        Thread.sleep(100)
      } else {
        return
      }
    }
    throw new Exception("Vitesse Quark failed to start Gluon")
  }

  def sendHdr(dataSz: Int): Unit = {
    // Must match those defined in CQuark side.
    logInfo("Vitesse: Sending Hdr, data size is " + dataSz)
    outStream.writeInt(LLQLQuark.Magic.LLQL_MAGIC_VALUE)
    outStream.writeShort(LLQLQuark.Magic.LLQL_VERSION_VALUE)
    outStream.writeShort(LLQLQuark.MessageType.MSG_QUARK_CMD_VALUE)

    // from_ref, not used by gluon.
    outStream.writeInt(0)
    outStream.writeInt(0)
    outStream.writeInt(0)
    outStream.writeInt(0)

    // to_ref, not used by gluon
    outStream.writeInt(0)
    outStream.writeInt(0)
    outStream.writeInt(0)
    outStream.writeInt(0)

    outStream.writeLong(dataSz) // data len
    outStream.writeInt(0)       // querynode_id, unused
    outStream.writeInt(0)       // marker, not used
    outStream.writeLong(0)      // data checksum, not used for QUARK_CMD at this moment.
    outStream.writeLong(0)      // hdr checksum, not used for QUARK_CMD at this moment.
  }

  def sendBody(data: Array[Byte]): Unit = {
    logInfo("Vitesse: Sending body, data size is " + data.size)
    outStream.write(data)
    outStream.flush()
  }


  def checkInt(a: Int, b: Int): Unit = {
    if (a != b) {
      throw new Exception("Vitesse: Gluon received corrupted message")
    }
  }

  def recvHdr(): Long = {
    logInfo("Vitesse: Trying to receive hdr ... ")
    val magic = inStream.readInt()
    checkInt(magic, LLQLQuark.Magic.LLQL_MAGIC_VALUE)
    val version = inStream.readShort()
    checkInt(version, LLQLQuark.Magic.LLQL_VERSION_VALUE)
    val mt = inStream.readShort()
    checkInt(mt, LLQLQuark.MessageType.MSG_QUARK_CMD_VALUE)

    // from_ref, not used by gluon
    inStream.readInt()
    inStream.readInt()
    inStream.readInt()
    inStream.readInt()

    // to_ref not used by gluon
    inStream.readInt()
    inStream.readInt()
    inStream.readInt()
    inStream.readInt()

    val dataSz = inStream.readLong()

    // Ingore
    inStream.readInt()
    inStream.readInt()
    inStream.readLong()
    inStream.readLong()

    logInfo("Vitesse: receive hdr data size " + dataSz)
    dataSz
  }

  def recvCmd(sz :Long) :LLQLQuark.QuarkCmd = {
    //
    // XXX (ftian):  The Array[Byte] is a must.
    // Seems that google protobuf try to read from socket, and it does not know when message ends.
    // So it tries to read till blocked by TCP, and stuck there.
    // Also, note that we use readFully instead of read, to avoid partial read.
    //
    logInfo("Vitesse: try to recv a command of size " + sz)
    val b = new Array[Byte] (sz.toInt)
    inStream.readFully(b)
    LLQLQuark.QuarkCmd.parseFrom(b)
  }

  def expectOK() {
    val datasz = recvHdr()
    val cmd = recvCmd(datasz)

    logInfo("Vitesse: received command from CQuark.")

    if (cmd.getCt() != LLQLQuark.QuarkCmd.CmdType.RETURN || !cmd.hasErr()) {
      logError("Vitesse: gluon cmd failed.  Returned message has wrong type.")
      throw new Exception("Vitesse: gluon cmd failed, return message has wrong type")
    }

    if (cmd.getErr().getEc() != LLQLError.ErrorCode.EC_OK) {
      logError("Vitesse: gluon cmd failed.  CQuark errored out"
               + cmd.getErr().getEc().toString()
               + cmd.getErr().getMsg());
      throw new Exception("Vitesse: start query failed, cquark errored")
    }
  }

  //
  // Register query sends a query to CQuark.   On CQuark side, the query is kept in a catalog
  // for future execution.  Note that cquark does not run anything -- only when each partition
  // run the RDD.compute, we do real work.  Note that a query should be registered only once,
  // but can be executed/computed many times.
  //
  // Each registered query will stay in CQuark forever -- so a memory leak in theory.  We could
  // hook up a finalizer somewhere but that is too much hacking and not reliable anyway.  This
  // is unlikely to be a real problem, but just in case, later we may need to revisit and fix ...
  //
  def registerQuery(qry_id: String, qry: Array[Byte]) {
    if (true) {
      // debugging stuff.  Dump a llql plan, so that later we can use it as test ...
      logInfo("Vitesse: Dumping query to " + qry_id + " size is " + qry.length);
      val fos = new FileOutputStream("/tmp/" + qry_id + ".llql")
      fos.write(qry)
      fos.close()
    }

    val cb = LLQLQuark.QuarkCmd.newBuilder()
    cb.setCt(LLQLQuark.QuarkCmd.CmdType.REGISTER_QUERY)
    cb.setQueryId(qry_id)
    cb.setQuery(ByteString.copyFrom(qry))
    val ba = cb.build().toByteArray()
    sendHdr(ba.size)
    sendBody(ba)
    expectOK()
  }

  //
  // Execute a partition.  Immediately returns.  Later, RDD should send another message to retrieve
  // RESULT.
  //
  def executePartition(vpart: VitessePartition, stageId: Int): Unit = {
    val cmdb = LLQLQuark.QuarkCmd.newBuilder()
    val cmd = cmdb.setCt(LLQLQuark.QuarkCmd.CmdType.EXECUTE_QUERY)
      .setQueryPartition(
        LLQLQuark.QueryPartition.newBuilder()
        // cmdb.getQueryPartitionBuilder()
          .setQueryId(vpart.queryId)
          .setStageId(stageId)
          .setPartition(vpart.idx)
          .setTotalPartition(vpart.totalPartition)
          .build()
      )
      .setQuery(ByteString.copyFrom(vpart.query))
      .build()
    val ba = cmd.toByteArray()

    logInfo("Vitesse Gluon send EXEC_QUERY " + vpart.queryId + " stage " + stageId)
    sendHdr(ba.size)
    sendBody(ba)
    expectOK()
    logInfo("Vitesse Gluon get back EXEC_QUERY OK")
    // Next, get query result.
  }

  def getQueryResult() :LLQLQuark.RelData = {
    logInfo("Vitesse Gluon get query result.")
    val datasz = recvHdr()
    logInfo("Vitesse Gluon get query result, hdr OK, datasz " + datasz)
    val cmd = recvCmd(datasz)
    logInfo("Vitesse Gluon get query result, data OK.")

    if (!cmd.hasQueryResult()) {
      throw new Exception("Vitesse: Query failed, no result")
    }

    cmd.getQueryResult()
  }
}
