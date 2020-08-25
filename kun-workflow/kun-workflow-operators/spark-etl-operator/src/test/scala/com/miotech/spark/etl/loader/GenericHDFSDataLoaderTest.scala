package com.miotech.spark.etl.loader

import java.nio.file.Files

class GenericHDFSDataLoaderTest extends LoaderTestBase {

  test("write and read orc ok") {
    // define target
    val dataStore = DataStore("hdfs", "", "", "")
    val dataLoader = new GenericHDFSDataLoader(dataStore, spark)

    val tempDir = Files.createTempDirectory("test-data").toFile
    tempDir.deleteOnExit()

    val dataSet = DataSet(dataStore, tempDir.getAbsolutePath)

    // write some test data
    dataLoader.write(getSampleData(), dataSet, Map("format" -> "orc"))

    // read
    val df = dataLoader.read(dataSet, Map("format" -> "orc"))

    // verify
    assertResult(100)(df.count())
  }

  test("write and read parquet ok") {
    // define target
    val dataStore = DataStore("hdfs", "", "", "")
    val dataLoader = new GenericHDFSDataLoader(dataStore, spark)
    val tempDir = Files.createTempDirectory("test-data").toFile
    tempDir.deleteOnExit()

    // default parquet file
    val dataSet = DataSet(dataStore, tempDir.getAbsolutePath)

    // write some test data
    dataLoader.write(getSampleData(), dataSet, Map())

    // read
    val df = dataLoader.read(dataSet, Map())

    // verify
    assertResult(100)(df.count())
  }

}
