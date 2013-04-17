package com.gu.LinkAuditor

import org.jsoup.{HttpStatusException, Jsoup}
import java.net.URL
import scala.collection.JavaConversions._

class PageLinkAuditor(targetDomain: String, originalDomain: String, allLinks: List[String], httpClient: HttpChecker) {
  val targetHost = new URL(targetDomain).getHost
  val originalHost = new URL(originalDomain).getHost

  lazy val (linksToTargetDomain, externalLinks) = partitionLinksToMatch(targetHost, allLinks)
  lazy val (linksToOriginalDomain, linksToNonGuDomains) = partitionLinksToMatch(originalHost, externalLinks)
  lazy val (workingLinksToTargetDomain, brokenLinkToTargetDomain) = linksToTargetDomain.partition(link => httpClient.getStatusCode(link) == 200)
  lazy val (workingLinksToOriginalDomain, brokenLinksToOriginalDomain) = linksToOriginalDomain.partition(link => httpClient.getStatusCode(link) == 200)
  lazy val (originalDomainLinksThatAreRedirectable, originalDomainLinksThatAreNotRedirectable) = workingLinksToOriginalDomain.partition {
    link =>
      val original: URL = new URL(link)
      val targetHostLink: String = new URL(original.getProtocol, targetHost, original.getFile).toExternalForm
      httpClient.getStatusCode(targetHostLink) == 200
  }

  def partitionLinksToMatch(host: String, links: List[String]): (List[String], List[String]) = {
    links.partition {
      link => {
        try {
          val linkUrl = new URL(link)
          val linkHost = linkUrl.getHost
          linkHost == host
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

object PageLinkAuditorClient extends App {

  val originalNfUrl = "http://www.gulocal.co.uk/"
  val targetNfUrl = "http://www.thegulocal.com/"
  val allLinks = Jsoup.connect(targetNfUrl).followRedirects(false).timeout(0).get().select("a[href]").map(_.attr("href")).filter(_.startsWith("http://")).distinct.toList
  val auditor = new PageLinkAuditor(targetNfUrl, originalNfUrl, allLinks, new HttpChecker)

  println("+++++ Links to non-GU domains +++++")
  auditor.linksToNonGuDomains.foreach(println)
  println()
  println("+++++ Links to original domain +++++")
  auditor.linksToOriginalDomain.foreach(println)
  println()
  println("+++++ Links to target domain +++++")
  auditor.linksToTargetDomain.foreach(println)
  println()
  println("+++++ Working links to target domain +++++")
  auditor.workingLinksToTargetDomain.foreach(println)
}
