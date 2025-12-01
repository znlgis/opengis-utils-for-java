package com.znlgis.ogu4j.model.layer;

/**
 * OGU要素过滤器接口
 * <p>
 * 函数式接口，用于筛选图层中的要素。
 * 可与OguLayer.filter()方法配合使用。
 * </p>
 */
@FunctionalInterface
public interface OguFeatureFilter {
    /**
     * 判断要素是否满足过滤条件
     *
     * @param feature 要判断的要素
     * @return true表示满足条件，false表示不满足
     */
    boolean apply(OguFeature feature);
}
