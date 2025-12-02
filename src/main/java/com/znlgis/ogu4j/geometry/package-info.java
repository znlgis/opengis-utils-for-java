/**
 * 几何处理工具包。
 * <p>
 * 提供基于JTS和ESRI Geometry API的几何创建、转换、属性查询、空间关系判断和空间分析功能。
 * </p>
 *
 * <h2>工具类</h2>
 * <ul>
 *   <li>{@link com.znlgis.ogu4j.geometry.GeometryUtil} - 几何处理工具类</li>
 * </ul>
 *
 * <h2>功能分类</h2>
 * <ul>
 *   <li><b>格式转换</b> - WKT、GeoJSON、ESRI JSON之间的相互转换</li>
 *   <li><b>几何创建</b> - 通过各种格式创建JTS或ESRI几何对象</li>
 *   <li><b>属性查询</b> - 面积、长度、中心点、维度、边界等</li>
 *   <li><b>空间关系</b> - 相交、包含、相离、接触等判断</li>
 *   <li><b>空间分析</b> - 缓冲区、交集、并集、差集等</li>
 *   <li><b>拓扑验证</b> - 几何有效性检查和修复</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // WKT转JTS Geometry
 * Geometry geom = GeometryUtil.wkt2Geometry(wkt);
 *
 * // 空间关系判断
 * boolean result = GeometryUtil.intersects(geomA, geomB);
 *
 * // 缓冲区分析
 * Geometry buffer = GeometryUtil.buffer(geom, 100);
 *
 * // 拓扑验证
 * TopologyValidationResult result = GeometryUtil.isValid(geom);
 * }</pre>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
package com.znlgis.ogu4j.geometry;
