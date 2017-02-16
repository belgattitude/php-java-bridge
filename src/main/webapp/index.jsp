<%@ page import="java.util.*" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>PhpJavaBridge</title>
    </head>
    <body>
        <h1>PHPJavaBridge server running</h1>
        <h2>Java System Properties</h2>
        <table>
            <tbody>
                <tr>
                    <td><strong>Property name</strong></td>
                    <td><strong>Property value</strong></td>
                </tr>
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
    </body>
</html>
