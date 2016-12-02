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
 
## Build process   
  
Currently the project can be built with [ant](http://ant.apache.org/). Be aware
that you need a java jdk installed as well as a PHP runtime (5.3-5.6).
  
### Build the servlet 

Clone the project and run the `ant` command.

```console
$ git clone https://github.com/belgattitude/php-java-bridge.git
$ cd php-java-bridge
$ ant
```

See the resulting files in the `/dist` folder :

- Files required to run the bridge under a servlet container (tomcat...)

    - `php-servlet.jar`: Servlet for php-java communication (required). 
    - `php-script.jar`: Lib to allow java to talk with a php-cgi instance.

- Command line standalone server to run the bridge without a servlet container (development)
    
    - `JavaBridge.jar`: the standalone server used in [pjbserver-tools](https://github.com/belgattitude/pjbserver-tools)

      *See also the [pjbserver-tools](https://github.com/belgattitude/pjbserver-tools) project which
       offers command line support and api from PHP.*
      
- Obsolete files

    - `script-api.jar`: *obsolete javax.script package. Included from Java 1.6, see [doc](https://docs.oracle.com/javase/7/docs/api/index.html?javax/script/package-summary.html)*
    - `Java.inc`: *obsolete php client, replaced by [soluble-japha](https://github.com/belgattitude/soluble-japha)*

     *Note that if you intend to build the `Java.inc` client, you must be sure to install php < 7.0
     on your machine. In case you have multiple version installed, run ant with `ant -buildfile build-php5.6.xml`.*  

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

