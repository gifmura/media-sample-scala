name := """media-sample-scala"""

version := "2.6.x"
lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.11.12", "2.12.4")

libraryDependencies += guice
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.3"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3"
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.13"
libraryDependencies += specs2 % Test
libraryDependencies += "commons-io" % "commons-io" % "2.6"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"
libraryDependencies += "jp.t2v" %% "play2-pager" % "0.2.0"
libraryDependencies += "org.abstractj.kalium" % "kalium" % "0.6.0"
libraryDependencies += "com.typesafe.akka" %% "akka-distributed-data" % "2.5.17"
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.11.478"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

TwirlKeys.templateImports += "jp.t2v.lab.play2.pager._"

import play.sbt.routes.RoutesKeys
RoutesKeys.routesImport += "jp.t2v.lab.play2.pager.Pager"
RoutesKeys.routesImport += "jp.t2v.lab.play2.pager.Bindables._"
RoutesKeys.routesImport += "models.Entry"