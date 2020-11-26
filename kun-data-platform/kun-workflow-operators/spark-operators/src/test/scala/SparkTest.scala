package com.spark.sparkapp

import java.util.Properties

import org.apache.spark.sql.{SaveMode, SparkSession}

object SparkTest {
  def main(args: Array[String]) = {
    val spark: SparkSession = {
      SparkSession.builder()
        .appName("testPg")
        .master("local[*]")
        .getOrCreate();
    }

    val prop = new Properties()
    prop.put("user", "postgres") //表示用户名
    prop.put("password", "password") //表示密码
    prop.put("driver", "org.postgresql.Driver") //表示驱动程序

    val df = spark.read.jdbc(url = "jdbc:postgresql://127.0.0.1:54321/kun", table = "kun_wf_operator", prop)
    df.select("id", "name").show()
    df.write
      .mode(SaveMode.Append)
      .format("jdbc")
      .option("url", "jdbc:postgresql://127.0.0.1:54321/kun")
      .option("dbtable", "test_spline")
      .option("user", "postgres")
      .option("password", "password")
      .option("driver", "org.postgresql.Driver")
      .save()
  }
}
