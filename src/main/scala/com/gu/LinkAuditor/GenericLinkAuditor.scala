package com.gu.LinkAuditor

import org.jsoup.{HttpStatusException, Jsoup}
import scala.collection.JavaConversions._
import java.net.URL
import org.jsoup.nodes.Document

object GenericLinkAuditor {

  def auditLinks(url: String,
                 filterLinksToFollow: String => Boolean,
                 filterLinksToRecord: String => Boolean,
                 recordLink: (String, String) => Unit,
                 process404: (String, String) => Unit,
                 processError: (String, Throwable) => Unit,
                 depth: Int) {

    def auditLinks0(url: String, depth: Int, urlsVisited: List[String]) {
      if (depth > 0) {
        getDocumentOrStatusCode(url) match {
          case Left(doc) => {
            val linksOut = doc.select("a[href]").map(_.attr("href")).filterNot(_.startsWith("#")).distinct
            val linksToFollow = linksOut.filter(filterLinksToFollow).filterNot(link => urlsVisited.contains(link))
            val linksToRecord = linksOut.filter(filterLinksToRecord)
            linksToRecord.foreach(link => recordLink(url, link))
            linksToFollow.foreach(auditLinks0(_, depth - 1, urlsVisited :+ url))
          }
          case Right(statusCode) => {
            if (statusCode == 404) process404(urlsVisited.last, url)
            else println("TODO deal with this: " + statusCode)
          }
        }
      }

      auditLinks0(url, depth, List(url))

    }
  }

  def getDocumentOrStatusCode(url: String): Either[Document, Int] = {
    try {
      Left(Jsoup.connect(url).followRedirects(false).timeout(0).get())
    }
    catch {
      case e: HttpStatusException => Right(e.getStatusCode)
      case e => Right(-1)
    }
  }

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
      link.startsWith("http://")
  }

  def recordLink: (String, String) => Unit = {

    case (origin, link) => {
      val statusCode = GenericLinkAuditor.getDocumentOrStatusCode(link) match {
        case Left(_) => 200
        case Right(code) => code
      }
      println(origin + " -> " + link + " status " + statusCode)
    }

  }

  def process404: (String, String) => Unit = {
    case (originUrl, targetUrl) => println("404: %s -> %s".format(originUrl, targetUrl))
  }

  def processError: (String, Throwable) => Unit = {
    case (url, throwable) => println("ERROR: " + url + ": " + throwable.getMessage)
  }

  GenericLinkAuditor.auditLinks(seedUrl, filterLinksToFollow, filterLinksToRecord, recordLink, process404, processError, depth)

}
