/**
 * 图层读写器接口包。
 * <p>
 * 定义图层读取和写入的标准接口，采用策略模式允许不同数据格式使用不同的实现。
 * </p>
 *
 * <h2>接口定义</h2>
 * <ul>
 *   <li>{@link com.znlgis.ogu4j.engine.io.LayerReader} - 图层读取器接口</li>
 *   <li>{@link com.znlgis.ogu4j.engine.io.LayerWriter} - 图层写入器接口</li>
 * </ul>
 *
 * <h2>功能说明</h2>
 * <ul>
 *   <li>支持属性过滤和空间过滤</li>
 *   <li>支持多种数据格式的读写</li>
 *   <li>采用策略模式，便于扩展新格式支持</li>
 * </ul>
 *
 * <h2>实现类</h2>
 * <ul>
 *   <li>{@link com.znlgis.ogu4j.engine.GeoToolsLayerReader} - GeoTools读取器实现</li>
 *   <li>{@link com.znlgis.ogu4j.engine.GeoToolsLayerWriter} - GeoTools写入器实现</li>
 *   <li>{@link com.znlgis.ogu4j.engine.GdalLayerReader} - GDAL读取器实现</li>
 *   <li>{@link com.znlgis.ogu4j.engine.GdalLayerWriter} - GDAL写入器实现</li>
 * </ul>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
package com.znlgis.ogu4j.engine.io;
