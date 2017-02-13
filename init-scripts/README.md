## Gradle init-scripts

> The gradle `init-scripts` offer an alternative method to include
> runtime dependencies in your build without changing the main [build.grade](https://github.com/belgattitude/php-java-bridge/blob/master/build.gradle) file.   


### Content

The [init-scripts folder](https://github.com/belgattitude/php-java-bridge/tree/master/init-scripts) 
contains ready-made init-scripts for some popular libraries: 

| File | Library | 
|------|---------|
| [./init.mysql.gradle](https://github.com/belgattitude/php-java-bridge/blob/master/init-scripts/init.mysql.gradle) | [MySQL JDBC connector](http://dev.mysql.com) |
| [./init.jasperreports.gradle](https://github.com/belgattitude/php-java-bridge/blob/master/init-scripts/init.jasperreports.gradle) | [JasperReports](http://community.jaspersoft.com/project/jasperreports-library) to generate PDF/XLS/HTML... reports |
| [./init.itext.gradle](https://github.com/belgattitude/php-java-bridge/blob/master/init-scripts/init.itext.gradle) | [IText](http://itextpdf.com/) low level library to generate PDF |
| [./init.opennlp.gradle](https://github.com/belgattitude/php-java-bridge/blob/master/init-scripts/init.opennlp.gradle) | [OpenNLP](https://opennlp.apache.org/) natural language processing |


> Those init-scripts can be used as example... You can use them to create your own dependencies requirements.

### Usage 

Add the `-I` (--init-script) parameter followed by the init script filename to the `./gradlew` command.

As an example, you can call the `war` task to generate a build with both `jasperreports` and `mysql-connector` like:

```shell
$ ./gradlew war -I init-scripts/init.jasperreports.gradle -I init-scripts/init.mysql.gradle
```

The newly builder war file present in the `build/libs` folder will now contains your dependencies
in its `WEB-INF/lib` internal directory.




