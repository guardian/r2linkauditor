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
