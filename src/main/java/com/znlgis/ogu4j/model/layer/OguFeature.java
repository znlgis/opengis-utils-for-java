package com.znlgis.ogu4j.model.layer;

import cn.hutool.core.collection.CollectionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * OGU要素类
 * <p>
 * 表示GIS图层中的一个地理要素，包含要素ID、几何信息（WKT格式）和属性值集合。
 * 提供属性值的获取和设置方法。
 * </p>
 *
 * @author znlgis
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class OguFeature implements Serializable {
    /**
     * 要素ID
     * 注意：GDAL、GEOTOOLS、ArcGIS、QGIS等对不同数据类型的FID或者OID的定义都不同，
     * 此处只是如实返回，没有做兼容，
     * 所以除非确定，最好不要把此字段当作OBJECTID用，推荐直接用属性过滤和查找
     */
    private String id;

    /**
     * 要素图形WKT (Well-Known Text)
     */
    private String geometry;

    /**
     * 要素属性值集合
     */
    private List<OguFieldValue> attributes;

    /**
     * 坐标点集合（用于TXT格式等特殊场景）
     */
    private List<OguCoordinate> coordinates;

    /**
     * 原始属性值列表（用于TXT格式等特殊场景）
     */
    private List<String> rawValues;

    /**
     * 获取指定名称的属性值对象
     *
     * @param fieldName 要获取的字段名称
     * @return 属性值对象，null表示该字段不存在
     */
    public OguFieldValue getAttribute(String fieldName) {
        if (CollectionUtil.isEmpty(attributes)) {
            return null;
        }
        return attributes.stream()
                .filter(attr -> attr.getField() != null && attr.getField().getName() != null
                        && attr.getField().getName().equalsIgnoreCase(fieldName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取指定字段的值
     *
     * @param fieldName 要获取的字段名称
     * @return 字段的值，null表示字段不存在或值为空
     */
    public Object getValue(String fieldName) {
        OguFieldValue attr = getAttribute(fieldName);
        return attr == null ? null : attr.getValue();
    }

    /**
     * 设置指定字段的值
     *
     * @param fieldName 字段名称
     * @param value     字段值
     * @return 是否设置成功
     */
    public boolean setValue(String fieldName, Object value) {
        OguFieldValue attr = getAttribute(fieldName);
        if (attr != null) {
            attr.setValue(value);
            return true;
        }
        return false;
    }

    /**
     * 无参构造函数
     */
    public OguFeature(){

    }
}
