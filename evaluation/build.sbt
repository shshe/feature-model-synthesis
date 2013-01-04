name := "evaluation"

version := "1.0"

organization := "ca.uwaterloo.gsd"

scalaVersion := "2.9.2"

resolvers += "Local Maven Repository" at Path.userHome.asURL + "/.m2/repository"

// only show 10 lines of stack traces
traceLevel in run := 10

scalacOptions := Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "1.6.1" % "test",
    "com.googlecode.kiama" %% "kiama" % "1.2.0"
)

libraryDependencies += "com.github.scopt" %% "scopt" % "2.1.0"

resolvers += "sonatype-public" at "https://oss.sonatype.org/content/groups/public"
