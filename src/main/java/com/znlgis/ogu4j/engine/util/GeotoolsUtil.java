package com.znlgis.ogu4j.engine.util;

import cn.hutool.core.text.CharSequenceUtil;
import com.znlgis.ogu4j.geometry.GeometryUtil;
import lombok.SneakyThrows;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import java.util.Map;

/**
 * GeoTools工具类
 * <p>
 * 提供基于GeoTools库的要素查询和过滤功能。
 * 支持属性过滤和空间过滤。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
public class GeotoolsUtil {
    private GeotoolsUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 过滤要素集合
     * <p>
     * 对GeoTools要素源应用属性过滤和/或空间过滤条件。
     * 属性过滤使用CQL表达式语法，空间过滤使用WKT格式几何进行相交查询。
     * </p>
     *
     * @param featureSource    GeoTools要素源
     * @param attributeFilter  属性过滤条件（CQL表达式），为null或空时不进行属性过滤
     * @param spatialFilterWkt 空间过滤条件（WKT格式），为null或空时不进行空间过滤
     * @return 过滤后的要素集合
     */
    @SneakyThrows
    public static SimpleFeatureCollection filter(SimpleFeatureSource featureSource, String attributeFilter, String spatialFilterWkt) {
        if (CharSequenceUtil.isBlank(attributeFilter) && CharSequenceUtil.isBlank(spatialFilterWkt)) {
            return DataUtilities.collection(featureSource.getFeatures());

        }

        Filter afilter = null;
        Filter sfilter = null;
        if (CharSequenceUtil.isNotBlank(attributeFilter)) {
            afilter = CQL.toFilter(attributeFilter);
        }

        FilterFactory ff = CommonFactoryFinder.getFilterFactory();
        if (CharSequenceUtil.isNotBlank(spatialFilterWkt)) {
            Map.Entry<Integer, CoordinateReferenceSystem> kv = CrsUtil.standardizeCRS(featureSource.getSchema().getCoordinateReferenceSystem());
            spatialFilterWkt = GeometryUtil.simplifyWkt(spatialFilterWkt, kv.getKey());
            sfilter = ff.intersects(ff.property(featureSource.getSchema().getGeometryDescriptor().getLocalName()),
                    ff.literal(GeometryUtil.wkt2Geometry(spatialFilterWkt)));
        }

        if (afilter == null) {
            return DataUtilities.collection(featureSource.getFeatures(sfilter));
        }

        if (sfilter == null) {
            return DataUtilities.collection(featureSource.getFeatures(afilter));
        }

        Filter filter = ff.and(afilter, sfilter);
        return DataUtilities.collection(featureSource.getFeatures(filter));
    }
}
