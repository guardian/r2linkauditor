package com.gu.LinkAuditor

import org.jsoup.{HttpStatusException, Jsoup}
import scala.collection.JavaConversions._
import java.net.URL

object GenericLinkAuditor {

  def auditLinks(url: String,
                 filterLinksToFollow: String => Boolean,
                 filterLinksToRecord: String => Boolean,
                 recordLink: (String, String) => Unit,
                 process404: String => Unit,
                 processError: (String, Throwable) => Unit,
                 depth: Int) {
    if (depth > 0) {
      try {
        val doc = Jsoup.connect(url).followRedirects(false).get()
        val linksOut = doc.select("a[href]").map(_.attr("href")).filterNot(_.startsWith("#")).distinct
        val linksToFollow = linksOut.filter(filterLinksToFollow)
        val linksToRecord = linksOut.filter(filterLinksToRecord)
        linksToRecord.foreach(link => recordLink(url, link))
        linksToFollow.foreach(auditLinks(_, filterLinksToFollow, filterLinksToRecord, recordLink, process404, processError, depth - 1))
      }
      catch {
        case e: HttpStatusException if (e.getStatusCode == 404) => process404(url)
        case e: HttpStatusException => println("TODO deal with this: " + e.getMessage)
        case e => processError(url, e)
      }
    }
  }

}


object LocalR2LinkAuditor {
  def filterLinksToRecord = true
}

object GenericLinkAuditorClient extends App {

  val host = "www.thegulocal.com"
  val seedUrl = "http://%s/".format(host)

  val depth = 1

  def filterLinksToFollow: String => Boolean = {
    link =>
      link.contains(".thegulocal.com") || link.contains(".gulocal.com")
  }

  def filterLinksToRecord: String => Boolean = {
    link =>
      link.contains(".gulocal.co.uk")
  }

  def recordLink: (String, String) => Unit = {
    case (origin, link) => println(origin + " -> " + link)

    val correctedLink = {
      val url = new URL(link)
      new URL(url.getProtocol, host, url.getFile).toExternalForm
    }
    try {
      Jsoup.connect(correctedLink).followRedirects(false).get()
      // TODO: record count
      println("Corrected link %s works".format(correctedLink))
    } catch {
      case e: HttpStatusException if (e.getStatusCode == 404) => println("Following corrected link %s gives 404".format(correctedLink))
      case e: HttpStatusException => println("TODO deal with this: " + e.getMessage)
      case e => processError(correctedLink, e)
    }
  }

  def process404: (String) => Unit = {
    http404 => println("404: " + http404)
  }

  def processError: (String, Throwable) => Unit = {
    case (url, throwable) => println("ERROR: " + url + ": " + throwable.getMessage)
  }

  GenericLinkAuditor.auditLinks(seedUrl, filterLinksToFollow, filterLinksToRecord, recordLink, process404, processError, depth)

}
