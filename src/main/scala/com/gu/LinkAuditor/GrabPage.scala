package com.gu.LinkAuditor

import org.jsoup.Jsoup
import org.jsoup.nodes._
import collection.JavaConversions
import java.net.URL
import collection.mutable.HashSet

object GrabPage extends App {

  val r1History = new HashSet[String]
  val visitedLinks = new HashSet[String]

  def getUrlAndRecurse(currentUrl: String, recursionDepth: Int, previousUrl: String) {
    if (recursionDepth >= 0 && !visitedLinks.contains(currentUrl)) {
      val target = new URL(currentUrl)
      val targetHost = target.getHost

      try {
        val doc = Jsoup.connect(target.toString).userAgent("Guardian dotCom linkAuditor").get

        val linkElements = doc.select("a[href]")
        val elementList: Iterator[Element] = JavaConversions.asScalaIterator(linkElements.listIterator())

        val linkList = elementList.toList.map(_.attr("href")).filterNot(_.startsWith("#")) // don't want fragment urls.
        val internalLinks = linkList.filter(_.startsWith("http://%s/".format(targetHost)))
        val relativeLinks = linkList.filter(_.startsWith("/"))
        val r1Links = internalLinks.filter(_.contains(",,"))

        println("%s; %s; %s".format(currentUrl, internalLinks.length, relativeLinks.length))

        r1Links filterNot (r1History.contains(_)) foreach{ r1Link =>
          r1History add r1Link
          println("%s contains r1 link: %s".format(currentUrl, r1Link))
        }

        visitedLinks add currentUrl   // add existing page to the "done" list.
        
        internalLinks foreach { nextUrl =>
          getUrlAndRecurse(nextUrl, recursionDepth - 1, currentUrl)
        }

      } catch {
        case ex: java.net.SocketTimeoutException => println("Timed out getting %s".format(currentUrl))
        case ex: org.jsoup.HttpStatusException => println("%s contains broken link: %s".format(previousUrl, currentUrl))
      }
    }
    }

    println("Url; Absolute internal link count; Relative link count")
    getUrlAndRecurse("http://www.guardian.co.uk", 2, "Start of job")
  }