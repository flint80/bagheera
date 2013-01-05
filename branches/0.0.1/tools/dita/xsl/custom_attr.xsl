<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    version="1.0">

  <xsl:attribute-set name="topic.title">
    <xsl:attribute name="font-family">Sans</xsl:attribute>
    <xsl:attribute name="border-bottom">3pt solid black</xsl:attribute>
    <xsl:attribute name="margin-top">0pc</xsl:attribute>
    <xsl:attribute name="margin-bottom">1.4pc</xsl:attribute>
    <xsl:attribute name="font-size">18pt</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="color">darkblue</xsl:attribute>
    <xsl:attribute name="padding-top">1.4pc</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="keyword_red">
    <xsl:attribute name="color">red</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="keyword_blue">
    <xsl:attribute name="color">blue</xsl:attribute>
  </xsl:attribute-set>

</xsl:stylesheet>