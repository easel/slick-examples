name := "slick-examples"

organization := "com.theseventhsense"

mainClass in Compile := Some("HelloSlick")

scalaVersion := "2.12.8"

lazy val `slick-examples` = project.in(file("."))
  .settings(
    fork in run := true,
    parallelExecution := false,
    libraryDependencies := List(
      "org.scalatest" %% "scalatest" % "3.0.4" % "test"
    ))
  .dependsOn(`slick-db-routing`)

lazy val `slick-db-routing` = project.in(file("./slick-db-routing")).settings(libraryDependencies := List(
  "com.typesafe.slick" %% "slick" % "3.3.0",
  "org.slf4j" % "slf4j-nop" % "1.7.25",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
) ++ Dependencies.CirceCore ++ Dependencies.SlickPg :+ Dependencies.PostgresDriver)
