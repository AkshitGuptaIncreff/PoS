<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:template match="/">
        <fo:root>
            <fo:layout-master-set>
                <fo:simple-page-master
                        master-name="A4"
                        page-height="29.7cm"
                        page-width="21cm"
                        margin="1cm">

                    <fo:region-body/>

                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="A4">

                <fo:flow flow-name="xsl-region-body">

                    <fo:block
                            font-size="24pt"
                            font-weight="bold"
                            text-align="center">

                        POS STORE

                    </fo:block>
                    <fo:block
                            text-align="center"
                            font-size="14pt"
                            space-after="10pt">

                        Customer Invoice

                    </fo:block>

                    <fo:block
                            border-bottom="1pt solid black"/>

                    <fo:block space-before="15pt"/>

                    <fo:block space-before="10pt">
                        Invoice Number:
                        <xsl:value-of select="invoice/invoiceNumber"/>
                    </fo:block>

                    <fo:block>
                        Order Id:
                        <xsl:value-of select="invoice/orderId"/>
                    </fo:block>

                    <fo:block>
                        Customer:
                        <xsl:value-of select="invoice/customerName"/>
                    </fo:block>

                    <fo:block>
                        Order Time:
                        <xsl:value-of select="invoice/orderTime"/>
                    </fo:block>


                    <fo:block space-before="20pt"/>

                    <fo:table
                            width="100%"
                            table-layout="fixed"
                            border-collapse="collapse">

                        <fo:table-column column-width="3cm"/>
                        <fo:table-column column-width="8cm"/>
                        <fo:table-column column-width="3cm"/>
                        <fo:table-column column-width="4cm"/>

                        <fo:table-header>

                            <fo:table-row>

                                <fo:table-cell border="1pt solid black">
                                    <fo:block
                                            font-weight="bold"
                                            text-align="center">
                                        Barcode
                                    </fo:block>
                                </fo:table-cell>

                                <fo:table-cell border="1pt solid black">
                                    <fo:block
                                            font-weight="bold"
                                            text-align="center">
                                        Product
                                    </fo:block>
                                </fo:table-cell>

                                <fo:table-cell border="1pt solid black">
                                    <fo:block
                                            font-weight="bold"
                                            text-align="center">
                                        Qty
                                    </fo:block>
                                </fo:table-cell>

                                <fo:table-cell border="1pt solid black">
                                    <fo:block
                                            font-weight="bold"
                                            text-align="center">
                                        Unit Price
                                    </fo:block>
                                </fo:table-cell>

                            </fo:table-row>

                        </fo:table-header>

                        <fo:table-body>

                            <xsl:for-each select="invoice/items/item">

                                <fo:table-row>

                                    <fo:table-cell border="1pt solid black">
                                        <fo:block text-align="center">
                                            <xsl:value-of select="barcode"/>
                                        </fo:block>
                                    </fo:table-cell>

                                    <fo:table-cell border="1pt solid black">
                                        <fo:block>
                                            <xsl:value-of select="productName"/>
                                        </fo:block>
                                    </fo:table-cell>

                                    <fo:table-cell border="1pt solid black">
                                        <fo:block text-align="center">
                                            <xsl:value-of select="quantity"/>
                                        </fo:block>
                                    </fo:table-cell>

                                    <fo:table-cell border="1pt solid black">
                                        <fo:block text-align="right">
                                            ₹<xsl:value-of select="sellingPrice"/>
                                        </fo:block>
                                    </fo:table-cell>

                                </fo:table-row>

                            </xsl:for-each>

                        </fo:table-body>

                    </fo:table>
                    <fo:block space-before="20pt"/>

                    <fo:block
                            font-size="14pt"
                            font-weight="bold"
                            text-align="right">

                        Grand Total :
                        ₹<xsl:value-of select="invoice/totalAmount"/>

                    </fo:block>

                    <fo:block
                            text-align="center"
                            font-size="8pt">

                        Generated by POS System

                    </fo:block>

                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>