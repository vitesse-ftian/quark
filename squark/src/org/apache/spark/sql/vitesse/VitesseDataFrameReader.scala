package org.apache.spark.sql.vitesse

import org.apache.spark.sql.{DataFrame, DataFrameReader, SQLContext}
import org.apache.spark.sql.types.StructType

/**
 * Created by ftian on 6/16/15.
 */
class VitesseDataFrameReader (vc :VitesseContext) {
  val sqlReader = vc.read
  def parquet(paths: String*): DataFrame = {
    sqlReader.parquet(paths.toList : _*)
  }
}

object VitesseDataFrameReader {
  implicit def toReader(vr :VitesseDataFrameReader) :DataFrameReader = vr.sqlReader
}

