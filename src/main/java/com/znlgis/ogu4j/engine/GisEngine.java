package com.znlgis.ogu4j.engine;

import com.znlgis.ogu4j.engine.enums.DataFormatType;
import com.znlgis.ogu4j.exception.OguException;
import com.znlgis.ogu4j.engine.io.LayerReader;
import com.znlgis.ogu4j.engine.io.LayerWriter;
import com.znlgis.ogu4j.engine.model.layer.OguLayer;

import java.util.Map;

/**
 * GIS引擎抽象接口
 * <p>
 * 定义GIS引擎的标准接口（适配器模式）。
 * 隔离底层GIS库（GeoTools/GDAL）的实现细节，
 * 使上层业务代码与具体GIS库解耦。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 * @see com.znlgis.ogu4j.engine.GeoToolsEngine
 * @see com.znlgis.ogu4j.engine.GdalEngine
 */
public interface GisEngine {

    /**
     * 获取引擎名称
     *
     * @return 引擎名称
     */
    String getName();

    /**
     * 检查引擎是否可用
     *
     * @return true表示引擎可用，false表示不可用
     */
    boolean isAvailable();

    /**
     * 检查引擎是否支持指定的数据格式
     *
     * @param formatType 数据格式类型
     * @return true表示支持，false表示不支持
     */
    boolean supports(DataFormatType formatType);

    /**
     * 获取指定格式的图层读取器
     *
     * @param formatType 数据格式类型
     * @return 图层读取器
     * @throws OguException 如果引擎不支持该格式
     */
    LayerReader getReader(DataFormatType formatType) throws OguException;

    /**
     * 获取指定格式的图层写入器
     *
     * @param formatType 数据格式类型
     * @return 图层写入器
     * @throws OguException 如果引擎不支持该格式
     */
    LayerWriter getWriter(DataFormatType formatType) throws OguException;

    /**
     * 读取图层数据
     *
     * @param formatType       数据格式类型
     * @param path             数据源路径
     * @param layerName        图层名称
     * @param attributeFilter  属性过滤条件
     * @param spatialFilterWkt 空间过滤条件
     * @return OguLayer图层对象
     * @throws OguException 读取失败时抛出异常
     */
    OguLayer readLayer(DataFormatType formatType, String path, String layerName,
                       String attributeFilter, String spatialFilterWkt) throws OguException;

    /**
     * 写入图层数据
     *
     * @param formatType 数据格式类型
     * @param layer      要写入的图层
     * @param path       目标路径
     * @param layerName  图层名称
     * @param options    写入选项
     * @throws OguException 写入失败时抛出异常
     */
    void writeLayer(DataFormatType formatType, OguLayer layer, String path,
                    String layerName, Map<String, Object> options) throws OguException;
}
