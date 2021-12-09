name := "hw5"

version := "0.1"

scalaVersion := "2.13.7"

libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % "3.2.0" withSources(),
    "org.apache.spark" %% "spark-sql" % "3.2.0" withSources(),
    "org.apache.spark" %% "spark-mllib" % "3.2.0" withSources()
)
