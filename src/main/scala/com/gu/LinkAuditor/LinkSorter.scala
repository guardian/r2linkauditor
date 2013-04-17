package com.gu.LinkAuditor

import org.jsoup.Jsoup
import java.net.URL

class LinkSorter(httpClient: HttpClient) {

  def filterWorkingRelativeLinks(originUrl: String, links: List[String]): List[String] = {
    val originHost = new URL(originUrl).getHost
    val linksInOriginDomain = links.filter {
      link => {
        val targetHost = new URL(link).getHost
        targetHost == originHost
      }
    }
    linksInOriginDomain.filter(link => httpClient.getStatusCode(link) == 200)
  }

}

class HttpClient {

  def getStatusCode(url: String): Int = -1

}
