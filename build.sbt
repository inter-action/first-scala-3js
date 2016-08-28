enablePlugins(ScalaJSPlugin)

name := "first-scala-3js"

version := "0.1"

scalaVersion := "2.11.7"


resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

scalaJSStage in Global := FastOptStage

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.0",
  "com.github.japgolly.fork.scalaz" %%% "scalaz-core" % "7.1.3",
  "org.denigma" %%% "threejs-facade" % "0.0.74-0.1.7"
)




