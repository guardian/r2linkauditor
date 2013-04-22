package com.gu.LinkAuditor

import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

class PageLinkAuditorTest extends FunSuite with MockitoSugar {

  test("Can filter out working target domain links") {
    val goodTheGuardianComLinks = "http://www.theguardian.com/" :: "http://www.theguardian.com/working/article/link" ::
      "http://www.theguardian.com/will/get/raptured" :: Nil
    val brokenTheGuardianComLinks = "http://www.theguardian.com/broken/article/link" :: Nil
    val workingGuardianCoUkLinks = "http://www.guardian.co.uk/will/get/raptured" :: "http://www.guardian.co.uk/left/behind" :: Nil
    val brokenGuardianCoUkLinks = "http://www.guardian.co.uk/broken/article/link" :: Nil
    val externalLinks = "http://www.adserver.com" :: Nil

    val httpClient = mock[HttpChecker]

    goodTheGuardianComLinks.foreach { link => when(httpClient.getStatusCode(link)) thenReturn 200}
    workingGuardianCoUkLinks.foreach { link => when(httpClient.getStatusCode(link)) thenReturn 200}
    externalLinks.foreach { link => when(httpClient.getStatusCode(link)) thenReturn 200}
    brokenGuardianCoUkLinks.foreach { link => when(httpClient.getStatusCode(link)) thenReturn 404}
    brokenTheGuardianComLinks.foreach { link => when(httpClient.getStatusCode(link)) thenReturn 404}

    val listOfLinks = goodTheGuardianComLinks ::: brokenTheGuardianComLinks :::
      workingGuardianCoUkLinks ::: brokenGuardianCoUkLinks ::: externalLinks

    val sorter: PageLinkAuditor = new PageLinkAuditor("www.theguardian.com", "www.guardian.co.uk", listOfLinks, httpClient)

    assert(sorter.brokenLinksToOriginalDomain === brokenGuardianCoUkLinks)
    assert(sorter.brokenLinkToTargetDomain === brokenTheGuardianComLinks)
    assert(sorter.linksToOriginalDomain === workingGuardianCoUkLinks ::: brokenGuardianCoUkLinks )
    assert(sorter.linksToTargetDomain === goodTheGuardianComLinks ::: brokenTheGuardianComLinks)
    assert(sorter.originalDomainLinksThatAreNotRedirectable === "http://www.guardian.co.uk/left/behind" :: Nil)
    assert(sorter.originalDomainLinksThatAreRedirectable === "http://www.guardian.co.uk/will/get/raptured" :: Nil)
    assert(sorter.workingLinksToOriginalDomain === workingGuardianCoUkLinks)
    assert(sorter.workingLinksToTargetDomain === goodTheGuardianComLinks)
  }

}
