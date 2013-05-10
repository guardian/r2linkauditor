name := "Link Auditor"

version := "1.0"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
    "org.jsoup" % "jsoup" % "1.7.1",
    "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.1-seq",
    "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.1-seq",
    "joda-time" % "joda-time" % "2.1",
    "org.joda" % "joda-convert" % "1.1",
    "commons-lang" % "commons-lang" % "2.6",
    "net.sourceforge.htmlunit" % "htmlunit" % "2.12",
    "org.scalatest" %% "scalatest" % "1.8" % "test",
    "org.mockito" % "mockito-core" % "1.9.5" % "test"
)

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

mainClass in oneJar := Some("com.gu.LinkAuditor.Itsy")
