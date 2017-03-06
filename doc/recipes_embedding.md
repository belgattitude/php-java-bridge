# Embed recipes

> This document is currently a work-in-progess stub. To learn more
> see the [#51](https://github.com/belgattitude/php-java-bridge/issues/51)
> and feel free to contribute.

## Introduction

The php-java-bridge provides a standalone server (`JavaBridgeRunner`) for 
embedding the bridge. In the soluble fork, this internal server have been
deprecated in favour of embedded Tomcat, Jetty... adopted more widely. 


## Embedded containers

  
### Tomcat


1. Add the embedded tomcat in your dependencies:

   With maven: 

   ```xml
   	<dependency>
   			<groupId>org.apache.tomcat.embed</groupId>
   			<artifactId>tomcat-embed-core</artifactId>
   			<version>7.0.75</version>
   		</dependency>		
   		<dependency>
   			<groupId>org.apache.tomcat</groupId>
   			<artifactId>tomcat-juli</artifactId>
   			<version>7.0.75</version>
   	</dependency>
   ```
   
   With gradle
   
   ```gradle
   // todo
   ```

2. Register servlet and start tomcat.

   ```java
   public void startTomcat() {
   	    Tomcat tomcat = new Tomcat();
           tomcat.setPort(8087);
           Context ctx = tomcat.addContext("/", new File(".").getAbsolutePath());
           Tomcat.addServlet(ctx, "jbs", new JavaBridgeServlet());
           ctx.addServletMapping("/*", "jbs");
           try {
   			tomcat.start();
   			log("started, now waiting for connections...");
   			tomcat.getServer().await();
   		} catch (Exception e) {
   			warning(e.getMessage());
   		}
   }   
   ```

### Jetty

> The doPut() method Seems buggy right now. See [#43](https://github.com/belgattitude/php-java-bridge/issues/43) 

### Undertow

(todo)

