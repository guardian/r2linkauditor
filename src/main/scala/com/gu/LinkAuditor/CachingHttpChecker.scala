package com.gu.LinkAuditor

import scala.collection.mutable.Map

class CachingHttpChecker extends HttpChecker{
  val statusCodeCache: Map[String, Int] = Map[String, Int]()

  override def getStatusCode(url: String): Int = {
    statusCodeCache getOrElseUpdate(url, super.getStatusCode(url))
  }
}
