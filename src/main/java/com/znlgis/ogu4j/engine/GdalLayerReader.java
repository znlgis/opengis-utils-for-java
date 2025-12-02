package com.znlgis.ogu4j.engine;

import cn.hutool.core.io.FileUtil;
import com.znlgis.ogu4j.engine.util.OgrUtil;
import com.znlgis.ogu4j.engine.util.ShpUtil;
import com.znlgis.ogu4j.engine.enums.DataFormatType;
import com.znlgis.ogu4j.exception.DataSourceException;
import com.znlgis.ogu4j.exception.OguException;
import com.znlgis.ogu4j.engine.io.LayerReader;
import com.znlgis.ogu4j.engine.model.layer.OguLayer;
import org.gdal.gdal.gdal;

import java.nio.charset.Charset;

/**
 * GDAL图层读取器
 * <p>
 * 基于GDAL/OGR库实现的图层读取器，支持Shapefile、GeoJSON、FileGDB、PostGIS格式。
 * </p>
 */
public class GdalLayerReader implements LayerReader {

    private final DataFormatType formatType;

    /**
     * 构造函数
     *
     * @param formatType 数据格式类型
     */
    public GdalLayerReader(DataFormatType formatType) {
        this.formatType = formatType;
    }

    @Override
    public OguLayer read(String path, String layerName, String attributeFilter, String spatialFilterWkt)
            throws OguException {
        OgrUtil.checkGdalEnv();

        switch (formatType) {
            case SHP:
                return readShapefile(path, layerName, attributeFilter, spatialFilterWkt);
            case GEOJSON:
                return readGeoJSON(path, layerName, attributeFilter, spatialFilterWkt);
            case FILEGDB:
                return readFileGDB(path, layerName, attributeFilter, spatialFilterWkt);
            case POSTGIS:
                return readPostGIS(path, layerName, attributeFilter, spatialFilterWkt);
            default:
                throw new DataSourceException("Unsupported format: " + formatType);
        }
    }

    @Override
    public boolean supports(String path) {
        if (path == null) {
            return false;
        }
        String lowerPath = path.toLowerCase();
        switch (formatType) {
            case SHP:
                return lowerPath.endsWith(".shp");
            case GEOJSON:
                return lowerPath.endsWith(".geojson") || lowerPath.endsWith(".json");
            case FILEGDB:
                return lowerPath.endsWith(".gdb");
            case POSTGIS:
                return path.contains("PG:") || path.contains("postgresql");
            default:
                return false;
        }
    }

    private OguLayer readShapefile(String shpPath, String layerName, String attributeFilter, String spatialFilterWkt)
            throws OguException {
        try {
            Charset shpCharset = ShpUtil.check(shpPath);
            gdal.SetConfigOption("SHAPE_ENCODING", shpCharset.name());
            String shpDir = FileUtil.getParent(shpPath, 1);
            String shpName = layerName != null ? layerName : FileUtil.mainName(shpPath);
            return OgrUtil.layer2OguLayer(DataFormatType.SHP, shpDir, shpName, attributeFilter, spatialFilterWkt);
        } catch (Exception e) {
            throw new DataSourceException("Failed to read Shapefile: " + shpPath, e);
        }
    }

    private OguLayer readGeoJSON(String geojsonPath, String layerName, String attributeFilter, String spatialFilterWkt)
            throws OguException {
        try {
            String name = layerName != null ? layerName : FileUtil.mainName(geojsonPath);
            return OgrUtil.layer2OguLayer(DataFormatType.GEOJSON, geojsonPath, name, attributeFilter, spatialFilterWkt);
        } catch (Exception e) {
            throw new DataSourceException("Failed to read GeoJSON: " + geojsonPath, e);
        }
    }

    private OguLayer readFileGDB(String gdbPath, String layerName, String attributeFilter, String spatialFilterWkt)
            throws OguException {
        try {
            return OgrUtil.layer2OguLayer(DataFormatType.FILEGDB, gdbPath, layerName, attributeFilter, spatialFilterWkt);
        } catch (Exception e) {
            throw new DataSourceException("Failed to read FileGDB layer: " + layerName, e);
        }
    }

    private OguLayer readPostGIS(String connStr, String layerName, String attributeFilter, String spatialFilterWkt)
            throws OguException {
        try {
            return OgrUtil.layer2OguLayer(DataFormatType.POSTGIS, connStr, layerName, attributeFilter, spatialFilterWkt);
        } catch (Exception e) {
            throw new DataSourceException("Failed to read PostGIS layer: " + layerName, e);
        }
    }
}
