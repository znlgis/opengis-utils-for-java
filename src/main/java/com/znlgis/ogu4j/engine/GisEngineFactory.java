package com.znlgis.ogu4j.engine;

import com.znlgis.ogu4j.engine.enums.DataFormatType;
import com.znlgis.ogu4j.engine.enums.GisEngineType;
import com.znlgis.ogu4j.exception.EngineNotSupportedException;
import com.znlgis.ogu4j.exception.OguException;

/**
 * GIS引擎工厂
 * <p>
 * 提供获取GIS引擎实例的工厂方法。
 * 支持根据引擎类型或数据格式自动选择合适的引擎。
 * </p>
 */
public final class GisEngineFactory {

    private static final GeoToolsEngine GEOTOOLS_ENGINE = new GeoToolsEngine();
    private static final GdalEngine GDAL_ENGINE = new GdalEngine();

    private GisEngineFactory() {
        throw new IllegalStateException("Utility class - do not instantiate");
    }

    /**
     * 根据引擎类型获取GIS引擎
     *
     * @param engineType 引擎类型
     * @return GIS引擎实例
     * @throws EngineNotSupportedException 如果引擎不可用
     */
    public static GisEngine getEngine(GisEngineType engineType) throws EngineNotSupportedException {
        if (engineType == null || engineType == GisEngineType.AUTO) {
            return getAutoEngine();
        }

        switch (engineType) {
            case GEOTOOLS:
                return GEOTOOLS_ENGINE;
            case GDAL:
                if (!GDAL_ENGINE.isAvailable()) {
                    throw new EngineNotSupportedException("GDAL engine is not available. Please ensure GDAL is properly installed.");
                }
                return GDAL_ENGINE;
            default:
                return getAutoEngine();
        }
    }

    /**
     * 根据数据格式获取合适的GIS引擎
     *
     * @param formatType 数据格式类型
     * @return 支持该格式的GIS引擎
     * @throws EngineNotSupportedException 如果没有引擎支持该格式
     */
    public static GisEngine getEngine(DataFormatType formatType) throws EngineNotSupportedException {
        // GDAL-only formats
        if (formatType == DataFormatType.FILEGDB) {
            if (!GDAL_ENGINE.isAvailable()) {
                throw new EngineNotSupportedException("FileGDB format requires GDAL engine, but GDAL is not available.");
            }
            return GDAL_ENGINE;
        }

        // Prefer GDAL if available, fallback to GeoTools
        if (GDAL_ENGINE.isAvailable() && GDAL_ENGINE.supports(formatType)) {
            return GDAL_ENGINE;
        }

        if (GEOTOOLS_ENGINE.supports(formatType)) {
            return GEOTOOLS_ENGINE;
        }

        throw new EngineNotSupportedException("No engine supports format: " + formatType);
    }

    /**
     * 根据引擎类型和数据格式获取GIS引擎
     *
     * @param engineType 引擎类型
     * @param formatType 数据格式类型
     * @return GIS引擎实例
     * @throws OguException 如果引擎不支持该格式
     */
    public static GisEngine getEngine(GisEngineType engineType, DataFormatType formatType) throws OguException {
        GisEngine engine = getEngine(engineType);

        if (!engine.supports(formatType)) {
            throw new EngineNotSupportedException(
                    String.format("Engine %s does not support format %s", engine.getName(), formatType));
        }

        return engine;
    }

    /**
     * 自动选择可用的引擎（GDAL优先）
     *
     * @return 可用的GIS引擎
     */
    private static GisEngine getAutoEngine() {
        if (GDAL_ENGINE.isAvailable()) {
            return GDAL_ENGINE;
        }
        return GEOTOOLS_ENGINE;
    }

    /**
     * 获取GeoTools引擎实例
     *
     * @return GeoTools引擎
     */
    public static GeoToolsEngine getGeoToolsEngine() {
        return GEOTOOLS_ENGINE;
    }

    /**
     * 获取GDAL引擎实例
     *
     * @return GDAL引擎
     * @throws EngineNotSupportedException 如果GDAL不可用
     */
    public static GdalEngine getGdalEngine() throws EngineNotSupportedException {
        if (!GDAL_ENGINE.isAvailable()) {
            throw new EngineNotSupportedException("GDAL engine is not available.");
        }
        return GDAL_ENGINE;
    }
}
