import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName         = "play-boilerplate-app"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "mysql" % "mysql-connector-java" % "5.1.18"
  )

  def customLessEntryPoints(base: File): PathFinder = (
    (base / "app" / "assets" / "stylesheets" / "bootstrap" * "bootstrap.less") +++
    (base / "app" / "assets" / "stylesheets" / "bootstrap" * "responsive.less") +++
    (base / "app" / "assets" / "stylesheets" * "*.less"))
    
  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
    .settings(
      lessEntryPoints <<= baseDirectory(customLessEntryPoints)    
    )

}
