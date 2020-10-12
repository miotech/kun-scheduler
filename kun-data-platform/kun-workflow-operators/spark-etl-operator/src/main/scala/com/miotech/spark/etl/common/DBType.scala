package com.miotech.spark.etl.common

sealed trait DBType { def name: String }
case object MYSQL extends DBType {
  override def name: String = "mysql"
}
case object POSTGRESQL extends DBType {
  override def name: String = "postgresql"
}
case object SQLSERVER extends DBType {
  override def name: String = "sqlserver"
}
