package com.gu.LinkAuditor

import collection._
import scalax.file.Path
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.jsoup.nodes.Document
import java.net.URL

object LinkFindingSpider extends App with Spider {

  val linkToFind = args(0)

  val dir = "links-%s".format(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm").print(new DateTime()))
  val brokenLinksFile = Path(dir, "404s")
  val linksFoundFile = Path(dir, "found")
  val parents = new mutable.HashSet[String]

  override def process(url: URL, doc: Document) {
    links(doc).foreach {
      link =>
        if (link.attr("href").startsWith(linkToFind) && !parents.contains(link.parent.toString)) {
          val parent = link.parent
          parents.add(parent.toString)
          linksFoundFile.append("%s\t%s\t%s\n".format(url, link.attr("href"), parent.toString.replace('\n', ' ')))
        }
    }
  }

  override def logToBrokenLinksFile(originatingUrl: String, targetUrl: String) {
    brokenLinksFile.append("Exception getting %s from %s\n".format(targetUrl, originatingUrl))
  }

  linksFoundFile.append("From\tTo\tParent Element\n")
  getUrlAndRecurse("http://www.guardian.co.uk", 5, "Start of job")
}
