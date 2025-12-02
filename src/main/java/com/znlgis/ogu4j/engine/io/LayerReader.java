package com.znlgis.ogu4j.engine.io;

import com.znlgis.ogu4j.exception.OguException;
import com.znlgis.ogu4j.engine.model.layer.OguLayer;

/**
 * 图层读取器接口
 * <p>
 * 定义图层读取的标准接口（策略模式）。
 * 每种数据格式可以实现此接口来提供特定的读取逻辑。
 * 支持属性过滤和空间过滤功能。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 * @see com.znlgis.ogu4j.engine.io.LayerWriter
 * @see com.znlgis.ogu4j.engine.enums.DataFormatType
 */
public interface LayerReader {

    /**
     * 读取图层数据
     *
     * @param path             数据源路径
     * @param layerName        图层名称（某些格式可能为null）
     * @param attributeFilter  属性过滤条件（CQL/SQL表达式），为null时不过滤
     * @param spatialFilterWkt 空间过滤条件（WKT格式），为null时不过滤
     * @return OguLayer图层对象
     * @throws OguException 读取失败时抛出异常
     */
    OguLayer read(String path, String layerName, String attributeFilter, String spatialFilterWkt) throws OguException;

    /**
     * 检查当前读取器是否支持指定的数据源
     *
     * @param path 数据源路径
     * @return true表示支持，false表示不支持
     */
    boolean supports(String path);
}
