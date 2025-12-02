package com.znlgis.ogu4j.engine.enums;

import com.znlgis.ogu4j.datasource.OguLayerUtil;
import com.znlgis.ogu4j.engine.util.OgrUtil;
import lombok.Getter;

/**
 * GIS数据格式类型枚举
 * <p>
 * 定义本库支持的GIS数据格式，包括矢量格式（Shapefile、FileGDB等）、
 * 交换格式（GeoJSON、WKT、ESRI JSON）和数据库格式（PostGIS）。
 * 提供与GDAL驱动名称的映射关系。
 * </p>
 *
 * @see OguLayerUtil
 * @see OgrUtil
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
