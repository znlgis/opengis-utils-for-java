package com.znlgis.ogu4j.model;

import lombok.Data;

import java.util.List;

/**
 * GDB图层组模型
 *
 * @author znlgis
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
