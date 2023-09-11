lazy val root = (project in file("."))
  .enablePlugins(SbtAutoBuildPlugin)
  .settings(
    version := "0.1",
    scalaVersion := "2.12.8"
  )
