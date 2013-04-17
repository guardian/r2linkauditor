package com.gu.LinkAuditor

import org.jsoup.{HttpStatusException, Jsoup}
import java.net.URL
import scala.collection.JavaConversions._

class LinkSorter(httpClient: HttpChecker) {

  def filterWorkingRelativeLinks(originUrl: String, links: List[String]): List[String] = {
    val originHost = new URL(originUrl).getHost
    val linksInOriginDomain = links.filter {
      link => {
        try {
          val targetUrl = new URL(link)
          val targetHost = targetUrl.getHost
          targetHost == originHost
        } catch {
          case e: Exception => {
            println("error getting URL %s".format(link))
            println("Error: %s".format(e.getMessage))
            false
          }
          case _ => false
        }
      }
    }
    linksInOriginDomain.filter(link => httpClient.getStatusCode(link) == 200)
  }

}

class HttpChecker {


  def getStatusCode(url: String): Int = {
    try {
      Jsoup.connect(url).followRedirects(false).timeout(60000).get()
      println("Successfully fetched " + url)
      200
    } catch {
      case e: HttpStatusException => e.getStatusCode
      case e => {
        println("Error connecting to " + url)
        println("Error: " + e.getMessage)
        -1
      }
    }
  }

}

object LinkSorterClient extends App {

  val url = "http://www.thegulocal.com/"
  val document = Jsoup.connect(url).get()
  val allLinks = document.select("a[href]").toList.map(_.attr("href"))
  val links = {
    allLinks.filterNot(_.startsWith("#")).filterNot(_.contains("edition-permission")).filter(_.startsWith("http://")).distinct
  }
  val workingRelativeLinks = new LinkSorter(new HttpChecker).filterWorkingRelativeLinks(url, links)
  workingRelativeLinks.foreach(println)
}
