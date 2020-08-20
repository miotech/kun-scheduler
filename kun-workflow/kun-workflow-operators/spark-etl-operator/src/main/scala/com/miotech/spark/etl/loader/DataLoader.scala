package com.miotech.spark.etl.loader

import org.apache.spark.sql.DataFrame

trait DataLoader {

  /**
    * execute a script in datasource
    * @param script
    */
  def execute(script: String)

  def read(dataSet: DataSet, config: Map[String, String]): DataFrame

  def write(dataFrame: DataFrame, dataSet: DataSet, config: Map[String, String])
}