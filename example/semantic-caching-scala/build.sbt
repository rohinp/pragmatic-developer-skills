ThisBuild / scalaVersion := "3.7.2"
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Werror")

lazy val root = project
  .in(file("."))
  .settings(
    name := "semantic-caching-scala",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.2.0" % Test,
      "org.scalameta" %% "munit-scalacheck" % "1.2.0" % Test
    )
  )

