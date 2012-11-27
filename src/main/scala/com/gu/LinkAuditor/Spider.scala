package com.gu.LinkAuditor

import org.jsoup.Jsoup
import collection.JavaConversions._
import java.net.URL
import collection.mutable.HashSet
import org.jsoup.nodes.{Element, Document}

trait Fetcher {
  def get(url: String): Document =
    Jsoup.connect(url).userAgent("Guardian dotCom linkAuditor").timeout(100000).get
}

trait Spider extends Fetcher {

  val visitedLinks = new HashSet[String]

  def getUrlAndRecurse(currentUrl: String, recursionDepth: Int, previousUrl: String) {
    if (recursionDepth >= 0 && !visitedLinks.contains(currentUrl)) {
      val target = new URL(currentUrl)
      val targetHost = target.getHost

      try {
        val doc = get(target.toString)

        val linkList = links(doc).map(_.attr("href")).filterNot(_.startsWith("#")) // don't want fragment urls.
        val internalLinks = linkList.filter(_.startsWith("http://%s/".format(targetHost)))

        visitedLinks add currentUrl // add existing page to the "done" list.

        process(target, doc)

        // TODO: when links have been relativized this will traverse fewer and fewer links - need to include relatives
        internalLinks foreach {
          nextUrl =>
            getUrlAndRecurse(nextUrl, recursionDepth - 1, currentUrl)
        }

      } catch {
        case ex: java.net.SocketTimeoutException => println("Timed out getting %s".format(currentUrl))
        case ex: org.jsoup.HttpStatusException if (ex.getStatusCode == 404) => {
          logToBrokenLinksFile(previousUrl, currentUrl)
        }
        case ex: org.jsoup.HttpStatusException => {
          println("HTTP Exception %d: From %s to %s".format(ex.getStatusCode, previousUrl, ex.getUrl))
        }
        case ex: javax.net.ssl.SSLHandshakeException => Console.println("problem getting " + currentUrl)
      }
    }
  }

  def links(doc: Document): List[Element] = {
    doc.select("a[href]").toList
  }

  def process(url: URL, doc: Document)

  def logToBrokenLinksFile(originatingUrl: String, targetUrl: String)
}
