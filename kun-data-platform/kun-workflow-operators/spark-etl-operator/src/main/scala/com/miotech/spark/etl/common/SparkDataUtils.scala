package com.miotech.spark.etl.common

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, concat_ws, lit, md5}

object SparkDataUtils {

  object implicits {

    trait DataFrameUtils {
      val value: DataFrame
      def addMD5(colName: Option[String] = None): DataFrame = {
        value.withColumn(colName.getOrElse("md5"), md5(concat_ws("", value.columns.map(_.toString).sorted.map(col(_)): _*)))
      }

      private def addUpdateTime(df: DataFrame): DataFrame = {
        df.withColumn("updateTime", lit(System.currentTimeMillis))
      }

      def addValidSignature(valid: Boolean): DataFrame = {
        value.withColumn("isValid", lit(if (valid) "Y" else "N"))
      }

      def addGlobalId(colNames: Seq[String], tableName: String): DataFrame = {
        val columnsToHash = colNames.sorted.map(col) ++ Seq(lit(tableName))
        value.withColumn("globalId", md5(concat_ws("", columnsToHash: _*)))
      }

      def lowerCaseColumns(): DataFrame = {
        value.select(value.columns.map(c => col(c).alias(c.toLowerCase)): _*)
      }

      def camelCaseColumns(): DataFrame = {
        value.select(value.columns.map(c => {
          val alias = c.split("_")
            .zipWithIndex
            .map { t =>
              if (t._2 == 0) t._1 else t._1.capitalize
            }.mkString("")
          col(c).alias(alias)
        }): _*)
      }
    }

    implicit def dataFrameToDataFrameUtilsConverter(item: DataFrame): DataFrameUtils = new DataFrameUtils {
      override val value: DataFrame = item
    }
  }
}
