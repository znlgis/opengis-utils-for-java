package com.znlgis.ogu4j.engine.model.layer;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * OGU字段值类
 * <p>
 * 表示要素的一个属性值，包含字段定义和具体值。
 * 提供多种类型的值获取方法（字符串、整数、浮点数等）。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class OguFieldValue implements Serializable {
    /**
     * 字段定义
     */
    private OguField field;

    /**
     * 字段值
     */
    private Object value;

    /**
     * 获取字段名称
     *
     * @return 字段名称
     */
    public String getFieldName() {
        return field == null ? null : field.getName();
    }

    /**
     * 获取字符串值
     *
     * @return 字符串值
     */
    public String getStringValue() {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 获取整数值
     *
     * @return 整数值，解析失败时返回null
     */
    public Integer getIntValue() {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取双精度浮点值
     *
     * @return 双精度浮点值，解析失败时返回null
     */
    public Double getDoubleValue() {
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 无参构造函数
     * <p>
     * 供JSON反序列化和框架创建空字段值对象使用。
     * </p>
     */
    public OguFieldValue(){

    }
}
