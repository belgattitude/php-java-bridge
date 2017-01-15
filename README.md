# php-java-bridge

**Unofficial** github maintained repository for the [PHP/Java bridge](http://php-java-bridge.sourceforge.net/pjb/).

The VM Bridge is a network protocol which can be used to
connect a native script engine, for example PHP, with a Java or ECMA 335 VM.

## Introduction

Fork of the official sourceforge [CVS PHP/Java bridge repository](https://sourceforge.net/p/php-java-bridge/code/) migrated
to git (from latest release 6.2.1).

## Motivations

This unofficial fork has been created to facilitate the development of [soluble-japha](https://github.com/belgattitude/soluble-japha)
and [pjbserver-tools](https://github.com/belgattitude/pjbserver-tools) projects.

## Documentation

- Some recipes available in this README.
- Documentation can be found in the [PHP/Java bridge]((http://php-java-bridge.sourceforge.net/pjb/)) site  
- See the [generated API doc](http://docs.soluble.io/php-java-bridge/api).


## Status

- php-java-bridge migrated from sourceforge CSV repo to Github (from v6.2.1)
 
## Build
  
### Requirements
  
Currently the project can be built with [ant](http://ant.apache.org/). Be aware
that you need a java jdk installed as well as a PHP runtime (5.3-5.6). Building
under PHP7+ does not work, see [see #4](https://github.com/belgattitude/php-java-bridge/issues/4) for a solution.  

  
### Building the project

Clone the project and run the `ant` command.

```console
$ git clone https://github.com/belgattitude/php-java-bridge.git
$ cd php-java-bridge
$ ant -Dphp_exec=/usr/bin/php
```

See the resulting files in the `/dist` folder :

- The bridge files.

    - `php-servlet.jar`: Servlet for php-java communication (required). Approx 58k. 
    - `php-script.jar`: Lib to allow java to talk with a php-cgi instance. Approx 58k.
        
- For convenience, you'll find ready to run war bundles, you can choose between :
  
    - `JavaBridgeTemplate.war`: Minimal war file (only php-servlet.jar, php-script.jar, web.xml and support for system php-cgi). Approx 500k.   
    - `JavaBridge.war`: Example war file with some lib and examples. Approx **47Mb** !!!        
            

- Command line standalone server to run the bridge without a servlet container (development)
    
    - `JavaBridge.jar`: the standalone server used in [pjbserver-tools](https://github.com/belgattitude/pjbserver-tools)

      *See also the [pjbserver-tools](https://github.com/belgattitude/pjbserver-tools) project which
       offers command line support and api from PHP.*
        
- Obsolete files

    - `script-api.jar`: *obsolete javax.script package. Included from Java 1.6, see [doc](https://docs.oracle.com/javase/7/docs/api/index.html?javax/script/package-summary.html)*
    - `Java.inc`: *obsolete php client, replaced by [soluble-japha](https://github.com/belgattitude/soluble-japha)*
       (for building under PHP7, [see #4](https://github.com/belgattitude/php-java-bridge/issues/4))
     
       

### How to deploy on Tomcat (Ubuntu)

Ensure you have tomcat installed and a php-cgi 

```console
$ sudo apt-get install tomcat7 tomcat7-admin
$ sudo apt-get install php-cgi
```

And copy the ready to run `JavaBridgeTemplate.war` (or `JavaBridge.war` or bundle yours) in the tomcat webapps folder:

```console
cp dist/JavaBridgeTemplate.war /var/lib/tomcat7/webapps
```

Wait few seconds for deployment and point your browser to [http://localhost:8080/JavaBridge](http://localhost:8080/JavaBridgeTemplate).

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

Documentation will be generated in the '/doc/API' folder.

## Note on branches
 
The master contains the latest code, while the original pjb621 fork (with few minor reformating) is kept
in its own branch names 'Original-6.2.1'.  

 
## Future

- Modernize documentation
- Document recipes to compile from github
- Provide a maven repo

