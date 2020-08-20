package com.miotech.spark.etl.loader


case class DataStore(dbType: String,
                     dataStoreUrl: String,
                     dataStoreUsername: String,
                     dataStorePassword: String) {}

case class DataSet(dataStore: DataStore,
                   name: String) {
  private val splits = name.split("\\.")
  val currentDatabase: Option[String] = if (splits.size > 1)
    Some(splits.head) else None
  val datasetName: String = currentDatabase.map(d =>
    splits.slice(1,splits.size).mkString("."))
    .getOrElse(name)
  val currentDatastore: DataStore = currentDatabase.map (c => dataStore.copy(
    dataStoreUrl =  dataStore.dataStoreUrl + c
  )).getOrElse(dataStore)
}
