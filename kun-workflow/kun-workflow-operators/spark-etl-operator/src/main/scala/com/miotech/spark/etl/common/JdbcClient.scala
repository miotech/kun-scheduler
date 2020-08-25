package com.miotech.spark.etl.common

import java.sql.{Connection, DriverManager, SQLException}

import org.slf4j.LoggerFactory

object JdbcClient {

  def apply(dbTypeName: String,
            url: String,
            username: String,
            password: String
           ) = {
    val dbType = dbTypeName match {
      case "postgresql" => POSTGRESQL
      case "mysql" => MYSQL
      case "sqlserver" => SQLSERVER
    }
    new JdbcClient(dbType, url, username, password)
  }
}

class JdbcClient( dbType: DBType,
                  url: String,
                  username: String,
                  password: String
                ) {

  private val logger = LoggerFactory.getLogger(getClass)

  private var conn: Connection = _

  private val jdbcDriverMap = Map(
    POSTGRESQL.name -> "org.postgresql.Driver",
    MYSQL.name -> "com.mysql.cj.jdbc.Driver",
    SQLSERVER.name -> "com.microsoft.sqlserver.jdbc.SQLServerDriver"
  )

  val driver: String = jdbcDriverMap(dbType.name)

  def getConnection(): Connection = {
    if (conn == null || conn.isClosed) {
      Class.forName(driver)
      DriverManager.getConnection(url, username, password)
    } else conn
  }

  def execute(sql:String, transactional:Boolean = true): Unit = {
    logger.debug("sql:", sql)
    val conn = getConnection()

    val start = System.currentTimeMillis()
    try {
      if (transactional) {
        conn.setAutoCommit(false)
        conn.createStatement().execute(sql)
        conn.commit()
      } else {
        conn.createStatement().execute(sql)
      }
    } catch {
      case e: SQLException =>
        logger.error("{}", e)
        throw e
    }
    val end = System.currentTimeMillis()
    logger.debug(s"takes: ${end - start} ms")
  }
}
