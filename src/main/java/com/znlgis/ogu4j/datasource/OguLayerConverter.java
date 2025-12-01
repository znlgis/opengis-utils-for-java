package com.znlgis.ogu4j.datasource;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import com.znlgis.ogu4j.common.EncodingUtil;
import com.znlgis.ogu4j.common.CrsUtil;
import com.znlgis.ogu4j.enums.DataFormatType;
import com.znlgis.ogu4j.enums.FieldDataType;
import com.znlgis.ogu4j.enums.GeometryType;
import com.znlgis.ogu4j.enums.GisEngineType;
import com.znlgis.ogu4j.geometry.EsriGeometryUtil;
import com.znlgis.ogu4j.geometry.GeometryConverter;
import com.znlgis.ogu4j.geometry.JtsGeometryUtil;
import com.znlgis.ogu4j.model.layer.OguFeature;
import com.znlgis.ogu4j.model.layer.OguField;
import com.znlgis.ogu4j.model.layer.OguFieldValue;
import com.znlgis.ogu4j.model.layer.OguLayer;
import com.znlgis.ogu4j.model.layer.OguLayerMetadata;
import com.znlgis.ogu4j.model.DbConnBaseModel;
import lombok.SneakyThrows;
import org.gdal.gdal.gdal;
import org.geotools.api.data.FeatureWriter;
import org.geotools.api.data.Transaction;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
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
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.feature.type.Name;
import org.geotools.api.feature.type.PropertyDescriptor;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 图层格式转换工具类
 * <p>
 * 提供OguLayer与各种GIS数据格式（Shapefile、GeoJSON、FileGDB、PostGIS、TXT）之间的相互转换功能。
 * 支持使用GeoTools或GDAL/OGR两种引擎进行转换。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 */
public class OguLayerConverter {
    private OguLayerConverter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 将GeoTools SimpleFeatureCollection转换为OguLayer
     * <p>
     * 从GeoTools要素集合创建统一的OguLayer对象，自动提取坐标系、几何类型、字段定义和要素数据。
     * </p>
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
                oguFeature.setGeometry(EsriGeometryUtil.simplify(wkt, layer.getWkid()));

                if (layer.getGeometryType() == null) {
                    layer.setGeometryType(JtsGeometryUtil.geometryType(GeometryConverter.wkt2Geometry(wkt)));
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

    /**
     * 将OguLayer转换为GeoTools SimpleFeatureCollection
     * <p>
     * 将统一的OguLayer对象转换为GeoTools要素集合，用于与GeoTools生态系统集成。
     * </p>
     *
     * @param layer OguLayer图层对象
     * @return GeoTools SimpleFeatureCollection要素集合
     */
    @SneakyThrows
    public static SimpleFeatureCollection toSimpleFeatureCollection(OguLayer layer) {
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
                builder.set("shape", GeometryConverter.wkt2Geometry(f.getGeometry()));
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

    /**
     * 国土TXT坐标文件转换为OguLayer
     * <p>
     * 读取国土资源部门标准TXT坐标文件，解析属性描述和地块坐标数据。
     * </p>
     *
     * @param txtPath TXT文件路径
     * @param fields  字段定义列表，为null时使用默认字段定义
     * @return OguLayer图层对象
     */
    public static OguLayer fromTxtFile(String txtPath, List<OguField> fields) {
        return GtTxtUtil.loadTxt(txtPath, fields);
    }

    /**
     * 将OguLayer转换为国土TXT坐标文件
     * <p>
     * 按照国土资源部门标准格式导出TXT坐标文件，仅支持新建文件。
     * </p>
     *
     * @param layer      OguLayer图层对象
     * @param txtPath    TXT文件输出路径
     * @param metadata   属性描述和扩展信息
     * @param fieldNames 输出字段名称列表，为null时使用默认字段
     * @param zoneNumber 投影带号
     */
    @SneakyThrows
    public static void toTxtFile(OguLayer layer, String txtPath, OguLayerMetadata metadata, List<String> fieldNames, Integer zoneNumber) {
        layer.validate();
        EsriGeometryUtil.excludeSpecialFields(layer.getFields());
        GtTxtUtil.saveTxt(layer, txtPath, metadata, fieldNames, zoneNumber);
    }

    /**
     * GeoJSON文件转换为OguLayer
     * <p>
     * 读取GeoJSON格式的地理数据文件，支持GeoTools和GDAL两种引擎。
     * </p>
     *
     * @param geojsonPath   GeoJSON文件路径
     * @param gisEngineType GIS引擎类型（GEOTOOLS/GDAL/AUTO）
     * @return OguLayer图层对象
     */
    @SneakyThrows
    public static OguLayer fromGeoJSON(String geojsonPath, GisEngineType gisEngineType) {
        gisEngineType = GisEngineType.getGisEngineType(gisEngineType);
        if (gisEngineType == GisEngineType.GDAL) {
            String layerName = FileUtil.mainName(geojsonPath);
            return OgrUtil.layer2OguLayer(DataFormatType.GEOJSON, geojsonPath, layerName, null, null);
        } else {
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
        }
    }

    /**
     * 将OguLayer转换为GeoJSON文件
     * <p>
     * 将图层数据导出为GeoJSON格式文件，仅支持新建文件。
     * 支持GeoTools和GDAL两种引擎。
     * </p>
     *
     * @param layer         OguLayer图层对象
     * @param geojsonPath   GeoJSON文件输出路径
     * @param gisEngineType GIS引擎类型（GEOTOOLS/GDAL/AUTO）
     */
    @SneakyThrows
    public static void toGeoJSON(OguLayer layer, String geojsonPath, GisEngineType gisEngineType) {
        layer.validate();
        EsriGeometryUtil.excludeSpecialFields(layer.getFields());

        gisEngineType = GisEngineType.getGisEngineType(gisEngineType);
        if (gisEngineType == GisEngineType.GDAL) {
            String layerName = FileUtil.mainName(geojsonPath);
            OgrUtil.oguLayer2Layer(DataFormatType.GEOJSON, geojsonPath, layer, layerName, null);
        } else {
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
        }
    }

    /**
     * Shapefile文件转换为OguLayer
     * <p>
     * 读取ESRI Shapefile格式数据，支持属性过滤和空间过滤。
     * 自动检测文件编码（支持UTF-8、GBK等）。
     * </p>
     *
     * @param shpPath          Shapefile文件路径（.shp文件）
     * @param attributeFilter  属性过滤条件（CQL表达式），为null时不过滤
     * @param spatialFilterWkt 空间过滤条件（WKT格式），为null时不过滤
     * @param gisEngineType    GIS引擎类型（GEOTOOLS/GDAL/AUTO）
     * @return OguLayer图层对象
     */
    @SneakyThrows
    public static OguLayer fromShapefile(String shpPath, String attributeFilter, String spatialFilterWkt, GisEngineType gisEngineType) {
        Charset shpCharset = ShpUtil.check(shpPath);
        gisEngineType = GisEngineType.getGisEngineType(gisEngineType);
        if (gisEngineType == GisEngineType.GDAL) {
            gdal.SetConfigOption("SHAPE_ENCODING", shpCharset.name());
            String shpDir = FileUtil.getParent(shpPath, 1);
            String shpName = FileUtil.mainName(shpPath);
            return OgrUtil.layer2OguLayer(DataFormatType.SHP, shpDir, shpName, attributeFilter, spatialFilterWkt);
        } else {
            File file = new File(shpPath);
            ShapefileDataStore shpDataStore = new ShapefileDataStore(file.toURI().toURL());
            shpDataStore.setCharset(shpCharset);
            String typeName = shpDataStore.getTypeNames()[0];
            SimpleFeatureSource source = shpDataStore.getFeatureSource(typeName);
            SimpleFeatureCollection simpleFeatureCollection = GeotoolsUtil.filter(source, attributeFilter, spatialFilterWkt);
            shpDataStore.dispose();
            return fromSimpleFeatureCollection(simpleFeatureCollection);
        }
    }

    /**
     * 将OguLayer转换为Shapefile文件
     * <p>
     * 将图层数据导出为ESRI Shapefile格式，仅支持新建文件。
     * 自动处理字段名长度限制（最大10字符）和编码（UTF-8）。
     * </p>
     *
     * @param layer         OguLayer图层对象
     * @param shpPath       Shapefile文件输出路径
     * @param gisEngineType GIS引擎类型（GEOTOOLS/GDAL/AUTO）
     */
    @SneakyThrows
    public static void toShapefile(OguLayer layer, String shpPath, GisEngineType gisEngineType) {
        layer.validate();
        EsriGeometryUtil.excludeSpecialFields(layer.getFields());
        ShpUtil.formatFieldName(layer.getFields());

        gisEngineType = GisEngineType.getGisEngineType(gisEngineType);
        if (gisEngineType == GisEngineType.GDAL) {
            gdal.SetConfigOption("SHAPE_ENCODING", "");
            Vector options = new Vector();
            options.add("ENCODING=UTF-8");
            String shpDir = FileUtil.getParent(shpPath, 1);
            String shpName = FileUtil.mainName(shpPath);
            OgrUtil.oguLayer2Layer(DataFormatType.SHP, shpDir, layer, shpName, options);
        } else {
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
        }
    }

    /**
     * FileGDB图层转换为OguLayer
     * <p>
     * 从ESRI FileGDB（文件地理数据库）读取指定图层。
     * 仅支持GDAL引擎（需要GDAL环境支持）。
     * </p>
     *
     * @param gdbPath          FileGDB路径（.gdb目录）
     * @param layerName        图层名称
     * @param attributeFilter  属性过滤条件（SQL WHERE子句），为null时不过滤
     * @param spatialFilterWkt 空间过滤条件（WKT格式），为null时不过滤
     * @param gisEngineType    GIS引擎类型（必须为GDAL）
     * @return OguLayer图层对象
     * @throws RuntimeException 如果引擎类型不是GDAL
     */
    @SneakyThrows
    public static OguLayer fromFileGDB(String gdbPath, String layerName, String attributeFilter, String spatialFilterWkt, GisEngineType gisEngineType) {
        gisEngineType = GisEngineType.getGisEngineType(gisEngineType);
        if (gisEngineType == GisEngineType.GDAL) {
            return OgrUtil.layer2OguLayer(DataFormatType.FILEGDB, gdbPath, layerName, attributeFilter, spatialFilterWkt);
        } else {
            throw new RuntimeException("GDB数据源需要GDAL支持");
        }
    }

    /**
     * 将OguLayer转换为FileGDB图层
     * <p>
     * 将图层数据导出到ESRI FileGDB（文件地理数据库）。
     * 图层存在时追加数据，不存在时新建图层。
     * 仅支持GDAL引擎（需要GDAL环境支持）。
     * </p>
     *
     * @param layer          OguLayer图层对象
     * @param gdbPath        FileGDB路径（.gdb目录）
     * @param featureDataset 要素集名称，为null时直接创建在GDB根目录
     * @param layerName      图层名称
     * @param gisEngineType  GIS引擎类型（必须为GDAL）
     * @throws RuntimeException 如果引擎类型不是GDAL
     */
    @SneakyThrows
    public static void toFileGDB(OguLayer layer, String gdbPath, String featureDataset, String layerName, GisEngineType gisEngineType) {
        layer.validate();
        EsriGeometryUtil.excludeSpecialFields(layer.getFields());

        gisEngineType = GisEngineType.getGisEngineType(gisEngineType);
        if (gisEngineType == GisEngineType.GDAL) {
            Vector options = null;
            if (CharSequenceUtil.isNotBlank(featureDataset)) {
                options = new Vector();
                options.add("FEATURE_DATASET=" + featureDataset);
            }
            OgrUtil.oguLayer2Layer(DataFormatType.FILEGDB, gdbPath, layer, layerName, options);
        } else {
            throw new RuntimeException("GDB数据源需要GDAL支持");
        }
    }

    /**
     * PostGIS图层转换为OguLayer
     * <p>
     * 从PostGIS空间数据库读取指定图层。
     * 支持GeoTools和GDAL两种引擎。
     * </p>
     *
     * @param dbConnBaseModel  数据库连接配置
     * @param layerName        图层名称（表名）
     * @param attributeFilter  属性过滤条件（SQL WHERE子句），为null时不过滤
     * @param spatialFilterWkt 空间过滤条件（WKT格式），为null时不过滤
     * @param gisEngineType    GIS引擎类型（GEOTOOLS/GDAL/AUTO）
     * @return OguLayer图层对象
     */
    @SneakyThrows
    public static OguLayer fromPostGIS(DbConnBaseModel dbConnBaseModel, String layerName, String attributeFilter, String spatialFilterWkt, GisEngineType gisEngineType) {
        gisEngineType = GisEngineType.getGisEngineType(gisEngineType);
        if (gisEngineType == GisEngineType.GDAL) {
            return OgrUtil.layer2OguLayer(DataFormatType.POSTGIS, PostgisUtil.toGdalPostgisConnStr(dbConnBaseModel), layerName, attributeFilter, spatialFilterWkt);
        } else {
            JDBCDataStore dataStore = PostgisUtil.getPostgisDataStore(dbConnBaseModel);
            SimpleFeatureSource source = dataStore.getFeatureSource(layerName);
            SimpleFeatureCollection simpleFeatureCollection = GeotoolsUtil.filter(source, attributeFilter, spatialFilterWkt);
            dataStore.dispose();
            return fromSimpleFeatureCollection(simpleFeatureCollection);
        }
    }

    /**
     * 将OguLayer转换为PostGIS图层
     * <p>
     * 将图层数据导出到PostGIS空间数据库。
     * 图层（表）存在时追加数据，不存在时新建表。
     * 支持GeoTools和GDAL两种引擎，GeoTools引擎使用批量写入优化性能。
     * </p>
     *
     * @param layer           OguLayer图层对象
     * @param dbConnBaseModel 数据库连接配置
     * @param layerName       图层名称（表名）
     * @param gisEngineType   GIS引擎类型（GEOTOOLS/GDAL/AUTO）
     */
    @SneakyThrows
    public static void toPostGIS(OguLayer layer, DbConnBaseModel dbConnBaseModel, String layerName, GisEngineType gisEngineType) {
        layer.validate();
        EsriGeometryUtil.excludeSpecialFields(layer.getFields());

        gisEngineType = GisEngineType.getGisEngineType(gisEngineType);
        if (gisEngineType == GisEngineType.GDAL) {
            OgrUtil.oguLayer2Layer4Postgis(DataFormatType.POSTGIS, dbConnBaseModel, layer, layerName, null);
        } else {
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
        }
    }
}
