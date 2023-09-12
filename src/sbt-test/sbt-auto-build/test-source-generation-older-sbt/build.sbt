import scala.io.Source

lazy val root = (project in file("."))
  .enablePlugins(play.sbt.PlayScala)
  .settings(
    version      := "0.1",
    scalaVersion := "2.12.14",
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
      val oldDateUpdated = Source.fromFile("app/OldController.scala").getLines().mkString.contains(s"Copyright ${java.time.Year.now()} HM Revenue & Customs")
      if (failed.nonEmpty || oldDateUpdated) {
        val message = s"${if (failed.nonEmpty) s"Header was not added to ${failed.mkString(",")}. " else ""}${if (oldDateUpdated) "Header date was updated in app/OdController.scala" else ""}"
        sys.error(message)
      }
    }
  )
