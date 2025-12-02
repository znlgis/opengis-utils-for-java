package com.znlgis.ogu4j.engine;

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
 * GeoTools引擎实现
 * <p>
 * 基于GeoTools库的GIS引擎实现。
 * 支持Shapefile、GeoJSON、PostGIS等格式。
 * 纯Java实现，无需额外的本地库依赖。
 * </p>
 */
public class GeoToolsEngine implements GisEngine {

    private static final String ENGINE_NAME = "GeoTools";

    private static final Set<DataFormatType> SUPPORTED_FORMATS = EnumSet.of(
            DataFormatType.SHP,
            DataFormatType.GEOJSON,
            DataFormatType.POSTGIS,
            DataFormatType.TXT
    );

    @Override
    public String getName() {
        return ENGINE_NAME;
    }

    @Override
    public boolean isAvailable() {
        return true; // GeoTools is always available as a pure Java library
    }

    @Override
    public boolean supports(DataFormatType formatType) {
        return SUPPORTED_FORMATS.contains(formatType);
    }

    @Override
    public LayerReader getReader(DataFormatType formatType) throws OguException {
        if (!supports(formatType)) {
            throw new EngineNotSupportedException(
                    String.format("GeoTools engine does not support reading format: %s", formatType));
        }
        return new GeoToolsLayerReader(formatType);
    }

    @Override
    public LayerWriter getWriter(DataFormatType formatType) throws OguException {
        if (!supports(formatType)) {
            throw new EngineNotSupportedException(
                    String.format("GeoTools engine does not support writing format: %s", formatType));
        }
        return new GeoToolsLayerWriter(formatType);
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
