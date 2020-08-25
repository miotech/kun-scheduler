package com.miotech.spark.etl.utils

import org.apache.http.HttpEntity
import org.apache.http.client.methods.{HttpGet, HttpPost, HttpPut}
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.HttpClients
import org.apache.http.nio.entity.NStringEntity
import org.apache.http.util.EntityUtils
import org.elasticsearch.client.RestClient


object HttpUtils {

  def get(endpoint: String,
          headers: Map[String,String]= Map()): String = {
    val httpClient = HttpClients.createDefault()
    val request = new HttpGet(endpoint)

    headers.foreach(t => request.setHeader(t._1, t._2))
    EntityUtils.toString(httpClient.execute(request).getEntity)
  }

  def put(endpoint: String,
          params: String,
          headers: Map[String,String] = Map()): String = {
    val httpClient = HttpClients.createDefault()
    val request = new HttpPut(endpoint)
    headers.foreach(t => request.setHeader(t._1, t._2))

    val entity: HttpEntity = new NStringEntity(params, ContentType.APPLICATION_JSON)
    request.setEntity(entity)

    EntityUtils.toString(httpClient.execute(request).getEntity)
  }

  def post(endpoint: String,
           params: String,
           headers: Map[String,String] = Map()): String = {
    val httpClient = HttpClients.createDefault()
    val request = new HttpPost(endpoint)
    headers.foreach(t => request.setHeader(t._1, t._2))

    val entity: HttpEntity = new NStringEntity(params, ContentType.APPLICATION_JSON)
    request.setEntity(entity)

    EntityUtils.toString(httpClient.execute(request).getEntity)
  }
}
