package com.znlgis.ogu4j.geometry;

import com.znlgis.ogu4j.enums.GeometryType;
import com.znlgis.ogu4j.enums.TopologyValidationErrorType;
import com.znlgis.ogu4j.model.SimpleGeometryResult;
import com.znlgis.ogu4j.model.TopologyValidationResult;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.algorithm.hull.ConcaveHull;
import org.locationtech.jts.densify.Densifier;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.valid.IsSimpleOp;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * JTS几何处理工具类
 * <p>
 * 提供基于JTS（Java Topology Suite）的几何属性查询、空间关系判断、空间分析和几何处理功能。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 */
public class JtsGeometryUtil {
    private JtsGeometryUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 判断几何是否为空
     *
     * @param geom geometry
     * @return true代表为空，false代表不为空
     */
    public static boolean isEmpty(Geometry geom) {
        return geom.isEmpty();
    }

    /**
     * 获取几何长度
     *
     * @param geom geometry
     * @return 长度
     */
    public static double length(Geometry geom) {
        return geom.getLength();
    }

    /**
     * 判断几何是否相交
     *
     * @param a geometry
     * @param b geometry
     * @return true代表相交，false代表不相交
     */
    public static boolean intersects(Geometry a, Geometry b) {
        return a.intersects(b);
    }

    /**
     * 判断几何拓扑是否合法
     *
     * @param geom geometry
     * @return 拓扑错误模型
     */
    public static TopologyValidationResult isValid(Geometry geom) {
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
     * 获取几何类型
     *
     * @param geom geometry
     * @return 几何类型
     */
    public static GeometryType geometryType(Geometry geom) {
        return GeometryType.valueOfByTypeName(geom.getGeometryType());
    }

    /**
     * 获取节点个数
     *
     * @param geom geometry
     * @return 节点个数
     */
    public static int numPoints(Geometry geom) {
        return geom.getNumPoints();
    }

    /**
     * 判断几何是否简单几何
     *
     * @param geom geometry
     * @return true代表简单几何，false代表不是简单几何
     */
    public static SimpleGeometryResult isSimple(Geometry geom) {
        IsSimpleOp isSimpleOp = new IsSimpleOp(geom);
        isSimpleOp.setFindAllLocations(true);
        if (!isSimpleOp.isSimple()) {
            return new SimpleGeometryResult(false, isSimpleOp.getNonSimpleLocations());
        }

        return new SimpleGeometryResult(true, null);
    }

    /**
     * 计算几何之间的距离，不论点线面都是几何间最短距离
     *
     * @param a geometry
     * @param b geometry
     * @return 距离
     */
    public static double distance(Geometry a, Geometry b) {
        return a.distance(b);
    }

    /**
     * 判断几何间最短距离是否小于给定距离
     *
     * @param a        geometry
     * @param b        geometry
     * @param distance 给定距离
     * @return true代表小于给定距离，false代表大于给定距离
     */
    public static boolean isWithinDistance(Geometry a, Geometry b, double distance) {
        return a.isWithinDistance(b, distance);
    }

    /**
     * 获取几何面积
     *
     * @param geom geometry
     * @return 面积
     */
    public static double area(Geometry geom) {
        return geom.getArea();
    }

    /**
     * 获取几何中心点
     *
     * @param geom geometry
     * @return 中心点
     */
    public static Geometry centroid(Geometry geom) {
        return geom.getCentroid();
    }

    /**
     * 获取几何内部中心点
     *
     * @param geom geometry
     * @return 内部中心点
     */
    public static Geometry interiorPoint(
            Geometry geom) {
        return geom.getInteriorPoint();
    }

    /**
     * 获取几何维度，点为0，线为1，面为2
     *
     * @param geom geometry
     * @return 维度
     */
    public static int dimension(Geometry geom) {
        return geom.getDimension();
    }

    /**
     * 获取几何边界
     *
     * @param geom geometry
     * @return 边界
     */
    public static Geometry boundary(Geometry geom) {
        return geom.getBoundary();
    }

    /**
     * 获取几何外包矩形
     *
     * @param geom geometry
     * @return 外包矩形
     */
    public static Geometry envelope(Geometry geom) {
        return geom.getEnvelope();
    }

    /**
     * 判断几何是否相离，一个公共点都没有
     *
     * @param a geometry
     * @param b geometry
     * @return true代表相离，false代表不相离
     */
    public static boolean disjoint(Geometry a, Geometry b) {
        return a.disjoint(b);
    }

    /**
     * 判断几何是否接触，有公共点但是没有公共区域
     *
     * @param a geometry
     * @param b geometry
     * @return true代表接触，false代表不接触
     */
    public static boolean touches(Geometry a, Geometry b) {
        return a.touches(b);
    }

    /**
     * 判断几何是否交叉，有公共区域但是不包含
     *
     * @param a geometry
     * @param b geometry
     * @return true代表相交，false代表不相交
     */
    public static boolean crosses(Geometry a, Geometry b) {
        return a.crosses(b);
    }

    /**
     * 判断几何A是否包含几何B
     *
     * @param a geometry
     * @param b geometry
     * @return true代表包含，false代表不包含
     */
    public static boolean contains(Geometry a, Geometry b) {
        return a.contains(b);
    }

    /**
     * 判断几何A是否在几何B内部
     *
     * @param a geometry
     * @param b geometry
     * @return true代表在内部，false代表不在内部
     */
    public static boolean within(Geometry a, Geometry b) {
        return a.within(b);
    }

    /**
     * 判断几何是否重叠，有公共区域且包含
     *
     * @param a geometry
     * @param b geometry
     * @return true代表重叠，false代表不重叠
     */
    public static boolean overlaps(Geometry a, Geometry b) {
        return a.overlaps(b);
    }

    /**
     * 判断几何是否符合给定关系
     *
     * @param a       geometry
     * @param b       geometry
     * @param pattern 给定关系，例如：T*T***FF*，其中T代表相交，F代表不相交，*代表不关心
     * @return true代表符合给定关系，false代表不符合给定关系
     */
    public static boolean relatePattern(Geometry a, Geometry b, String pattern) {
        return a.relate(b, pattern);
    }

    /**
     * 获取几何关系，例如：T*T***FF*
     *
     * @param a geometry
     * @param b geometry
     * @return 几何关系
     */
    public static String relate(Geometry a, Geometry b) {
        return a.relate(b).toString();
    }

    /**
     * 获取几何缓冲区
     *
     * @param geom     geometry
     * @param distance 缓冲区距离
     * @return 缓冲区
     */
    public static Geometry buffer(Geometry geom, double distance) {
        return geom.buffer(distance);
    }

    /**
     * 获取几何凸包
     *
     * @param geom geometry
     * @return 凸包
     */
    public static Geometry convexHull(Geometry geom) {
        ConvexHull ch = new ConvexHull(geom);
        return ch.getConvexHull();
    }

    /**
     * 获取几何凹包
     *
     * @param geom geometry
     * @return 凹包
     */
    public static Geometry concaveHull(Geometry geom) {
        ConcaveHull ch = new ConcaveHull(geom);
        return ch.getHull();
    }

    /**
     * 获取几何交集
     *
     * @param a geometry
     * @param b geometry
     * @return 交集
     */
    public static Geometry intersection(Geometry a, Geometry b) {
        return a.intersection(b);
    }

    /**
     * 获取几何并集
     *
     * @param geoms geometrys
     * @return 并集
     */
    public static Geometry union(Geometry... geoms) {
        Geometry result = null;
        for (Geometry g : geoms) {
            if (result == null) {
                result = g;
            } else {
                result = result.union(g);
            }
        }
        return result;
    }

    /**
     * 获取A与B并集擦除B的部分
     *
     * @param a geometry
     * @param b geometry
     * @return 差集
     */
    public static Geometry difference(Geometry a, Geometry b) {
        return a.difference(b);
    }

    /**
     * 获取A与B并集减去A与B交集
     *
     * @param a geometry
     * @param b geometry
     * @return 对称差集
     */
    public static Geometry symDifference(Geometry a, Geometry b) {
        return a.symDifference(b);
    }

    /**
     * 按照给定容差判断几何是否对象结构相等，必须有相同的节点和相同的节点顺序
     *
     * @param a         geometry
     * @param b         geometry
     * @param tolerance 容差
     * @return true代表对象结构相等，false代表对象结构不相等
     */
    public static boolean equalsExactTolerance(Geometry a, Geometry b, double tolerance) {
        return a.equalsExact(b, tolerance);
    }

    /**
     * 判断几何是否对象结构相等，必须有相同的节点和相同的节点顺序
     *
     * @param a geometry
     * @param b geometry
     * @return true代表对象结构相等，false代表对象结构不相等
     */
    public static boolean equalsExact(Geometry a, Geometry b) {
        return a.equalsExact(b);
    }

    /**
     * 判断几何是否对象结构相等，不判断节点顺序
     *
     * @param a geometry
     * @param b geometry
     * @return true代表对象结构相等，false代表对象结构不相等
     */
    public static boolean equalsNorm(Geometry a, Geometry b) {
        return a.equalsNorm(b);
    }

    /**
     * 判断几何是否拓扑相等
     *
     * @param a geometry
     * @param b geometry
     * @return true代表拓扑相等，false代表拓扑不相等
     */
    public static boolean equalsTopo(Geometry a, Geometry b) {
        return a.equalsTopo(b);
    }

    /**
     * 获取几何个数
     *
     * @param collection geometry
     * @return 个数
     */
    public static int numGeometries(Geometry collection) {
        return collection.getNumGeometries();
    }

    /**
     * 按照索引获取几何
     *
     * @param collection 几何集合
     * @param index      索引
     * @return 几何
     */
    public static Geometry getGeometryN(GeometryCollection collection, int index) {
        return collection.getGeometryN(index);
    }

    /**
     * 获取点的X坐标
     *
     * @param point 点
     * @return X坐标
     */
    public static double getX(Point point) {
        return point.getX();
    }

    /**
     * 获取点的Y坐标
     *
     * @param point 点
     * @return Y坐标
     */
    public static double getY(Point point) {
        return point.getY();
    }

    /**
     * 判断线是否闭合
     *
     * @param line 线
     * @return true代表闭合，false代表不闭合
     */
    public static boolean isClosed(LineString line) {
        return line.isClosed();
    }

    /**
     * 按照索引获取线的节点
     *
     * @param line  线
     * @param index 索引
     * @return 节点
     */
    public static Point pointN(LineString line, int index) {
        return line.getPointN(index);
    }

    /**
     * 获取线的起点
     *
     * @param line 线
     * @return 起点
     */
    public static Point startPoint(LineString line) {
        return line.getStartPoint();
    }

    /**
     * 获取线的终点
     *
     * @param line 线
     * @return 终点
     */
    public static Point endPoint(LineString line) {
        return line.getEndPoint();
    }

    /**
     * 判断线是否为环
     *
     * @param line 线
     * @return true代表为环，false代表不为环
     */
    public static boolean isRing(LineString line) {
        return line.isRing();
    }

    /**
     * 获取面的外环
     *
     * @param polygon 面
     * @return 外环
     */
    public static Geometry exteriorRing(Polygon polygon) {
        return polygon.getExteriorRing();
    }

    /**
     * 获取面的内环个数
     *
     * @param polygon 面
     * @return 内环个数
     */
    public static int numInteriorRing(Polygon polygon) {
        return polygon.getNumInteriorRing();
    }

    /**
     * 按照索引获取面的内环
     *
     * @param polygon 面
     * @param index   索引
     * @return 内环
     */
    public static Geometry interiorRingN(Polygon polygon, int index) {
        return polygon.getInteriorRingN(index);
    }

    /**
     * 简化几何
     *
     * @param geom     geometry
     * @param distance 简化容差
     * @return 简化后的几何
     */
    public static Geometry simplify(Geometry geom, double distance) {
        return DouglasPeuckerSimplifier.simplify(geom, distance);
    }

    /**
     * 获取/创建给定几何体的有效版本。如果几何体是多边形或多多边形，则自相交/不一致性得到了修复。否则将返回几何体。
     *
     * @param geom 几何
     * @return 几何图形
     */
    public static Geometry validate(Geometry geom) {
        if (geom instanceof Polygon) {
            if (geom.isValid()) {
                geom.normalize();
                return geom;
            }
            Polygonizer polygonizer = new Polygonizer();
            addPolygon((Polygon) geom, polygonizer);
            return toPolygonGeometry(polygonizer.getPolygons());
        } else if (geom instanceof MultiPolygon) {
            if (geom.isValid()) {
                geom.normalize();
                return geom;
            }
            Polygonizer polygonizer = new Polygonizer();
            for (int n = geom.getNumGeometries(); n-- > 0; ) {
                addPolygon((Polygon) geom.getGeometryN(n), polygonizer);
            }
            return toPolygonGeometry(polygonizer.getPolygons());
        } else {
            return geom;
        }
    }

    /**
     * 将给定多边形中的所有字符串添加到给定多边形
     *
     * @param polygonizer 多边形化器
     * @param polygon     从中提取字符串的多边形
     */
    private static void addPolygon(Polygon polygon, Polygonizer polygonizer) {
        addLineString(polygon.getExteriorRing(), polygonizer);
        for (int n = polygon.getNumInteriorRing(); n-- > 0; ) {
            addLineString(polygon.getInteriorRingN(n), polygonizer);
        }
    }

    /**
     * 将给定给多边形化器的线串相加
     *
     * @param lineString  线串
     * @param polygonizer 多边形化器
     */
    private static void addLineString(LineString lineString, Polygonizer polygonizer) {

        if (lineString instanceof LinearRing) {
            lineString = lineString.getFactory().createLineString(lineString.getCoordinateSequence());
        }

        Point point = lineString.getFactory().createPoint(lineString.getCoordinateN(0));
        Geometry toAdd = lineString.union(point);

        polygonizer.add(toAdd);
    }

    /**
     * 从多边形集合中获取几何体。
     *
     * @param polygons 多边形集合
     * @return 几何体
     */
    private static Geometry toPolygonGeometry(Collection<Polygon> polygons) {
        switch (polygons.size()) {
            case 0:
                return null;
            case 1:
                return polygons.iterator().next();
            default:
                Iterator<Polygon> iter = polygons.iterator();
                Geometry ret = iter.next();
                while (iter.hasNext()) {
                    ret = ret.symDifference(iter.next());
                }
                return ret;
        }
    }

    /**
     * 增加几何节点密度
     *
     * @param geom     geometry
     * @param distance 节点间距离
     * @return 增加节点密度后的几何
     */
    public static Geometry densify(Geometry geom, double distance) {
        return Densifier.densify(geom, distance);
    }

    /**
     * 多边形化几何
     *
     * @param geom geometry
     * @return 多边形化后的几何
     */
    public static Geometry polygonize(Geometry geom) {
        List lines = LineStringExtracter.getLines(geom);
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(lines);
        Collection polys = polygonizer.getPolygons();
        Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
        return geom.getFactory().createGeometryCollection(polyArray);
    }

    /**
     * 按照给定线切割多边形
     *
     * @param polygon 多边形
     * @param line    线
     * @return 切割后的几何
     */
    public static Geometry splitPolygon(Geometry polygon, LineString line) {
        Geometry nodedLinework = polygon.getBoundary().union(line);
        Geometry polys = polygonize(nodedLinework);

        List<Polygon> output = new ArrayList<>();
        for (int i = 0; i < polys.getNumGeometries(); i++) {
            Polygon candpoly = (Polygon) polys.getGeometryN(i);
            if (polygon.contains(candpoly.getInteriorPoint())) {
                output.add(candpoly);
            }
        }
        return polygon.getFactory()
                .createGeometryCollection(GeometryFactory.toGeometryArray(output));
    }
}
