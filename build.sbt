ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.17"
val sparkVersion = "3.2.2"

lazy val root = (project in file("."))
  .settings(
    name := "tendo-candidate-exercise-p1",
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "com.crealytics" %% "spark-excel" % s"${sparkVersion}_0.18.0"
)