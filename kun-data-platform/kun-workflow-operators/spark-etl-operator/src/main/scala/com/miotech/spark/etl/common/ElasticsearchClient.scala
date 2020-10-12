package com.miotech.spark.etl.common

import java.io.IOException

import com.google.gson.JsonObject
import com.miotech.spark.etl.utils.JSONUtils
import org.apache.http.{HttpEntity, HttpHost}
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.nio.entity.NStringEntity
import org.apache.http.util.EntityUtils
import org.elasticsearch.client.{Request, RestClient, RestHighLevelClient}
import org.slf4j.LoggerFactory

class ElasticsearchClient(connectionUrl: String,
                          username: String,
                          password: String) {
  import scala.collection.JavaConverters._
  private val logger = LoggerFactory.getLogger(getClass)
  val esClient = initEsClient()

  private def initEsClient() = {
    val credentialsProvider = new BasicCredentialsProvider
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password))

    val hostAndPort = connectionUrl.split(":")
    val host = hostAndPort(0)
    val port = if (hostAndPort.size > 1) {
      hostAndPort(1).toInt
    } else 9200

    new RestHighLevelClient(
      RestClient.builder(new HttpHost(host, port, "http")));
  }

  @throws[IOException]
  def executeScript(url: String, payload: String) = {
    var method: String = "POST"
    var api: String = url
    if (url.matches("(?i)(PUT|DELETE|POST)\\s+.*")) {
      val splits: Array[String] = url.split("\\s+")
      method = splits(0)
      api = splits(1)
    }
    val entity: HttpEntity = new NStringEntity(payload, ContentType.APPLICATION_JSON)
    val response: String = this.performRequest(method.toUpperCase, api, Map(), entity)
    JSONUtils.toJsonObject(response)
  }

  def getResource(resource: String) = {
    val indexAndType = resource.split("\\/")

    val indexName = indexAndType.head
    if (indexAndType.size == 1) {
      val indexPayload = getIndex(indexName)
      val indexOrAliasName = indexPayload
        .entrySet().iterator().next()
        .getKey
      val mappings = indexPayload.getAsJsonObject(indexOrAliasName)
        .getAsJsonObject("mappings")
      val docType = mappings
        .entrySet().iterator().next()
        .getKey
      Seq(indexName, docType).mkString("/")
    } else resource
  }

  /**
    * execute script
    * @param url
    * @param script
    * @return
    */
  def executeAndWait(url: String, script: String): Boolean = {
    val taskOrResult = executeScript(url, script)
    if (!taskOrResult.has("task")) {
      if (taskOrResult.has("failures")
        && taskOrResult.getAsJsonArray("failures").asScala.nonEmpty) {
        throw new RuntimeException(s"Execution error, error: ${taskOrResult.get("failures").toString}")
      } else true
    } else {
      val taskId = taskOrResult.get("task").getAsString
      var isCompleted = false
      var task = getTask(taskId)

      logger.info(s"Get task ${taskId}: ${task.toString}")
      while (!isCompleted) {
        Thread.sleep(10000L)
        task = getTask(taskId)
        isCompleted = task.has("completed") && task.get("completed").getAsJsonPrimitive.getAsBoolean
        if (task.has("response") && task.has("failures")) {
          val failures = task.get("response")
            .getAsJsonObject
            .get("failures")
            .getAsJsonArray
            .asScala
          if (failures.nonEmpty) {
            isCompleted = true
            logger.error(failures.mkString(","))
            throw new IllegalStateException(
              s"Execution error, taskId: ${taskId}, error: ${failures.mkString(",")}"
            )
          }
        }
      }
      true
    }
  }

  def getTask(taskId: String): JsonObject = {
    val response = performRequest("GET", String.format("/_tasks/%s", taskId))
    JSONUtils.toJsonObject(response)
  }

  def getIndex(indexName: String): JsonObject = {
    val response = performRequest("GET", String.format("/%s", indexName))
    JSONUtils.toJsonObject(response)
  }

  private def performRequest(method: String,
                             endpoint: String,
                             params: Map[String, String] = Map(),
                             entity: HttpEntity = null) = {
    val request = new Request(method, endpoint)
    params.foreach(t => request.addParameter(t._1, t._2))
    request.setEntity(entity)
    EntityUtils.toString(esClient.getLowLevelClient.performRequest(request).getEntity)
  }
}
