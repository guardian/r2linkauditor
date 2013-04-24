package com.gu.LinkAuditor

import org.jsoup.{HttpStatusException, Jsoup}
import scala.collection.JavaConversions._

class HttpChecker {

  private def connectTo(url: String) = {
    Jsoup.connect(url).followRedirects(false).timeout(60000)
  }

  def getStatusCode(url: String): Int = {
    try {
      val response = connectTo(url).execute()
      val statusCode = response.statusCode()
      println("Fetched %s [%d]".format(url, response.statusCode()))
      statusCode
    } catch {
      case e: HttpStatusException => {
        println("ERROR fetching %s: [%s: %d]".format(url, e.getMessage, e.getStatusCode))
        -1
      }
      case e => {
        println("ERROR fetching %s: [%s]".format(url, e.getMessage))
        -1
      }
    }
  }

  def listAllLinks(url: String): List[String] = {
    try {
      connectTo(url).get().select("a[href]").map(_.attr("href")).filter(_.startsWith("http://")).distinct.toList
    } catch {
      case e: HttpStatusException => {
        println("ERROR fetching %s: [%s: %d]".format(url, e.getMessage, e.getStatusCode))
        Nil
      }
      case e => {
        println("ERROR fetching %s: [%s]".format(url, e.getMessage))
        Nil
      }
    }
  }

}

