package com.znlgis.ogu4j.datasource;

import com.znlgis.ogu4j.engine.GisEngine;
import com.znlgis.ogu4j.engine.GisEngineFactory;
import com.znlgis.ogu4j.engine.enums.DataFormatType;
import com.znlgis.ogu4j.engine.enums.GisEngineType;
import com.znlgis.ogu4j.exception.OguException;
import com.znlgis.ogu4j.engine.model.layer.OguLayer;

import java.util.Map;

/**
 * 图层格式转换工具类
 * <p>
 * 提供OguLayer与各种GIS数据格式（Shapefile、GeoJSON、FileGDB、PostGIS、TXT）之间的相互转换功能。
 * 支持使用GeoTools或GDAL/OGR两种引擎进行转换。
 * 通过{@link GisEngineFactory}获取引擎来实现格式转换。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 * @see com.znlgis.ogu4j.engine.GisEngine
 * @see com.znlgis.ogu4j.engine.GisEngineFactory
 * @see com.znlgis.ogu4j.engine.io.LayerReader
 * @see com.znlgis.ogu4j.engine.io.LayerWriter
 */
public class OguLayerUtil {
    private OguLayerUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 读取图层
     * <p>
     * 使用引擎抽象层API读取图层数据。
     * </p>
     *
     * @param formatType       数据格式类型
     * @param path             数据源路径
     * @param layerName        图层名称
     * @param attributeFilter  属性过滤条件
     * @param spatialFilterWkt 空间过滤条件
     * @param gisEngineType    GIS引擎类型
     * @return OguLayer图层对象
     * @throws OguException 读取失败时抛出异常
     */
    public static OguLayer readLayer(DataFormatType formatType, String path, String layerName,
                                      String attributeFilter, String spatialFilterWkt,
                                      GisEngineType gisEngineType) throws OguException {
        GisEngine engine = GisEngineFactory.getEngine(gisEngineType, formatType);
        return engine.readLayer(formatType, path, layerName, attributeFilter, spatialFilterWkt);
    }

    /**
     * 写入图层
     * <p>
     * 使用引擎抽象层API写入图层数据。
     * </p>
     *
     * @param formatType    数据格式类型
     * @param layer         要写入的图层
     * @param path          目标路径
     * @param layerName     图层名称
     * @param options       写入选项
     * @param gisEngineType GIS引擎类型
     * @throws OguException 写入失败时抛出异常
     */
    public static void writeLayer(DataFormatType formatType, OguLayer layer, String path,
                                  String layerName, Map<String, Object> options,
                                  GisEngineType gisEngineType) throws OguException {
        layer.validate();
        GisEngine engine = GisEngineFactory.getEngine(gisEngineType, formatType);
        engine.writeLayer(formatType, layer, path, layerName, options);
    }
}
