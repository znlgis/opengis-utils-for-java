package com.znlgis.ogu4j.geometry;

import lombok.SneakyThrows;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.geometry.jts.WKTWriter2;
import org.locationtech.jts.geom.Geometry;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * 几何格式转换工具类
 * <p>
 * 提供JTS Geometry与各种几何格式（WKT、GeoJSON、ESRI JSON）之间的相互转换功能。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 */
public class GeometryConverter {
    private GeometryConverter() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== WKT转换方法 ====================

    /**
     * WKT转JTS Geometry
     *
     * @param wkt WKT格式的字符串
     * @return JTS Geometry对象
     */
    @SneakyThrows
    public static Geometry wkt2Geometry(String wkt) {
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
        Geometry geometry = wkt2Geometry(wkt);
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
        com.esri.core.geometry.Geometry geometry = EsriGeometryUtil.createGeometryByWkt(wkt);
        return EsriGeometryUtil.toEsriJson(wkid, geometry);
    }

    // ==================== GeoJSON转换方法 ====================

    /**
     * GeoJSON转JTS Geometry
     *
     * @param geojson GeoJSON格式的字符串
     * @return JTS Geometry对象
     */
    @SneakyThrows
    public static Geometry geojson2Geometry(String geojson) {
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
        Geometry geometry = geojson2Geometry(geojson);
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
        com.esri.core.geometry.Geometry geometry = EsriGeometryUtil.createGeometryByGeoJson(geojson);
        return EsriGeometryUtil.toEsriJson(wkid, geometry);
    }

    // ==================== JTS Geometry转换方法 ====================

    /**
     * JTS Geometry转WKT
     *
     * @param geometry JTS Geometry对象
     * @return WKT格式的字符串
     */
    @SneakyThrows
    public static String geometry2Wkt(Geometry geometry) {
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
    public static String geometry2Geojson(Geometry geometry) {
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
    public static String geometry2EsriJson(Geometry geometry, int wkid) {
        WKTWriter2 writer = new WKTWriter2();
        String wkt = writer.write(geometry);
        return wkt2EsriJson(wkt, wkid);
    }

    // ==================== ESRI JSON转换方法 ====================

    /**
     * ESRI JSON转WKT
     *
     * @param esrijson ESRI JSON格式的字符串
     * @return WKT格式的字符串
     */
    public static String esriJson2Wkt(String esrijson) {
        com.esri.core.geometry.Geometry geometry = EsriGeometryUtil.createGeometryByJson(esrijson);
        return EsriGeometryUtil.toWkt(geometry);
    }

    /**
     * ESRI JSON转GeoJSON
     *
     * @param esrijson ESRI JSON格式的字符串
     * @return GeoJSON格式的字符串
     */
    public static String esriJson2GeoJson(String esrijson) {
        com.esri.core.geometry.Geometry geometry = EsriGeometryUtil.createGeometryByJson(esrijson);
        return EsriGeometryUtil.toGeoJson(geometry);
    }

    /**
     * ESRI JSON转JTS Geometry
     *
     * @param esrijson ESRI JSON格式的字符串
     * @return JTS Geometry对象
     */
    public static Geometry esriJson2Geometry(String esrijson) {
        com.esri.core.geometry.Geometry geometry = EsriGeometryUtil.createGeometryByJson(esrijson);
        String wkt = EsriGeometryUtil.toWkt(geometry);
        return wkt2Geometry(wkt);
    }
}
