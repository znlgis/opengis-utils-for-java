package com.znlgis.ogu4j.engine.model.layer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * OGU坐标类
 * <p>
 * 表示一个地理坐标点，支持二维/三维坐标。
 * 包含点号和圈号属性，用于国土TXT格式等特殊场景。
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OguCoordinate implements Serializable {
    /**
     * X坐标（经度）
     */
    private Double x;

    /**
     * Y坐标（纬度）
     */
    private Double y;

    /**
     * Z坐标（高程，可选）
     */
    private Double z;

    /**
     * 点号（用于TXT格式等特殊场景）
     */
    private String pointNumber;

    /**
     * 圈号/环号（用于多边形等场景）
     */
    private Integer ringNumber;

    /**
     * 简化构造函数（二维坐标）
     *
     * @param x X坐标
     * @param y Y坐标
     */
    public OguCoordinate(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 简化构造函数（三维坐标）
     *
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     */
    public OguCoordinate(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
