package com.znlgis.ogu4j.model;

import lombok.Data;

import java.util.List;

/**
 * FileGDB图层组结构模型
 * <p>
 * 用于表示ESRI FileGDB（文件地理数据库）的层级结构，包含要素数据集和图层信息。
 * 支持嵌套的图层组结构，可完整描述GDB的组织层次。
 * 主要用于GdalCmdUtil.getGdbDataStructure()方法的返回值。
 * </p>
 *
 * @see com.znlgis.ogu4j.common.GdalCmdUtil#getGdbDataStructure(String)
 */
@Data
public class GdbGroupModel {
    /**
     * 图层组名称
     */
    private String name;
    /**
     * 图层名称
     */
    private List<String> layerNames;
    /**
     * 图层组
     */
    private List<GdbGroupModel> groups;

    /**
     * 默认构造函数。
     * <p>
     * 供序列化框架和反射创建空的图层组模型实例使用。
     * </p>
     */
    public GdbGroupModel() {
        // no-op default constructor
    }
}
