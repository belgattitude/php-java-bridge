<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform' >
<xsl:template match="/">
<html>
<head>
<title>
<xsl:value-of select="/X/@t"/>
</title>
</head>
<body>
<TABLE>
<xsl:for-each select="/X/P">
<xsl:sort order="descending" select="/X/P/@v"/>
<TR><TD><xsl:value-of select="./@v"/></TD> <TD><xsl:value-of select="./*/@v"/></TD></TR>
</xsl:for-each>
</TABLE>
</body>
</html>
</xsl:template>
</xsl:stylesheet>
