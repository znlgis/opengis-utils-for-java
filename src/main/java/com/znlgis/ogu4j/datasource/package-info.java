/**
 * 数据源读写工具包。
 * <p>
 * 提供OguLayer与各种GIS数据格式之间的相互转换功能，是OGU4J的核心数据访问层。
 * </p>
 *
 * <h2>主要类</h2>
 * <ul>
 *   <li>{@link com.znlgis.ogu4j.datasource.OguLayerUtil} - 图层格式转换工具类，提供统一的读写接口</li>
 *   <li>{@link com.znlgis.ogu4j.datasource.GtTxtUtil} - 国土TXT坐标文件工具类</li>
 * </ul>
 *
 * <h2>支持的数据格式</h2>
 * <ul>
 *   <li><b>Shapefile</b> - ESRI Shape格式，最常用的矢量数据格式</li>
 *   <li><b>GeoJSON</b> - 基于JSON的地理空间数据交换格式</li>
 *   <li><b>FileGDB</b> - ESRI文件地理数据库（需要GDAL支持）</li>
 *   <li><b>PostGIS</b> - PostgreSQL空间数据库扩展</li>
 *   <li><b>TXT</b> - 国土资源部门TXT坐标文件格式</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 读取Shapefile
 * OguLayer layer = OguLayerUtil.readLayer(
 *     DataFormatType.SHP, shpPath, null, null, null, GisEngineType.GEOTOOLS);
 *
 * // 保存为GeoJSON
 * OguLayerUtil.writeLayer(
 *     DataFormatType.GEOJSON, layer, geojsonPath, null, null, GisEngineType.GEOTOOLS);
 * }</pre>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 * @see com.znlgis.ogu4j.engine.model.layer.OguLayer
 * @see com.znlgis.ogu4j.engine.enums.DataFormatType
 */
package com.znlgis.ogu4j.datasource;
