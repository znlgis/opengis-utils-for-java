package com.znlgis.ogu4j.datasource;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import com.znlgis.ogu4j.common.CrsUtil;
import com.znlgis.ogu4j.enums.DataFormatType;
import com.znlgis.ogu4j.enums.FieldDataType;
import com.znlgis.ogu4j.enums.GeometryType;
import com.znlgis.ogu4j.geometry.EsriGeometryUtil;
import com.znlgis.ogu4j.geometry.GeometryConverter;
import com.znlgis.ogu4j.geometry.JtsGeometryUtil;
import com.znlgis.ogu4j.model.layer.OguFeature;
import com.znlgis.ogu4j.model.layer.OguField;
import com.znlgis.ogu4j.model.layer.OguFieldValue;
import com.znlgis.ogu4j.model.layer.OguLayer;
import com.znlgis.ogu4j.model.DbConnBaseModel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.gdal.gdal.gdal;
import org.gdal.ogr.*;
import org.gdal.osr.SpatialReference;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * GDAL/OGR工具类
 * <p>
 * 提供基于GDAL/OGR库的GIS数据读写功能，支持Shapefile、FileGDB、GeoJSON、PostGIS等多种格式。
 * 使用前需确保GDAL环境已正确安装和配置。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 *
 * @author znlgis
 * @since 1.0.0
 */
public class OgrUtil {
    @Getter
    private static Boolean ogrInitSuccess;

    static {
        try {
            ogr.RegisterAll();
            gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");

            ogrInitSuccess = true;
        } catch (Throwable e) {
            ogrInitSuccess = false;
        }
    }

    private OgrUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 检查GDAL环境
     */
    public static void checkGdalEnv() {
        if (!Boolean.TRUE.equals(ogrInitSuccess)) {
            throw new RuntimeException("OGR初始化失败");
        }
    }

    /**
     * 获取驱动
     *
     * @param driverType 驱动类型
     * @return 驱动
     */
    private static Driver getDriver(DataFormatType driverType) {
        checkGdalEnv();
        return ogr.GetDriverByName(driverType.getGdalDriverName());
    }

    /**
     * 创建数据源
     *
     * @param driverType 驱动类型
     * @param path       数据源路径
     * @return 数据源
     */
    public static DataSource createDataSource(DataFormatType driverType, String path) {
        Driver driver = getDriver(driverType);
        return driver.CreateDataSource(path);
    }

    /**
     * 打开数据源
     *
     * @param driverType 驱动类型
     * @param path       数据源路径
     * @return 数据源
     */
    public static DataSource openDataSource(DataFormatType driverType, String path) {
        Driver driver = getDriver(driverType);
        return driver.Open(path, 1);
    }

    /**
     * 关闭数据源
     *
     * @param dataSource 数据源
     */
    public static void closeDataSource(DataSource dataSource) {
        if (dataSource != null) {
            dataSource.delete();
        }
    }

    /**
     * 获取图层名称
     *
     * @param dataSource 数据源
     * @return 图层名称
     */
    public static List<String> getLayerNames(DataSource dataSource) {
        List<String> layerNames = new ArrayList<>();
        if (dataSource.GetLayerCount() <= 0) {
            return layerNames;
        }

        for (int i = 0; i < dataSource.GetLayerCount(); i++) {
            Layer layer = dataSource.GetLayer(i);
            layerNames.add(layer.GetName());
        }
        return layerNames;
    }

    /**
     * 获取图层
     *
     * @param dataSource 数据源
     * @param layerIndex 图层索引
     * @return 图层
     */
    public static Layer getLayer(DataSource dataSource, int layerIndex) {
        return dataSource.GetLayer(layerIndex);
    }

    /**
     * 获取图层
     *
     * @param dataSource 数据源
     * @param layerName  图层名称
     * @return 图层
     */
    public static Layer getLayer(DataSource dataSource, String layerName) {
        return dataSource.GetLayerByName(layerName);
    }

    /**
     * Layer转OguLayer
     *
     * @param layer            图层
     * @param attributeFilter  属性过滤条件
     * @param spatialFilterWkt 空间过滤条件
     * @return OguLayer
     */
    public static OguLayer layer2OguLayer(Layer layer, String attributeFilter, String spatialFilterWkt) {
        OguLayer oguLayer = new OguLayer();
        oguLayer.setName(layer.GetName());
        oguLayer.setAlias(layer.GetName());

        SpatialReference sr = layer.GetSpatialRef();
        Map.Entry<Integer, CoordinateReferenceSystem> m = CrsUtil.standardizeCRS(sr.ExportToWkt());
        oguLayer.setWkid(m.getKey());
        oguLayer.setTolerance(CrsUtil.getTolerance(m.getValue()));
        int geotype = layer.GetGeomType();
        oguLayer.setGeometryType(GeometryType.valueOfByWkbGeometryType(geotype));

        List<OguField> fields = new ArrayList<>();
        for (int i = 0; i < layer.GetLayerDefn().GetFieldCount(); i++) {
            OguField field = new OguField();
            field.setName(layer.GetLayerDefn().GetFieldDefn(i).GetName());
            field.setAlias(layer.GetLayerDefn().GetFieldDefn(i).GetNameRef());
            field.setDataType(FieldDataType.fieldDataTypeByGdalCode(layer.GetLayerDefn().GetFieldDefn(i).GetFieldType()));
            fields.add(field);
        }

        oguLayer.setFields(fields);

        List<OguFeature> features = new ArrayList<>();
        if (CharSequenceUtil.isNotBlank(attributeFilter)) {
            layer.SetAttributeFilter(attributeFilter);
        }

        if (CharSequenceUtil.isNotBlank(spatialFilterWkt)) {
            spatialFilterWkt = EsriGeometryUtil.simplify(spatialFilterWkt, oguLayer.getWkid());
            Geometry spatialFilter = ogr.CreateGeometryFromWkt(spatialFilterWkt);
            layer.SetSpatialFilter(spatialFilter);
        }

        Feature feature = layer.GetNextFeature();
        while (feature != null) {
            OguFeature oguFeature = new OguFeature();
            String wkt = feature.GetGeometryRef().ExportToWkt();
            oguFeature.setGeometry(EsriGeometryUtil.simplify(wkt, oguLayer.getWkid()));

            if (oguLayer.getGeometryType() == null) {
                oguLayer.setGeometryType(JtsGeometryUtil.geometryType(GeometryConverter.wkt2Geometry(wkt)));
            }

            long id = feature.GetFID();
            if (id >= 0) {
                oguFeature.setId(String.valueOf(id));
            } else {
                oguFeature.setId(IdUtil.simpleUUID());
            }

            List<OguFieldValue> fieldValues = new ArrayList<>();
            for (OguField field : fields) {
                String fieldName = field.getName();
                OguFieldValue fieldValue = new OguFieldValue();
                fieldValue.setField(field);
                switch (field.getDataType()) {
                    case INTEGER:
                        fieldValue.setValue(feature.GetFieldAsInteger(fieldName));
                        break;
                    case DOUBLE:
                        fieldValue.setValue(feature.GetFieldAsDouble(fieldName));
                        break;
                    case BINARY:
                        fieldValue.setValue(feature.GetFieldAsBinary(fieldName));
                        break;
                    case LONG:
                        fieldValue.setValue(feature.GetFieldAsInteger64(fieldName));
                        break;
                    case DATE:
                    case TIME:
                    case DATETIME:
                        fieldValue.setValue(feature.GetFieldAsISO8601DateTime(fieldName));
                        break;
                    case STRING:
                    default:
                        fieldValue.setValue(feature.GetFieldAsString(fieldName));
                }

                fieldValues.add(fieldValue);
            }

            oguFeature.setAttributes(fieldValues);
            features.add(oguFeature);
            feature = layer.GetNextFeature();
        }

        oguLayer.setFeatures(features);
        oguLayer.validate();
        return oguLayer;
    }

    /**
     * 创建图层
     *
     * @param dataSource   数据源
     * @param layerName    图层名称
     * @param wkid         wkid
     * @param geometryType 几何类型
     * @param options      选项
     * @return 图层
     */
    public static Layer createLayer(DataSource dataSource, String layerName, Integer wkid, GeometryType geometryType, Vector options) {
        SpatialReference sr = new SpatialReference();
        sr.ImportFromEPSG(wkid);
        return dataSource.CreateLayer(layerName, sr, geometryType.getWkbGeometryType(), options);
    }

    /**
     * 初始化图层
     *
     * @param driverType  驱动类型
     * @param path        数据源路径
     * @param oguLayer OguLayer
     * @param layerName   图层名称
     * @param options     选项
     */
    private static void initLayer(DataFormatType driverType, String path, OguLayer oguLayer, String layerName,
                                  Vector options) {
        DataSource dataSource = OgrUtil.openDataSource(driverType, path);
        if (dataSource == null) {
            dataSource = OgrUtil.createDataSource(driverType, path);
        }

        Layer layer = OgrUtil.getLayer(dataSource, layerName);
        if (layer == null) {
            layer = OgrUtil.createLayer(dataSource, layerName, oguLayer.getWkid(), oguLayer.getGeometryType(), options);
        }

        for (OguField field : oguLayer.getFields()) {
            if (layer.GetLayerDefn().GetFieldIndex(field.getName()) < 0) {
                FieldDefn fieldDefn = new FieldDefn(field.getName(), field.getDataType().getDefaultGdalCode());
                layer.CreateField(fieldDefn);
            }
        }

        closeDataSource(dataSource);
    }

    /**
     * 要素集合转图层
     *
     * @param driverType 驱动类型
     * @param path       数据源路径
     * @param fields     OguField集合
     * @param features   OguFeature集合
     * @param layerName  图层名称
     */
    private static void oguFeatures2Layer(DataFormatType driverType, String path, List<OguField> fields, List<OguFeature> features, String layerName) {
        DataSource dataSource = OgrUtil.openDataSource(driverType, path);
        Layer layer;
        if (CharSequenceUtil.isNotBlank(layerName)) {
            layer = OgrUtil.getLayer(dataSource, layerName);
        } else {
            layer = OgrUtil.getLayer(dataSource, 0);
        }

        for (OguField field : fields) {
            if (layer.GetLayerDefn().GetFieldIndex(field.getName()) < 0) {
                FieldDefn fieldDefn = new FieldDefn(field.getName(), field.getDataType().getDefaultGdalCode());
                layer.CreateField(fieldDefn);
            }
        }

        for (OguFeature oguFeature : features) {
            Feature feature = new Feature(layer.GetLayerDefn());
            feature.SetGeometry(ogr.CreateGeometryFromWkt(oguFeature.getGeometry()));
            for (int i = 0; i < layer.GetLayerDefn().GetFieldCount(); i++) {
                FieldDefn fieldDefn = layer.GetLayerDefn().GetFieldDefn(i);
                String fieldName = fieldDefn.GetName();
                Optional<OguFieldValue> kv = oguFeature.getAttributes().stream().filter(kvModel ->
                        kvModel.getField().getName().equalsIgnoreCase(fieldName)).findFirst();
                if (!kv.isPresent() || kv.get().getValue() == null) {
                    continue;
                }
                switch (FieldDataType.fieldDataTypeByGdalCode(fieldDefn.GetFieldType())) {
                    case INTEGER:
                        feature.SetField(fieldName,
                                NumberUtil.parseInt(kv.get().getValue().toString()));
                        break;
                    case DOUBLE:
                        feature.SetField(fieldName,
                                NumberUtil.parseDouble(kv.get().getValue().toString()));
                        break;
                    case BINARY:
                        feature.SetFieldBinaryFromHexString(fieldName,
                                HexUtil.encodeHexStr((byte[]) kv.get().getValue()));
                        break;
                    case LONG:
                        feature.SetFieldInteger64(i,
                                NumberUtil.parseLong(kv.get().getValue().toString()));
                        break;
                    case DATE:
                    case TIME:
                    case DATETIME:
                    case STRING:
                    default:
                        feature.SetField(fieldName, kv.get().getValue().toString());
                }
            }

            layer.CreateFeature(feature);
        }

        closeDataSource(dataSource);
    }

    /**
     * 要素集合转POSTGIS图层
     *
     * @param driverType  驱动名称
     * @param path        数据源路径
     * @param oguLayer OguLayer
     * @param layerName   图层名称
     */
    @SneakyThrows
    private static void oguLayer2Layer4Postgis(DataFormatType driverType, String path, OguLayer oguLayer, String layerName) {
        int batchSize = 1000;
        int count = oguLayer.getFeatures().size() / batchSize;
        ExecutorService executorService = ThreadUtil.newExecutor(count);
        for (int j = 0; j <= count; j++) {
            List<OguFeature> subList;
            if (j == count) {
                subList = oguLayer.getFeatures().subList(j * batchSize, oguLayer.getFeatures().size());
            } else {
                subList = oguLayer.getFeatures().subList(j * batchSize, (j + 1) * batchSize);
            }

            executorService.execute(() -> {
                try {
                    oguFeatures2Layer(driverType, path, oguLayer.getFields(), subList, layerName);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    /**
     * OguLayer转图层
     *
     * @param driverType  驱动类型
     * @param path        数据源路径
     * @param oguLayer OguLayer
     * @param layerName   图层名称
     * @param options     选项
     */
    public static void oguLayer2Layer(DataFormatType driverType, String path, OguLayer oguLayer, String layerName, Vector options) {
        initLayer(driverType, path, oguLayer, layerName, options);
        oguFeatures2Layer(driverType, path, oguLayer.getFields(), oguLayer.getFeatures(), layerName);
    }

    /**
     * OguLayer转POSTGIS图层
     *
     * @param driverType      驱动类型
     * @param dbConnBaseModel 数据库连接
     * @param oguLayer     OguLayer
     * @param layerName       图层名称
     * @param options         选项
     */
    public static void oguLayer2Layer4Postgis(DataFormatType driverType, DbConnBaseModel dbConnBaseModel, OguLayer oguLayer, String layerName, Vector options) {
        if (options == null) {
            options = new Vector();
        }
        options.add("GEOMETRY_NAME=SHAPE");
        options.add("FID=FID");
        options.add("FID64=TRUE");
        String path = PostgisUtil.toGdalPostgisConnStr(dbConnBaseModel);
        initLayer(driverType, path, oguLayer, layerName, options);
        oguLayer2Layer4Postgis(driverType, path, oguLayer, layerName);
    }

    /**
     * 图层转OguLayer
     *
     * @param driverType       驱动类型
     * @param path             数据源路径
     * @param layerName        图层名称
     * @param attributeFilter  属性过滤条件
     * @param spatialFilterWkt 空间过滤条件
     * @return OguLayer
     */
    public static OguLayer layer2OguLayer(DataFormatType driverType, String path, String layerName, String attributeFilter, String spatialFilterWkt) {
        DataSource dataSource = OgrUtil.openDataSource(driverType, path);
        Layer layer = OgrUtil.getLayer(dataSource, layerName);
        OguLayer oguLayer = OgrUtil.layer2OguLayer(layer, attributeFilter, spatialFilterWkt);
        OgrUtil.closeDataSource(dataSource);
        return oguLayer;
    }
}
