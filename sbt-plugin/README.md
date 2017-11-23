[![Bintray](https://api.bintray.com/packages/sugakandrey/sbt-plugins/sbt-scalamu/images/download.svg?style=flat-square)](https://bintray.com/sugakandrey/sbt-plugins/sbt-scalamu/_latestVersion)
# sbt-scalamu
sbt-scalamu is a plugin for SBT that allows for convenient integration of 
mutation testing into your build.

## Getting started
Make sure your SBT version is supported (currently sbt-scalamu is cross-built
against sbt 0.13 and 1.0).
```
sbt.version = 1.0.3
```
Add the plugin to `project/plugins.sbt`:
```
addSbtPlugin("io.github.sugakandrey" % "sbt-scalamu" % "0.1.2")
```
Execute `scalamuRun` command inside SBT shell:
```
$ sbt mutationTest
```
## Plugin configuration
sbt-scalamu aims to reuse as much of your sbt settings as possible.
It will automatically make use of `scalacOptions`, `testOptions`,`javaOptions` etc.
### Active mutators
You can choose from a set of available mutation operators via 
`scalamuEnabledMutators` key:
```
import org.sbt.scalamu.Mutators
scalamuEnabledMutators := Seq(
  ReplaceMathOperators, 
  NeverExecuteConditionals, 
  ChangeConditionalBoundaries,
  ReplaceWithNone
)
```
For the list of all available mutators see [available mutators](../scalac-plugin/src/main/scala/org/scalamu/plugin/ScalamuPluginConfig.scala#L9L-L26).
### Timeouts
Every now and then mutations will cause infinite loops/recursion to avoid
waiting indefinitely every test is given `regular_execution_time * timeout_factor + timeout_const` ms of run time.
`timeout_factor` and `timeout_const` are controlled by `scalamuTimeoutFactor` and `scalamuTimeoutConst` keys respectively.
Default values are 1.5 and 2000.
Example:
```
scalamuTimeoutFactor := 2,
scalamuTimeoutConst  := 5000
```
### Targets
You can choose to only mutate certain packages/classes and/or run certain tests and/or ignore trees with certain symbol names
Example: 
```
scalamuTargetOwners  := Seq("org.example.Foo".r, "org.example2.baz.*".r),
scalamuTargetTests   := Seq("org.example.fast.*".r),
scalamuIgnoreSymbols := Seq("scala.Predef.println".r)
```

### Misc
`scalamuParallelism` key controls the number of simultaneously running mutation
analysis processes (defaults to 1) and `scalamuVerbose` key is responsible for
verbose logging (defaults to `false`). `scalamuAnalyserJavaOptions` allows to 
control options passed to a mutations analyser when forking (default value is `javaOptions in Test`), 
as a rule of thumb this should have all options required to run your tests as well as a little extra heap
size if your application is memory-hungry.

Example:
```
scalamuParallelism         := 4,
scalamuVerbose             := true   
scalamuAnalyserJavaOptions := Seq("-Xmx1g", "-Dprop=foo")
```

You can specify a directory where report is generated using `target` key scoped `Scalamu`,
in case of a really big project you might want to modify `javaOptions` of the mutation engine itself.
By default `mutationTest` will aggregate all (if any) project in the `.dependsOn` clause, to disable this behaviour
set `aggregate in Scalamu` to `false`.
Example:
```
target in Scalamu      := file("./scalamu"),
javaOptions in Scalamu := Seq("-Xmx4g")
```
