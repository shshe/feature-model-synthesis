import sbt._

object FMSBuild extends Build {
    lazy val graph = Project(id = "graph", base = file("graph/"))
    lazy val graphCompat = Project(id = "graph-compat", base = file("graph-compat/")) dependsOn (graph)
    lazy val root = Project(id = "feature-model-synthesis", base = file(".")) dependsOn (graph, graphCompat)

    lazy val evaluation = Project(id = "evaluation", base = file("evaluation/")) dependsOn (root) 
}

