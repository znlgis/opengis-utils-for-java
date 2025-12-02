package com.znlgis.ogu4j.engine.util;

import cn.hutool.core.util.ObjectUtil;
import com.znlgis.ogu4j.geometry.GeometryUtil;
import com.znlgis.ogu4j.engine.model.layer.OguFeature;
import com.znlgis.ogu4j.engine.model.layer.OguLayer;
import lombok.SneakyThrows;
import org.geotools.data.crs.ForceCoordinateSystemFeatureResults;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.parameter.GeneralParameterValue;
import org.geotools.api.parameter.ParameterValueGroup;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.crs.GeographicCRS;
import org.geotools.api.referencing.crs.ProjectedCRS;
import org.geotools.api.referencing.datum.Ellipsoid;
import org.geotools.api.referencing.operation.MathTransform;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 坐标参考系（CRS）工具类
 * <p>
 * 提供坐标参考系的获取、转换、判断和几何对象的坐标转换功能。
 * 默认支持EPSG:4490-4554范围内的坐标系（中国2000国家大地坐标系及其投影）。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
public class CrsUtil {
    private static Map<Integer, CoordinateReferenceSystem> supportedCRSList;

    private CrsUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取支持的投影坐标系列表
     *
     * @return 支持的投影坐标系列表，KEY为WKID，VALUE为坐标系
     */
    @SneakyThrows
    private static Map<Integer, CoordinateReferenceSystem> supportedCRSList() {
        if (supportedCRSList != null && !supportedCRSList.isEmpty()) {
            return supportedCRSList;
        }

        supportedCRSList = new HashMap<>();
        for (int i = 4490; i < 4555; i++) {
            supportedCRSList.put(i, CRS.decode("EPSG:" + i, true));
        }
        return supportedCRSList;
    }

    /**
     * (只读，Map不可修改)获取支持的投影坐标系列表
     *
     * @return 支持的投影坐标系列表，KEY为WKID，VALUE为坐标系
     */
    public static Map<Integer, CoordinateReferenceSystem> getSupportedCRSList() {
        return Collections.unmodifiableMap(supportedCRSList());
    }

    /**
     * 获取被支持的标准的EPSG Code和对应坐标系信息，如果不存在则增加
     *
     * @param wkid 坐标系WKID
     * @return Key为EPSG Code，Value为对应坐标系信息
     */
    @SneakyThrows
    public static Map.Entry<Integer, CoordinateReferenceSystem> getSupportedCRS(Integer wkid) {
        if (!supportedCRSList().containsKey(wkid)) {
            CoordinateReferenceSystem crs = CRS.decode("EPSG:" + wkid, true);
            supportedCRSList().put(wkid, crs);
            return new HashMap.SimpleEntry<>(wkid, crs);
        }

        return new HashMap.SimpleEntry<>(wkid, supportedCRSList().get(wkid));
    }

    /**
     * 在被支持的坐标系范围内获取符合GeoTools标准的EPSG Code和对应坐标系信息
     *
     * @param crs 坐标系
     * @return Key为EPSG Code，Value为对应坐标系信息
     */
    public static Map.Entry<Integer, CoordinateReferenceSystem> standardizeCRS(CoordinateReferenceSystem crs) {
        for (Map.Entry<Integer, CoordinateReferenceSystem> entry : supportedCRSList().entrySet()) {
            if (isSameCRS(crs, entry.getValue())) {
                return entry;
            }
        }

        throw new RuntimeException("不支持的坐标系");
    }

    /**
     * 在被支持的坐标系范围内获取符合GeoTools标准的EPSG Code和对应坐标系信息
     *
     * @param wkt 坐标系WKT
     * @return Key为EPSG Code，Value为对应坐标系信息
     */
    @SneakyThrows
    public static Map.Entry<Integer, CoordinateReferenceSystem> standardizeCRS(String wkt) {
        CoordinateReferenceSystem crs = CRS.parseWKT(wkt);
        return standardizeCRS(crs);
    }

    /**
     * 判断坐标系是否为投影坐标系
     *
     * @param crs 坐标系
     * @return 是否为投影坐标系
     */
    public static boolean isProjectedCRS(CoordinateReferenceSystem crs) {
        Map.Entry<Integer, CoordinateReferenceSystem> entry = standardizeCRS(crs);
        return entry.getValue() instanceof ProjectedCRS;
    }

    /**
     * 获取坐标系容差
     *
     * @param wkid WKID
     * @return 容差
     */
    public static double getTolerance(Integer wkid) {
        Map.Entry<Integer, CoordinateReferenceSystem> entry = getSupportedCRS(wkid);
        return getTolerance(entry.getValue());
    }

    /**
     * 获取坐标系容差
     *
     * @param crs 坐标系
     * @return 容差
     */
    public static double getTolerance(CoordinateReferenceSystem crs) {
        if (isProjectedCRS(crs)) {
            return 0.0001;
        } else {
            return 0.000000001;
        }
    }

    /**
     * 根据WKT字符串获取几何所在带号
     *
     * @param wkt WKT格式字符串
     * @return 所在带号
     */
    public static int getDh(String wkt) {
        Geometry geom = GeometryUtil.wkt2Geometry(wkt);
        return getDh(geom);
    }

    /**
     * 获取几何所在带号
     *
     * @param geometry 几何对象
     * @return 所在带号
     */
    public static int getDh(Geometry geometry) {
        Point point = geometry.getCentroid();
        int dh = 0;
        if (point.getX() < 180) {
            dh = (int) ((point.getX() + 1.5) / 3);
        } else if (point.getX() / 10000000 > 3) {
            dh = (int) (point.getX() / 1000000);
        }

        return dh;
    }

    /**
     * 获取几何所在带号
     *
     * @param projectedWkid 投影坐标系WKID
     * @return 所在带号
     */
    public static int getDh(int projectedWkid) {
        return projectedWkid - 4488;
    }

    /**
     * 获取几何WKID
     *
     * @param geometry 几何对象
     * @return WKID，如果是经纬度则返回4490，否则返回投影坐标系WKID
     */
    public static Integer getWkid(Geometry geometry) {
        Point point = geometry.getCentroid();
        if (point.getX() < 180) {
            return 4490;
        } else {
            return getProjectedWkid(getDh(geometry));
        }
    }

    /**
     * 获取投影坐标系WKID
     *
     * @param dh 带号
     * @return WKID
     */
    public static Integer getProjectedWkid(int dh) {
        return 4488 + dh;
    }

    /**
     * 获取投影坐标系WKID
     *
     * @param geometry 几何对象
     * @return WKID
     */
    public static Integer getProjectedWkid(Geometry geometry) {
        return getProjectedWkid(getDh(geometry));
    }

    /**
     * WKT格式字符串的坐标转换
     *
     * @param wkt        WKT格式字符串
     * @param sourceWkid 源坐标系WKID
     * @param targetWkid 目标坐标系WKID
     * @return 转换后的WKT格式字符串，如果转换失败返回null
     */
    public static String transform(String wkt, Integer sourceWkid, Integer targetWkid) {
        Geometry geom = GeometryUtil.wkt2Geometry(wkt);
        geom = transform(geom, sourceWkid, targetWkid);
        return geom == null ? null : geom.toText();
    }

    /**
     * 几何对象转换坐标系
     *
     * @param geometry   几何对象
     * @param sourceWkid 源坐标系WKID
     * @param targetWkid 目标坐标系WKID
     * @return 坐标转换后的几何对象
     */
    public static Geometry transform(Geometry geometry, Integer sourceWkid, Integer targetWkid) {
        if (sourceWkid.equals(targetWkid)) {
            return geometry;
        }

        CoordinateReferenceSystem sourceCRS = getSupportedCRS(sourceWkid).getValue();
        CoordinateReferenceSystem targetCRS = getSupportedCRS(targetWkid).getValue();
        return transform(geometry, sourceCRS, targetCRS);
    }

    /**
     * 几何对象转换坐标系
     *
     * @param geometry  几何对象
     * @param sourceCRS 源坐标系
     * @param targetCRS 目标坐标系
     * @return 坐标转换后的几何对象
     */
    @SneakyThrows
    public static Geometry transform(Geometry geometry, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) {
        if (isSameCRS(sourceCRS, targetCRS)) {
            return geometry;
        }

        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
        return JTS.transform(geometry, transform);
    }

    /**
     * 转换坐标系
     *
     * @param oguLayer   OGU图层
     * @param targetWkid 目标坐标系WKID
     * @return 转换后的OGU图层
     */
    @SneakyThrows
    public static OguLayer reproject(OguLayer oguLayer, Integer targetWkid) {
        oguLayer.validate();

        OguLayer clone = ObjectUtil.cloneByStream(oguLayer);
        if (clone.getWkid().equals(targetWkid)) {
            return clone;
        }

        for (OguFeature feature : clone.getFeatures()) {
            Geometry geometry = new WKTReader().read(feature.getGeometry());
            Geometry targetGeometry = transform(geometry, oguLayer.getWkid(), targetWkid);
            feature.setGeometry(targetGeometry.toText());
        }

        clone.setWkid(targetWkid);
        clone.setTolerance(getTolerance(targetWkid));
        return clone;
    }

    /**
     * 转换坐标系
     *
     * @param featureCollection 要素集合
     * @param targetWkid        目标坐标系WKID
     * @return 转换后的要素集合
     */
    @SneakyThrows
    public static FeatureCollection<SimpleFeatureType, SimpleFeature> reproject(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection, Integer targetWkid) {
        CoordinateReferenceSystem sourceCRS = standardizeCRS(
                featureCollection.getSchema().getCoordinateReferenceSystem()).getValue();
        CoordinateReferenceSystem targetCRS = getSupportedCRS(targetWkid).getValue();
        if (isSameCRS(sourceCRS, targetCRS)) {
            return featureCollection;
        }

        if (sourceCRS != null) {
            featureCollection = new ForceCoordinateSystemFeatureResults(featureCollection, sourceCRS, false);
        }
        if (targetCRS != null) {
            featureCollection = new ReprojectingFeatureCollection(featureCollection, targetCRS);
        }

        return featureCollection;
    }

    /**
     * 判断坐标系是否相同
     *
     * @param sourceCrs 源坐标系
     * @param targetCrs 目标坐标系
     * @return 是否相同
     */
    public static boolean isSameCRS(CoordinateReferenceSystem sourceCrs, CoordinateReferenceSystem targetCrs) {
        if (sourceCrs instanceof ProjectedCRS sourceProjectedCRS && targetCrs instanceof ProjectedCRS targetProjectedCRS) {
            boolean isSameBaseCRS = isSameCRS(sourceProjectedCRS.getBaseCRS(), targetProjectedCRS.getBaseCRS());
            boolean isSameConversionFromBase = true;
            ParameterValueGroup sourceParameterValueGroup = sourceProjectedCRS.getConversionFromBase().getParameterValues();
            ParameterValueGroup targetParameterValueGroup = targetProjectedCRS.getConversionFromBase().getParameterValues();
            for (int i = 0; i < sourceParameterValueGroup.values().size(); i++) {
                GeneralParameterValue s = sourceParameterValueGroup.values().get(i);
                GeneralParameterValue t = targetParameterValueGroup.values().get(i);
                if (!s.equals(t)) {
                    isSameConversionFromBase = false;
                    break;
                }
            }
            return isSameBaseCRS && isSameConversionFromBase;
        } else if (sourceCrs instanceof GeographicCRS sourceGeographicCRS && targetCrs instanceof GeographicCRS targetGeographicCRS) {
            Ellipsoid s = sourceGeographicCRS.getDatum().getEllipsoid();
            Ellipsoid t = targetGeographicCRS.getDatum().getEllipsoid();
            return s.getSemiMajorAxis() == t.getSemiMajorAxis()
                    && s.getSemiMinorAxis() == t.getSemiMinorAxis()
                    && s.getInverseFlattening() == t.getInverseFlattening();
        }

        return false;
    }
}
