# php-java-bridge

**Unofficial** github repository for the [PHP/Java bridge](http://php-java-bridge.sourceforge.net/pjb/).

The VM Bridge is a network protocol which can be used to
connect a native script engine, for example PHP, with a Java or ECMA 335 VM.

## Introduction

Fork of the official sourceforge [CVS PHP/Java bridge repository](https://sourceforge.net/p/php-java-bridge/code/) migrated
to git (from latest release 6.2.1).

This unofficial fork has been created to facilitate the development of

- [soluble-japha](https://github.com/belgattitude/soluble-japha)
- [pjbserver-tools](https://github.com/belgattitude/pjbserver-tools)

To get more info, see the [official readme](./README) or
the [official project page](https://sourceforge.net/p/php-java-bridge/code/).

## Motivations

- Migration from CSV to Git
- Cleanup of obsolete version (start at 6.2.1)

## Build 

Clone the project and run the `ant` command.

```console
$ git clone https://github.com/belgattitude/php-java-bridge.git
$ cd php-java-bridge
$ ant
```

See the resulting files in the `/dist` folder :

- Files required to run the bridge

    - `php-servlet.jar`:
    - `php-script.jar`:
    - `JavaBridge.jar`: the standalone server used in [pjbserver-tools](https://github.com/belgattitude/pjbserver-tools)

- Obsolete files

    - `Java.inc`: *obsolete php client, replaced by [soluble-japha](https://github.com/belgattitude/soluble-japha)*
    - `script-api.jar`: *obsolete javax.script package. Included from Java 1.6, see [doc](https://docs.oracle.com/javase/7/docs/api/index.html?javax/script/AbstractScriptEngine.html)*

## Future

- Modernize documentation
- Document recipes to compile from github
- Provide a maven repo






