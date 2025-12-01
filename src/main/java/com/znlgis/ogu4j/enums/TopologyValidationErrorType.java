package com.znlgis.ogu4j.enums;

import lombok.Getter;

/**
 * 拓扑验证错误类型枚举
 * <p>
 * 定义几何对象拓扑验证中可能出现的各类错误，包括自相交、洞重叠、环未闭合等。
 * 每种错误类型对应JTS IsValidOp返回的错误代码。
 * </p>
 *
 * @see com.znlgis.ogu4j.model.TopologyValidationResult
 * @see com.znlgis.ogu4j.geometry.JtsGeometryUtil#isValid(org.locationtech.jts.geom.Geometry)
 */

@Getter
public enum TopologyValidationErrorType {
    /**
     * 拓扑检查错误
     */
    ERROR(0, "拓扑检查错误"),
    /**
     * 点重叠
     */
    REPEATED_POINT(1, "点重叠"),
    /**
     * 洞在图形外
     */
    HOLE_OUTSIDE_SHELL(2, "洞在图形外"),
    /**
     * 洞重叠
     */
    NESTED_HOLES(3, "洞重叠"),
    /**
     * 图形内部不连通
     */
    DISCONNECTED_INTERIOR(4, "图形内部不连通"),
    /**
     * 自相交
     */
    SELF_INTERSECTION(5, "自相交"),
    /**
     * 环自相交
     */
    RING_SELF_INTERSECTION(6, "环自相交"),
    /**
     * 图形重叠
     */
    NESTED_SHELLS(7, "图形重叠"),
    /**
     * 环重复
     */
    DUPLICATE_RINGS(8, "环重复"),
    /**
     * 点太少无法构成有效几何
     */
    TOO_FEW_POINTS(9, "点太少无法构成有效几何"),
    /**
     * 无效坐标
     */
    INVALID_COORDINATE(10, "无效坐标"),
    /**
     * 环未闭合
     */
    RING_NOT_CLOSED(11, "环未闭合");

    /**
     * 错误描述
     */
    private final String desc;
    /**
     * 错误类型
     */
    private final int errorType;

    /**
     * 构造函数
     *
     * @param errorType 错误类型
     * @param desc      错误描述
     */
    TopologyValidationErrorType(int errorType, String desc) {
        this.errorType = errorType;
        this.desc = desc;
    }

    /**
     * 根据错误类型获取描述
     *
     * @param errorType 错误类型
     * @return 描述
     */
    public static String getDescByErrorType(int errorType) {
        for (TopologyValidationErrorType status : TopologyValidationErrorType.values()) {
            if (status.getErrorType() == errorType) {
                return status.getDesc();
            }
        }

        return null;
    }

    /**
     * 根据错误类型获取枚举
     *
     * @param errorType 错误类型
     * @return 枚举
     */
    public static TopologyValidationErrorType getByErrorType(int errorType) {
        for (TopologyValidationErrorType status : TopologyValidationErrorType.values()) {
            if (status.getErrorType() == errorType) {
                return status;
            }
        }

        return null;
    }
}
