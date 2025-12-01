package com.znlgis.ogu4j.model.layer;

import com.znlgis.ogu4j.enums.FieldDataType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * OGU字段定义类
 * <p>
 * 表示图层中的一个字段的元数据信息，包含字段名称、别名、数据类型等属性。
 * </p>
 *
 * @author znlgis
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OguField implements Serializable {
    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段别名
     */
    private String alias;

    /**
     * 字段描述
     */
    private String description;

    /**
     * 字段数据类型
     */
    private FieldDataType dataType;

    /**
     * 字段长度（用于字符串类型）
     */
    private Integer length;

    /**
     * 是否可为空
     */
    private Boolean nullable;

    /**
     * 默认值
     */
    private Object defaultValue;

    /**
     * 简化构造函数
     *
     * @param name     字段名称
     * @param alias    字段别名
     * @param dataType 字段数据类型
     */
    public OguField(String name, String alias, FieldDataType dataType) {
        this.name = name;
        this.alias = alias;
        this.dataType = dataType;
    }

    /**
     * 简化构造函数（含描述）
     *
     * @param name        字段名称
     * @param alias       字段别名
     * @param description 字段描述
     * @param dataType    字段数据类型
     */
    public OguField(String name, String alias, String description, FieldDataType dataType) {
        this.name = name;
        this.alias = alias;
        this.description = description;
        this.dataType = dataType;
    }
}
