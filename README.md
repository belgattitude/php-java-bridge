[![Build Status](https://travis-ci.org/belgattitude/php-java-bridge.svg?branch=master)](https://travis-ci.org/belgattitude/php-java-bridge)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.soluble.pjb/php-java-bridge/badge.svg?style=plastic])](https://maven-badges.herokuapp.com/maven-central/io.soluble.pjb/php-java-bridge)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/belgattitude/php-java-bridge/blob/master/LICENSE.md)
(develop branch: 
[![Build Status](https://travis-ci.org/belgattitude/php-java-bridge.svg?branch=develop)](https://travis-ci.org/belgattitude/php-java-bridge))



# Unofficial fork of the [PHP/Java bridge](http://php-java-bridge.sourceforge.net/pjb/) server.

The VM Bridge is a network protocol which can be used to connect a native 
script engine, for example PHP, with a Java VM. To get more information have a 
look to those projects:

## Disclaimer

> This fork have been made on the *no-longer maintained* sourceforge [CVS PHP/Java bridge repository](https://sourceforge.net/p/php-java-bridge/code/) and
> is based on the latest official release (6.2.1) excluding all previous versions and history (cleanup from the CVS repo).
> See the [CHANGELOG.md](https://github.com/belgattitude/php-java-bridge/blob/master/CHANGELOG.md) or have a look to the [CHANGESET](https://github.com/belgattitude/php-java-bridge/compare/Original-6.2.1...master).
> A copy of the original 6.2.1 release is available on a [separate branch](https://github.com/belgattitude/php-java-bridge/tree/Original-6.2.1). All new changes are currently made on the master branch, releases starting at 6.2.10.

## Status

Latest version 6.2.1 has been released long ago but, AFAIK, proved stable and mature. Here are some plans and statuses of the fork:  

- [x] Migration from sourceforge CVS to github.
- [x] Support for PHP7 and rewrite of the client `Java.inc`, see [soluble-japha](https://github.com/belgattitude/soluble-japha)
- [x] Gradle support for project builds.
- [x] Ensure support of Tomcat 8+, JDK 8.
- [x] Prepare a starter project to customize builds, see [pjb-starter-gradle](https://github.com/belgattitude/pjb-starter-gradle).
- [x] Update namespace to `io.soluble.pjb` and host [Java API doc](http://docs.soluble.io/php-java-bridge/api)
- [x] License to Apache 2.0 and drop GPL code, see [#10](https://github.com/belgattitude/php-java-bridge/issues/10)
- [x] Artifact published and available on maven central. 
- [x] Clean-up of obsolete code and unused resources.
- [x] Port and convert most of `./test.php5` in [soluble-japha](https://github.com/belgattitude/soluble-japha).
- [ ] Deprecate and remove completely the `Java.inc` client.
- [ ] Security review and safe practices.
- [ ] Documentation (always a wip)

Please **[participate in the discussion for future ideas here](https://github.com/belgattitude/php-java-bridge/issues/6)**. 

## Documentation

Server API doc
   
- [API doc](http://docs.soluble.io/php-java-bridge/api).

For the PHP client part (*replaces Java.inc*), documentation is located on another project:

- [soluble-japha](https://github.com/belgattitude/soluble-japha) PHP client to interact with the bridge.


> Older documentation can be found in the [PHP/Java bridge](http://php-java-bridge.sourceforge.net/pjb/) site


## Releases

- You can download pre-compiled [java bridge binaries](https://github.com/belgattitude/php-java-bridge/releases) on the releases page (jdk8). 
- Alternatively you can build the project, first clone the project and follow the build steps.

> Note: Evaluate the [pjb-starter-gradle](https://github.com/belgattitude/pjb-starter-gradle) if you like to
> customize your php-java-bridge server build.  

## Installation

Major releases are published on [Maven central](https://search.maven.org/#search%7Cga%7C1%7Cio.soluble.pjb.php-java-bridge).

With maven:

```
<dependency>
    <groupId>io.soluble.pjb</groupId>
    <artifactId>php-java-bridge</artifactId>
    <version>VERSION</version>
</dependency>
```

or gradle

```
compile 'io.soluble.pjb:php-java-bridge:VERSION'
```

## Build the project

### Requirements

 - Oracle JDK 7,8
 - Optionally gradle (gradlew provided) and ant for old Java.inc generation
 
### Get the sources

You can either clone the project with:

```shell
$ git clone https://github.com/belgattitude/php-java-bridge.git
```

or download a sip tarball from the github page.

### Gradle build 

Build the project with the provided gradle wrapper:

```shell
$ cd php-java-bridge
$ ./gradlew build 
```

The generated files are available in the  `/build/libs` folder:

| File          | Description   | 
| ------------- | ------------- | 
| `php-java-bridge-<VERSION>.jar`  | JavaBridge library (servlet and standalone). | 
| `php-java-bridge-<VERSION>-sources.jar`  | Source code. | 
| `php-java-bridge-<VERSION>-javadoc.jar`  | Generated api doc. |

Additionally a generic template file is automatically generated: 

| File          | Description   | 
| -------------| ------------- | 
| `JavaBridgeTemplate.war`  | A ready to deploy war example file. |
 
                                                                                                                 
## Usage

> Currently only tested on Tomcat 7/8, should be running on any servlet 2.5 compatible container.

### Servlet registration

You can have a look to the [web.xml](https://github.com/belgattitude/php-java-bridge/blob/master/src/main/webapp/WEB-INF/web.xml) default configuration
for the servlet configuration settings. 

### Deploy

Ensure you have tomcat installed and a php-cgi

```shell
$ sudo apt-get install tomcat8 tomcat8-admin
$ sudo apt-get install php-cgi
```

And copy the ready to run `JavaBridgeTemplate.war` in the tomcat webapps folder:

```shell
$ sudo cp ./build/libs/JavaBridgeTemplate.war /var/lib/tomcat8/webapps/JavaBridgeTemplate.war
```

Wait few seconds for deployment and point your browser to [http://localhost:8080/JavaBridgeTemplate](http://localhost:8080/JavaBridgeTemplate).

Have a look to the error log if needed:

```shell
$ cat /var/log/tomcat8/catalina.out
```

## Develop

For development, the use of the `./gradlew tomcatRun` and `./gradlew tomcatStop` allows testing 
without the need of deployment.

Dependencies can be added in the `build.gradle` file.   

## FAQ

### OutOfMemory errors under Tomcat

If you get OutOfMemory errors, you can increase the java heap tomcat:

```shell
$ sudo vi /etc/default/tomcat8
```

Look for the Xmx default at 128m and increase 

```
JAVA_OPTS="-Djava.awt.headless=true -Xmx512m -XX:+UseConcMarkSweepGC"
```

and restart

```shell
$ sudo service tomcat8 restart
```
 
## Contribute

Feel free to fork and submit pull requests :)

## Credits

Original developers:

- Jost Boekemeier
- Andre Felipe Machado, 
- Sam Ruby, 
- Kai Londenberg, 
- Nandika Jayawardana, 
- Sanka Samaranayake, 

Forked version

- [Christian P. Lerch](https://github.com/cplerch): Java refactorings and modernizations. 
- [Sébastien Vanvelthem](https://github.com/belgattitude): Fork initiator.


See the [CREDITS.md](./CREDITS.md) for an up to date of list of contributors.