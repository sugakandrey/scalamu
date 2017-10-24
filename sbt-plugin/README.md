# sbt-scalamu
sbt-scalamu is a plugin for SBT that allows for convinient integration of 
mutation testing into your build.

## Getting started
Make sure your SBT version is supported (currently sbt-scalamu is cross-built
against sbt 0.13 and 1.0).
```
sbt.version = 1.0.2
```
Add the plugin to `project/plugins.sbt`:
```
addSbtPlugin("org.scalamu" % "sbt-scalamu" % "0.1-SNAPSHOT")
```
Run the `mutationTest` command inside SBT shell:
```
$ sbt mutationTest
```
## Plugin configuration
sbt-scalamu aims to reuse as much of your sbt settings as possible.
It will automatically make use of `scalacOptions`, `testOptions`,`javaOptions` etc.
### Active mutators
You can choose from a set of available mutation operators via 
`activeMutators` key:
```
ScalamuKeys.activeMutators := Seq(
  "ReplaceMathOperators", 
  "NeverExecuteConditionals", 
  "ReplaceConditionalBoundaries",
  "ReplaceWithNone"
)
```
For the list of all available mutators see [available mutators](../plugin/src/main/scala/org/scalamu/plugin/ScalamuPluginConfig.scala#L9L-L23).
### Timeouts
Every now and then mutations will cause infinite loops/recursion to avoid
waiting indefinitely every test is given `regular_execution_time * timeout_factor + timeout_const` ms of run time.
`timeout_factor` and `timeout_const` are controlled by `timeoutFactor` and `timeoutConst` keys respectively.
Default values are 1.5 and 2000.
Example:
```
ScalamuKeys.timeoutFactor := 2,
ScalamuKeys.timeoutConst  := 5000
```
### Targets
You can choose to only mutate certain classes/run certain tests and/or ignore trees with certain symbol names
Example: 
```
ScalamuKeys.targetClasses := Seq("org.example.Foo".r, "org.example2.baz.*".r),
ScalamuKeys.targetTests   := Seq("org.example.fast.*".r),
ScalamuKeys.ignoreSymbols := Seq("scala.Predef.println".r)
```

### Misc
`parallelism` key controls the number of simultaneously running mutation
analysis processes (defaults to 1) and `verbose` key is responsible for
verbose logging (defaults to `false`). `analyserJavaOptions` allows to 
control options passed to a mutations analyser when forking (default value is `javaOptions in Test`), 
as a rule of thumb this should have all options required to run your tests as well as a little extra heap
size if your application is memory-hungry.

Example:
```
ScalamuKeys.parallelism         := 4,
ScalamuKeys.verbose             := true   
ScalamuKeys.analyserJavaOptions := Seq("-Xmx1g", "-Dprop=foo")
```

You can specify a directory where report is generated using `target` key scoped `Scalamu`,
in case of a really big project you might want to modify `javaOptions` of the mutation engine itself.
Example:
```
target in Scalamu      := file(".") 
javaOptions in Scalamu := Seq("-Xmx4g")
```
