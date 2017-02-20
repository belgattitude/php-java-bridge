<%@ page import="java.util.*" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>PhpJavaBridge</title>

        <link rel="stylesheet" type="text/css" href="style.css">

    </head>
    <body>

        <header class="Site-header">
            <div class="Header Header--cozy" role="banner">
                <div class="Header-titles">
                    <h1 class="Header-title"><a href="#">Javabridge</a></h1>
                    <h2 class="Header-subTitle">PHPJavaBridge server.</h2>
                </div>
                <div class="Header-actions">
                    <a class="Header-button Button Button--action Button--wide"
                       href="https://github.com/belgattitude/php-java-bridge" target="_blank">&nbsp; View Project Source </a>
                </div>
            </div>
        </header>


        <main class="Site-content">
            <div class="Container">
                <div class="Demo Demo--spaced u-ieMinHeightBugFix">
                    <div class="Aligner">
                        <div class="Aligner-item Aligner-item--fixed">
                            <div class="Landing">
                                <h1>Server running</h1>
                                <p>
                                    The PHPJavabridge server is successfully running and listening for connections.
                                </p>
                            </div>
                        </div>

                    </div>


                </div>
                <div>

                    <div class="System-info">
                        <h3>JVM info</h3>

                        <%
                            Properties props = System.getProperties();
                        %>


                        <p><%=    props.getProperty("os.name") + " "
                                + props.getProperty("os.version") + " "
                                + props.getProperty("java.vm.name") + " "
                                + props.getProperty("java.version") + " "
                                + props.getProperty("java.vm.vendor") + " "
                        %> (user.timezone: <%= props.getProperty("user.timezone") %>)</p>


                    </div>
                    <div class="System-info">
                        <h3>Registered libraries</h3>
                        <%
                            final String libsPath = "/WEB-INF/lib/";
                            final Set<String> libs = application.getResourcePaths(libsPath);
                            if (libs != null) {
                        %>
                        <p>
                            <%
                                for (String lib: libs) {
                            %>

                                <%= lib.substring(libsPath.length()) %>,&nbsp;

                            <%
                                }
                            %>
                        </p>
                        <%
                            } else {
                        %>
                        <p><em>No registered libraries found in the '/WEB-INF/lib' war folder. <small>Note that listing of libraries is not available when using gradle tomcatRun.</small></em></p>
                        <%
                            }
                        %>

                    </div>
                    <div class="System-info">
                        <h3>Java system properties</h3>
                        <table>
                            <thead>
                                <tr>
                                    <th>Property name</th>
                                    <th>Property value</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    Properties sp = System.getProperties();
                                    for (String name : sp.stringPropertyNames()) {
                                %>
                                <tr>
                                    <td><%= name %></td>
                                    <td><%= sp.getProperty(name).replace(",", ", ") %></td>
                                </tr>
                                <%
                                    }
                                %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </main>

    </body>
</html>
