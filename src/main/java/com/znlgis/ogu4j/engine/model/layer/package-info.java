/**
 * 图层模型定义包。
 * <p>
 * 定义OGU4J统一图层模型，提供简洁的图层、要素、字段抽象，屏蔽底层GIS库差异。
 * 这是OGU4J最核心的数据模型包。
 * </p>
 *
 * <h2>核心类</h2>
 * <ul>
 *   <li>{@link com.znlgis.ogu4j.engine.model.layer.OguLayer} - 统一的GIS图层定义</li>
 *   <li>{@link com.znlgis.ogu4j.engine.model.layer.OguFeature} - 统一的要素类</li>
 *   <li>{@link com.znlgis.ogu4j.engine.model.layer.OguField} - 统一的字段定义类</li>
 *   <li>{@link com.znlgis.ogu4j.engine.model.layer.OguFieldValue} - 字段值容器</li>
 * </ul>
 *
 * <h2>辅助类</h2>
 * <ul>
 *   <li>{@link com.znlgis.ogu4j.engine.model.layer.OguCoordinate} - 坐标类</li>
 *   <li>{@link com.znlgis.ogu4j.engine.model.layer.OguFeatureFilter} - 要素过滤器接口</li>
 *   <li>{@link com.znlgis.ogu4j.engine.model.layer.OguLayerMetadata} - 图层元数据</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 创建图层
 * OguLayer layer = OguLayer.fromJSON(jsonString);
 *
 * // 验证图层
 * layer.validate();
 *
 * // 过滤要素
 * List<OguFeature> filtered = layer.filter(feature ->
 *     "北京".equals(feature.getValue("city")));
 *
 * // 获取要素属性
 * OguFeature feature = layer.getFeatures().get(0);
 * String name = feature.getAttribute("name").getStringValue();
 * }</pre>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
package com.znlgis.ogu4j.engine.model.layer;
