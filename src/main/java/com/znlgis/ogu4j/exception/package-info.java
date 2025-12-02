/**
 * 自定义异常类包。
 * <p>
 * 定义OGU4J中使用的自定义异常类，提供统一的异常处理机制。
 * </p>
 *
 * <h2>异常类</h2>
 * <ul>
 *   <li>{@link com.znlgis.ogu4j.exception.OguException} - OGU异常基类</li>
 *   <li>{@link com.znlgis.ogu4j.exception.DataSourceException} - 数据源连接异常</li>
 *   <li>{@link com.znlgis.ogu4j.exception.FormatParseException} - 格式解析异常</li>
 *   <li>{@link com.znlgis.ogu4j.exception.EngineNotSupportedException} - GIS引擎不支持异常</li>
 *   <li>{@link com.znlgis.ogu4j.exception.LayerValidationException} - 图层验证异常</li>
 *   <li>{@link com.znlgis.ogu4j.exception.TopologyException} - 空间拓扑异常</li>
 * </ul>
 *
 * <h2>异常层次</h2>
 * <pre>
 * Exception
 * ├── OguException (检查型异常)
 * │   ├── DataSourceException
 * │   ├── FormatParseException
 * │   ├── EngineNotSupportedException
 * │   └── TopologyException
 * └── RuntimeException
 *     └── LayerValidationException (非检查型异常)
 * </pre>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
package com.znlgis.ogu4j.exception;
