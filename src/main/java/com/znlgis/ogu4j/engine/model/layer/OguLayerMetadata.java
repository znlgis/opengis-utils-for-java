package com.znlgis.ogu4j.engine.model.layer;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * OGU图层元数据类
 * <p>
 * 用于存储图层的扩展属性信息，如坐标系参数、数据来源、投影类型等。
 * 主要用于国土TXT坐标文件的属性描述部分。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class OguLayerMetadata implements Serializable {
    /**
     * 格式版本号
     */
    private String formatVersion;

    /**
     * 数据产生单位
     */
    private String dataSource;

    /**
     * 数据产生日期
     */
    private String dataDate;

    /**
     * 坐标系名称
     */
    private String coordinateSystemName;

    /**
     * 几度分带（3度或6度分带）
     */
    private String zoneDivision;

    /**
     * 投影类型
     */
    private String projectionType;

    /**
     * 计量单位
     */
    private String measureUnit;

    /**
     * 带号
     */
    private String zoneNumber;

    /**
     * 精度
     */
    private String precision;

    /**
     * 转换参数
     */
    private String transformParams;

    /**
     * 扩展信息集合
     */
    private List<ExtendedInfo> extendedInfos;

    /**
     * 扩展信息
     */
    @Data
    @AllArgsConstructor
    public static class ExtendedInfo implements Serializable {
        /**
         * 扩展信息名称
         */
        private String name;

        /**
         * 扩展信息字段
         */
        private LinkedHashMap<String, String> properties;

        /**
         * 默认构造函数。用于延伸信息反序列化或延迟填充场景。
         */
        public ExtendedInfo() {
        }
    }

    /**
     * 默认构造函数。用于创建空的图层元数据对象。
     */
    public OguLayerMetadata() {
    }
}
