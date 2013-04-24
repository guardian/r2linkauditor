package com.gu.LinkAuditor

import java.net.URL
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import scalax.file.Path
import scala.collection.parallel.ParSeq

class PageLinkAuditor(targetHost: String, originalHost: String, allLinks: List[String], httpClient: HttpChecker) {

  lazy val (linksToTargetDomain, externalLinks) = partitionLinksToMatch(targetHost, allLinks.par)
  lazy val (linksToOriginalDomain, linksToNonGuDomains) = partitionLinksToMatch(originalHost, externalLinks.par)
  lazy val (workingLinksToTargetDomain, brokenLinksToTargetDomain) = linksToTargetDomain.par.partition(link => httpClient.getStatusCode(link) == 200)
  lazy val (workingLinksToOriginalDomain, brokenLinksToOriginalDomain) = linksToOriginalDomain.par.partition(link => httpClient.getStatusCode(link) == 200)
  lazy val (originalDomainLinksThatAreRedirectable, originalDomainLinksThatAreNotRedirectable) = workingLinksToOriginalDomain.par.partition {
    link =>
      val original: URL = new URL(link)
      val targetHostLink: String = new URL(original.getProtocol, targetHost, original.getFile).toExternalForm
      httpClient.getStatusCode(targetHostLink) == 200
  }

  def partitionLinksToMatch(host: String, links: ParSeq[String]): (ParSeq[String], ParSeq[String]) = {
    links.partition(new URL(_).getHost == host)
  }

}


class PageSpider(originalHost: String, targetHost: String, seedPath: String, recursionDepth: Int) {

  val reportDir = "links-%s".format(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH-mm").print(new DateTime()))

  val httpChecker = new CachingHttpChecker

  def audit(url: String, recursionDepth: Int) {
    if (recursionDepth > 0) {
      val allLinks = httpChecker.listAllLinks(url)
      val auditor = new PageLinkAuditor(targetHost, originalHost, allLinks, httpChecker)

      val reportFile = {
        val urlPathAsFilename = new URL(url).getFile.replace('/', '_') + ".txt"
        Path(reportDir, urlPathAsFilename)
      }

      reportFile.append("Checking URL %s\n".format(url))
      reportFile.append("Total links found: %d\n\n".format(allLinks.size))
      report(reportFile, auditor.linksToNonGuDomains, "Links to non-GU domains", 1)
      report(reportFile, auditor.workingLinksToTargetDomain, "Working links to new domain", 2)
      report(reportFile, auditor.brokenLinksToTargetDomain, "Broken links to new domain", 3)
      report(reportFile, auditor.brokenLinksToOriginalDomain, "Broken links to old domain", 4)
      report(reportFile, auditor.originalDomainLinksThatAreRedirectable, "Links to old domain that can be rewritten to new domain", 5)
      report(reportFile, auditor.originalDomainLinksThatAreNotRedirectable, "Links to old domain that cannot be rewritten to new domain", 6)

      auditor.workingLinksToTargetDomain.par foreach (audit(_, recursionDepth - 1))
    }
  }

  def report(reportFile: Path, links: ParSeq[String], linkCategory: String, categoryKey: Int) {
    reportFile.append('\n')
    reportFile.append('\n')
    reportFile.append("+++++ %s +++++\n".format(linkCategory))
    reportFile.append("Links found: %d\n".format(links.size))
    reportFile.append('\n')
    if (links.isEmpty) reportFile.append("NONE\n")
    else links.foreach(link => reportFile.append("[%s] %s\n".format(categoryKey, link)))
    reportFile.append('\n')
    reportFile.append('\n')
  }

  println("Writing to " + reportDir)
  audit("http://%s%s".format(targetHost, seedPath), recursionDepth)

}


object PageSpiderClient extends App {

  val oldHost = args(0)
  val newHost = args(1)
  val seedPath = args(2)
  val recursionDepth = args(3).toInt

  val spider = new PageSpider(oldHost, newHost, seedPath, recursionDepth)

}
