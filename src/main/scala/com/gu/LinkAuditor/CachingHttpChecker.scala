package com.gu.LinkAuditor

import scala.collection.mutable

class CachingHttpChecker(delegate: HttpChecker) extends HttpChecker {

  val statusCodeCache: mutable.Map[String, Int] = mutable.Map[String, Int]()
  val contentReferencesCache: mutable.Map[(String, String), List[String]] = mutable.Map[(String, String), List[String]]()

  override def getStatusCode(url: String): Int = {
    statusCodeCache.get(url).getOrElse(synchronized {
      statusCodeCache.getOrElseUpdate(url, delegate.getStatusCode(url))
    })
  }

  override def findContentInContext(url: String, toFind: String): List[String] = {
    contentReferencesCache.get((url, toFind)).getOrElse(synchronized {
      contentReferencesCache.getOrElseUpdate((url, toFind), delegate.findContentInContext(url, toFind))
    })
  }

  def listAllLinks(url: String): List[String] = delegate.listAllLinks(url)

}
