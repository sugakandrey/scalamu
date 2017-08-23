# sbt-scalamu
sbt-scalamu is a plugin for SBT that allows for convinient integration of 
mutation testing into your build.

## Getting started
Make sure your SBT version is supported (currently sbt-scalamu is cross-built
against sbt 0.13.16 and 1.0.0).
```
sbt.version = 1.0.0
```
Add the plugin to `project/plugins.sbt`:
```
addSbtPlugin("org.scalamu" % "sbt-scalamu" % "0.1-SNAPSHOT")
```
Run the `mutationTest` command inside SBT shell:
```
$ sbt mutationTest
```
By default the report will be generated inside `target` directory. 
*NOTE:* in a case of a multi project build definition all the subprojects
will be aggregated and only one report will be generated.

## Plugin configuration
sbt-scalamu aims to reuse as much of your sbt settings as possible.
It will automatically make use of `scalacOptions`, `testOptions`,`javaOptions` etc.
### Active mutation operators
You can choose from a set of available mutation operators via 
`activeMutators` key:
```
activeMutators := Seq(
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
timeoutFactor := 2,
timeoutConst  := 5000
```
### Class name constraints
You can choose to only mutate certain classes/run certain tests by 
providing a `Seq[Regex]` for class
Example: 
```
includeSources := Seq("org.example.Foo".r, "org.example2.baz.*".r),
includeTests   := Seq("org.example.fast.*".r)
```

### Misc
`parallelism` key controls the number of simultaneously running mutation
analysis processes (defaults to 1) and `verbose` key is responsible for
verbose logging (defaults to `false`). `analyserJavaOptions` allows to 
control options passed to a mutations analyser when forking (default value is `javaOptions in Test`).

Example:
```
parallelism        := 4,
verbose            := true   
analyserJVMOptions := Seq("-Xmx1g", "-Dprop=foo")
```
