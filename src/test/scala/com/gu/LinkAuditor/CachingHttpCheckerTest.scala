package com.gu.LinkAuditor

import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import collection.parallel.ForkJoinTasks

class CachingHttpCheckerTest extends FunSuite with MockitoSugar {

  ForkJoinTasks.defaultForkJoinPool.setParallelism(32)

  val urls = List.fill(10000)("url").par

  test("should only cache result of same request in different threads once") {
    val delegate = mock[HttpChecker]
    when(delegate.getStatusCode("url")) thenReturn 200
    val checker = new CachingHttpChecker(delegate)
    urls foreach (checker.getStatusCode(_))

    assert(checker.statusCodeCache.size === 1)
  }

  test("should only cache references in same request in different threads once") {
    val delegate = mock[HttpChecker]
    when(delegate.findContentInContext("url", "ref")) thenReturn "context of ref in content of url" :: Nil
    val checker = new CachingHttpChecker(delegate)
    urls foreach (checker.findContentInContext(_, "ref"))

    assert(checker.contentReferencesCache.size === 1)
  }

}
