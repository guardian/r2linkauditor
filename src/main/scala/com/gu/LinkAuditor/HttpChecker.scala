package com.gu.LinkAuditor

import org.jsoup.{HttpStatusException, Jsoup}

class HttpChecker {
  def getStatusCode(url: String): Int = {
    try {
      Jsoup.connect(url).followRedirects(false).timeout(60000).get()
      println("Successfully fetched " + url)
      200
    } catch {
      case e: HttpStatusException => e.getStatusCode
      case e => {
        println("Error connecting to " + url)
        println("Error: " + e.getMessage)
        -1
      }
    }
  }

}