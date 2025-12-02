package com.znlgis.ogu4j.engine.enums;

import lombok.Getter;
import org.locationtech.jts.geom.*;

/**
 * GIS几何类型枚举
 * <p>
 * 定义GIS系统中支持的几何类型，包括点、线、面及其多部件形式。
 * 提供几何类型与JTS Geometry类、WKB类型代码之间的映射关系。
 * </p>
 *
 * @see org.locationtech.jts.geom.Geometry
 */
@Getter
public enum GeometryType {
    /**
     * 点
     */
    POINT(0, "Point", Point.class, 1),
    /**
     * 多点
     */
    MULTIPOINT(1, "MultiPoint", MultiPoint.class, 4),
    /**
     * 线
     */
    LINESTRING(2, "LineString", LineString.class, 2),
    /**
     * 环
     */
    LINEARRING(3, "LinearRing", LinearRing.class, 101),
    /**
     * 多线
     */
    MULTILINESTRING(4, "MultiLineString", MultiLineString.class, 5),
    /**
     * 面
     */
    POLYGON(5, "Polygon", Polygon.class, 3),
    /**
     * 多面
     */
    MULTIPOLYGON(6, "MultiPolygon", MultiPolygon.class, 6),
    /**
     * 几何集合
     */
    GEOMETRYCOLLECTION(7, "GeometryCollection", GeometryCollection.class, 7),
    ;

    /**
     * 类型代码
     */
    private final int typeCode;
    /**
     * 类型名称
     */
    private final String typeName;
    /**
     * 类型类
     */
    private final Class<?> typeClass;
    /**
     * WKB类型
     */
    private final int wkbGeometryType;

    /**
     * 构造函数
     *
     * @param typeCode        类型代码
     * @param typeName        类型名称
     * @param typeClass       类型类
     * @param wkbGeometryType WKB类型
     */
    GeometryType(int typeCode, String typeName, Class<?> typeClass, int wkbGeometryType) {
        this.typeCode = typeCode;
        this.typeName = typeName;
        this.typeClass = typeClass;
        this.wkbGeometryType = wkbGeometryType;
    }

    /**
     * 根据类型名称获取枚举
     *
     * @param typeName 类型名称
     * @return 枚举
     */
    public static GeometryType valueOfByTypeName(String typeName) {
        for (GeometryType status : GeometryType.values()) {
            if (status.getTypeName().equalsIgnoreCase(typeName)) {
                return status;
            }
        }

        return null;
    }

    /**
     * 根据类型代码获取枚举
     *
     * @param typeCode 类型代码
     * @return 枚举
     */
    public static GeometryType valueOfByTypeCode(int typeCode) {
        for (GeometryType status : GeometryType.values()) {
            if (status.getTypeCode() == typeCode) {
                return status;
            }
        }

        return null;
    }

    /**
     * 根据类型获取枚举
     *
     * @param typeClass 类型类
     * @return 枚举
     */
    public static GeometryType valueOfByTypeClass(Class<?> typeClass) {
        for (GeometryType status : GeometryType.values()) {
            if (status.getTypeClass().equals(typeClass)) {
                return status;
            }
        }

        return null;
    }

    /**
     * 根据WKB类型获取枚举
     *
     * @param wkbGeometryType WKB类型
     * @return 枚举
     */
    public static GeometryType valueOfByWkbGeometryType(int wkbGeometryType) {
        for (GeometryType status : GeometryType.values()) {
            if (status.getWkbGeometryType() == wkbGeometryType) {
                return status;
            }
        }

        return null;
    }
}
