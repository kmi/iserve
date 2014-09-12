<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

    <xsl:import href="result-osm-trimmed.xsl"/>

    <xsl:template match="result" mode="extension">
        <script type="text/javascript" src="{$_resourceRoot}scripts/staging.js"></script>
    </xsl:template>

</xsl:stylesheet>