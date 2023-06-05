ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "grackle-demo",
    dockerExposedPorts ++= Seq(8080),
    commonSettings,
    libraryDependencies ++=
      commonDeps ++
        circeDeps ++
        doobieDeps ++
        http4sDeps ++
        grackleDeps
  )
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(ScalafmtPlugin)

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-Wunused:all",
    "-language:implicitConversions",
    "-source:future",
    "-feature",
    "-deprecation"
  )
) ++ scalafmtSettings

lazy val scalafmtSettings = Seq(scalafmtOnCompile := true)

lazy val commonDeps = Seq(deps.cats, deps.catsCollections, deps.catsEffect, deps.kittens)
lazy val circeDeps = Seq(deps.circe, deps.circeGeneric, deps.circeParser)
lazy val doobieDeps = Seq(deps.doobie, deps.doobieHikari, deps.doobiePostgres)
lazy val grackleDeps = Seq(
  deps.grackle,
  deps.grackleCirce,
  deps.grackleDoobiePg,
  deps.grackleGeneric
)
lazy val http4sDeps =
  Seq(deps.http4sClient, deps.http4sServer, deps.http4sCirce, deps.http4sDsl)

lazy val deps = new {
  val catsVersion = "2.9.0"
  val catsCollectionVersion = "0.9.6"
  val catsEffectVersion = "3.5.0"
  val circeVersion = "0.14.5"
  val doobieVersion = "1.0.0-RC2"
  val grackleVersion = "0.12.0"
  val http4sVersion = "1.0.0-M39"
  val kittensVersion = "3.0.0"

  val cats = "org.typelevel" %% "cats-core" % catsVersion
  val catsCollections = "org.typelevel" %% "cats-collections-core" % catsCollectionVersion
  val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
  val kittens = "org.typelevel" %% "kittens" % kittensVersion

  val circe = "io.circe" %% "circe-core" % circeVersion
  val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  val circeParser = "io.circe" %% "circe-parser" % circeVersion

  val doobie = "org.tpolecat" %% "doobie-core" % doobieVersion
  val doobieHikari = "org.tpolecat" %% "doobie-hikari" % doobieVersion
  val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % doobieVersion

  val grackle = "edu.gemini" %% "gsp-graphql-core" % grackleVersion
  val grackleGeneric = "edu.gemini" %% "gsp-graphql-generic" % grackleVersion
  val grackleCirce = "edu.gemini" %% "gsp-graphql-circe" % grackleVersion
  val grackleDoobiePg = "edu.gemini" %% "gsp-graphql-doobie-pg" % grackleVersion

  val http4sClient = "org.http4s" %% "http4s-ember-client" % http4sVersion
  val http4sServer = "org.http4s" %% "http4s-ember-server" % http4sVersion
  val http4sCirce = "org.http4s" %% "http4s-circe" % http4sVersion
  val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion
}
