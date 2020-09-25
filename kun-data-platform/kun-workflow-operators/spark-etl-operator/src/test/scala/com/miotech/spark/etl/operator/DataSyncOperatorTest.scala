package com.miotech.spark.etl.operator

import com.miotech.spark.etl.common.{DatasourceClient, JdbcClient}
import com.miotech.spark.etl.loader.{DataSet, DataStore}
import org.mockito.Mockito.{mock, when}
import org.testcontainers.containers.{MySQLContainer, PostgreSQLContainer}

class DataSyncOperatorTest extends OperatorTestBase {

  var operator: DataSyncOperator = _
  val datasourceClient: DatasourceClient = mock(classOf[DatasourceClient])

  override def beforeEach() = {
    super.beforeEach()
    operator = new DataSyncOperator(config , spark)
    operator.setDatasourceClient(datasourceClient)
  }

  override def configure() = {
    Map(
      "sourceDataSource" -> "1",
      "sourceDatasetName" -> "table1",
      "targetDataSource" -> "2",
      "targetDatasetName" -> "table2",
      "writeMode" -> "overwrite"
    )
  }

  test("read jdbc postgres and write hive") {
    val postgres = new PostgreSQLContainer()
    postgres.start()
    val pgStore = DataStore(
      "postgresql",
      postgres.getJdbcUrl(),
      postgres.getUsername,
      postgres.getPassword)
    prepareJdbcData(DataSet(pgStore, "table1"))
    when(datasourceClient.getDataStore("1"))
      .thenReturn(pgStore)
    when(datasourceClient.getDataStore("2"))
      .thenReturn(DataStore("hive", "", "", ""))
    operator.execute()

    assertResult(100)(spark.table("table2").count())
    postgres.stop()
  }

  test("read hive and write postgres") {
    val postgres = new PostgreSQLContainer()
    postgres.start()
    val pgStore = DataStore(
      "postgresql",
      postgres.getJdbcUrl(),
      postgres.getUsername,
      postgres.getPassword)
    val hiveStore = DataStore("hive", "", "", "")
    prepareHiveData(DataSet(hiveStore, "table1"))
    when(datasourceClient.getDataStore("1"))
      .thenReturn(hiveStore)
    when(datasourceClient.getDataStore("2"))
      .thenReturn(pgStore)

    operator.execute()

    postgres.stop()
  }

  test("read jdbc mysql and write postgres") {
    val mysql = new MySQLContainer()
    mysql.start()
    val mysqlDatastore = DataStore(
      "mysql",
      mysql.getJdbcUrl(),
      mysql.getUsername,
      mysql.getPassword)
    prepareJdbcData(DataSet(mysqlDatastore, "table1"))
    when(datasourceClient.getDataStore("1"))
      .thenReturn(mysqlDatastore)

    val postgres = new PostgreSQLContainer()
    postgres.start()
    val pgStore = DataStore(
      "postgresql",
      postgres.getJdbcUrl(),
      postgres.getUsername,
      postgres.getPassword)
    when(datasourceClient.getDataStore("2"))
      .thenReturn(pgStore)

    operator.execute()

    mysql.stop()
    postgres.stop()
  }

  test("read jdbc mysql and write hive") {
    val mysql = new MySQLContainer()
    mysql.start()
    val mysqlDatastore = DataStore(
      "mysql",
      mysql.getJdbcUrl(),
      mysql.getUsername,
      mysql.getPassword)
    prepareJdbcData(DataSet(mysqlDatastore, "table1"))
    when(datasourceClient.getDataStore("1"))
      .thenReturn(mysqlDatastore)
    when(datasourceClient.getDataStore("2"))
      .thenReturn(DataStore("hive", "", "", ""))

    operator.execute()

    assertResult(100)(spark.table("table2").count())
    mysql.stop()
  }

  private def prepareHiveData(dataSet: DataSet, size: Int = 100): Unit = {
    val data = ( 1 to size)
      .map( i => (i, "value - " + i))

    val rdd = spark.sparkContext.parallelize(data)
    spark.createDataFrame(rdd)
      .write
      .saveAsTable(dataSet.datasetName)
  }

  private def countJdbcData(dataset: DataSet): Long = {
    val dataStore = dataset.dataStore
    val jdbcClient = JdbcClient(
      dataStore.dbType,
      dataStore.dataStoreUrl,
      dataStore.dataStoreUsername,
      dataStore.dataStorePassword
    )
    1l
  }

  private def prepareJdbcData(dataset: DataSet, size: Int = 100): Unit = {
    val dataStore = dataset.dataStore
    val jdbcClient = JdbcClient(
      dataStore.dbType,
      dataStore.dataStoreUrl,
      dataStore.dataStoreUsername,
      dataStore.dataStorePassword
    )

    jdbcClient.execute(s"CREATE TABLE ${dataset.datasetName} ( id int , v1 text)")
    val values = ( 1 to size)
      .map( i => s"($i, 'value - $i')")
      .mkString(",")
    jdbcClient.execute(s"insert into ${dataset.datasetName} (id, v1) values $values")
  }
}
