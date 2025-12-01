package com.znlgis.ogu4j.model;

import com.znlgis.ogu4j.enums.TopologyValidationErrorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;

/**
 * 拓扑错误模型
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
