import sbt._

object FMSBuild extends Build {
    lazy val graph = Project(id = "graph", base = file("graph/"))
    lazy val root = Project(id = "feature-model-synthesis", base = file(".")) dependsOn (graph)
}

