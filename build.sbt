import sbt.Keys.libraryDependencies
import sbt.ModuleID
import sbt.internal.IvyConsole.Dependencies


name := "data-obf-transformer"

version := "0.1"

scalaVersion := "2.11.8"
val akkaVersion = "2.5.13"
val akkaStreamVersion = "2.5.13"
val akkaHttpVersion = "10.1.1"
val schwatcherVersion = "0.3.2"
val testcontainersScalaVersion = "0.37.0"
val mLeapVersion = "0.13.0"

resolvers += Resolver.bintrayRepo("everpeace","maven")


libraryDependencies ++= Seq(
  "org.apache.flink" % "flink-core" % "1.10.1",
  "org.apache.flink" %% "flink-scala" % "1.10.1",
  "org.apache.flink" %% "flink-streaming-scala" % "1.10.1",
  "org.apache.flink" %  "flink-connector-kafka_2.11" % "1.10.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
  "com.typesafe.akka" %%  "akka-http" % akkaHttpVersion,
  "com.github.everpeace"  %% "healthchecks-core" %"0.3.0",
  "com.github.everpeace" %% "healthchecks-k8s-probes" % "0.3.0",
  "com.lihaoyi" %% "upickle" % "0.6.6",
  "com.beachape.filemanagement" %% "schwatcher" % schwatcherVersion,
  "org.springframework" % "spring-core" % "4.3.8.RELEASE",
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "ch.qos.logback" % "logback-core" % "1.2.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.lihaoyi" %% "upickle"  % "0.7.4")

libraryDependencies += "io.redbee" %% "ofertador-etls" % "0.1-SNAPSHOT"  notTransitive() //intransitive()
libraryDependencies += "io.redbee" %% "ofertador-etls" % "0.1-SNAPSHOT" % "test" classifier "tests" notTransitive() //intransitive()

libraryDependencies += "net.manub" %% "scalatest-embedded-kafka" % "2.0.0" % "test"
libraryDependencies += "net.manub" %% "scalatest-embedded-kafka-streams" % "2.0.0" % "test"
libraryDependencies += "com.dimafeng" %% "testcontainers-scala-scalatest" % testcontainersScalaVersion % "test"
libraryDependencies += "com.dimafeng" %% "testcontainers-scala-kafka" % testcontainersScalaVersion % "test"

libraryDependencies += "com.lihaoyi" %% "ujson" % "0.6.5"