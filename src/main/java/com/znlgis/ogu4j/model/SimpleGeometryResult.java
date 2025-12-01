package com.znlgis.ogu4j.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;

/**
 * 简单几何判断结果模型
 * <p>
 * 用于封装几何对象简单性检查的结果，包含几何是否简单以及非简单点的坐标列表。
 * 简单几何是指不存在自相交或重复点的几何对象。
 * 主要用于GeometryUtil.isSimple()方法的返回值。
 * </p>
 *
 * @see com.znlgis.ogu4j.geometry.GeometryUtil#isSimple(org.locationtech.jts.geom.Geometry)
 */
@Data
@AllArgsConstructor
public class SimpleGeometryResult {
    /**
     * 是否是简单几何
     */
    private boolean isSimple;
    /**
     * 非简单几何点集合
     */
    private List<Coordinate> nonSimplePts;

    /**
     * 默认无参构造函数。
     * <p>
     * 为序列化框架提供空的结果对象实例，字段采用默认值进行初始化。
     * </p>
     */
    public SimpleGeometryResult() {
        // no-op default constructor
    }
}
