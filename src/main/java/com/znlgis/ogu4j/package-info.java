/**
 * OGU4J - OpenGIS Utils for Java 核心包。
 * <p>
 * OGU4J是一个基于开源GIS库（GeoTools、JTS、GDAL/OGR、ESRI Geometry API）的Java GIS二次开发工具库。
 * 它提供了统一的图层模型和便捷的格式转换功能，简化了GIS数据的读取、处理和导出操作。
 * </p>
 *
 * <h2>主要特性</h2>
 * <ul>
 *   <li><b>统一图层模型</b>：提供简洁的图层、要素、字段抽象，屏蔽底层GIS库差异</li>
 *   <li><b>多格式支持</b>：支持Shapefile、GeoJSON、FileGDB、PostGIS、国土TXT坐标文件等格式</li>
 *   <li><b>双引擎架构</b>：支持GeoTools和GDAL/OGR两种引擎，可根据需求灵活切换</li>
 *   <li><b>几何处理</b>：基于JTS和ESRI Geometry API提供丰富的几何操作和空间分析功能</li>
 *   <li><b>坐标系管理</b>：内置CGCS2000坐标系支持，提供坐标转换功能</li>
 *   <li><b>实用工具</b>：提供ZIP压缩/解压、文件编码检测、自然排序等实用工具</li>
 * </ul>
 *
 * <h2>子包说明</h2>
 * <ul>
 *   <li>{@link com.znlgis.ogu4j.engine} - GIS引擎接口和实现</li>
 *   <li>{@link com.znlgis.ogu4j.geometry} - 几何处理工具</li>
 *   <li>{@link com.znlgis.ogu4j.datasource} - 数据源读写工具</li>
 *   <li>{@link com.znlgis.ogu4j.utils} - 通用工具类</li>
 *   <li>{@link com.znlgis.ogu4j.exception} - 自定义异常类</li>
 * </ul>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
package com.znlgis.ogu4j;
