/**
 * 引擎相关工具类包。
 * <p>
 * 提供与GIS引擎相关的工具类，包括坐标系处理、Shapefile操作、PostGIS访问、GDAL操作等。
 * </p>
 *
 * <h2>工具类</h2>
 * <ul>
 *   <li>{@link com.znlgis.ogu4j.engine.util.CrsUtil} - 坐标参考系工具类</li>
 *   <li>{@link com.znlgis.ogu4j.engine.util.ShpUtil} - Shapefile工具类</li>
 *   <li>{@link com.znlgis.ogu4j.engine.util.PostgisUtil} - PostGIS数据库工具类</li>
 *   <li>{@link com.znlgis.ogu4j.engine.util.GeotoolsUtil} - GeoTools工具类</li>
 *   <li>{@link com.znlgis.ogu4j.engine.util.OgrUtil} - GDAL/OGR工具类</li>
 *   <li>{@link com.znlgis.ogu4j.engine.util.GdalCmdUtil} - GDAL命令行工具类</li>
 * </ul>
 *
 * <h2>坐标系支持</h2>
 * <p>
 * CrsUtil默认支持EPSG:4490-4554范围内的坐标系（中国2000国家大地坐标系及其投影），
 * 提供坐标转换、带号计算、投影判断等功能。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
package com.znlgis.ogu4j.engine.util;
