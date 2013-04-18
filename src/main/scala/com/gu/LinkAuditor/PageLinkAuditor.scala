package com.gu.LinkAuditor

import org.jsoup.{HttpStatusException, Jsoup}
import java.net.URL
import scala.collection.JavaConversions._
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import scalax.file.Path

class PageLinkAuditor(targetDomain: String, originalDomain: String, allLinks: List[String], httpClient: HttpChecker) {
  val targetHost = new URL(targetDomain).getHost
  val originalHost = new URL(originalDomain).getHost

  lazy val (linksToTargetDomain, externalLinks) = partitionLinksToMatch(targetHost, allLinks)
  lazy val (linksToOriginalDomain, linksToNonGuDomains) = partitionLinksToMatch(originalHost, externalLinks)
  lazy val (workingLinksToTargetDomain, brokenLinkToTargetDomain) = linksToTargetDomain.partition(link => httpClient.getStatusCode(link) == 200)
  lazy val (workingLinksToOriginalDomain, brokenLinksToOriginalDomain) = linksToOriginalDomain.partition(link => httpClient.getStatusCode(link) == 200)
  lazy val (originalDomainLinksThatAreRedirectable, originalDomainLinksThatAreNotRedirectable) = workingLinksToOriginalDomain.partition {
    link =>
      val original: URL = new URL(link)
      val targetHostLink: String = new URL(original.getProtocol, targetHost, original.getFile).toExternalForm
      httpClient.getStatusCode(targetHostLink) == 200
  }

  def partitionLinksToMatch(host: String, links: List[String]): (List[String], List[String]) = {
    links.partition {
      link => {
        try {
          val linkUrl = new URL(link)
          val linkHost = linkUrl.getHost
          linkHost == host
        } catch {
          case e: Exception => {
            println("error getting URL %s".format(link))
            println("Error: %s".format(e.getMessage))
            false
          }
          case _ => false
        }
      }
    }
  }

}


object PageLinkAuditorClient extends App {

  val oldUrl = args(0)
  val newUrl = args(1)

  val reportFile = {
    val dir = "links-%s".format(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH-mm").print(new DateTime()))
    val urlPathAsFilename = new URL(newUrl).getFile.replace('/', '_') + ".txt"
    Path(dir, urlPathAsFilename)
  }

  val allLinks = Jsoup.connect(newUrl).followRedirects(false).timeout(0).get().select("a[href]").map(_.attr("href")).filter(_.startsWith("http://")).distinct.toList
  val auditor = new PageLinkAuditor(newUrl, oldUrl, allLinks, new HttpChecker)

  def report(links: List[String], linkCategory: String) {
    reportFile.append('\n')
    reportFile.append('\n')
    reportFile.append("+++++ %s +++++\n".format(linkCategory))
    reportFile.append('\n')
    if (links.isEmpty) reportFile.append("NONE\n")
    else links.foreach(link => reportFile.append(link + '\n'))
    reportFile.append('\n')
    reportFile.append('\n')
  }

  reportFile.append("Old URL: %s\n".format(oldUrl))
  reportFile.append("New URL: %s\n".format(newUrl))
  report(auditor.workingLinksToTargetDomain, "Working links to new domain")
  report(auditor.workingLinksToOriginalDomain, "Working links to old domain")
  report(auditor.brokenLinkToTargetDomain, "Broken links to new domain")
  report(auditor.brokenLinksToOriginalDomain, "Broken links to old domain")
  report(auditor.originalDomainLinksThatAreRedirectable, "Links to old domain that can be rewritten to new domain")
  report(auditor.originalDomainLinksThatAreNotRedirectable, "Links to old domain that cannot be rewritten to new domain")

}
