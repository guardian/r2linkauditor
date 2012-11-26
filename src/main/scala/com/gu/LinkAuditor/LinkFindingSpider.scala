package com.gu.LinkAuditor

import collection.mutable.HashSet
import scalax.file.Path
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.jsoup.nodes.Document
import java.net.URL

object LinkFindingSpider extends App with Spider {

  val dir = "links-%s".format(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm").print(new DateTime()))
  val brokenLinksFile = Path(dir, "404s")
  val parents = new HashSet[String]

  override def process(url: URL, doc: Document) {

    links(doc).foreach {
      link =>
        if (link.attr("href").startsWith("http://www.guardiannews.com/uk-home") && !parents.contains(link.parent.toString)) {
          val parent = link.parent
          parents.add(parent.toString)
          println("%s\t%s\t%s".format(url, link.attr("href"), parent.toString.replace('\n', ' ')))
        }
    }
  }

  override def logToBrokenLinksFile(originatingUrl: String, targetUrl: String) {
    brokenLinksFile.append("Exception getting %s from %s\n".format(targetUrl, originatingUrl))
  }

  println("From\tTo\tParent Element")
  getUrlAndRecurse("http://www.guardian.co.uk", 5, "Start of job")
}
