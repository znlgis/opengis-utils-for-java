package com.znlgis.ogu4j.engine;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.znlgis.ogu4j.engine.util.CrsUtil;
import com.znlgis.ogu4j.utils.EncodingUtil;
import com.znlgis.ogu4j.engine.util.GeotoolsUtil;
import com.znlgis.ogu4j.datasource.GtTxtUtil;
import com.znlgis.ogu4j.engine.util.PostgisUtil;
import com.znlgis.ogu4j.engine.util.ShpUtil;
import com.znlgis.ogu4j.engine.enums.DataFormatType;
import com.znlgis.ogu4j.engine.enums.FieldDataType;
import com.znlgis.ogu4j.engine.enums.GeometryType;
import com.znlgis.ogu4j.exception.DataSourceException;
import com.znlgis.ogu4j.exception.FormatParseException;
import com.znlgis.ogu4j.exception.OguException;
import com.znlgis.ogu4j.geometry.GeometryUtil;
import com.znlgis.ogu4j.engine.io.LayerReader;
import com.znlgis.ogu4j.engine.model.DbConnBaseModel;
import com.znlgis.ogu4j.engine.model.layer.OguFeature;
import com.znlgis.ogu4j.engine.model.layer.OguField;
import com.znlgis.ogu4j.engine.model.layer.OguFieldValue;
import com.znlgis.ogu4j.engine.model.layer.OguLayer;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.jdbc.JDBCDataStore;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GeoTools图层读取器
 * <p>
 * 基于GeoTools库实现的图层读取器，支持Shapefile、GeoJSON、PostGIS、TXT格式。
 * </p>
 */
public class GeoToolsLayerReader implements LayerReader {

    private final DataFormatType formatType;

    /**
     * 构造函数
     *
     * @param formatType 数据格式类型
     */
    public GeoToolsLayerReader(DataFormatType formatType) {
        this.formatType = formatType;
    }

    @Override
    public OguLayer read(String path, String layerName, String attributeFilter, String spatialFilterWkt)
            throws OguException {
        switch (formatType) {
            case SHP:
                return readShapefile(path, attributeFilter, spatialFilterWkt);
            case GEOJSON:
                return readGeoJSON(path);
            case POSTGIS:
                return readPostGIS(path, layerName, attributeFilter, spatialFilterWkt);
            case TXT:
                return readTxt(path);
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
            case TXT:
                return lowerPath.endsWith(".txt");
            case POSTGIS:
                return path.contains("postgis") || path.contains("postgresql");
            default:
                return false;
        }
    }

    private OguLayer readShapefile(String shpPath, String attributeFilter, String spatialFilterWkt)
            throws OguException {
        try {
            Charset shpCharset = ShpUtil.check(shpPath);
            File file = new File(shpPath);
            ShapefileDataStore shpDataStore = new ShapefileDataStore(file.toURI().toURL());
            shpDataStore.setCharset(shpCharset);
            String typeName = shpDataStore.getTypeNames()[0];
            SimpleFeatureSource source = shpDataStore.getFeatureSource(typeName);
            SimpleFeatureCollection simpleFeatureCollection = GeotoolsUtil.filter(source, attributeFilter, spatialFilterWkt);
            shpDataStore.dispose();
            return fromSimpleFeatureCollection(simpleFeatureCollection);
        } catch (Exception e) {
            throw new DataSourceException("Failed to read Shapefile: " + shpPath, e);
        }
    }

    private OguLayer readGeoJSON(String geojsonPath) throws OguException {
        try {
            File file = new File(geojsonPath);
            Charset encoding = EncodingUtil.getFileEncoding(file);
            String geojsonString = FileUtil.readString(file, encoding);

            GeometryJSON gjson = new GeometryJSON(16);
            FeatureJSON fjson = new FeatureJSON(gjson);

            SimpleFeatureType simpleFeatureType = fjson.readFeatureCollectionSchema(geojsonString, true);
            ListFeatureCollection featureCollection = new ListFeatureCollection(simpleFeatureType);
            try (FeatureIterator<SimpleFeature> features = fjson.streamFeatureCollection(geojsonString)) {
                while (features.hasNext()) {
                    featureCollection.add(features.next());
                }
            }

            return fromSimpleFeatureCollection(featureCollection);
        } catch (Exception e) {
            throw new FormatParseException("Failed to parse GeoJSON: " + geojsonPath, e);
        }
    }

    private OguLayer readPostGIS(String connStr, String layerName, String attributeFilter, String spatialFilterWkt)
            throws OguException {
        try {
            DbConnBaseModel dbConnBaseModel = PostgisUtil.parseConnectionString(connStr);
            JDBCDataStore dataStore = PostgisUtil.getPostgisDataStore(dbConnBaseModel);
            SimpleFeatureSource source = dataStore.getFeatureSource(layerName);
            SimpleFeatureCollection simpleFeatureCollection = GeotoolsUtil.filter(source, attributeFilter, spatialFilterWkt);
            dataStore.dispose();
            return fromSimpleFeatureCollection(simpleFeatureCollection);
        } catch (Exception e) {
            throw new DataSourceException("Failed to read PostGIS layer: " + layerName, e);
        }
    }

    private OguLayer readTxt(String txtPath) throws OguException {
        try {
            return GtTxtUtil.loadTxt(txtPath, null);
        } catch (Exception e) {
            throw new FormatParseException("Failed to parse TXT file: " + txtPath, e);
        }
    }

    /**
     * 将GeoTools SimpleFeatureCollection转换为OguLayer
     *
     * @param featureCollection GeoTools要素集合
     * @return OguLayer图层对象
     */
    public static OguLayer fromSimpleFeatureCollection(SimpleFeatureCollection featureCollection) {
        OguLayer layer = new OguLayer();
        SimpleFeatureType featureType = featureCollection.getSchema();
        layer.setName(featureType.getName().getLocalPart());
        CoordinateReferenceSystem crs = featureType.getCoordinateReferenceSystem();
        Map.Entry<Integer, CoordinateReferenceSystem> entry = CrsUtil.standardizeCRS(crs);
        layer.setWkid(entry.getKey());
        layer.setTolerance(CrsUtil.getTolerance(entry.getValue()));
        Class<?> binding = featureType.getGeometryDescriptor().getType().getBinding();
        if (binding != null) {
            layer.setGeometryType(GeometryType.valueOfByTypeClass(binding));
        } else {
            String typeName = featureType.getGeometryDescriptor().getType().getName().getLocalPart();
            layer.setGeometryType(GeometryType.valueOfByTypeName(typeName));
        }

        List<OguField> fields = new ArrayList<>();
        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            if (featureType.getDescriptor(i) instanceof GeometryDescriptor) {
                continue;
            }
            OguField field = new OguField();
            field.setName(featureType.getDescriptor(i).getLocalName());
            field.setAlias(featureType.getDescriptor(i).getLocalName());
            field.setDataType(FieldDataType.fieldDataTypeByTypeClass(featureType.getDescriptor(i).getType().getBinding()));
            fields.add(field);
        }
        layer.setFields(fields);

        List<OguFeature> features = new ArrayList<>();
        try (FeatureIterator<SimpleFeature> featureIterator = featureCollection.features()) {
            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();
                OguFeature oguFeature = new OguFeature();

                String id = feature.getID();
                if (CharSequenceUtil.isBlank(id)) {
                    id = IdUtil.simpleUUID();
                }
                oguFeature.setId(id);

                String wkt = ((Geometry) feature.getDefaultGeometry()).toText();
                oguFeature.setGeometry(GeometryUtil.simplifyWkt(wkt, layer.getWkid()));

                if (layer.getGeometryType() == null) {
                    layer.setGeometryType(GeometryUtil.geometryType(GeometryUtil.wkt2Geometry(wkt)));
                }

                List<OguFieldValue> fieldValues = new ArrayList<>();
                for (int i = 0; i < feature.getAttributeCount(); i++) {
                    String fn = featureType.getDescriptor(i).getLocalName();
                    OguField field = fields.stream().filter(f ->
                            CharSequenceUtil.equals(f.getName(), fn, true)).findFirst().orElse(null);
                    if (field != null) {
                        OguFieldValue fieldValue = new OguFieldValue();
                        fieldValue.setField(field);
                        fieldValue.setValue(feature.getAttribute(fn));
                        fieldValues.add(fieldValue);
                    }
                }

                oguFeature.setAttributes(fieldValues);
                features.add(oguFeature);
            }
        }

        layer.setFeatures(features);
        layer.validate();
        return layer;
    }
}
