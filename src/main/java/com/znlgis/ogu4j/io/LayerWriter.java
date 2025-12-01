package com.znlgis.ogu4j.io;

import com.znlgis.ogu4j.exception.OguException;
import com.znlgis.ogu4j.model.layer.OguLayer;

import java.util.Map;

/**
 * 图层写入器接口
 * <p>
 * 定义图层写入的标准接口（策略模式）。
 * 每种数据格式可以实现此接口来提供特定的写入逻辑。
 * </p>
 *
 * @see com.znlgis.ogu4j.io.LayerReader
 * @see com.znlgis.ogu4j.enums.DataFormatType
 */
public interface LayerWriter {

    /**
     * 写入图层数据
     *
     * @param layer     要写入的OguLayer图层对象
     * @param path      目标数据源路径
     * @param layerName 图层名称（某些格式可能被忽略）
     * @param options   写入选项（可为null）
     * @throws OguException 写入失败时抛出异常
     */
    void write(OguLayer layer, String path, String layerName, Map<String, Object> options) throws OguException;

    /**
     * 检查当前写入器是否支持指定的目标路径
     *
     * @param path 目标数据源路径
     * @return true表示支持，false表示不支持
     */
    boolean supports(String path);
}
