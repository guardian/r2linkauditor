package com.gu.LinkAuditor

import org.jsoup.Jsoup
import org.jsoup.nodes._
import collection.JavaConversions._
import java.net.URL
import collection.mutable.HashSet
import scalax.file.Path
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object CountingSpider extends App {

  val r1History = new HashSet[String]
  val visitedLinks = new HashSet[String]

  val dir = "links-%s".format(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm").print(new DateTime))
  val linkComparisonFile = Path(dir, "linkComparison")
  val r1LinksFile = Path(dir, "r1Links")
  val brokenLinksFile = Path(dir, "404s")

  def getUrlAndRecurse(currentUrl: String, recursionDepth: Int, previousUrl: String) {
    if (recursionDepth >= 0 && !visitedLinks.contains(currentUrl)) {
      val target = new URL(currentUrl)
      val targetHost = target.getHost

      try {
        val doc = Jsoup.connect(target.toString).userAgent("Guardian dotCom linkAuditor").get

        val elementList = doc.select("a[href]").toList

        val linkList = elementList.map(_.attr("href")).filterNot(_.startsWith("#")) // don't want fragment urls.
        val internalLinks = linkList.filter(_.startsWith("http://%s/".format(targetHost)))
        val relativeLinks = linkList.filter(_.startsWith("/"))
        val r1Links = internalLinks.filter(_.contains(",,"))

        linkComparisonFile.append("%s| %s| %s\n".format(currentUrl, internalLinks.length, relativeLinks.length))

        r1Links filterNot (r1History.contains(_)) foreach {
          r1Link =>
            r1History add r1Link
            r1LinksFile.append("%s contains r1 link: %s\n".format(currentUrl, r1Link))
        }

        visitedLinks add currentUrl // add existing page to the "done" list.

        internalLinks foreach {
          nextUrl =>
            getUrlAndRecurse(nextUrl, recursionDepth - 1, currentUrl)
        }

      } catch {
        case ex: java.net.SocketTimeoutException => println("Timed out getting %s".format(currentUrl))
        case ex: org.jsoup.HttpStatusException => {
          brokenLinksFile.append("Exception getting %s from %s\n".format(currentUrl, previousUrl))
        }
      }
    }
  }

  println("Url; Absolute internal link count; Relative link count")
  getUrlAndRecurse("http://www.guardian.co.uk", 2, "Start of job")
}