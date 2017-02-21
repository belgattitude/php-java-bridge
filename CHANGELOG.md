# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).

## 6.2.12 (2017-02-21)

### Added

- Landing page additions: listing of registered libraries and improved debug variables

## 6.2.11 (2017-02-17)

### Added

- Landing page added, see [#33](https://github.com/belgattitude/php-java-bridge/issues/33)

### Changed

- `pom` downgraded provided compile `javax.servlet:javax.servlet-api:2.5`, 3.0.1 works but 2.5 kept for compatibility  

### Fixed

- `pom` Log4j 1.2.17 re-introduced as runtime dependency 


## 6.2.11-rc-2 (2017-02-11)

### Changed

- License from MIT to Apache 2.0, see [#10](https://github.com/belgattitude/php-java-bridge/issues/10)

### Removed

- Removed PHPDebugger.inc, PHPDebugger.java, see [#23](https://github.com/belgattitude/php-java-bridge/issues/23) and [#16](https://github.com/belgattitude/php-java-bridge/issues/16)
- `maven publish` - removed dependency of log4j in pom.xml.
 

### Fixed

- `maven publish` - Fixed dependency scope of servlet-api to 'provided', see [#24](https://github.com/belgattitude/php-java-bridge/issues/24)
- `maven publish` - Duplicate inclusion of servlet-api in pom dependencies (no side effect - cleanup doubles in gradle)


## 6.2.11-rc-1 (2017-02-06)

### Added

- Artifacts available on Maven central under io.soluble.pjb group. [#19](https://github.com/belgattitude/php-java-bridge/issues/19)
- Build: Gradle support (still relying on ant build.xml for generating old php client - genAll and cleanGen tasks), see [#5](https://github.com/belgattitude/php-java-bridge/issues/5) and [#9](https://github.com/belgattitude/php-java-bridge/pull/9)
- Build: Jacoco coverage with 'jacocoTestReport' task, see P/R [#9](https://github.com/belgattitude/php-java-bridge/pull/9)
- Test: Resurrect existing server unit tests
- Doc: trick to build when default interpreter is php 7+, see [#4](https://github.com/belgattitude/php-java-bridge/issues/4)
- License: MIT license where applicable, see [#10](https://github.com/belgattitude/php-java-bridge/issues/10) 

### Removed

- Removed obsolete javax.script sources. 
- Removed unsupported directory [#11](https://github.com/belgattitude/php-java-bridge/issues/11)
- Removed PHPDebugger [#16](https://github.com/belgattitude/php-java-bridge/issues/16) due to license issues.

### Changed

- **[possible BC-break]** Moved namespace `php.java` into `io.soluble.pjb`, see P/R [#9](https://github.com/belgattitude/php-java-bridge/pull/9)
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