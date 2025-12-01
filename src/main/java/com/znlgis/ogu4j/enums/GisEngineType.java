package com.znlgis.ogu4j.enums;

import com.znlgis.ogu4j.datasource.OgrUtil;
import lombok.Getter;

/**
 * GIS引擎类型枚举
 * <p>
 * 定义本库支持的GIS数据处理引擎类型。
 * GEOTOOLS为纯Java实现，无需额外依赖；GDAL需要安装GDAL本地库。
 * AUTO模式会自动选择可用的引擎，优先使用GDAL。
 * </p>
 *
 * @see com.znlgis.ogu4j.datasource.OguLayerConverter
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
