# Publishing

## Signing

Not going into details, but keep it mind that signing requires you to generate a gpg key...

As an example:

```shell
$ gpg2 --gen-key    # follow instructions
$ gpg2 --list-key   # to list your keys, copy paste
# Distribute your public key, i.e. 'C6EED57A'
$ gpg2 --keyserver hkp://pool.sks-keyservers.net --send-keys <the copy pasted key>
```

If you're using gpg2.1+, also generate the secring.gpg for compatibility:

```shell
$ gpg2 --export-secret-keys -o ~/.gnupg/secring.gpg
```

Add references to the key in your `~/.gradle/gradle.properties` file. 

```
signing.keyId=1A1A1A1A
signing.password=*******
signing.secretKeyRingFile=/home/user/.gnupg/secring.gpg
```

> **Do not put those info in your repo, should be kept private !!! home directory shoudl be good.**

Gradle should be ready to sign.

## Publish snapshot to mavenLocal

```shell
$ gradle clean build publishToMavenLocal
```



## Publish snapshot to maven

Be sure you have set your credentials in the `~/.gradle/gradle.properties` file.

```
ossrhUsername=<username>
ossrhPassword=<password>
```
 

```shell
$ gradle clean build publish
```

Then release the deployment manually http://central.sonatype.org/pages/releasing-the-deployment.html

