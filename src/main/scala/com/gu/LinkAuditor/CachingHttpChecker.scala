package com.gu.LinkAuditor

import scala.collection.mutable

class CachingHttpChecker(delegate: HttpChecker) extends HttpChecker {

  val statusCodeCache: mutable.Map[String, Int] = mutable.Map[String, Int]()
  val linkCache: mutable.Map[String, List[String]] = mutable.Map[String, List[String]]()
  val contentReferenceCache: mutable.Map[(String, String), List[String]] = mutable.Map[(String, String), List[String]]()

  override def getStatusCode(url: String): Int = {
    /*
     * In a multi-threaded environment this will occasionally update a value it already holds
     * but it's not worth making it synchronized as that's slower and the effect is harmless anyway
     */
    statusCodeCache.getOrElseUpdate(url, delegate.getStatusCode(url))
  }

  override def listAllLinks(url: String): List[String] = {
    /*
     * In a multi-threaded environment this will occasionally update a value it already holds
     * but it's not worth making it synchronized as that's slower and the effect is harmless anyway
     */
    linkCache.getOrElseUpdate(url, delegate.listAllLinks(url))
  }

  override def findContentInContext(url: String, toFind: String): List[String] = {
    /*
     * In a multi-threaded environment this will occasionally update a value it already holds
     * but it's not worth making it synchronized as that's slower and the effect is harmless anyway
     */
    contentReferenceCache.getOrElseUpdate((url, toFind), delegate.findContentInContext(url, toFind))
  }

}
