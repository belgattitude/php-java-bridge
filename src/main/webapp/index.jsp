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
                    <h2 class="Header-subTitle">PHPJavaBridge server running.</h2>
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
                                    The PHPJavabridge is successfully running and listening for connections.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
                <div>

                    <table>
                        <thead>
                            <tr>
                                <th>Java system property</th>
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
                                <td><%= sp.getProperty(name) %></td>
                            </tr>
                            <%
                                }
                            %>
                        </tbody>
                    </table>


                </div>
            </div>
        </main>

    </body>
</html>
