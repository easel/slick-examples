import sbt._

object Dependencies {

  val CirceCore =
    Seq("io.circe" %% "circe-core" % "0.11.1")

  val PostgresDriver =
    "org.postgresql" % "postgresql" % "42.2.5"

  val ScalaCheck =
    "org.scalacheck" %% "scalacheck" % "1.13.5"

  val Simulacrum =
    "com.github.mpilquist" %% "simulacrum" % "0.15.0"

  val SlickPg =
    Seq(
      "com.github.tminglei" %% "slick-pg",
      "com.github.tminglei" %% "slick-pg_core",
      "com.github.tminglei" %% "slick-pg_circe-json"
    ).map(_ % "0.17.2")

}
