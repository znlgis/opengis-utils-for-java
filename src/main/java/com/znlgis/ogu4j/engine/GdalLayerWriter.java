package com.znlgis.ogu4j.engine;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.znlgis.ogu4j.engine.util.OgrUtil;
import com.znlgis.ogu4j.engine.util.PostgisUtil;
import com.znlgis.ogu4j.engine.enums.DataFormatType;
import com.znlgis.ogu4j.exception.DataSourceException;
import com.znlgis.ogu4j.exception.OguException;
import com.znlgis.ogu4j.geometry.GeometryUtil;
import com.znlgis.ogu4j.engine.io.LayerWriter;
import com.znlgis.ogu4j.engine.model.DbConnBaseModel;
import com.znlgis.ogu4j.engine.model.layer.OguLayer;
import org.gdal.gdal.gdal;

import java.util.Map;
import java.util.Vector;

/**
 * GDAL图层写入器
 * <p>
 * 基于GDAL/OGR库实现的图层写入器，支持Shapefile、GeoJSON、FileGDB、PostGIS格式。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
public class GdalLayerWriter implements LayerWriter {

    private final DataFormatType formatType;

    /**
     * 构造函数
     *
     * @param formatType 数据格式类型
     */
    public GdalLayerWriter(DataFormatType formatType) {
        this.formatType = formatType;
    }

    @Override
    public void write(OguLayer layer, String path, String layerName, Map<String, Object> options)
            throws OguException {
        OgrUtil.checkGdalEnv();
        GeometryUtil.excludeSpecialFields(layer.getFields());

        switch (formatType) {
            case SHP:
                writeShapefile(layer, path, layerName, options);
                break;
            case GEOJSON:
                writeGeoJSON(layer, path, layerName, options);
                break;
            case FILEGDB:
                writeFileGDB(layer, path, layerName, options);
                break;
            case POSTGIS:
                writePostGIS(layer, path, layerName, options);
                break;
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

    @SuppressWarnings("unchecked")
    private void writeShapefile(OguLayer layer, String shpPath, String layerName, Map<String, Object> options)
            throws OguException {
        try {
            gdal.SetConfigOption("SHAPE_ENCODING", "");
            Vector<String> gdalOptions = new Vector<>();
            gdalOptions.add("ENCODING=UTF-8");

            if (options != null && options.containsKey("gdalOptions")) {
                gdalOptions.addAll((java.util.Collection<String>) options.get("gdalOptions"));
            }

            String shpDir = FileUtil.getParent(shpPath, 1);
            String shpName = layerName != null ? layerName : FileUtil.mainName(shpPath);
            OgrUtil.oguLayer2Layer(DataFormatType.SHP, shpDir, layer, shpName, gdalOptions);
        } catch (Exception e) {
            throw new DataSourceException("Failed to write Shapefile: " + shpPath, e);
        }
    }

    private void writeGeoJSON(OguLayer layer, String geojsonPath, String layerName, Map<String, Object> options)
            throws OguException {
        try {
            String name = layerName != null ? layerName : FileUtil.mainName(geojsonPath);
            OgrUtil.oguLayer2Layer(DataFormatType.GEOJSON, geojsonPath, layer, name, null);
        } catch (Exception e) {
            throw new DataSourceException("Failed to write GeoJSON: " + geojsonPath, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeFileGDB(OguLayer layer, String gdbPath, String layerName, Map<String, Object> options)
            throws OguException {
        try {
            Vector<String> gdalOptions = null;
            if (options != null && options.containsKey("featureDataset")) {
                String featureDataset = (String) options.get("featureDataset");
                if (CharSequenceUtil.isNotBlank(featureDataset)) {
                    gdalOptions = new Vector<>();
                    gdalOptions.add("FEATURE_DATASET=" + featureDataset);
                }
            }
            OgrUtil.oguLayer2Layer(DataFormatType.FILEGDB, gdbPath, layer, layerName, gdalOptions);
        } catch (Exception e) {
            throw new DataSourceException("Failed to write FileGDB layer: " + layerName, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void writePostGIS(OguLayer layer, String connStr, String layerName, Map<String, Object> options)
            throws OguException {
        try {
            DbConnBaseModel dbConnBaseModel = PostgisUtil.parseConnectionString(connStr);
            Vector<String> gdalOptions = null;
            if (options != null && options.containsKey("gdalOptions")) {
                gdalOptions = new Vector<>();
                gdalOptions.addAll((java.util.Collection<String>) options.get("gdalOptions"));
            }
            OgrUtil.oguLayer2Layer4Postgis(DataFormatType.POSTGIS, dbConnBaseModel, layer, layerName, gdalOptions);
        } catch (Exception e) {
            throw new DataSourceException("Failed to write PostGIS layer: " + layerName, e);
        }
    }
}
