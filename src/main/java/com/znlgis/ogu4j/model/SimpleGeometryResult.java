package com.znlgis.ogu4j.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;

/**
 * 简单几何判断模型
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
