import scala.io.Source

lazy val root = (project in file("."))
  .enablePlugins(play.sbt.PlayScala)
  .settings(
    version      := "0.1",
    scalaVersion := "2.12.18",
    TaskKey[Unit]("check") := {
      val sources = List(
        "app/Controller.scala",
        "app/Page.scala.html"
      )
      def noHeader(source: String): Boolean = {
        val updated = Source.fromFile(source).getLines.mkString
        !updated.contains(s"Copyright ${java.time.Year.now()} HM Revenue & Customs")
      }
      val failed = sources.collect { case source if noHeader(source) => source }
      if (failed.nonEmpty)
        sys.error(s"Header was not added to ${failed.mkString(",")}")
    }
  )
