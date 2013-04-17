package com.gu.LinkAuditor

import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

class LinkSorterTest extends FunSuite with MockitoSugar {

  test("Can find working domain-relative links") {

    val goodTheGuardianComLink = "http://www.theguardian.com/" :: "http://www.theguardian.com/working/article/link" :: Nil
    val brokenTheGuardianComLink = "http://www.theguardian.com/broken/article/link"
    val workingGuardianCoUkLink = "http://www.guardian.co.uk/old/link"

    val httpClient = mock[HttpChecker]
    goodTheGuardianComLink.foreach {
      link =>
        when(httpClient.getStatusCode(link)) thenReturn 200
    }
    when(httpClient.getStatusCode(workingGuardianCoUkLink)) thenReturn 200
    when(httpClient.getStatusCode(brokenTheGuardianComLink)) thenReturn 404

    val listOfLinks = goodTheGuardianComLink ::: brokenTheGuardianComLink :: workingGuardianCoUkLink :: Nil

    val result = new LinkSorter(httpClient).filterWorkingRelativeLinks("http://www.theguardian.com", listOfLinks)

    assert(result === goodTheGuardianComLink)

  }

}
