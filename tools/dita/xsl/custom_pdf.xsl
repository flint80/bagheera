<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    version="1.1">

  <xsl:template match="*[contains(@class,' topic/keyword ')]">

    <xsl:choose>
      <xsl:when test="@otherprops='red'">
        <fo:inline xsl:use-attribute-sets="keyword_red" id="{@id}">
          <xsl:apply-templates/>
        </fo:inline>
      </xsl:when>
      <xsl:otherwise>
        <fo:inline xsl:use-attribute-sets="keyword_blue" id="{@id}">
          <xsl:apply-templates/>
        </fo:inline>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/shortdesc ')]">
    <!--do nothing-->
  </xsl:template>
  
</xsl:stylesheet>