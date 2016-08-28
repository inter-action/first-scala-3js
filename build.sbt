enablePlugins(ScalaJSPlugin)

name := "first-scala-3js"

version := "0.1"

scalaVersion := "2.11.7"

resolvers += "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "releases"  at "https://oss.sonatype.org/content/groups/scala-tools"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

scalaJSStage in Global := FastOptStage

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.0",
  "com.github.japgolly.fork.scalaz" %%% "scalaz-core" % "7.1.3"
)

