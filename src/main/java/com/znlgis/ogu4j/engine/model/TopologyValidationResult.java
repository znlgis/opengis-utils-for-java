package com.znlgis.ogu4j.engine.model;

import com.znlgis.ogu4j.engine.enums.TopologyValidationErrorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;

/**
 * 拓扑验证结果模型
 * <p>
 * 用于封装几何对象拓扑验证的结果，包含验证是否通过、错误位置、错误类型和错误信息。
 * 主要用于GeometryUtil.isValid()方法的返回值。
 * </p>
 *
 * @see com.znlgis.ogu4j.geometry.GeometryUtil#isValid(org.locationtech.jts.geom.Geometry)
 */
@Data
@AllArgsConstructor
public class TopologyValidationResult {
    /**
     * 是否有效
     */
    private boolean isValid;
    /**
     * 错误位置坐标
     */
    private Coordinate coordinate;
    /**
     * 错误类型
     */
    private TopologyValidationErrorType errorType;
    /**
     * 错误信息
     */
    private String message;

    /**
     * 默认无参构造函数。
     * <p>
     * 为序列化和基于反射的框架提供空的拓扑验证结果实例。
     * </p>
     */
    public TopologyValidationResult() {
        // no-op default constructor
    }
}
