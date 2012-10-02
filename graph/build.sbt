name := "graph"

version := "1.0"

organization := "ca.uwaterloo.gsd"

scalaVersion := "2.9.1"

resolvers += "Local Maven Repository" at Path.userHome.asURL + "/.m2/repository"

// only show 10 lines of stack traces
traceLevel in run := 10
