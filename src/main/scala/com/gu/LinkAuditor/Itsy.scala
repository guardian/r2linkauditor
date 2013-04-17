package com.gu.LinkAuditor

import java.net.URL
import collection.JavaConversions._


object Itsy extends App with Fetcher {
  val target = args(0)
  println(target)
  val targetHost = new URL(target).getHost

  val doc = get(target.toString)

  val elementList = doc.select("a[href]").toList

  val linkList = elementList.map(_.attr("href")).filterNot(_.startsWith("#"))
  // don't want fragment urls.
  val internalLinks = linkList.filter(_.startsWith("http://%s/".format(targetHost)))
  val relativeLinks = linkList.filter(_.startsWith("/"))
  val r1Links = internalLinks.filter(_.contains(",,"))

  println("%s ; %s absolute; %s relative".format(target, internalLinks.length, relativeLinks.length))
}