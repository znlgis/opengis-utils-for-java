package com.znlgis.ogu4j.engine;

import com.znlgis.ogu4j.engine.util.OgrUtil;
import com.znlgis.ogu4j.engine.enums.DataFormatType;
import com.znlgis.ogu4j.exception.EngineNotSupportedException;
import com.znlgis.ogu4j.exception.OguException;
import com.znlgis.ogu4j.engine.io.LayerReader;
import com.znlgis.ogu4j.engine.io.LayerWriter;
import com.znlgis.ogu4j.engine.model.layer.OguLayer;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * GDAL/OGR引擎实现
 * <p>
 * 基于GDAL/OGR库的GIS引擎实现。
 * 支持FileGDB、Shapefile、GeoJSON、PostGIS等多种格式。
 * 需要安装GDAL本地库并正确配置环境变量。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
public class GdalEngine implements GisEngine {

    private static final String ENGINE_NAME = "GDAL";

    private static final Set<DataFormatType> SUPPORTED_FORMATS = EnumSet.of(
            DataFormatType.SHP,
            DataFormatType.GEOJSON,
            DataFormatType.FILEGDB,
            DataFormatType.POSTGIS
    );

    @Override
    public String getName() {
        return ENGINE_NAME;
    }

    @Override
    public boolean isAvailable() {
        return Boolean.TRUE.equals(OgrUtil.getOgrInitSuccess());
    }

    @Override
    public boolean supports(DataFormatType formatType) {
        if (!isAvailable()) {
            return false;
        }
        return SUPPORTED_FORMATS.contains(formatType);
    }

    @Override
    public LayerReader getReader(DataFormatType formatType) throws OguException {
        if (!isAvailable()) {
            throw new EngineNotSupportedException("GDAL engine is not available.");
        }
        if (!supports(formatType)) {
            throw new EngineNotSupportedException(
                    String.format("GDAL engine does not support reading format: %s", formatType));
        }
        return new GdalLayerReader(formatType);
    }

    @Override
    public LayerWriter getWriter(DataFormatType formatType) throws OguException {
        if (!isAvailable()) {
            throw new EngineNotSupportedException("GDAL engine is not available.");
        }
        if (!supports(formatType)) {
            throw new EngineNotSupportedException(
                    String.format("GDAL engine does not support writing format: %s", formatType));
        }
        return new GdalLayerWriter(formatType);
    }

    @Override
    public OguLayer readLayer(DataFormatType formatType, String path, String layerName,
                              String attributeFilter, String spatialFilterWkt) throws OguException {
        LayerReader reader = getReader(formatType);
        return reader.read(path, layerName, attributeFilter, spatialFilterWkt);
    }

    @Override
    public void writeLayer(DataFormatType formatType, OguLayer layer, String path,
                           String layerName, Map<String, Object> options) throws OguException {
        LayerWriter writer = getWriter(formatType);
        writer.write(layer, path, layerName, options);
    }
}
