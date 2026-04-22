<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:param name="selectedUserId"/>
    <xsl:output method="html" indent="yes"/>

    <xsl:template match="/">
        <div class="xsl-content">
            <h2>Recipes Displayed with XSL</h2>

            <p>
                Selected user:
                <strong>
                    <xsl:value-of select="/recipeApp/users/user[@id = $selectedUserId]/name"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="/recipeApp/users/user[@id = $selectedUserId]/surname"/>
                </strong>
            </p>

            <p>
                Matching skill level:
                <strong>
                    <xsl:value-of select="/recipeApp/users/user[@id = $selectedUserId]/skillLevel"/>
                </strong>
            </p>

            <table>
                <thead>
                    <tr>
                        <th>Title</th>
                        <th>Cuisine 1</th>
                        <th>Cuisine 2</th>
                        <th>Difficulty</th>
                    </tr>
                </thead>

                <tbody>
                    <xsl:for-each select="recipeApp/recipes/recipe">
                        <xsl:variable name="selectedUserSkill"
                                      select="/recipeApp/users/user[@id = $selectedUserId]/skillLevel"/>

                        <tr>
                            <xsl:choose>
                                <xsl:when test="difficulty = $selectedUserSkill">
                                    <xsl:attribute name="class">match-row</xsl:attribute>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="class">other-row</xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>

                            <td><xsl:value-of select="title"/></td>
                            <td><xsl:value-of select="cuisines/cuisine[1]"/></td>
                            <td><xsl:value-of select="cuisines/cuisine[2]"/></td>
                            <td><xsl:value-of select="difficulty"/></td>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table>
        </div>
    </xsl:template>

</xsl:stylesheet>