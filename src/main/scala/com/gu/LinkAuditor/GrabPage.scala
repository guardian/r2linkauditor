package com.gu.LinkAuditor

import org.jsoup.Jsoup
import org.jsoup.nodes._
import collection.JavaConversions
import java.net.URL
import collection.immutable.HashSet

object GrabPage extends App {

  def getUrlAndRecurse(url: String, recursionDepth: Int, history: Set[String], origin: String) {
    if (recursionDepth >= 0 && !history.contains(url)) {
      val target = new URL(url)
      val targetHost = target.getHost

      try {
        val doc = Jsoup.connect(target.toString).userAgent("Guardian dotCom linkAuditor").get

        val linkElements = doc.select("a[href]")
        val elementList: Iterator[Element] = JavaConversions.asScalaIterator(linkElements.listIterator())

        val linkList = elementList.toList.map(_.attr("href")).filterNot(_.startsWith("#")) // don't want fragment urls.
        val internalLinks = linkList.filter(_.startsWith("http://%s/".format(targetHost)))
        val relativeLinks = linkList.filter(_.startsWith("/"))

        println("%s; %s; %s".format(url, internalLinks.length, relativeLinks.length))
        internalLinks foreach { getUrlAndRecurse(_, recursionDepth - 1, history + url, url)}
      } catch {
        case ex: java.net.SocketTimeoutException => println("Timed out getting %s".format(url))
        case ex: org.jsoup.HttpStatusException => println("%s contains broken link: %s".format(origin, url))
      }
    }
    }

    println("Url; Absolute internal link count; Relative link count")
    getUrlAndRecurse("http://www.guardian.co.uk", 2, new HashSet, "Start of job")
  }