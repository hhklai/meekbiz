name := "meekbiz"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  // WebJars infrastructure
  "org.webjars" % "webjars-locator" % "0.13",
  "org.webjars" %% "webjars-play" % "2.2.2",
  // WebJars dependencies
  "org.webjars" % "underscorejs" % "1.8.3",
  "org.webjars" % "jquery" % "2.1.1",
  "org.webjars" % "bootstrap" % "3.1.1" exclude("org.webjars", "jquery"),
  "org.webjars" % "angularjs" % "1.3.0-rc.1" exclude("org.webjars", "jquery"),
  "org.webjars" % "angular-sanitize" % "1.3.0-beta.18",
  "org.webjars" % "jquery-cookie" % "1.4.0",
  "org.webjars" % "angular-ui-bootstrap" % "0.11.0-2",
  // Java infrastructure
  javaJdbc,
  javaJpa,
  cache,
  "org.hibernate" % "hibernate-entitymanager" % "3.6.9.Final",
  "org.hibernate" % "hibernate-commons-annotations" % "3.2.0.Final",
  "mysql" % "mysql-connector-java" % "5.1.21",
  // Java libraries
  "org.mindrot" % "jbcrypt" % "0.3m",
  "commons-codec" % "commons-codec" % "1.9",
  "com.groupon" % "locality-uuid" % "1.1.1",
  "org.owasp" % "antisamy" % "1.4",
  // Play plugins
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.0",
  // Test dependencies
  "org.powermock" % "powermock-module-junit4" % "1.5" % "test",
  "org.powermock" % "powermock-api-mockito" % "1.5" % "test",
  "org.unitils" % "unitils-core" % "3.3" % "test"
)

playJavaSettings

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/repo"

// This tells Play to optimize this file and its dependencies
requireJs += "main.js"

// The main config file
// See http://requirejs.org/docs/optimization.html#mainConfigFile
requireJsShim := "build.js"

// To completely override the optimization process, use this config option:
//requireNativePath := Some("node r.js -o name=main out=javascript-min/main.min.js")

