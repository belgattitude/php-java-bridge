# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

### Added

- Build: Support for Gradle (still relying on ant build.xml), see [#5](https://github.com/belgattitude/php-java-bridge/issues/5) and [#9](https://github.com/belgattitude/php-java-bridge/pull/9)
- Build: New tasks added in gradle 'jacocoTestReport', see P/R [#9](https://github.com/belgattitude/php-java-bridge/pull/9)
- Test: Resurrect existing unit tests
- Test: test and coverage using Jacoco added, see P/R [#9](https://github.com/belgattitude/php-java-bridge/pull/9)
- Doc: trick to build when default interpreter is php 7+, see [#4](https://github.com/belgattitude/php-java-bridge/issues/4)
- License: MIT license where applicable, see [#10](https://github.com/belgattitude/php-java-bridge/issues/10) 

### Removed

- Removed obsolete javax.script sources
- Removed unsupported directory [#11](https://github.com/belgattitude/php-java-bridge/issues/11)
- Removed PHPDebugger [#16](https://github.com/belgattitude/php-java-bridge/issues/16)

### Changed

- Moved namespace `php.java` into `io.soluble.pjb`, see P/R [#9](https://github.com/belgattitude/php-java-bridge/pull/9)
- Project directory structure _mavenized_, sources moved from './server' to './src/main/java', see P/R [#9](https://github.com/belgattitude/php-java-bridge/pull/9)
  

## 6.2.10 (2017-01-15)
   
### Changed

- Removal embedded php-cgi binaries [see #3](https://github.com/belgattitude/php-java-bridge/issues/3)
- Source code indentation
- README.md: added Recipe for building and deploying on tomcat

### Fixed
 
- Added a `build-php5.6.xml` ant config 


## 6.2.1 (2010-06-01)

For reference see the doc/legacy/Changelog