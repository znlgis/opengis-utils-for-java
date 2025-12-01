package com.znlgis.ogu4j.model.layer;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.znlgis.ogu4j.common.CrsUtil;
import com.znlgis.ogu4j.enums.GeometryType;
import com.znlgis.ogu4j.exception.LayerValidationException;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OGU图层类
 * <p>
 * 统一的GIS图层定义，提供图层名称、坐标系、几何类型、字段定义和要素集合等属性。
 * 支持JSON序列化/反序列化，以及要素过滤功能。
 * </p>
 */
@Data
public class OguLayer implements Serializable {
    /**
     * 图层名称
     */
    private String name;

    /**
     * 图层别名
     */
    private String alias;

    /**
     * 空间参考WKID（EPSG代码）
     */
    private Integer wkid;

    /**
     * 空间类型
     */
    private GeometryType geometryType;

    /**
     * 容差
     */
    private Double tolerance;

    /**
     * 字段定义集合
     */
    private List<OguField> fields;

    /**
     * 要素集合
     */
    private List<OguFeature> features;

    /**
     * 图层元数据
     */
    private OguLayerMetadata metadata;

    /**
     * 默认构造函数。供反射/序列化框架创建空图层对象使用。
     */
    public OguLayer() {
        // no-op default constructor
    }

    /**
     * 从JSON字符串解析为OguLayer
     * <p>
     * 支持包含类名信息的JSON字符串解析。
     * </p>
     *
     * @param json JSON字符串
     * @return OguLayer对象
     */
    public static OguLayer fromJSON(String json) {
        JSONReader.Context context = new JSONReader.Context();
        context.config(JSONReader.Feature.SupportClassForName, true);
        return JSON.parseObject(json, OguLayer.class, context);
    }

    /**
     * 转换为JSON字符串
     *
     * @return JSON字符串
     */
    public String toJSON() {
        JSONWriter.Context context = new JSONWriter.Context();
        context.config(JSONWriter.Feature.ReferenceDetection, false);
        context.config(JSONWriter.Feature.LargeObject, true);
        return JSON.toJSONString(this, context);
    }

    /**
     * 验证图层数据完整性
     *
     * @throws LayerValidationException 验证失败时抛出异常
     */
    public void validate() throws LayerValidationException {
        if (this.getGeometryType() == null) {
            throw new LayerValidationException("未获取到几何类型");
        }

        if (CharSequenceUtil.isBlank(this.getName())) {
            throw new LayerValidationException("未获取到图层名称");
        }

        if (this.getWkid() == null) {
            throw new LayerValidationException("未获取到坐标系");
        }

        if (this.getTolerance() == null) {
            this.setTolerance(CrsUtil.getTolerance(this.getWkid()));
        }
    }

    /**
     * 应用要素过滤器
     *
     * @param filter 过滤器函数
     * @return 过滤后的要素列表
     */
    public List<OguFeature> filter(OguFeatureFilter filter) {
        if (features == null) {
            return List.of();
        }
        return features.stream().filter(filter::apply).collect(Collectors.toList());
    }

    /**
     * 获取要素数量
     *
     * @return 要素数量
     */
    public int getFeatureCount() {
        return features == null ? 0 : features.size();
    }

    /**
     * 获取字段数量
     *
     * @return 字段数量
     */
    public int getFieldCount() {
        return fields == null ? 0 : fields.size();
    }
}
