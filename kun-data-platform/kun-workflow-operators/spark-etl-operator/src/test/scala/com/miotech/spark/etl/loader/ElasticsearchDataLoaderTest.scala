package com.miotech.spark.etl.loader

import org.testcontainers.elasticsearch.ElasticsearchContainer

class ElasticsearchDataLoaderTest extends LoaderTestBase {

  var esContainer: ElasticsearchContainer = _
  var esStore: DataStore = _
  var esDataLoader: ElasticsearchDataLoader = _
  // do not resolve test container ip
  val netConfig = Map(
    "es.nodes.wan.only" -> "true"
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    esContainer = new ElasticsearchContainer()
    esContainer.start()

    esStore = DataStore("elasticsearch", esContainer.getHttpHostAddress, "", "")
    esDataLoader = new ElasticsearchDataLoader(esStore, spark)
  }

  test("execute script ok") {
    // define target
    val dataSet = DataSet(esStore, "test-index/_doc")

    // write some test data
    esDataLoader.write(getSampleData(100), dataSet, netConfig)

    // read
    esDataLoader.execute(
      """
        |POST /test-index/_doc/_update_by_query?wait_for_completion=false
        |{
        |  "script": {
        |    "source": "ctx._source.id=1",
        |    "lang": "painless"
        | }
        |}
        |""".stripMargin)

    // verify
    val df = esDataLoader.read(dataSet, netConfig)
    assertResult(1)(df.distinct().head().getAs[Int](0))
  }

  test("write and read ok") {
    // define target
    val dataSet = DataSet(esStore, "test-index/_doc")

    // write some test data
    esDataLoader.write(getSampleData(10000), dataSet, netConfig)

    // read
    val df = esDataLoader.read(dataSet, netConfig)

    // verify
    assertResult(10000)(df.count())
  }

  test("write and read without _type ok") {
    // define target
    val dataSet = DataSet(esStore, "test-index/_doc")

    // write some test data
    esDataLoader.write(getSampleData(), dataSet, netConfig)

    // read
    val nonDataSet = DataSet(esStore, "test-index")
    esDataLoader.write(getSampleData(), nonDataSet, netConfig)

    val df = esDataLoader.read(nonDataSet, netConfig)

    // verify
    assertResult(200)(df.count())
  }

  test("write insert and read double") {
    // define target
    val dataSet = DataSet(esStore, "test-index/_doc")

    // write some test data
    esDataLoader.write(getSampleData(), dataSet, netConfig)
    esDataLoader.write(getSampleData(), dataSet, netConfig)

    // read
    val df = esDataLoader.read(dataSet, netConfig)

    // verify
    assertResult(200)(df.count())
  }

  test("write upsert and read unique") {
    // define target
    val dataSet = DataSet(esStore, "test-index/_doc")

    // write some test data
    esDataLoader.write(getSampleData(), dataSet, netConfig ++ Map("writeMode" -> "upsert", "idField" -> "id"))
    esDataLoader.write(getSampleData(), dataSet, netConfig ++ Map("writeMode" -> "upsert", "idField" -> "id"))

    // read
    val df = esDataLoader.read(dataSet, netConfig)

    assertResult(Seq("id", "value"))(df.columns)
    // verify
    assertResult(100)(df.count())
  }


  override def afterEach(): Unit = {
    super.afterEach()
    esContainer.stop()
  }
}
