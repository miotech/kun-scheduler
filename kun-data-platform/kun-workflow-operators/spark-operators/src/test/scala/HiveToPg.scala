package com.spark.sparkapp

import java.util.Properties

import org.apache.spark.sql.{SaveMode, SparkSession}

object HiveToPg {
  def main(args: Array[String]) = {
    val spark: SparkSession = {
      SparkSession.builder()
        .appName("testEs")
        .master("local[*]")
        .getOrCreate();
    }

    val prop = new Properties()
    prop.put("user", "root") //表示用户名
    prop.put("password", "root") //表示密码
    prop.put("driver", "org.apache.hive.jdbc.HiveDriver") //表示驱动程序
    val df = spark.
      read.
      jdbc(url = "jdbc:hive2://127.0.0.1:10000/sparktest", table = "test", prop)
    df.show()
    val pgDf = df.toDF("key", "value")
    pgDf.show()
    pgDf.write
      .mode(SaveMode.Overwrite)
      .format("jdbc")
      .option("createTableColumnTypes", "key varchar(128),value varchar(128)")
      .option("url", "jdbc:postgresql://127.0.0.1:54321/kun")
      .option("dbtable", "test_hive_pg")
      .option("user", "postgres")
      .option("password", "password")
      .save()
  }
}
