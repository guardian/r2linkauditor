package com.gu.LinkAuditor

import org.jsoup.{HttpStatusException, Jsoup}
import scala.collection.JavaConversions._
import com.gargoylesoftware.htmlunit._
import com.gargoylesoftware.htmlunit.html.{DomElement, DomAttr, HtmlPage}
import java.net.URL

trait HttpChecker {
  def getStatusCode(url: String): Int

  def listAllLinks(url: String): List[String]

  def findContentInContext(url: String, toFind: String): List[String]
}


class Proxy(hostAndPort: String) {
  private val pattern = """(\w+):(\d+)""".r

  val (host: String, port: Int) = hostAndPort match {
    case pattern(h, p) => (h, p.toInt)
    case _ => ("", -1)
  }
}


class JsoupHttpChecker(val proxyHostAndPort: Option[String] = None) extends HttpChecker {

  proxyHostAndPort foreach {
    hostAndPort =>
      val proxy = new Proxy(hostAndPort)
      System.setProperty("http.proxyHost", proxy.host)
      System.setProperty("http.proxyPort", proxy.port.toString)
  }

  private def connectTo(url: String) = {
    Jsoup.connect(url).followRedirects(false).timeout(60000).header("X-GU-DEV", "true")
  }

  override def getStatusCode(url: String): Int = {
    try {
      val response = connectTo(url).execute()
      val statusCode = response.statusCode()

      val msg = "Fetched %s [%d]".format(url, statusCode) +
        (Option(response.header("X-GU-PageRenderer")) map
          (renderer => " rendered by %s".format(renderer))).getOrElse("")
      println(msg)

      statusCode
    } catch {
      case e: HttpStatusException => {
        println("ERROR fetching %s: [%s: %d]".format(url, e.getMessage, e.getStatusCode))
        -1
      }
      case e => {
        println("ERROR fetching %s: [%s]".format(url, e.getMessage))
        -1
      }
    }
  }

  override def listAllLinks(url: String): List[String] = {
    try {
      val response = connectTo(url).execute()
      val statusCode = response.statusCode()
      if (statusCode == 200) {
        response.parse().
          getElementsByAttribute("href").map(_.attr("href")).
          filter(_.startsWith("http://"))
          .sorted.distinct.toList
      } else {
        println("ERROR finding links in %s: [%d]".format(url, statusCode))
        Nil
      }
    } catch {
      case e: HttpStatusException => {
        println("ERROR finding links in %s: [%s: %d]".format(url, e.getMessage, e.getStatusCode))
        Nil
      }
      case e => {
        println("ERROR finding links in %s: [%s]".format(url, e.getMessage))
        Nil
      }
    }
  }

  override def findContentInContext(url: String, toFind: String): List[String] = {
    try {
      val elements = connectTo(url).get().getElementsContainingOwnText(toFind)
      (elements map (_.toString)).toList
    } catch {
      case e: HttpStatusException => {
        println("ERROR fetching %s: [%s: %d]".format(url, e.getMessage, e.getStatusCode))
        Nil
      }
      case e => {
        println("ERROR fetching %s: [%s]".format(url, e.getMessage))
        Nil
      }
    }
  }

}


class HtmlUnitHttpChecker(val proxyHostAndPort: Option[String] = None) extends HttpChecker {

  // webclient is not thread-safe so building a new one for each request
  private def webClient = {
    val client = new WebClient
    client.addRequestHeader("X-GU-DEV", "true")
    val options = client.getOptions
    options.setTimeout(60000)
    options.setRedirectEnabled(false)
    options.setCssEnabled(true)
    options.setPopupBlockerEnabled(true)
    options.setPrintContentOnFailingStatusCode(false)
    options.setThrowExceptionOnFailingStatusCode(false)
    options.setThrowExceptionOnScriptError(false)
    proxyHostAndPort foreach {
      hostAndPort =>
        val proxy = new Proxy(hostAndPort)
        options.setProxyConfig(new ProxyConfig(proxy.host, proxy.port))
    }
    client
  }

  override def getStatusCode(url: String): Int = {

    // a head request is accurate enough to indicate working or not
    val request = new WebRequest(new URL(url), HttpMethod.HEAD)
    val response = webClient.loadWebResponse(request)
    val statusCode = response.getStatusCode

    val msg = "Thread %s fetched %s [%d]".format(Thread.currentThread.getName, url, statusCode) +
      (Option(response.getResponseHeaderValue("X-GU-PageRenderer")) map
        (renderer => " rendered by %s".format(renderer))).getOrElse("")
    println(msg)

    statusCode
  }

  private def processPage(url: String)(process: (HtmlPage) => List[String]): List[String] = {
    val statusCode = getStatusCode(url)

    statusCode match {
      case 200 => process(webClient.getPage(url).asInstanceOf[HtmlPage])
      case 301 | 302 => processRedirect(url, process)
      case _ => {
        println("ERROR processing %s: [%d]".format(url, statusCode))
        Nil
      }
    }

  }

  private def processRedirect(url: String, process: (HtmlPage) => List[String], redirects: List[String] = Nil): List[String] = {
    val request = new WebRequest(new URL(url), HttpMethod.HEAD)
    val response = webClient.loadWebResponse(request)
    val targetUrl = response.getResponseHeaderValue("Location")

    println("Redirecting %s -> %s [redirection depth %d]".format(url, targetUrl, redirects.length + 1))

    val statusCode = getStatusCode(targetUrl)

    statusCode match {
      case 200 => processPage(targetUrl)(process)
      case 301 | 302 => {
        if (redirects.length < 10) processRedirect(url, process, redirects :+ targetUrl)
        else {
          val redirectLoop = redirects.mkString(" -> ")
          println("ERROR redirect loop: %s".format(redirectLoop))
          Nil
        }
      }
      case _ => {
        println("ERROR processing %s: [%d]".format(url, statusCode))
        Nil
      }
    }

  }

  override def listAllLinks(url: String): List[String] = {
    processPage(url) {
      page =>
        val linkAttributes = page.getByXPath("//a/@href")
        linkAttributes.map(_.asInstanceOf[DomAttr].getValue).filter(_.startsWith("http://")).sorted.distinct.toList
    }
  }

  override def findContentInContext(url: String, toFind: String): List[String] = {
    processPage(url) {
      page =>
        val matchingElements = page.getHtmlElementDescendants filter {
          el => el.getChildElements.isEmpty && el.asXml.contains(toFind)
        }
        matchingElements.map(_.asInstanceOf[DomElement].asXml).toList
    }
  }

}
