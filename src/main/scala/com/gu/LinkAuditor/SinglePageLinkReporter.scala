package com.gu.LinkAuditor

import collection.JavaConversions._
import java.net.URL
import org.jsoup.nodes.Element

object SinglePageLinkReporter extends App with Fetcher {

  def reportOutboundLinks(url: String) {

    def report(links: List[Element]) {
      links.foreach(link => {
        println("%s:".format(link.parent.toString.replace('\n', ' ')))
        println("%s".format(link.attr("href")))
      })
    }

    val target = new URL(url)
    val targetHost = target.getHost

    try {
      val doc = get(target.toString)

      val links = doc.select("a[href]").toList.filterNot(_.attr("href").startsWith("#")) // don't want fragment urls.
      val internalAbsLinks =
        links.filter(_.attr("href").startsWith("http://%s/".format(targetHost)))
          .sortBy(_.attr("href"))
      val domainRelLinks = links.filter(_.attr("href").startsWith("/"))
        .sortBy(_.attr("href"))
      val externalLinks =
        (links.toSet &~ internalAbsLinks.toSet &~ domainRelLinks.toSet).toList
          .sortBy(_.attr("href"))

      println("+++++ INTERNAL ABS +++++")
      println()
      report(internalAbsLinks)

      println("+++++ DOMAIN REL +++++")
      println()
      report(domainRelLinks)

      println("+++++ EXTERNAL +++++")
      println()
      report(externalLinks)

      println("+++++ SUMMARY +++++")
      println("%d links".format(links.size))
      println("%d internal absolute links".format(internalAbsLinks.size))
      println("%d domain relative links".format(domainRelLinks.size))
      println("%d external links".format(externalLinks.size))

    } catch {
      case ex: java.net.SocketTimeoutException => println("Timed out getting %s".format(url))
      case other: Exception => println(other.getMessage)
    }
  }

  reportOutboundLinks(args(0))
}
