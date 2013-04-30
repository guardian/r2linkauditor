package com.gu.LinkAuditor

import scala.collection.mutable

class CachingHttpChecker(override val proxy: Option[String] = None) extends HttpChecker(proxy) {
  val statusCodeCache: mutable.Map[String, Int] = mutable.Map[String, Int]()
  val contentReferencesCache: mutable.Map[(String, String), List[String]] = mutable.Map[(String, String), List[String]]()

  override def getStatusCode(url: String): Int = {
    statusCodeCache getOrElseUpdate(url, super.getStatusCode(url))
  }

  override def findContentInContext(url: String, toFind: String): List[String] =
    contentReferencesCache getOrElseUpdate((url, toFind), super.findContentInContext(url, toFind))
}
