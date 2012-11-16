package com.gu.LinkAuditor

import org.jsoup.Jsoup
import collection.JavaConversions._
import java.net.URL
import org.jsoup.nodes.Element

object GrabPage extends App {

  def getUrlAndRecurse(url: String, recursionDepth: Int) {
    if (recursionDepth >= 0) {
      val target = new URL(url)
      val targetHost = target.getHost

      try {
        val doc = Jsoup.connect(target.toString).get

        val links = doc.select("a[href]").toList.map(_.attr("href")).filterNot(_.startsWith("#")) // don't want fragment urls.
        val internalLinks = links.filter(_.startsWith("http://%s/".format(targetHost)))
        val relativeLinks = links.filter(_.startsWith("/"))

        println("%s; %s; %s".format(url, internalLinks.length, relativeLinks.length))
        internalLinks foreach {
          getUrlAndRecurse(_, recursionDepth - 1)
        }
      } catch {
        case ex: java.net.SocketTimeoutException => println("Timed out getting %s".format(url))
      }
    }
  }

  def reportOutboundLinks(url: String) {

    def report(links: List[Element]) {
      links.foreach(link => println("%s:\n%s\n".format(link.text(), link.attr("href"))))
    }

    val target = new URL(url)
    val targetHost = target.getHost

    try {
      val doc = Jsoup.connect(target.toString).get

      val links = doc.select("a[href]").toList.filterNot(_.attr("href").startsWith("#")) // don't want fragment urls.
      val internalAbsLinks =
        links.filter(_.attr("href").startsWith("http://%s/".format(targetHost)))
          .sortBy(_.attr("href"))
      val domainRelLinkElements = links.filter(_.attr("href").startsWith("/"))
        .sortBy(_.attr("href"))
      val externalLinks =
        (links.toSet &~ internalAbsLinks.toSet &~ domainRelLinkElements.toSet).toList
          .sortBy(_.attr("href"))

      println("+++++ INTERNAL ABS +++++")
      println()
      report(internalAbsLinks)

      println("+++++ DOMAIN REL +++++")
      println()
      report(domainRelLinkElements)

      println("+++++ EXTERNAL +++++")
      println()
      report(externalLinks)

    } catch {
      case ex: java.net.SocketTimeoutException => println("Timed out getting %s".format(url))
      case other: Exception => println(other.getMessage)
    }
  }

  println("Url; Absolute internal link count; Relative link count")
  getUrlAndRecurse("http://www.guardian.co.uk", 2)
}
