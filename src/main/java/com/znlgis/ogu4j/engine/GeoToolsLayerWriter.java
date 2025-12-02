package com.znlgis.ogu4j.engine;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.znlgis.ogu4j.engine.util.CrsUtil;
import com.znlgis.ogu4j.datasource.GtTxtUtil;
import com.znlgis.ogu4j.engine.util.PostgisUtil;
import com.znlgis.ogu4j.engine.util.ShpUtil;
import com.znlgis.ogu4j.engine.enums.DataFormatType;
import com.znlgis.ogu4j.engine.enums.FieldDataType;
import com.znlgis.ogu4j.exception.DataSourceException;
import com.znlgis.ogu4j.exception.OguException;
import com.znlgis.ogu4j.geometry.GeometryUtil;
import com.znlgis.ogu4j.engine.io.LayerWriter;
import com.znlgis.ogu4j.engine.model.DbConnBaseModel;
import com.znlgis.ogu4j.engine.model.layer.OguFeature;
import com.znlgis.ogu4j.engine.model.layer.OguFieldValue;
import com.znlgis.ogu4j.engine.model.layer.OguLayer;
import com.znlgis.ogu4j.engine.model.layer.OguLayerMetadata;
import org.geotools.api.data.FeatureWriter;
import org.geotools.api.data.Transaction;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.Name;
import org.geotools.api.feature.type.PropertyDescriptor;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.crs.ForceCoordinateSystemFeatureResults;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.jdbc.JDBCDataStore;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * GeoTools图层写入器
 * <p>
 * 基于GeoTools库实现的图层写入器，支持Shapefile、GeoJSON、PostGIS、TXT格式。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
public class GeoToolsLayerWriter implements LayerWriter {

    private final DataFormatType formatType;

    /**
     * 构造函数
     *
     * @param formatType 数据格式类型
     */
    public GeoToolsLayerWriter(DataFormatType formatType) {
        this.formatType = formatType;
    }

    @Override
    public void write(OguLayer layer, String path, String layerName, Map<String, Object> options)
            throws OguException {
        switch (formatType) {
            case SHP:
                writeShapefile(layer, path);
                break;
            case GEOJSON:
                writeGeoJSON(layer, path);
                break;
            case POSTGIS:
                writePostGIS(layer, path, layerName);
                break;
            case TXT:
                writeTxt(layer, path, options);
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
            case TXT:
                return lowerPath.endsWith(".txt");
            case POSTGIS:
                return path.contains("postgis") || path.contains("postgresql");
            default:
                return false;
        }
    }

    private void writeShapefile(OguLayer layer, String shpPath) throws OguException {
        try {
            GeometryUtil.excludeSpecialFields(layer.getFields());
            ShpUtil.formatFieldName(layer.getFields());

            File shapeFile = new File(shpPath);
            SimpleFeatureCollection featureCollection = toSimpleFeatureCollection(layer);
            Map<String, Serializable> params = new HashMap<>();
            params.put(ShapefileDataStoreFactory.URLP.key, shapeFile.toURI().toURL());

            ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
            SimpleFeatureType featureType = featureCollection.getSchema();
            ds.createSchema(featureType);
            Charset charset = StandardCharsets.UTF_8;
            ds.setCharset(charset);

            String typeName = ds.getTypeNames()[0];
            FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriterAppend(typeName, Transaction.AUTO_COMMIT);

            try (FeatureIterator<SimpleFeature> features = featureCollection.features()) {
                while (features.hasNext()) {
                    SimpleFeature feature = features.next();
                    writer.hasNext();
                    SimpleFeature writefeature = writer.next();
                    writefeature.setDefaultGeometry(feature.getDefaultGeometry());

                    for (PropertyDescriptor d : featureType.getDescriptors()) {
                        if (!(feature.getAttribute(d.getName()) instanceof Geometry)) {
                            Name name = d.getName();
                            Object value = feature.getAttribute(name);
                            writefeature.setAttribute(name, value);
                        }
                    }

                    writer.write();
                }
            }

            writer.close();
            ds.dispose();

            String cpgPath = shpPath.substring(0, shpPath.lastIndexOf(".")) + ".cpg";
            FileUtil.writeString("UTF-8", cpgPath, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DataSourceException("Failed to write Shapefile: " + shpPath, e);
        }
    }

    private void writeGeoJSON(OguLayer layer, String geojsonPath) throws OguException {
        try {
            GeometryUtil.excludeSpecialFields(layer.getFields());

            SimpleFeatureCollection featureCollection = toSimpleFeatureCollection(layer);
            GeometryJSON gjson = new GeometryJSON(16);
            FeatureJSON fjson = new FeatureJSON(gjson);

            CoordinateReferenceSystem crs = featureCollection.getSchema().getCoordinateReferenceSystem();
            if (crs != null) {
                Integer wkid = CrsUtil.standardizeCRS(crs).getKey();
                featureCollection = new ForceCoordinateSystemFeatureResults(featureCollection, CrsUtil.getSupportedCRS(wkid).getValue(), false);
            }

            String geojsonString = fjson.toString(featureCollection);
            FileUtil.writeString(geojsonString, geojsonPath, "utf-8");
        } catch (Exception e) {
            throw new DataSourceException("Failed to write GeoJSON: " + geojsonPath, e);
        }
    }

    private void writePostGIS(OguLayer layer, String connStr, String layerName) throws OguException {
        try {
            GeometryUtil.excludeSpecialFields(layer.getFields());

            DbConnBaseModel dbConnBaseModel = PostgisUtil.parseConnectionString(connStr);
            SimpleFeatureCollection featureCollection = toSimpleFeatureCollection(layer);
            SimpleFeatureType simpleFeatureType = featureCollection.getSchema();
            JDBCDataStore dataStore = PostgisUtil.getPostgisDataStore(dbConnBaseModel);
            if (!Arrays.asList(dataStore.getTypeNames()).contains(layerName)) {
                SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
                tb.init(simpleFeatureType);
                tb.setName(layerName);
                dataStore.createSchema(tb.buildFeatureType());
            }

            SimpleFeatureType featureType = dataStore.getSchema(layerName);
            Map<String, String> fieldMap = new HashMap<>();
            for (int i = 0; i < featureType.getAttributeCount(); i++) {
                String wn = featureType.getDescriptor(i).getLocalName();
                Optional<PropertyDescriptor> first = simpleFeatureType.getDescriptors().stream()
                        .filter(m -> CharSequenceUtil.equals(m.getName().getLocalPart(), wn, true))
                        .findFirst();
                first.ifPresent(propertyDescriptor -> fieldMap.put(wn, propertyDescriptor.getName().getLocalPart()));
            }

            dataStore.dispose();

            List<SimpleFeature> features = new ArrayList<>();
            try (FeatureIterator<SimpleFeature> iterator = featureCollection.features()) {
                while (iterator.hasNext()) {
                    features.add(iterator.next());
                }
            }

            int batchSize = 1000;
            int count = features.size() / batchSize;
            ExecutorService executorService = ThreadUtil.newExecutor(count);
            for (int i = 0; i <= count; i++) {
                List<SimpleFeature> subList;
                if (i == count) {
                    subList = features.subList(i * batchSize, features.size());
                } else {
                    subList = features.subList(i * batchSize, (i + 1) * batchSize);
                }

                executorService.execute(() -> {
                    try {
                        JDBCDataStore ds = PostgisUtil.getPostgisDataStore(dbConnBaseModel);
                        Transaction transaction = new DefaultTransaction("create");
                        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriterAppend(layerName, transaction);
                        try {
                            for (SimpleFeature feature : subList) {
                                writer.hasNext();
                                SimpleFeature writefeature = writer.next();
                                writefeature.setDefaultGeometry(feature.getDefaultGeometry());

                                for (Map.Entry<String, String> kv : fieldMap.entrySet()) {
                                    writefeature.setAttribute(kv.getKey(), feature.getAttribute(kv.getValue()));
                                }

                                writer.write();
                            }

                            transaction.commit();
                        } catch (Exception e) {
                            transaction.rollback();
                            throw new RuntimeException(e);
                        } finally {
                            writer.close();
                            transaction.close();
                            ds.dispose();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (Exception e) {
            throw new DataSourceException("Failed to write PostGIS layer: " + layerName, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeTxt(OguLayer layer, String txtPath, Map<String, Object> options) throws OguException {
        try {
            GeometryUtil.excludeSpecialFields(layer.getFields());

            OguLayerMetadata metadata = null;
            List<String> fieldNames = null;
            Integer zoneNumber = null;

            if (options != null) {
                metadata = (OguLayerMetadata) options.get("metadata");
                fieldNames = (List<String>) options.get("fieldNames");
                zoneNumber = (Integer) options.get("zoneNumber");
            }

            GtTxtUtil.saveTxt(layer, txtPath, metadata, fieldNames, zoneNumber);
        } catch (Exception e) {
            throw new DataSourceException("Failed to write TXT file: " + txtPath, e);
        }
    }

    /**
     * 将OguLayer转换为GeoTools SimpleFeatureCollection
     *
     * @param layer OguLayer图层对象
     * @return GeoTools SimpleFeatureCollection要素集合
     * @throws Exception 转换失败时抛出异常
     */
    public static SimpleFeatureCollection toSimpleFeatureCollection(OguLayer layer) throws Exception {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setCRS(CrsUtil.getSupportedCRS(layer.getWkid()).getValue());
        tb.setName(layer.getName());
        tb.setDefaultGeometry("shape");
        tb.add("shape", layer.getGeometryType().getTypeClass());

        LinkedHashMap<String, FieldDataType> fieldMap = new LinkedHashMap<>();
        layer.getFields().forEach(ff -> {
            if (!fieldMap.containsKey(ff.getName())) {
                fieldMap.put(ff.getName(), ff.getDataType());
            } else {
                if (fieldMap.get(ff.getName()) == null && ff.getDataType() != null) {
                    fieldMap.put(ff.getName(), ff.getDataType());
                }
            }
        });

        fieldMap.forEach((k, v) -> {
            if (v != null) {
                tb.add(k, v.getTypeClass());
            } else {
                tb.add(k, String.class);
            }
        });

        SimpleFeatureType featureType = tb.buildFeatureType();
        ListFeatureCollection featureCollection = new ListFeatureCollection(featureType);
        for (OguFeature f : layer.getFeatures()) {
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
            if (CharSequenceUtil.isNotBlank(f.getGeometry())) {
                builder.set("shape", GeometryUtil.wkt2Geometry(f.getGeometry()));
            } else {
                builder.set("shape", null);
            }

            layer.getFields().forEach(ff -> {
                Optional<OguFieldValue> optional = f.getAttributes().stream()
                        .filter(m -> CharSequenceUtil.equals(m.getField().getName(), ff.getName(), true))
                        .findFirst();
                optional.ifPresent(fieldValue -> builder.set(fieldValue.getField().getName(), fieldValue.getValue()));
            });

            featureCollection.add(builder.buildFeature(null));
        }

        return featureCollection;
    }
}
