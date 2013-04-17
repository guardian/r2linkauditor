package com.gu.LinkAuditor

import collection.JavaConversions._
import org.jsoup.nodes.{Document, Element, Comment}

object ComponentFinder extends App with Fetcher {
  def extractTemplates(l: List[String]) = {
    l.map(item => item.split(' ').filter(_.endsWith(".vm"))).flatten
  }

  def extractTemplateMentions(doc: Document): Set[String] = {
    val elements: List[Element] = doc.getAllElements.toList
    val htmlComments = elements.flatMap(x => x.childNodes).filter(_.isInstanceOf[Comment])

    val velocityMentions = htmlComments.map(_.toString).filter(_.endsWith(".vm -->"))

    extractTemplates(velocityMentions).toSet
  }

  // load the page up and find all the comments.
  val target = "http://www.gucode.gnl"
  val doc = get(target)


  println(extractTemplateMentions(doc))
}

object ComponentList {

}
