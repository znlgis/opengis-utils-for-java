package com.znlgis.ogu4j.enums;

import lombok.Getter;

/**
 * 支持的GIS数据格式枚举
 *
 * @author znlgis
 */
@Getter
public enum DataFormatType {
    /**
     * WKT
     */
    WKT("WKT", null),
    /**
     * GEOJSON
     */
    GEOJSON("GEOJSON", "GeoJSON"),
    /**
     * ESRIJSON
     */
    ESRIJSON("ESRIJSON", "ESRIJSON"),
    /**
     * SHP
     */
    SHP("SHP文件", "ESRI Shapefile"),
    /**
     * TXT
     */
    TXT("国土TXT", null),
    /**
     * FILEGDB
     */
    FILEGDB("FILEGDB", "OpenFileGDB"),
    /**
     * POSTGIS
     */
    POSTGIS("POSTGIS", "PostgreSQL"),
    /**
     * ARCSDE
     */
    ARCSDE("ARCSDE", null);

    /**
     * 描述
     */
    private final String desc;
    /**
     * GDAL驱动名称
     */
    private final String gdalDriverName;

    /**
     * 构造函数
     *
     * @param desc           描述
     * @param gdalDriverName GDAL驱动名称
     */
    DataFormatType(String desc, String gdalDriverName) {
        this.desc = desc;
        this.gdalDriverName = gdalDriverName;
    }
}
