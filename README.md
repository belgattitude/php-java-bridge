# php-java-bridge

**Unofficial** repository for the [PHP/Java bridge](http://php-java-bridge.sourceforge.net/pjb/).

The VM Bridge is a network protocol which can be used to connect a native 
script engine, for example PHP, with a Java or ECMA 335 VM.

## Disclaimer

This fork have been made on the *no-longer maintained* sourceforge [CVS PHP/Java bridge repository](https://sourceforge.net/p/php-java-bridge/code/) and
is based on the latest official release (6.2.1) excluding all history versions (cleanup from the CVS repo).


See the [CHANGELOG.md](https://github.com/belgattitude/php-java-bridge/blob/master/CHANGELOG.md) or have a look to the [CHANGESET](https://github.com/belgattitude/php-java-bridge/compare/Original-6.2.1...master).

A copy of the original 6.2.1 release is available on a [separate branch](https://github.com/belgattitude/php-java-bridge/tree/Original-6.2.1). All new changes are currently made on the master branch, releases starting at 6.2.10.

    
Primarily this fork has been created to facilitate the development of [soluble-japha](https://github.com/belgattitude/soluble-japha)
and [pjbserver-tools](https://github.com/belgattitude/pjbserver-tools) projects which solves the PHP7+ compatibility. 
 
## Documentation

- Still a WIP, check this README.md and the various doc files   
- [API doc](http://docs.soluble.io/php-java-bridge/api).
- Older documentation can be found in the [PHP/Java bridge](http://php-java-bridge.sourceforge.net/pjb/) site

## Releases

- You can download pre-compiled [java bridge binaries](https://github.com/belgattitude/php-java-bridge/releases) on the releases page (jdk8). 
- Alternatively you can build the project, first clone the project and follow the build steps.
- [WIP] [Gradle starter project](https://github.com/belgattitude/pjb-starter-gradle) on it's way !!! 
 
## Clone the project

Run the `git clone` command clone in a directory:

```console
$ git clone https://github.com/belgattitude/php-java-bridge.git
$ cd php-java-bridge
```

## Build

Building the project requires a php interpreter (5.3 - 5.6) installed, see [#5](https://github.com/belgattitude/php-java-bridge/issues/5).
You can specify its location through the `-Dphp_exec=/usr/bin/php5.6`.

See the `build.xml` for registered tasks. 

### Option 1: with ant
  
```console
$ ant -Dphp_exec=/usr/bin/php5.6
```

**Warning** due to gradle support, the ant `clean` has been renamed in `cleanBuild`. See [5](https://github.com/belgattitude/php-java-bridge/issues/5). 

### Option 2: with gradle

```
$ gradle all -Dphp_exec=php5.6
```

## Compiled files

See the `/dist` folder :

- The bridge files.

    - `JavaBridge.jar`: Main library, providing also a standalone server
    - `php-servlet.jar`: Servlet for php-java communication (required). Approx 58k. 
    - `php-script.jar`: Lib to allow java to talk with a php-cgi instance. Approx 58k.
       
- For convenience, you'll find ready to run war bundles, you can choose between :
  
    - `JavaBridgeTemplate.war`: Minimal war file (only php-servlet.jar, php-script.jar, web.xml and support for system php-cgi). Approx 500k.   
    - `JavaBridge.war`: Example war file with some lib and examples. Approx **47Mb** !!!        
                    
- Obsolete files kept for compatibility
    - `script-api.jar`: *obsolete javax.script package. Included from Java 1.6, see [doc](https://docs.oracle.com/javase/7/docs/api/index.html?javax/script/package-summary.html)*
    - `Java.inc`: *obsolete php client, replaced by [soluble-japha](https://github.com/belgattitude/soluble-japha)*
       (for building under PHP7, [see #4](https://github.com/belgattitude/php-java-bridge/issues/4))
     
              
## Deploy

Tested containers are :

- Tomcat 7 
- Tomcat 8

### Tomcat (Ubuntu)

Ensure you have tomcat installed and a php-cgi 

```console
$ sudo apt-get install tomcat8 tomcat8-admin
$ sudo apt-get install php-cgi
```

And copy the ready to run `JavaBridgeTemplate.war` (or `JavaBridge.war` or bundle yours) in the tomcat webapps folder:

```console
cp dist/JavaBridgeTemplate.war /var/lib/tomcat8/webapps
```

Wait few seconds for deployment and point your browser to [http://localhost:8080/JavaBridgeTemplate](http://localhost:8080/JavaBridgeTemplate).

Errors are logged by default into

```console
$ cat /var/log/tomcat7/catalina.out
```

### Tomcat tuning tips

If you get OutOfMemory errors, you can increase the java heap tomcat:

```console
$ vi /etc/default/tomcat7
```

Look for the Xmx default at 128m and increase 

```
JAVA_OPTS="-Djava.awt.headless=true -Xmx512m -XX:+UseConcMarkSweepGC"
```

and restart

```console
sudo service tomcat7 restart
```

### How to build documentation

You can build the doc with

```console
$ ant JavaDoc
```

Documentation will be generated in the `/doc/API` folder.
 
## Future

- Modernize documentation
- Complete removal of `Java.inc` and remove deps to php in the build process.
- Setup a maven repository or viable alternatives
- More work on the [pjb-starter-grade](https://github.com/belgattitude/pjb-starter-gradle) project.


## Credits


Original developers:

- Andre Felipe Machado, 
- Sam Ruby, 
- Kai Londenberg, 
- Nandika Jayawardana, 
- Sanka Samaranayake, 
- Jost Boekemeier