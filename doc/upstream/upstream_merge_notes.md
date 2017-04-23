# Upstream notes

## CVS

### Checkout release

```shell
cvs -z3 -d:pserver:anonymous@php-java-bridge.cvs.sourceforge.net:/cvsroot/php-java-bridge co -P php-java-bridge
```

### Export specific branch

```shell
cvs -z3 -d:pserver:anonymous@php-java-bridge.cvs.sourceforge.net:/cvsroot/php-java-bridge export -d pjb7.0.1 -r Release_7-0-1 php-java-bridge
```

or head

```shell
cvs -z3 -d:pserver:anonymous@php-java-bridge.cvs.sourceforge.net:/cvsroot/php-java-bridge export -d pjb-head -r HEAD php-java-bridge
```

### List releases 

This command list all branches on the original sourceforge repo

```shell
cvs -Q -d :pserver:anonymous@php-java-bridge.cvs.sourceforge.net:/cvsroot/php-java-bridge rlog -h php-java-bridge | awk -F"[.:]" '/^\t/&&$(NF-1)==0{print $1}' | sort -u
```

You can optionally list all tags:

```shell
cvs -Q -d :pserver:anonymous@php-java-bridge.cvs.sourceforge.net:/cvsroot/php-java-bridge rlog -h php-java-bridge | awk -F"[.:]" '/^\t/&&$(NF-1)!=0{print $1}' | sort -u
```

> Prefer using branches instead of tags for the sourceforge release. Not every release was tagged.

### Compare changes
 
You can compare changes made on releases, first checkout head and run: 

For changed files: 

    ```shell
    cvs diff -N -c -r Release_7-0-1 -r HEAD > pjb701-head.diff    
    ``` 

For rdiff summary:

    ```shell
    cvs rdiff -s -r Release_7-0-1 -r HEAD php-java-bridge > rdiff-701-head-summary.txt
    ``` 

## Example

### 7.0.1 to HEAD

```shell
# Save head in directory ./pjb-head
cvs -z3 -d:pserver:anonymous@php-java-bridge.cvs.sourceforge.net:/cvsroot/php-java-bridge export -d pjb-head -r HEAD php-java-bridge

```