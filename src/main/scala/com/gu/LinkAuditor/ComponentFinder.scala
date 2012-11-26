package com.gu.LinkAuditor

import org.jsoup.Jsoup
import collection.JavaConversions._
import org.jsoup.nodes.{Document, Element, Comment, Node}

object ComponentFinder extends App {
  def extractTemplates(l : List[String]) = {
   l.map(item => item.split(' ').filter(_.endsWith(".vm"))).flatten
  }

  def extractTemplateMentions(doc: Document): Set[String] = {
    val elements:List[Element] = doc.getAllElements.toList
    val htmlComments = elements.flatMap(x => x.childNodes).filter(_.isInstanceOf[Comment])

    val velocityMentions = htmlComments.map(_.toString).filter(_.endsWith(".vm -->"))

    extractTemplates(velocityMentions).toSet
  }

  // load the page up and find all the comments.
  val target = "http://www.gucode.gnl"
  val doc = Jsoup.connect(target.toString).userAgent("Guardian dotCom linkAuditor").timeout(10000).get


  println(extractTemplateMentions(doc))
}

object ComponentList {

}
