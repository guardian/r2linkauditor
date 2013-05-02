package com.gu.LinkAuditor

import org.jsoup.{HttpStatusException, Jsoup}
import scala.collection.JavaConversions._

trait HttpChecker {
  def getStatusCode(url: String): Int

  def listAllLinks(url: String): List[String]

  def findContentInContext(url: String, toFind: String): List[String]
}

class JsoupHttpChecker(val proxy: Option[String] = None) extends HttpChecker {

  proxy foreach {
    p =>
      val proxyParts = p.split(":")
      System.setProperty("http.proxyHost", proxyParts(0))
      System.setProperty("http.proxyPort", proxyParts(1))
  }

  private def connectTo(url: String) = {
    Jsoup.connect(url).followRedirects(false).timeout(60000).header("X-GU-DEV", "true")
  }

  def getStatusCode(url: String): Int = {
    try {
      val response = connectTo(url).execute()
      val statusCode = response.statusCode()

      val msg = "Fetched %s [%d]".format(url, response.statusCode()) +
        (Option(response.header("X-GU-PageRenderer")) map (renderer => " rendered by %s".format(renderer))).getOrElse("")
      println(msg)

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
      val response = connectTo(url).execute()
      val statusCode = response.statusCode()
      if (statusCode == 200) {
        response.parse().
          getElementsByAttribute("href").map(_.attr("href")).
          filter(_.startsWith("http://"))
          .distinct.toList.sorted
      } else {
        println("ERROR finding links in %s: [%d]".format(url, statusCode))
        Nil
      }
    } catch {
      case e: HttpStatusException => {
        println("ERROR finding links in %s: [%s: %d]".format(url, e.getMessage, e.getStatusCode))
        Nil
      }
      case e => {
        println("ERROR finding links in %s: [%s]".format(url, e.getMessage))
        Nil
      }
    }
  }

  def findContentInContext(url: String, toFind: String): List[String] = {
    try {
      val elements = connectTo(url).get().getElementsContainingOwnText(toFind)
      (elements map (_.toString)).toList
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
