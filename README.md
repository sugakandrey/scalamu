[![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/io/github/sugakandrey/scalamu_2.12/maven-metadata.xml.svg?style=flat-square)](http://repo1.maven.org/maven2/io/github/sugakandrey/scalamu_2.12/)

# Scalamu 

Scalamu is a mutation testing engine for Scala.

## Getting started
### CLI quick start
Although the preferred method is to use SBT/IntelliJ plugin scalamu can be run
from the command line too. Simply download `org.scalamu.entry-point` jar
and launch it from the command line as follows:
```
java -jar %path to scalamu jar% \
    %optional configuration parameters%
    %report directory% \
    %directories containing source files% \
    %directories containing test classes% \
```
For an in-depth explanation on configuration parameters see usage info below.
```
scalamu
Usage: scalamu-cli [options] <reportDir> <sourceDirs> <testClassDirs>

  <reportDir>              directory to create reports in
  <sourceDirs>             list of source directories
  <testClassDirs>          list of test class directories
  --cp <value>             list of "compile" classpath elements
  --tcp <value>            list of "test" classpath elements
  --jvmOpts <value>        arguments for forked JVM running tests
  --mutations <value>      set of active mutators
  --includeSource <regex1>,<regex2>..
                           only mutate certain source files
  --includeTestClasses <regex1>,<regex2>..
                           only run certain test classes
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
