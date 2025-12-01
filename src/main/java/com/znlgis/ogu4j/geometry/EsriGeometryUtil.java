package com.znlgis.ogu4j.geometry;

import com.esri.core.geometry.*;
import com.znlgis.ogu4j.enums.GeometryType;
import com.znlgis.ogu4j.model.layer.OguField;

import java.util.List;

/**
 * ESRI几何处理工具类
 * <p>
 * 提供基于ESRI Geometry API的几何创建、转换和空间分析功能。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 *
 * @author znlgis
 * @since 1.0.0
 */
public class EsriGeometryUtil {
    private EsriGeometryUtil() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== 几何创建方法 ====================

    /**
     * 通过GeoJSON创建ESRI Geometry
     *
     * @param geojson GeoJSON格式的字符串
     * @return ESRI Geometry对象
     */
    public static Geometry createGeometryByGeoJson(String geojson) {
        return GeometryEngine.geoJsonToGeometry(geojson, 0, Geometry.Type.Unknown).getGeometry();
    }

    /**
     * 通过ESRI JSON创建ESRI Geometry
     *
     * @param esrijson ESRI JSON格式的字符串
     * @return ESRI Geometry对象
     */
    public static Geometry createGeometryByJson(String esrijson) {
        return GeometryEngine.jsonToGeometry(esrijson).getGeometry();
    }

    /**
     * 通过WKT创建ESRI Geometry
     * <p>
     * 如果标准WKT解析失败，会尝试通过JTS进行格式修正后再次解析。
     * </p>
     *
     * @param wkt WKT格式的字符串
     * @return ESRI Geometry对象
     */
    public static Geometry createGeometryByWkt(String wkt) {
        Geometry geometry;
        try {
            geometry = GeometryEngine.geometryFromWkt(wkt, 0, Geometry.Type.Unknown);
        } catch (Exception e) {
            wkt = GeometryConverter.wkt2Geometry(wkt).toText();
            geometry = GeometryEngine.geometryFromWkt(wkt, 0, Geometry.Type.Unknown);
        }
        return geometry;
    }

    // ==================== 几何输出方法 ====================

    /**
     * 将ESRI Geometry转换为GeoJSON格式字符串
     *
     * @param geometry ESRI Geometry对象
     * @return GeoJSON格式的字符串
     */
    public static String toGeoJson(Geometry geometry) {
        return GeometryEngine.geometryToGeoJson(geometry);
    }

    /**
     * 将ESRI Geometry转换为ESRI JSON格式字符串
     *
     * @param wkid     坐标系WKID
     * @param geometry ESRI Geometry对象
     * @return ESRI JSON格式的字符串
     */
    public static String toEsriJson(int wkid, Geometry geometry) {
        return GeometryEngine.geometryToJson(wkid, geometry);
    }

    /**
     * 将ESRI Geometry转换为WKT格式字符串
     *
     * @param geometry ESRI Geometry对象
     * @return WKT格式的字符串
     */
    public static String toWkt(Geometry geometry) {
        return GeometryEngine.geometryToWkt(geometry, 0);
    }

    // ==================== 格式转换便捷方法 ====================

    /**
     * 将ESRI JSON转换为WKT格式
     *
     * @param esriJson ESRI JSON格式的字符串
     * @return WKT格式的字符串
     */
    public static String esriJson2Wkt(String esriJson) {
        Geometry geom = createGeometryByJson(esriJson);
        return toWkt(geom);
    }

    /**
     * 将WKT转换为ESRI JSON格式
     *
     * @param wkt WKT格式的字符串
     * @return ESRI JSON格式的字符串（使用默认坐标系）
     */
    public static String wkt2EsriJson(String wkt) {
        Geometry geom = createGeometryByWkt(wkt);
        return toEsriJson(0, geom);
    }

    // ==================== 字段处理方法 ====================

    /**
     * 排除特殊字段
     * <p>
     * 从字段列表中移除以SHAPE或OBJECTID开头的字段，这些字段通常是GIS系统的保留字段。
     * </p>
     *
     * @param fields 字段列表
     */
    public static void excludeSpecialFields(List<OguField> fields) {
        fields.removeIf(field -> field.getName().toUpperCase().startsWith("SHAPE")
                || field.getName().toUpperCase().startsWith("OBJECTID"));
    }

    // ==================== 几何属性查询方法 ====================

    /**
     * 几何是否为空
     *
     * @param wkt WKT格式的字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String wkt) {
        Geometry geom = createGeometryByWkt(wkt);
        return geom.isEmpty();
    }

    /**
     * 获取几何的长度
     *
     * @param wkt WKT格式的字符串
     * @return 长度
     */
    public static double length(String wkt) {
        Geometry geom = createGeometryByWkt(wkt);
        return geom.calculateLength2D();
    }

    /**
     * 几何是否相交
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否相交
     */
    public static boolean intersects(String awkt, String bwkt, Integer wkid) {
        Geometry a = createGeometryByWkt(awkt);
        Geometry b = createGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorIntersects.local().execute(a, b, sr, null);
    }

    /**
     * 获取几何类型
     *
     * @param wkt WKT格式的字符串
     * @return 几何类型
     */
    public static GeometryType geometryType(String wkt) {
        Geometry geom = createGeometryByWkt(wkt);
        return GeometryType.valueOf(geom.getType().name().toUpperCase());
    }

    /**
     * 几何是否简单
     *
     * @param wkt  WKT格式的字符串
     * @param wkid 坐标系
     * @return 是否简单
     */
    public static boolean isSimple(String wkt, Integer wkid) {
        Geometry geom = createGeometryByWkt(wkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorSimplifyOGC.local().isSimpleOGC(geom, sr, false, null, null);
    }

    /**
     * 计算几何的距离
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 距离
     */
    public static double distance(String awkt, String bwkt, Integer wkid) {
        Geometry a = createGeometryByWkt(awkt);
        Geometry b = createGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return GeometryEngine.distance(a, b, sr);
    }

    /**
     * 计算几何的面积
     *
     * @param wkt WKT格式的字符串
     * @return 面积
     */
    public static double area(String wkt) {
        Geometry geom = createGeometryByWkt(wkt);
        return geom.calculateArea2D();
    }

    /**
     * 计算几何的中心点
     *
     * @param wkt WKT格式的字符串
     * @return 中心点的WKT格式字符串
     */
    public static String centroid(String wkt) {
        Geometry geom = createGeometryByWkt(wkt);
        Point2D point2D = OperatorCentroid2D.local().execute(geom, null);
        Point point = new Point(point2D.x, point2D.y);
        return toWkt(point);
    }

    /**
     * 计算几何的维度
     *
     * @param wkt WKT格式的字符串
     * @return 维度
     */
    public static int dimension(String wkt) {
        Geometry geom = createGeometryByWkt(wkt);
        return geom.getDimension();
    }

    /**
     * 计算几何的边界
     *
     * @param wkt WKT格式的字符串
     * @return 边界的WKT格式字符串
     */
    public static String boundary(String wkt) {
        Geometry geom = createGeometryByWkt(wkt);
        Geometry boundary = OperatorBoundary.local().execute(geom, null);
        return toWkt(boundary);
    }

    /**
     * 判断几何是否相离，一个公共点都没有
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否相离
     */
    public static boolean disjoint(String awkt, String bwkt, Integer wkid) {
        Geometry a = createGeometryByWkt(awkt);
        Geometry b = createGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorDisjoint.local().execute(a, b, sr, null);
    }

    /**
     * 判断几何是否接触，有公共点但是没有公共区域
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否接触
     */
    public static boolean touches(String awkt, String bwkt, Integer wkid) {
        Geometry a = createGeometryByWkt(awkt);
        Geometry b = createGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorTouches.local().execute(a, b, sr, null);
    }

    /**
     * 判断几何是否交叉，有公共区域但是不包含
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否交叉
     */
    public static boolean crosses(String awkt, String bwkt, Integer wkid) {
        Geometry a = createGeometryByWkt(awkt);
        Geometry b = createGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorCrosses.local().execute(a, b, sr, null);
    }

    /**
     * 判断几何A是否包含几何B
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否包含
     */
    public static boolean contains(String awkt, String bwkt, Integer wkid) {
        Geometry a = createGeometryByWkt(awkt);
        Geometry b = createGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorContains.local().execute(a, b, sr, null);
    }

    /**
     * 判断几何A是否在几何B内部
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否在内部
     */
    public static boolean within(String awkt, String bwkt, Integer wkid) {
        Geometry a = createGeometryByWkt(awkt);
        Geometry b = createGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorWithin.local().execute(a, b, sr, null);
    }

    /**
     * 判断几何是否重叠，有公共区域且包含
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否有重叠
     */
    public static boolean overlaps(String awkt, String bwkt, Integer wkid) {
        Geometry a = createGeometryByWkt(awkt);
        Geometry b = createGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorOverlaps.local().execute(a, b, sr, null);
    }

    /**
     * 判断几何是否符合给定关系
     *
     * @param awkt    A的WKT格式字符串
     * @param bwkt    B的WKT格式字符串
     * @param wkid    坐标系
     * @param pattern 关系模式
     * @return 是否符合关系
     */
    public static boolean relatePattern(String awkt, String bwkt, Integer wkid, String pattern) {
        Geometry a = createGeometryByWkt(awkt);
        Geometry b = createGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorRelate.local().execute(a, b, sr, pattern, null);
    }

    /**
     * 计算几何的缓冲区
     *
     * @param wkt      WKT格式字符串
     * @param wkid     坐标系
     * @param distance 缓冲区距离
     * @return 缓冲区的WKT格式字符串
     */
    public static String buffer(String wkt, Integer wkid, double distance) {
        Geometry geom = createGeometryByWkt(wkt);
        SpatialReference sr = SpatialReference.create(wkid);
        Geometry buffer = OperatorBuffer.local().execute(geom, sr, distance, null);
        return toWkt(buffer);
    }

    /**
     * 计算几何的凸包
     *
     * @param wkt WKT格式字符串
     * @return 凸包的WKT格式字符串
     */
    public static String convexHull(String wkt) {
        Geometry geom = createGeometryByWkt(wkt);
        Geometry convexHull = OperatorConvexHull.local().execute(geom, null);
        return toWkt(convexHull);
    }

    /**
     * 计算几何的交集
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 交集的WKT格式字符串
     */
    public static String intersection(String awkt, String bwkt, Integer wkid) {
        Geometry a = createGeometryByWkt(awkt);
        Geometry b = createGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        Geometry intersection = OperatorIntersection.local().execute(a, b, sr, null);
        return toWkt(intersection);
    }

    /**
     * 计算几何的并集
     *
     * @param wkts WKT格式字符串列表
     * @param wkid 坐标系
     * @return 并集的WKT格式字符串
     */
    public static String union(List<String> wkts, Integer wkid) {
        Geometry[] geoms = wkts.stream().map(EsriGeometryUtil::createGeometryByWkt).toArray(Geometry[]::new);
        SpatialReference sr = SpatialReference.create(wkid);
        Geometry union = GeometryEngine.union(geoms, sr);
        return toWkt(union);
    }

    /**
     * 获取A与B并集擦除B的部分
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 差集的WKT格式字符串
     */
    public static String difference(String awkt, String bwkt, Integer wkid) {
        Geometry a = createGeometryByWkt(awkt);
        Geometry b = createGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        Geometry difference = OperatorDifference.local().execute(a, b, sr, null);
        return toWkt(difference);
    }

    /**
     * 获取A与B并集减去A与B交集
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 对称差的WKT格式字符串
     */
    public static String symDifference(String awkt, String bwkt, Integer wkid) {
        Geometry a = createGeometryByWkt(awkt);
        Geometry b = createGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        Geometry symDifference = OperatorSymmetricDifference.local().execute(a, b, sr, null);
        return toWkt(symDifference);
    }

    /**
     * 判断几何是否相等
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否相等
     */
    public static boolean equals(String awkt, String bwkt, Integer wkid) {
        Geometry a = createGeometryByWkt(awkt);
        Geometry b = createGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorEquals.local().execute(a, b, sr, null);
    }

    /**
     * 简化几何
     *
     * @param wkt  WKT格式字符串
     * @param wkid 坐标系
     * @return 简化后的WKT格式字符串
     */
    public static String simplify(String wkt, Integer wkid) {
        Geometry geom = createGeometryByWkt(wkt);
        SpatialReference sr = SpatialReference.create(wkid);
        Geometry simplified = OperatorSimplifyOGC.local().execute(geom, sr, false, null);
        return toWkt(simplified);
    }
}
