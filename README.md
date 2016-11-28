# php-java-bridge

**Unofficial** github repository for the [PHP/Java bridge](http://php-java-bridge.sourceforge.net/pjb/).

The VM Bridge is a network protocol which can be used to
connect a native script engine, for example PHP, with a Java or ECMA 335 VM.

## Introduction

Fork of the official sourceforge [CVS PHP/Java bridge repository](https://sourceforge.net/p/php-java-bridge/code/) migrated
to git (from latest release 6.2.1).

## Motivations

This unofficial fork has been created to facilitate the development of [soluble-japha](https://github.com/belgattitude/soluble-japha)
and [pjbserver-tools](https://github.com/belgattitude/pjbserver-tools) projects.

## Status

- php-java-bridge migrated from sourceforge CSV repo to Github (from v6.2.1)
  
## How to build 

Clone the project and run the `ant` command.

```console
$ git clone https://github.com/belgattitude/php-java-bridge.git
$ cd php-java-bridge
$ ant
```

See the resulting files in the `/dist` folder :

- Files required to run the bridge

    - `php-servlet.jar`: @todo
    - `php-script.jar`: @todo
    - `JavaBridge.jar`: the standalone server used in [pjbserver-tools](https://github.com/belgattitude/pjbserver-tools)

- Obsolete files

    - `Java.inc`: *obsolete php client, replaced by [soluble-japha](https://github.com/belgattitude/soluble-japha)*
    - `script-api.jar`: *obsolete javax.script package. Included from Java 1.6, see [doc](https://docs.oracle.com/javase/7/docs/api/index.html?javax/script/package-summary.html)*

Note that if you intend to build the `Java.inc` client, you must be sure to install php < 7.0
on your machine. In case you have multiple version installed, run ant with `ant -buildfile build-php5.6.xml`.  

## Api documentation

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

