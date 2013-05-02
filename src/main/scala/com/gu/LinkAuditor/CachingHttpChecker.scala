package com.gu.LinkAuditor

import scala.collection.mutable
import scala.collection.JavaConversions._
import java.util.concurrent.ConcurrentHashMap

class CachingHttpChecker(delegate: HttpChecker) extends HttpChecker {

  val statusCodeCache: mutable.ConcurrentMap[String, Int] =
    new JConcurrentMapWrapper(new ConcurrentHashMap[String, Int]())

  val contentReferencesCache: mutable.ConcurrentMap[(String, String), List[String]] =
    new JConcurrentMapWrapper(new ConcurrentHashMap[(String, String), List[String]]())

  override def getStatusCode(url: String): Int = {
    val prevValue = statusCodeCache.putIfAbsent(url, delegate.getStatusCode(url))
    prevValue.getOrElse(statusCodeCache.get(url).get)
  }

  override def findContentInContext(url: String, toFind: String): List[String] = {
    val prevValue = contentReferencesCache.putIfAbsent((url, toFind), delegate.findContentInContext(url, toFind))
    prevValue.getOrElse(contentReferencesCache.get((url, toFind)).get)
  }

  def listAllLinks(url: String): List[String] = delegate.listAllLinks(url)

}
