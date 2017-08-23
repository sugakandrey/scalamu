# Scalamu 

Scalamu is a mutation testing engine for Scala.

## Getting started
### CLI quick start
Although the preferred method is to use SBT/IntelliJ plugin scalamu can be run
from the command line too. Simply download `org.scalamu.entry-point` jar
and launch it from the command line as follows:
```
scala -cp %path to scalamu jar% \
    org.scalamu.entry.EntryPoint \
    %report directory% \
    %directories containing source files% \
    %directories containing test classes% \
    %optional configuration parameters%
```
For an in-depth explanation on configuration parameters see usage info below.
```
scalamu
Usage: scalamu-cli [options] <reportDir> <sourceDirs> <testClassDirs>

  <reportDir>              directory to create reports in
  <sourceDirs>             comma-separated list of source directories
  <testClassDirs>          comma-separated list of test class directories
  --cp <value>             comma-separated list of "compile" classpath elements
  --tcp <value>            comma-separated list of "test" classpath elements
  --jvmOpts <value>        jvm args used by tests
  --mutations <value>      set of mutation operators
  --includeSource <regex1>,<regex2>..
                           list of filters for ignored source files
  --includeTestClasses <regex1>,<regex2>..
                           list of filters for ignored test classes
  --testOptions framework1=optionString1, framework2=optionString2...
                           per framework test runner options
  --scalacOptions <value>  options to be passed to scalac
  --timeoutFactor <value>  factor to apply to normal test duration before considering being stuck in a loop
  --timeoutConst <value>   flat amount of additional time for mutation analysis test runs
  --parallelism <value>    number of runners used to perform mutation analysis
  --verbose                be verbose about every step
  --recompileOnly          do not perform mutation analysis (internal testing option)
```
### SBT quick start
See [sbt-scalamu](./sbt-plugin)

### IntelliJ quick start
IntelliJ integration is currently in the works.

## Credits
Scalamu was hugely inspired by [PIT](http://pitest.org) an amazing 
mutation testing system for JVM.
