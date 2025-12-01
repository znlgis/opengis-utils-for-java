package com.znlgis.ogu4j.enums;

import com.znlgis.ogu4j.datasource.OgrUtil;
import lombok.Getter;

/**
 * 所支持的GIS引擎类型
 *
 * @author znlgis
 */
@Getter
public enum GisEngineType {
    /**
     * GEOTOOLS
     */
    GEOTOOLS("GEOTOOLS"),
    /**
     * GDAL
     */
    GDAL("GDAL"),
    /**
     * AUTO
     */
    AUTO("自动选择，GDAL优先");

    /**
     * 描述
     */
    private final String desc;

    /**
     * 构造函数
     *
     * @param desc 描述
     */
    GisEngineType(String desc) {
        this.desc = desc;
    }

    /**
     * 获取GIS引擎类型
     *
     * @param gisEngineType GIS引擎类型
     * @return GIS引擎类型
     */
    public static GisEngineType getGisEngineType(GisEngineType gisEngineType) {
        if (gisEngineType == null || gisEngineType == AUTO) {
            if (Boolean.TRUE.equals(OgrUtil.getOgrInitSuccess())) {
                return GDAL;
            } else {
                return GEOTOOLS;
            }
        }

        return gisEngineType;
    }
}
