<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!DOCTYPE xsl:stylesheet>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<!-- KEYWORD TEMPLATE TUTORIAL EXAMPLE-->

<xsl:template match="*[contains(@class,' topic/keyword ')]" name="topic.keyword">
  <xsl:choose>
    <xsl:when test="@otherprops='red'">
      <span class="red_keyword">
        <xsl:call-template name="commonattributes"/>
        <xsl:call-template name="setidaname"/>
        <xsl:call-template name="flagcheck"/>
        <xsl:call-template name="revtext"/>
      </span>
    </xsl:when>
    <xsl:otherwise>
      <span class="blue_keyword">
        <xsl:call-template name="commonattributes"/>
        <xsl:call-template name="setidaname"/>
        <xsl:call-template name="flagcheck"/>
        <xsl:call-template name="revtext"/>
      </span>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

  <!-- ********************************************* -->
  <xsl:template match="*[contains(@class,' ckg-d/fooditem ')]" name="topic.ckg-d.fooditem">


    <span class="fooditem">
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template  name="setidaname"/>
      <xsl:call-template name="flagcheck"/>
      <xsl:call-template name="revtext"/>
    </span>
  </xsl:template>
  

  <xsl:template match="*[contains(@class,' topic/ul ')]" name="topic.ul">
    <xsl:choose>
      <xsl:when test="@spectitle">
        <xsl:call-template name="sect-heading" />
      </xsl:when>
    </xsl:choose>
    <xsl:variable name="revtest">
      <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')">
        <xsl:call-template name="find-active-rev-flag">
          <xsl:with-param name="allrevs" select="@rev"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$revtest=1">
        <!-- Rev is active - add the DIV -->
        <div class="{@rev}">
          <xsl:apply-templates select="."  mode="ul-fmt" />
        </div>
      </xsl:when>
      <xsl:otherwise>
        <!-- Rev wasn't active - process normally -->
        <xsl:apply-templates select="."  mode="ul-fmt" />
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

</xsl:stylesheet>
