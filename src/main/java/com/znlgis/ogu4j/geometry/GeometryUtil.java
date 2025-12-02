package com.znlgis.ogu4j.geometry;

import com.esri.core.geometry.*;
import com.znlgis.ogu4j.engine.enums.GeometryType;
import com.znlgis.ogu4j.engine.enums.TopologyValidationErrorType;
import com.znlgis.ogu4j.engine.model.SimpleGeometryResult;
import com.znlgis.ogu4j.engine.model.TopologyValidationResult;
import com.znlgis.ogu4j.engine.model.layer.OguField;
import lombok.SneakyThrows;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.algorithm.hull.ConcaveHull;
import org.locationtech.jts.densify.Densifier;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.valid.IsSimpleOp;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 几何处理工具类
 * <p>
 * 提供基于JTS和ESRI Geometry API的几何创建、转换、属性查询、空间关系判断和空间分析功能。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 */
public class GeometryUtil {
    private GeometryUtil() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== 格式转换方法 ====================

    /**
     * WKT转JTS Geometry
     *
     * @param wkt WKT格式的字符串
     * @return JTS Geometry对象
     */
    @SneakyThrows
    public static org.locationtech.jts.geom.Geometry wkt2Geometry(String wkt) {
        WKTReader2 reader = new WKTReader2();
        return reader.read(wkt);
    }

    /**
     * WKT转GeoJSON
     *
     * @param wkt WKT格式的字符串
     * @return GeoJSON格式的字符串
     */
    public static String wkt2Geojson(String wkt) {
        org.locationtech.jts.geom.Geometry geometry = wkt2Geometry(wkt);
        return geometry2Geojson(geometry);
    }

    /**
     * WKT转ESRI JSON
     *
     * @param wkt  WKT格式的字符串
     * @param wkid 坐标系WKID
     * @return ESRI JSON格式的字符串
     */
    public static String wkt2EsriJson(String wkt, int wkid) {
        com.esri.core.geometry.Geometry geometry = createEsriGeometryByWkt(wkt);
        return toEsriJson(wkid, geometry);
    }

    /**
     * GeoJSON转JTS Geometry
     *
     * @param geojson GeoJSON格式的字符串
     * @return JTS Geometry对象
     */
    @SneakyThrows
    public static org.locationtech.jts.geom.Geometry geojson2Geometry(String geojson) {
        GeometryJSON gjson = new GeometryJSON(16);
        return gjson.read(new StringReader(geojson));
    }

    /**
     * GeoJSON转WKT
     *
     * @param geojson GeoJSON格式的字符串
     * @return WKT格式的字符串
     */
    public static String geojson2Wkt(String geojson) {
        org.locationtech.jts.geom.Geometry geometry = geojson2Geometry(geojson);
        return geometry2Wkt(geometry);
    }

    /**
     * GeoJSON转ESRI JSON
     *
     * @param wkid    坐标系WKID
     * @param geojson GeoJSON格式的字符串
     * @return ESRI JSON格式的字符串
     */
    public static String geoJson2EsriJson(int wkid, String geojson) {
        com.esri.core.geometry.Geometry geometry = createEsriGeometryByGeoJson(geojson);
        return toEsriJson(wkid, geometry);
    }

    /**
     * JTS Geometry转WKT
     *
     * @param geometry JTS Geometry对象
     * @return WKT格式的字符串
     */
    @SneakyThrows
    public static String geometry2Wkt(org.locationtech.jts.geom.Geometry geometry) {
        WKTWriter2 writer = new WKTWriter2();
        return writer.write(geometry);
    }

    /**
     * JTS Geometry转GeoJSON
     *
     * @param geometry JTS Geometry对象
     * @return GeoJSON格式的字符串
     */
    @SneakyThrows
    public static String geometry2Geojson(org.locationtech.jts.geom.Geometry geometry) {
        GeometryJSON gjson = new GeometryJSON(16);
        StringWriter writer = new StringWriter();
        gjson.write(geometry, writer);
        return writer.toString();
    }

    /**
     * JTS Geometry转ESRI JSON
     *
     * @param geometry JTS Geometry对象
     * @param wkid     坐标系WKID
     * @return ESRI JSON格式的字符串
     */
    public static String geometry2EsriJson(org.locationtech.jts.geom.Geometry geometry, int wkid) {
        WKTWriter2 writer = new WKTWriter2();
        String wkt = writer.write(geometry);
        return wkt2EsriJson(wkt, wkid);
    }

    /**
     * ESRI JSON转WKT
     *
     * @param esrijson ESRI JSON格式的字符串
     * @return WKT格式的字符串
     */
    public static String esriJson2Wkt(String esrijson) {
        com.esri.core.geometry.Geometry geometry = createEsriGeometryByJson(esrijson);
        return toWkt(geometry);
    }

    /**
     * ESRI JSON转GeoJSON
     *
     * @param esrijson ESRI JSON格式的字符串
     * @return GeoJSON格式的字符串
     */
    public static String esriJson2GeoJson(String esrijson) {
        com.esri.core.geometry.Geometry geometry = createEsriGeometryByJson(esrijson);
        return toGeoJson(geometry);
    }

    /**
     * ESRI JSON转JTS Geometry
     *
     * @param esrijson ESRI JSON格式的字符串
     * @return JTS Geometry对象
     */
    public static org.locationtech.jts.geom.Geometry esriJson2Geometry(String esrijson) {
        com.esri.core.geometry.Geometry geometry = createEsriGeometryByJson(esrijson);
        String wkt = toWkt(geometry);
        return wkt2Geometry(wkt);
    }

    // ==================== ESRI几何创建方法 ====================

    /**
     * 通过GeoJSON创建ESRI Geometry
     *
     * @param geojson GeoJSON格式的字符串
     * @return ESRI Geometry对象
     */
    public static com.esri.core.geometry.Geometry createEsriGeometryByGeoJson(String geojson) {
        return GeometryEngine.geoJsonToGeometry(geojson, 0, com.esri.core.geometry.Geometry.Type.Unknown).getGeometry();
    }

    /**
     * 通过ESRI JSON创建ESRI Geometry
     *
     * @param esrijson ESRI JSON格式的字符串
     * @return ESRI Geometry对象
     */
    public static com.esri.core.geometry.Geometry createEsriGeometryByJson(String esrijson) {
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
    public static com.esri.core.geometry.Geometry createEsriGeometryByWkt(String wkt) {
        com.esri.core.geometry.Geometry geometry;
        try {
            geometry = GeometryEngine.geometryFromWkt(wkt, 0, com.esri.core.geometry.Geometry.Type.Unknown);
        } catch (Exception e) {
            wkt = wkt2Geometry(wkt).toText();
            geometry = GeometryEngine.geometryFromWkt(wkt, 0, com.esri.core.geometry.Geometry.Type.Unknown);
        }
        return geometry;
    }

    // ==================== ESRI几何输出方法 ====================

    /**
     * 将ESRI Geometry转换为GeoJSON格式字符串
     *
     * @param geometry ESRI Geometry对象
     * @return GeoJSON格式的字符串
     */
    public static String toGeoJson(com.esri.core.geometry.Geometry geometry) {
        return GeometryEngine.geometryToGeoJson(geometry);
    }

    /**
     * 将ESRI Geometry转换为ESRI JSON格式字符串
     *
     * @param wkid     坐标系WKID
     * @param geometry ESRI Geometry对象
     * @return ESRI JSON格式的字符串
     */
    public static String toEsriJson(int wkid, com.esri.core.geometry.Geometry geometry) {
        return GeometryEngine.geometryToJson(wkid, geometry);
    }

    /**
     * 将ESRI Geometry转换为WKT格式字符串
     *
     * @param geometry ESRI Geometry对象
     * @return WKT格式的字符串
     */
    public static String toWkt(com.esri.core.geometry.Geometry geometry) {
        return GeometryEngine.geometryToWkt(geometry, 0);
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

    // ==================== JTS几何属性查询方法 ====================

    /**
     * 判断JTS几何是否为空
     *
     * @param geom JTS Geometry对象
     * @return true代表为空，false代表不为空
     */
    public static boolean isEmpty(org.locationtech.jts.geom.Geometry geom) {
        return geom.isEmpty();
    }

    /**
     * 获取JTS几何长度
     *
     * @param geom JTS Geometry对象
     * @return 长度
     */
    public static double length(org.locationtech.jts.geom.Geometry geom) {
        return geom.getLength();
    }

    /**
     * 判断JTS几何是否相交
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return true代表相交，false代表不相交
     */
    public static boolean intersects(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.intersects(b);
    }

    /**
     * 判断JTS几何拓扑是否合法
     *
     * @param geom JTS Geometry对象
     * @return 拓扑错误模型
     */
    public static TopologyValidationResult isValid(org.locationtech.jts.geom.Geometry geom) {
        IsValidOp isValidOp = new IsValidOp(geom);
        if (!isValidOp.isValid()) {
            TopologyValidationErrorType type = TopologyValidationErrorType.getByErrorType(isValidOp.getValidationError().getErrorType());
            String msg;
            if (type != null) {
                msg = type.getDesc();
            } else {
                msg = "未知拓扑错误";
            }
            return new TopologyValidationResult(false,
                    isValidOp.getValidationError().getCoordinate(), type, msg);
        }

        return new TopologyValidationResult(true, null, null, null);
    }

    /**
     * 获取JTS几何类型
     *
     * @param geom JTS Geometry对象
     * @return 几何类型
     */
    public static GeometryType geometryType(org.locationtech.jts.geom.Geometry geom) {
        return GeometryType.valueOfByTypeName(geom.getGeometryType());
    }

    /**
     * 获取JTS几何节点个数
     *
     * @param geom JTS Geometry对象
     * @return 节点个数
     */
    public static int numPoints(org.locationtech.jts.geom.Geometry geom) {
        return geom.getNumPoints();
    }

    /**
     * 判断JTS几何是否简单几何
     *
     * @param geom JTS Geometry对象
     * @return 简单几何结果
     */
    public static SimpleGeometryResult isSimple(org.locationtech.jts.geom.Geometry geom) {
        IsSimpleOp isSimpleOp = new IsSimpleOp(geom);
        isSimpleOp.setFindAllLocations(true);
        if (!isSimpleOp.isSimple()) {
            return new SimpleGeometryResult(false, isSimpleOp.getNonSimpleLocations());
        }

        return new SimpleGeometryResult(true, null);
    }

    /**
     * 计算JTS几何之间的距离
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return 距离
     */
    public static double distance(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.distance(b);
    }

    /**
     * 判断JTS几何间最短距离是否小于给定距离
     *
     * @param a        JTS Geometry对象
     * @param b        JTS Geometry对象
     * @param distance 给定距离
     * @return true代表小于给定距离，false代表大于给定距离
     */
    public static boolean isWithinDistance(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b, double distance) {
        return a.isWithinDistance(b, distance);
    }

    /**
     * 获取JTS几何面积
     *
     * @param geom JTS Geometry对象
     * @return 面积
     */
    public static double area(org.locationtech.jts.geom.Geometry geom) {
        return geom.getArea();
    }

    /**
     * 获取JTS几何中心点
     *
     * @param geom JTS Geometry对象
     * @return 中心点
     */
    public static org.locationtech.jts.geom.Geometry centroid(org.locationtech.jts.geom.Geometry geom) {
        return geom.getCentroid();
    }

    /**
     * 获取JTS几何内部中心点
     *
     * @param geom JTS Geometry对象
     * @return 内部中心点
     */
    public static org.locationtech.jts.geom.Geometry interiorPoint(org.locationtech.jts.geom.Geometry geom) {
        return geom.getInteriorPoint();
    }

    /**
     * 获取JTS几何维度，点为0，线为1，面为2
     *
     * @param geom JTS Geometry对象
     * @return 维度
     */
    public static int dimension(org.locationtech.jts.geom.Geometry geom) {
        return geom.getDimension();
    }

    /**
     * 获取JTS几何边界
     *
     * @param geom JTS Geometry对象
     * @return 边界
     */
    public static org.locationtech.jts.geom.Geometry boundary(org.locationtech.jts.geom.Geometry geom) {
        return geom.getBoundary();
    }

    /**
     * 获取JTS几何外包矩形
     *
     * @param geom JTS Geometry对象
     * @return 外包矩形
     */
    public static org.locationtech.jts.geom.Geometry envelope(org.locationtech.jts.geom.Geometry geom) {
        return geom.getEnvelope();
    }

    /**
     * 判断JTS几何是否相离
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return true代表相离，false代表不相离
     */
    public static boolean disjoint(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.disjoint(b);
    }

    /**
     * 判断JTS几何是否接触
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return true代表接触，false代表不接触
     */
    public static boolean touches(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.touches(b);
    }

    /**
     * 判断JTS几何是否交叉
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return true代表相交，false代表不相交
     */
    public static boolean crosses(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.crosses(b);
    }

    /**
     * 判断JTS几何A是否包含几何B
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return true代表包含，false代表不包含
     */
    public static boolean contains(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.contains(b);
    }

    /**
     * 判断JTS几何A是否在几何B内部
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return true代表在内部，false代表不在内部
     */
    public static boolean within(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.within(b);
    }

    /**
     * 判断JTS几何是否重叠
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return true代表重叠，false代表不重叠
     */
    public static boolean overlaps(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.overlaps(b);
    }

    /**
     * 判断JTS几何是否符合给定关系
     *
     * @param a       JTS Geometry对象
     * @param b       JTS Geometry对象
     * @param pattern 给定关系模式
     * @return true代表符合给定关系，false代表不符合给定关系
     */
    public static boolean relatePattern(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b, String pattern) {
        return a.relate(b, pattern);
    }

    /**
     * 获取JTS几何关系
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return 几何关系
     */
    public static String relate(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.relate(b).toString();
    }

    /**
     * 获取JTS几何缓冲区
     *
     * @param geom     JTS Geometry对象
     * @param distance 缓冲区距离
     * @return 缓冲区
     */
    public static org.locationtech.jts.geom.Geometry buffer(org.locationtech.jts.geom.Geometry geom, double distance) {
        return geom.buffer(distance);
    }

    /**
     * 获取JTS几何凸包
     *
     * @param geom JTS Geometry对象
     * @return 凸包
     */
    public static org.locationtech.jts.geom.Geometry convexHull(org.locationtech.jts.geom.Geometry geom) {
        ConvexHull ch = new ConvexHull(geom);
        return ch.getConvexHull();
    }

    /**
     * 获取JTS几何凹包
     *
     * @param geom JTS Geometry对象
     * @return 凹包
     */
    public static org.locationtech.jts.geom.Geometry concaveHull(org.locationtech.jts.geom.Geometry geom) {
        ConcaveHull ch = new ConcaveHull(geom);
        return ch.getHull();
    }

    /**
     * 获取JTS几何交集
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return 交集
     */
    public static org.locationtech.jts.geom.Geometry intersection(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.intersection(b);
    }

    /**
     * 获取JTS几何并集
     *
     * @param geoms JTS Geometry对象数组
     * @return 并集
     */
    public static org.locationtech.jts.geom.Geometry union(org.locationtech.jts.geom.Geometry... geoms) {
        org.locationtech.jts.geom.Geometry result = null;
        for (org.locationtech.jts.geom.Geometry g : geoms) {
            if (result == null) {
                result = g;
            } else {
                result = result.union(g);
            }
        }
        return result;
    }

    /**
     * 获取JTS几何A与B并集擦除B的部分
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return 差集
     */
    public static org.locationtech.jts.geom.Geometry difference(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.difference(b);
    }

    /**
     * 获取JTS几何A与B并集减去A与B交集
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return 对称差集
     */
    public static org.locationtech.jts.geom.Geometry symDifference(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.symDifference(b);
    }

    /**
     * 按照给定容差判断JTS几何是否对象结构相等
     *
     * @param a         JTS Geometry对象
     * @param b         JTS Geometry对象
     * @param tolerance 容差
     * @return true代表对象结构相等，false代表对象结构不相等
     */
    public static boolean equalsExactTolerance(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b, double tolerance) {
        return a.equalsExact(b, tolerance);
    }

    /**
     * 判断JTS几何是否对象结构相等
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return true代表对象结构相等，false代表对象结构不相等
     */
    public static boolean equalsExact(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.equalsExact(b);
    }

    /**
     * 判断JTS几何是否对象结构相等，不判断节点顺序
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return true代表对象结构相等，false代表对象结构不相等
     */
    public static boolean equalsNorm(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.equalsNorm(b);
    }

    /**
     * 判断JTS几何是否拓扑相等
     *
     * @param a JTS Geometry对象
     * @param b JTS Geometry对象
     * @return true代表拓扑相等，false代表拓扑不相等
     */
    public static boolean equalsTopo(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return a.equalsTopo(b);
    }

    /**
     * 获取JTS几何个数
     *
     * @param collection JTS Geometry对象
     * @return 个数
     */
    public static int numGeometries(org.locationtech.jts.geom.Geometry collection) {
        return collection.getNumGeometries();
    }

    /**
     * 按照索引获取JTS几何
     *
     * @param collection JTS几何集合
     * @param index      索引
     * @return 几何
     */
    public static org.locationtech.jts.geom.Geometry getGeometryN(GeometryCollection collection, int index) {
        return collection.getGeometryN(index);
    }

    /**
     * 获取JTS点的X坐标
     *
     * @param point 点
     * @return X坐标
     */
    public static double getX(org.locationtech.jts.geom.Point point) {
        return point.getX();
    }

    /**
     * 获取JTS点的Y坐标
     *
     * @param point 点
     * @return Y坐标
     */
    public static double getY(org.locationtech.jts.geom.Point point) {
        return point.getY();
    }

    /**
     * 判断JTS线是否闭合
     *
     * @param line 线
     * @return true代表闭合，false代表不闭合
     */
    public static boolean isClosed(LineString line) {
        return line.isClosed();
    }

    /**
     * 按照索引获取JTS线的节点
     *
     * @param line  线
     * @param index 索引
     * @return 节点
     */
    public static org.locationtech.jts.geom.Point pointN(LineString line, int index) {
        return line.getPointN(index);
    }

    /**
     * 获取JTS线的起点
     *
     * @param line 线
     * @return 起点
     */
    public static org.locationtech.jts.geom.Point startPoint(LineString line) {
        return line.getStartPoint();
    }

    /**
     * 获取JTS线的终点
     *
     * @param line 线
     * @return 终点
     */
    public static org.locationtech.jts.geom.Point endPoint(LineString line) {
        return line.getEndPoint();
    }

    /**
     * 判断JTS线是否为环
     *
     * @param line 线
     * @return true代表为环，false代表不为环
     */
    public static boolean isRing(LineString line) {
        return line.isRing();
    }

    /**
     * 获取JTS面的外环
     *
     * @param polygon 面
     * @return 外环
     */
    public static org.locationtech.jts.geom.Geometry exteriorRing(org.locationtech.jts.geom.Polygon polygon) {
        return polygon.getExteriorRing();
    }

    /**
     * 获取JTS面的内环个数
     *
     * @param polygon 面
     * @return 内环个数
     */
    public static int numInteriorRing(org.locationtech.jts.geom.Polygon polygon) {
        return polygon.getNumInteriorRing();
    }

    /**
     * 按照索引获取JTS面的内环
     *
     * @param polygon 面
     * @param index   索引
     * @return 内环
     */
    public static org.locationtech.jts.geom.Geometry interiorRingN(org.locationtech.jts.geom.Polygon polygon, int index) {
        return polygon.getInteriorRingN(index);
    }

    /**
     * 简化JTS几何
     *
     * @param geom     JTS Geometry对象
     * @param distance 简化容差
     * @return 简化后的几何
     */
    public static org.locationtech.jts.geom.Geometry simplify(org.locationtech.jts.geom.Geometry geom, double distance) {
        return DouglasPeuckerSimplifier.simplify(geom, distance);
    }

    /**
     * 获取/创建给定JTS几何体的有效版本
     *
     * @param geom JTS几何
     * @return 几何图形
     */
    public static org.locationtech.jts.geom.Geometry validate(org.locationtech.jts.geom.Geometry geom) {
        if (geom instanceof org.locationtech.jts.geom.Polygon) {
            if (geom.isValid()) {
                geom.normalize();
                return geom;
            }
            Polygonizer polygonizer = new Polygonizer();
            addPolygon((org.locationtech.jts.geom.Polygon) geom, polygonizer);
            return toPolygonGeometry(polygonizer.getPolygons());
        } else if (geom instanceof MultiPolygon) {
            if (geom.isValid()) {
                geom.normalize();
                return geom;
            }
            Polygonizer polygonizer = new Polygonizer();
            for (int n = geom.getNumGeometries(); n-- > 0; ) {
                addPolygon((org.locationtech.jts.geom.Polygon) geom.getGeometryN(n), polygonizer);
            }
            return toPolygonGeometry(polygonizer.getPolygons());
        } else {
            return geom;
        }
    }

    /**
     * 将给定多边形中的所有字符串添加到给定多边形
     */
    private static void addPolygon(org.locationtech.jts.geom.Polygon polygon, Polygonizer polygonizer) {
        addLineString(polygon.getExteriorRing(), polygonizer);
        for (int n = polygon.getNumInteriorRing(); n-- > 0; ) {
            addLineString(polygon.getInteriorRingN(n), polygonizer);
        }
    }

    /**
     * 将给定给多边形化器的线串相加
     */
    private static void addLineString(LineString lineString, Polygonizer polygonizer) {
        if (lineString instanceof LinearRing) {
            lineString = lineString.getFactory().createLineString(lineString.getCoordinateSequence());
        }

        org.locationtech.jts.geom.Point point = lineString.getFactory().createPoint(lineString.getCoordinateN(0));
        org.locationtech.jts.geom.Geometry toAdd = lineString.union(point);

        polygonizer.add(toAdd);
    }

    /**
     * 从多边形集合中获取几何体
     */
    private static org.locationtech.jts.geom.Geometry toPolygonGeometry(Collection<org.locationtech.jts.geom.Polygon> polygons) {
        switch (polygons.size()) {
            case 0:
                return null;
            case 1:
                return polygons.iterator().next();
            default:
                Iterator<org.locationtech.jts.geom.Polygon> iter = polygons.iterator();
                org.locationtech.jts.geom.Geometry ret = iter.next();
                while (iter.hasNext()) {
                    ret = ret.symDifference(iter.next());
                }
                return ret;
        }
    }

    /**
     * 增加JTS几何节点密度
     *
     * @param geom     JTS Geometry对象
     * @param distance 节点间距离
     * @return 增加节点密度后的几何
     */
    public static org.locationtech.jts.geom.Geometry densify(org.locationtech.jts.geom.Geometry geom, double distance) {
        return Densifier.densify(geom, distance);
    }

    /**
     * 多边形化JTS几何
     *
     * @param geom JTS Geometry对象
     * @return 多边形化后的几何
     */
    public static org.locationtech.jts.geom.Geometry polygonize(org.locationtech.jts.geom.Geometry geom) {
        List lines = LineStringExtracter.getLines(geom);
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(lines);
        Collection polys = polygonizer.getPolygons();
        org.locationtech.jts.geom.Polygon[] polyArray = org.locationtech.jts.geom.GeometryFactory.toPolygonArray(polys);
        return geom.getFactory().createGeometryCollection(polyArray);
    }

    /**
     * 按照给定线切割JTS多边形
     *
     * @param polygon 多边形
     * @param line    线
     * @return 切割后的几何
     */
    public static org.locationtech.jts.geom.Geometry splitPolygon(org.locationtech.jts.geom.Geometry polygon, LineString line) {
        org.locationtech.jts.geom.Geometry nodedLinework = polygon.getBoundary().union(line);
        org.locationtech.jts.geom.Geometry polys = polygonize(nodedLinework);

        List<org.locationtech.jts.geom.Polygon> output = new ArrayList<>();
        for (int i = 0; i < polys.getNumGeometries(); i++) {
            org.locationtech.jts.geom.Polygon candpoly = (org.locationtech.jts.geom.Polygon) polys.getGeometryN(i);
            if (polygon.contains(candpoly.getInteriorPoint())) {
                output.add(candpoly);
            }
        }
        return polygon.getFactory()
                .createGeometryCollection(org.locationtech.jts.geom.GeometryFactory.toGeometryArray(output));
    }

    // ==================== ESRI几何属性查询方法（WKT版本） ====================

    /**
     * 几何是否为空（WKT版本）
     *
     * @param wkt WKT格式的字符串
     * @return 是否为空
     */
    public static boolean isEmptyWkt(String wkt) {
        com.esri.core.geometry.Geometry geom = createEsriGeometryByWkt(wkt);
        return geom.isEmpty();
    }

    /**
     * 获取几何的长度（WKT版本）
     *
     * @param wkt WKT格式的字符串
     * @return 长度
     */
    public static double lengthWkt(String wkt) {
        com.esri.core.geometry.Geometry geom = createEsriGeometryByWkt(wkt);
        return geom.calculateLength2D();
    }

    /**
     * 几何是否相交（WKT版本）
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否相交
     */
    public static boolean intersectsWkt(String awkt, String bwkt, Integer wkid) {
        com.esri.core.geometry.Geometry a = createEsriGeometryByWkt(awkt);
        com.esri.core.geometry.Geometry b = createEsriGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorIntersects.local().execute(a, b, sr, null);
    }

    /**
     * 获取几何类型（WKT版本）
     *
     * @param wkt WKT格式的字符串
     * @return 几何类型
     */
    public static GeometryType geometryTypeWkt(String wkt) {
        com.esri.core.geometry.Geometry geom = createEsriGeometryByWkt(wkt);
        return GeometryType.valueOf(geom.getType().name().toUpperCase());
    }

    /**
     * 几何是否简单（WKT版本）
     *
     * @param wkt  WKT格式的字符串
     * @param wkid 坐标系
     * @return 是否简单
     */
    public static boolean isSimpleWkt(String wkt, Integer wkid) {
        com.esri.core.geometry.Geometry geom = createEsriGeometryByWkt(wkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorSimplifyOGC.local().isSimpleOGC(geom, sr, false, null, null);
    }

    /**
     * 计算几何的距离（WKT版本）
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 距离
     */
    public static double distanceWkt(String awkt, String bwkt, Integer wkid) {
        com.esri.core.geometry.Geometry a = createEsriGeometryByWkt(awkt);
        com.esri.core.geometry.Geometry b = createEsriGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return GeometryEngine.distance(a, b, sr);
    }

    /**
     * 计算几何的面积（WKT版本）
     *
     * @param wkt WKT格式的字符串
     * @return 面积
     */
    public static double areaWkt(String wkt) {
        com.esri.core.geometry.Geometry geom = createEsriGeometryByWkt(wkt);
        return geom.calculateArea2D();
    }

    /**
     * 计算几何的中心点（WKT版本）
     *
     * @param wkt WKT格式的字符串
     * @return 中心点的WKT格式字符串
     */
    public static String centroidWkt(String wkt) {
        com.esri.core.geometry.Geometry geom = createEsriGeometryByWkt(wkt);
        Point2D point2D = OperatorCentroid2D.local().execute(geom, null);
        com.esri.core.geometry.Point point = new com.esri.core.geometry.Point(point2D.x, point2D.y);
        return toWkt(point);
    }

    /**
     * 计算几何的维度（WKT版本）
     *
     * @param wkt WKT格式的字符串
     * @return 维度
     */
    public static int dimensionWkt(String wkt) {
        com.esri.core.geometry.Geometry geom = createEsriGeometryByWkt(wkt);
        return geom.getDimension();
    }

    /**
     * 计算几何的边界（WKT版本）
     *
     * @param wkt WKT格式的字符串
     * @return 边界的WKT格式字符串
     */
    public static String boundaryWkt(String wkt) {
        com.esri.core.geometry.Geometry geom = createEsriGeometryByWkt(wkt);
        com.esri.core.geometry.Geometry boundary = OperatorBoundary.local().execute(geom, null);
        return toWkt(boundary);
    }

    /**
     * 判断几何是否相离（WKT版本）
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否相离
     */
    public static boolean disjointWkt(String awkt, String bwkt, Integer wkid) {
        com.esri.core.geometry.Geometry a = createEsriGeometryByWkt(awkt);
        com.esri.core.geometry.Geometry b = createEsriGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorDisjoint.local().execute(a, b, sr, null);
    }

    /**
     * 判断几何是否接触（WKT版本）
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否接触
     */
    public static boolean touchesWkt(String awkt, String bwkt, Integer wkid) {
        com.esri.core.geometry.Geometry a = createEsriGeometryByWkt(awkt);
        com.esri.core.geometry.Geometry b = createEsriGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorTouches.local().execute(a, b, sr, null);
    }

    /**
     * 判断几何是否交叉（WKT版本）
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否交叉
     */
    public static boolean crossesWkt(String awkt, String bwkt, Integer wkid) {
        com.esri.core.geometry.Geometry a = createEsriGeometryByWkt(awkt);
        com.esri.core.geometry.Geometry b = createEsriGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorCrosses.local().execute(a, b, sr, null);
    }

    /**
     * 判断几何A是否包含几何B（WKT版本）
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否包含
     */
    public static boolean containsWkt(String awkt, String bwkt, Integer wkid) {
        com.esri.core.geometry.Geometry a = createEsriGeometryByWkt(awkt);
        com.esri.core.geometry.Geometry b = createEsriGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorContains.local().execute(a, b, sr, null);
    }

    /**
     * 判断几何A是否在几何B内部（WKT版本）
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否在内部
     */
    public static boolean withinWkt(String awkt, String bwkt, Integer wkid) {
        com.esri.core.geometry.Geometry a = createEsriGeometryByWkt(awkt);
        com.esri.core.geometry.Geometry b = createEsriGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorWithin.local().execute(a, b, sr, null);
    }

    /**
     * 判断几何是否重叠（WKT版本）
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否有重叠
     */
    public static boolean overlapsWkt(String awkt, String bwkt, Integer wkid) {
        com.esri.core.geometry.Geometry a = createEsriGeometryByWkt(awkt);
        com.esri.core.geometry.Geometry b = createEsriGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorOverlaps.local().execute(a, b, sr, null);
    }

    /**
     * 判断几何是否符合给定关系（WKT版本）
     *
     * @param awkt    A的WKT格式字符串
     * @param bwkt    B的WKT格式字符串
     * @param wkid    坐标系
     * @param pattern 关系模式
     * @return 是否符合关系
     */
    public static boolean relatePatternWkt(String awkt, String bwkt, Integer wkid, String pattern) {
        com.esri.core.geometry.Geometry a = createEsriGeometryByWkt(awkt);
        com.esri.core.geometry.Geometry b = createEsriGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorRelate.local().execute(a, b, sr, pattern, null);
    }

    /**
     * 计算几何的缓冲区（WKT版本）
     *
     * @param wkt      WKT格式字符串
     * @param wkid     坐标系
     * @param distance 缓冲区距离
     * @return 缓冲区的WKT格式字符串
     */
    public static String bufferWkt(String wkt, Integer wkid, double distance) {
        com.esri.core.geometry.Geometry geom = createEsriGeometryByWkt(wkt);
        SpatialReference sr = SpatialReference.create(wkid);
        com.esri.core.geometry.Geometry buffer = OperatorBuffer.local().execute(geom, sr, distance, null);
        return toWkt(buffer);
    }

    /**
     * 计算几何的凸包（WKT版本）
     *
     * @param wkt WKT格式字符串
     * @return 凸包的WKT格式字符串
     */
    public static String convexHullWkt(String wkt) {
        com.esri.core.geometry.Geometry geom = createEsriGeometryByWkt(wkt);
        com.esri.core.geometry.Geometry convexHull = OperatorConvexHull.local().execute(geom, null);
        return toWkt(convexHull);
    }

    /**
     * 计算几何的交集（WKT版本）
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 交集的WKT格式字符串
     */
    public static String intersectionWkt(String awkt, String bwkt, Integer wkid) {
        com.esri.core.geometry.Geometry a = createEsriGeometryByWkt(awkt);
        com.esri.core.geometry.Geometry b = createEsriGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        com.esri.core.geometry.Geometry intersection = OperatorIntersection.local().execute(a, b, sr, null);
        return toWkt(intersection);
    }

    /**
     * 计算几何的并集（WKT版本）
     *
     * @param wkts WKT格式字符串列表
     * @param wkid 坐标系
     * @return 并集的WKT格式字符串
     */
    public static String unionWkt(List<String> wkts, Integer wkid) {
        com.esri.core.geometry.Geometry[] geoms = wkts.stream().map(GeometryUtil::createEsriGeometryByWkt).toArray(com.esri.core.geometry.Geometry[]::new);
        SpatialReference sr = SpatialReference.create(wkid);
        com.esri.core.geometry.Geometry union = GeometryEngine.union(geoms, sr);
        return toWkt(union);
    }

    /**
     * 获取A与B并集擦除B的部分（WKT版本）
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 差集的WKT格式字符串
     */
    public static String differenceWkt(String awkt, String bwkt, Integer wkid) {
        com.esri.core.geometry.Geometry a = createEsriGeometryByWkt(awkt);
        com.esri.core.geometry.Geometry b = createEsriGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        com.esri.core.geometry.Geometry difference = OperatorDifference.local().execute(a, b, sr, null);
        return toWkt(difference);
    }

    /**
     * 获取A与B并集减去A与B交集（WKT版本）
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 对称差的WKT格式字符串
     */
    public static String symDifferenceWkt(String awkt, String bwkt, Integer wkid) {
        com.esri.core.geometry.Geometry a = createEsriGeometryByWkt(awkt);
        com.esri.core.geometry.Geometry b = createEsriGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        com.esri.core.geometry.Geometry symDifference = OperatorSymmetricDifference.local().execute(a, b, sr, null);
        return toWkt(symDifference);
    }

    /**
     * 判断几何是否相等（WKT版本）
     *
     * @param awkt A的WKT格式字符串
     * @param bwkt B的WKT格式字符串
     * @param wkid 坐标系
     * @return 是否相等
     */
    public static boolean equalsWkt(String awkt, String bwkt, Integer wkid) {
        com.esri.core.geometry.Geometry a = createEsriGeometryByWkt(awkt);
        com.esri.core.geometry.Geometry b = createEsriGeometryByWkt(bwkt);
        SpatialReference sr = SpatialReference.create(wkid);
        return OperatorEquals.local().execute(a, b, sr, null);
    }

    /**
     * 简化几何（WKT版本）
     *
     * @param wkt  WKT格式字符串
     * @param wkid 坐标系
     * @return 简化后的WKT格式字符串
     */
    public static String simplifyWkt(String wkt, Integer wkid) {
        com.esri.core.geometry.Geometry geom = createEsriGeometryByWkt(wkt);
        SpatialReference sr = SpatialReference.create(wkid);
        com.esri.core.geometry.Geometry simplified = OperatorSimplifyOGC.local().execute(geom, sr, false, null);
        return toWkt(simplified);
    }
}
