package com.gu.LinkAuditor

import org.jsoup.Jsoup
import java.net.URL
import collection.JavaConversions._


object Itsy extends App {
  val target = args(0)
  println(target)
  val targetHost = new URL(target).getHost

  val doc = Jsoup.connect(target.toString).userAgent("Guardian dotCom linkAuditor").get

  val elementList = doc.select("a[href]").toList

  val linkList = elementList.map(_.attr("href")).filterNot(_.startsWith("#")) // don't want fragment urls.
  val internalLinks = linkList.filter(_.startsWith("http://%s/".format(targetHost)))
  val relativeLinks = linkList.filter(_.startsWith("/"))
  val r1Links = internalLinks.filter(_.contains(",,"))

  println("%s ; %s absolute; %s relative".format(target, internalLinks.length, relativeLinks.length))
}