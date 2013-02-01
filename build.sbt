name := "feature-model-synthesis"

version := "1.0"

organization := "ca.uwaterloo.gsd"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "1.6.1" % "test",
    "com.googlecode.kiama" %% "kiama" % "1.2.0",
    "org.sat4j" % "org.sat4j.core" % "2.3.1",
    "com.google.guava" % "guava" % "14.0-rc1"
)

resolvers += "Local Maven Repository" at Path.userHome.asURL + "/.m2/repository"

// only show 10 lines of stack traces
traceLevel := 10

javaOptions += "-Xss8192k -Xmx2048m"

scalacOptions := Seq("-deprecation", "-unchecked")

