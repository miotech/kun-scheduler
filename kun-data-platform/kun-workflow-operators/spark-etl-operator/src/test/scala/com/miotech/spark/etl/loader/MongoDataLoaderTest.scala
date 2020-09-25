package com.miotech.spark.etl.loader

import org.testcontainers.containers.MongoDBContainer

class MongoDataLoaderTest extends LoaderTestBase {

  var mongo: MongoDBContainer = _
  var mongoStore: DataStore = _
  var mongoDataLoader: MongoDataLoader = _

  override def beforeEach(): Unit = {
    super.beforeEach()
    mongo = new MongoDBContainer()
    mongo.start()

    mongoStore = DataStore("mongo", mongo.getReplicaSetUrl, "", "")
    mongoDataLoader = new MongoDataLoader(mongoStore, spark)
  }

  test("write and read ok") {
    // define target
    val dataSet = DataSet(mongoStore, "testdb.testcollection")

    // write some test data
    mongoDataLoader.write(getSampleData(10000), dataSet, Map())

    // read
    val df = mongoDataLoader.read(dataSet, Map())

    // verify
    assertResult(10000)(df.count())
    assert(df.columns.contains("_id"))
  }

  test("write and read without oid") {
    // define target
    val dataSet = DataSet(mongoStore, "testdb.testcollection")

    // write some test data
    mongoDataLoader.write(getSampleData(10000), dataSet, Map())

    // read
    val df = mongoDataLoader.read(dataSet, Map("dropOid" -> "true"))

    // verify
    assertResult(10000)(df.count())
    assert(df.columns.contains("_id"))
  }

  override def afterEach(): Unit = {
    super.afterEach()
    mongo.stop()
  }
}
