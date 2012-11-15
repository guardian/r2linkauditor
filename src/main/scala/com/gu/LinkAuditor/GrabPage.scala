package com.gu.LinkAuditor

import org.jsoup.Jsoup
import org.jsoup.nodes._
import collection.JavaConversions

object GrabPage extends App {
  val doc = Jsoup.connect("http://www.guardian.co.uk").get()

  // I want to answer 3 simple questions:
  // 1. how many links are there on the pages that I spider from the network front (2 deep)
  // 2. how many times is www.guardian.co.uk/* referenced?
  // 3. how many links are not pointing to www.guardian.co.uk/* ?

  // and I need this information recorded someplace so that I can compare it over time.

  val links: Iterator[Element] = JavaConversions.asScalaIterator(doc.select("a[href]").listIterator())
  val linkList = links.toList
  val internalLinks = linkList.filter(_.attr("href").startsWith("http://www.guardian.co.uk/"))
  val fragmentUrls = linkList.filter(_.attr("href").startsWith("#"))
  val externalLinks = linkList -- internalLinks -- fragmentUrls

  println("Total number of links on www.guardian.co.uk: " + linkList.length)
  println("Total number of links to same domain: " + internalLinks.length)
  println("Total number of links to same page: " + fragmentUrls.length)
  println ("Total number of links to external domain: " + externalLinks.length)

}