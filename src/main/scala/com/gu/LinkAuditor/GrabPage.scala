package com.gu.LinkAuditor

import org.jsoup.Jsoup
import collection.JavaConversions._
import java.net.URL

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

  println("Url; Absolute internal link count; Relative link count")
  getUrlAndRecurse("http://www.guardian.co.uk", 2)
}
