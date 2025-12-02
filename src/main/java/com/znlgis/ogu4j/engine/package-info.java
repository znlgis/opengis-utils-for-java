/**
 * GIS引擎核心包。
 * <p>
 * 提供GIS引擎的接口定义和实现，支持GeoTools和GDAL两种引擎。
 * 采用策略模式和工厂模式，实现引擎的解耦和灵活切换。
 * </p>
 *
 * <h2>核心类</h2>
 * <ul>
 *   <li>{@link com.znlgis.ogu4j.engine.GisEngine} - GIS引擎抽象接口</li>
 *   <li>{@link com.znlgis.ogu4j.engine.GisEngineFactory} - 引擎工厂类</li>
 *   <li>{@link com.znlgis.ogu4j.engine.GeoToolsEngine} - GeoTools引擎实现</li>
 *   <li>{@link com.znlgis.ogu4j.engine.GdalEngine} - GDAL/OGR引擎实现</li>
 * </ul>
 *
 * <h2>读写器</h2>
 * <ul>
 *   <li>{@link com.znlgis.ogu4j.engine.GeoToolsLayerReader} - GeoTools图层读取器</li>
 *   <li>{@link com.znlgis.ogu4j.engine.GeoToolsLayerWriter} - GeoTools图层写入器</li>
 *   <li>{@link com.znlgis.ogu4j.engine.GdalLayerReader} - GDAL图层读取器</li>
 *   <li>{@link com.znlgis.ogu4j.engine.GdalLayerWriter} - GDAL图层写入器</li>
 * </ul>
 *
 * <h2>子包</h2>
 * <ul>
 *   <li>{@link com.znlgis.ogu4j.engine.enums} - 枚举类型定义</li>
 *   <li>{@link com.znlgis.ogu4j.engine.io} - 读写器接口定义</li>
 *   <li>{@link com.znlgis.ogu4j.engine.model} - 数据模型定义</li>
 *   <li>{@link com.znlgis.ogu4j.engine.util} - 引擎相关工具类</li>
 * </ul>
 *
 * <h2>设计模式</h2>
 * <ul>
 *   <li><b>策略模式</b> - LayerReader/LayerWriter接口允许不同格式使用不同策略</li>
 *   <li><b>工厂模式</b> - GisEngineFactory负责创建和管理引擎实例</li>
 *   <li><b>适配器模式</b> - GisEngine接口隔离底层GIS库的差异</li>
 * </ul>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
package com.znlgis.ogu4j.engine;
