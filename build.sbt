import sbt.Keys.sourceDirectory


val projname = "tddplusplus"
val ver = "0.0.1"
organization := "org.vitalstar"


// coverageMinimum := 50
// coverageFailOnMinimum := true
// coverageExcludedPackages := "<empty>;xyz.*;.*abc.*;aaa\\.bbb\\..*"
// javaOptions in Test ++= Seq("-Xmx12g", "-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n")

EclipseKeys.eclipseOutput := Some("target/scala-2.11/classes")  // share the binary folder with sbt
fork in Test := true  // this is needed for the ClassLoader to work under `sbt test`

lazy val commonSettings = Seq(

version := ver,
scalaVersion := "2.11.8",
EclipseKeys.withSource := true,
// parallelExecution in test := false,
test in assembly := {},
assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}
) ++ packAutoSettings

lazy val project = Project(
id = projname,
base = file(".")).settings(commonSettings).settings(
name := projname,

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "2.3.1",
  "org.apache.spark" %% "spark-core" % "2.3.1",
  "org.apache.spark" %% "spark-mllib" %"2.3.1",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "org.apache.commons" % "commons-lang3" % "3.8",

  "com.typesafe.akka" %% "akka-http"   % "10.1.1",
  "com.typesafe.akka" %% "akka-stream" % "2.5.11",
  "io.spray" %%  "spray-json" % "1.3.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1",

  "com.lihaoyi" % "requests_2.11" % "0.1.8",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "com.typesafe.play" %% "play-json" % "2.7.4",

  //*********** test only ****************
  "org.mockito" % "mockito-core" % "1.8.5" % "test",
  "junit" % "junit" % "4.10" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.11" % "test")
)
