package com.gu.LinkAuditor

import java.net.URL
import collection.mutable.HashSet
import scalax.file.Path
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.jsoup.nodes.Document

object CountingSpider extends App with Spider {

  val r1History = new HashSet[String]

  val dir = "links-%s".format(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm").print(new DateTime()))
  val linkComparisonFile = Path(dir, "linkComparison")
  val r1LinksFile = Path(dir, "r1Links")
  val brokenLinksFile = Path(dir, "404s")
  val componentList = Path(dir, "components")

  override def process(url: URL, doc: Document) {
    val templates = ComponentFinder.extractTemplateMentions(doc)
    componentList.append("\n URL>> " + url + "\n")
    componentList.append(templates.toList.mkString("\n"))

    val linkList = links(doc).map(_.attr("href")).filterNot(_.startsWith("#")) // don't want fragment urls.
    val internalLinks = linkList.filter(_.startsWith("http://%s/".format(url.getHost)))
    val relativeLinks = linkList.filter(_.startsWith("/"))
    val r1Links = internalLinks.filter(_.contains(",,"))

    linkComparisonFile.append("%s| %s| %s\n".format(url, internalLinks.length, relativeLinks.length))

    r1Links filterNot (r1History.contains(_)) foreach {
      r1Link =>
        r1History add r1Link
        r1LinksFile.append("%s contains r1 link: %s\n".format(url, r1Link))
    }
  }

  override def logToBrokenLinksFile(originatingUrl: String, targetUrl: String) {
    brokenLinksFile.append("Exception getting %s from %s\n".format(targetUrl, originatingUrl))
  }

  println("Url; Absolute internal link count; Relative link count")
  getUrlAndRecurse("http://www.gucode.gnl", 1, "Start of job")
}