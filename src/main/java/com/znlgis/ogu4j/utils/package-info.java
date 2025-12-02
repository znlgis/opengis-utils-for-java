/**
 * 通用工具类包。
 * <p>
 * 提供与GIS无关的通用工具类，包括文件压缩、编码检测、排序、数字格式化等功能。
 * </p>
 *
 * <h2>工具类</h2>
 * <ul>
 *   <li>{@link com.znlgis.ogu4j.utils.ZipUtil} - ZIP压缩解压工具类</li>
 *   <li>{@link com.znlgis.ogu4j.utils.EncodingUtil} - 文件编码检测工具类</li>
 *   <li>{@link com.znlgis.ogu4j.utils.SortUtil} - 字符串自然排序工具类</li>
 *   <li>{@link com.znlgis.ogu4j.utils.NumUtil} - 数字格式化工具类</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // ZIP压缩
 * ZipUtil.zip(folder, "output.zip");
 *
 * // 编码检测
 * Charset charset = EncodingUtil.getFileEncoding(file);
 *
 * // 自然排序比较
 * int result = SortUtil.compareString("第5章", "第10章");
 *
 * // 去除科学计数法
 * String plain = NumUtil.getPlainString(1.234E10);
 * }</pre>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
package com.znlgis.ogu4j.utils;
