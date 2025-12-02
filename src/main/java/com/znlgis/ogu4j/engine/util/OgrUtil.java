package com.znlgis.ogu4j.engine.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import com.znlgis.ogu4j.engine.enums.DataFormatType;
import com.znlgis.ogu4j.engine.enums.FieldDataType;
import com.znlgis.ogu4j.engine.enums.GeometryType;
import com.znlgis.ogu4j.exception.EngineNotSupportedException;
import com.znlgis.ogu4j.geometry.GeometryUtil;
import com.znlgis.ogu4j.engine.model.layer.OguFeature;
import com.znlgis.ogu4j.engine.model.layer.OguField;
import com.znlgis.ogu4j.engine.model.layer.OguFieldValue;
import com.znlgis.ogu4j.engine.model.layer.OguLayer;
import com.znlgis.ogu4j.engine.model.DbConnBaseModel;
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
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 * @see com.znlgis.ogu4j.engine.GdalEngine
 * @see com.znlgis.ogu4j.engine.GdalLayerReader
 * @see com.znlgis.ogu4j.engine.GdalLayerWriter
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
     * 检查GDAL环境是否已正确初始化
     *
     * @throws EngineNotSupportedException 如果OGR初始化失败
     */
    public static void checkGdalEnv() throws EngineNotSupportedException {
        if (!Boolean.TRUE.equals(ogrInitSuccess)) {
            throw new EngineNotSupportedException("OGR初始化失败");
        }
    }

    /**
     * 获取GDAL/OGR驱动
     *
     * @param driverType 数据格式类型
     * @return OGR驱动对象
     * @throws RuntimeException 如果OGR未初始化
     */
    private static Driver getDriver(DataFormatType driverType) {
        checkGdalEnv();
        return ogr.GetDriverByName(driverType.getGdalDriverName());
    }

    /**
     * 创建OGR数据源
     * <p>
     * 根据数据格式类型创建新的数据源。如果数据源已存在，行为取决于驱动类型。
     * </p>
     *
     * @param driverType 数据格式类型
     * @param path       数据源路径（文件路径或目录路径）
     * @return OGR数据源对象
     */
    public static DataSource createDataSource(DataFormatType driverType, String path) {
        Driver driver = getDriver(driverType);
        return driver.CreateDataSource(path);
    }

    /**
     * 打开已存在的OGR数据源
     * <p>
     * 以可读写模式打开数据源。如果数据源不存在，返回null。
     * </p>
     *
     * @param driverType 数据格式类型
     * @param path       数据源路径（文件路径或目录路径）
     * @return OGR数据源对象，如果打开失败返回null
     */
    public static DataSource openDataSource(DataFormatType driverType, String path) {
        Driver driver = getDriver(driverType);
        return driver.Open(path, 1);
    }

    /**
     * 关闭OGR数据源
     * <p>
     * 释放数据源相关资源。如果数据源为null，则不执行任何操作。
     * </p>
     *
     * @param dataSource 要关闭的数据源
     */
    public static void closeDataSource(DataSource dataSource) {
        if (dataSource != null) {
            dataSource.delete();
        }
    }

    /**
     * 获取数据源中的所有图层名称
     *
     * @param dataSource OGR数据源
     * @return 图层名称列表，如果没有图层则返回空列表
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
     * 根据索引获取图层
     *
     * @param dataSource OGR数据源
     * @param layerIndex 图层索引（从0开始）
     * @return OGR图层对象
     */
    public static Layer getLayer(DataSource dataSource, int layerIndex) {
        return dataSource.GetLayer(layerIndex);
    }

    /**
     * 根据名称获取图层
     *
     * @param dataSource OGR数据源
     * @param layerName  图层名称
     * @return OGR图层对象，如果不存在返回null
     */
    public static Layer getLayer(DataSource dataSource, String layerName) {
        return dataSource.GetLayerByName(layerName);
    }

    /**
     * 将OGR图层转换为OguLayer
     * <p>
     * 读取OGR图层的所有要素，转换为OguLayer对象。
     * 支持属性过滤和空间过滤条件。
     * </p>
     *
     * @param layer            OGR图层对象
     * @param attributeFilter  属性过滤条件（SQL WHERE子句），为null时不过滤
     * @param spatialFilterWkt 空间过滤条件（WKT格式），为null时不过滤
     * @return OguLayer图层对象
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
            spatialFilterWkt = GeometryUtil.simplifyWkt(spatialFilterWkt, oguLayer.getWkid());
            Geometry spatialFilter = ogr.CreateGeometryFromWkt(spatialFilterWkt);
            layer.SetSpatialFilter(spatialFilter);
        }

        Feature feature = layer.GetNextFeature();
        while (feature != null) {
            OguFeature oguFeature = new OguFeature();
            String wkt = feature.GetGeometryRef().ExportToWkt();
            oguFeature.setGeometry(GeometryUtil.simplifyWkt(wkt, oguLayer.getWkid()));

            if (oguLayer.getGeometryType() == null) {
                oguLayer.setGeometryType(GeometryUtil.geometryType(GeometryUtil.wkt2Geometry(wkt)));
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
     * 在数据源中创建新图层
     * <p>
     * 创建具有指定坐标系和几何类型的新图层。
     * </p>
     *
     * @param dataSource   OGR数据源
     * @param layerName    图层名称
     * @param wkid         坐标系WKID（EPSG代码）
     * @param geometryType 几何类型
     * @param options      创建选项（可选参数）
     * @return 新创建的OGR图层对象
     */
    public static Layer createLayer(DataSource dataSource, String layerName, Integer wkid, GeometryType geometryType, Vector options) {
        SpatialReference sr = new SpatialReference();
        sr.ImportFromEPSG(wkid);
        return dataSource.CreateLayer(layerName, sr, geometryType.getWkbGeometryType(), options);
    }

    /**
     * 初始化图层结构
     * <p>
     * 如果数据源不存在则创建，如果图层不存在则创建。
     * 自动添加OguLayer中定义的字段到图层中。
     * </p>
     *
     * @param driverType 数据格式类型
     * @param path       数据源路径
     * @param oguLayer   OguLayer图层对象（提供字段定义和坐标系信息）
     * @param layerName  图层名称
     * @param options    创建选项
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
     * 将OguFeature列表写入OGR图层
     * <p>
     * 批量将OguFeature要素写入指定的OGR图层。
     * 会自动添加缺失的字段定义。
     * </p>
     *
     * @param driverType 数据格式类型
     * @param path       数据源路径
     * @param fields     字段定义列表
     * @param features   要素列表
     * @param layerName  图层名称，为空时使用第一个图层
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
     * 将OguLayer写入PostGIS图层（批量处理）
     * <p>
     * 使用多线程批量写入要素到PostGIS数据库，每批1000条记录。
     * 适用于大数据量的高效写入场景。
     * </p>
     *
     * @param driverType 数据格式类型
     * @param path       PostGIS连接字符串
     * @param oguLayer   OguLayer图层对象
     * @param layerName  图层名称
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
     * 将OguLayer写入OGR图层
     * <p>
     * 将OguLayer转换并写入指定的数据源中。
     * 如果图层不存在则创建，存在则追加数据。
     * </p>
     *
     * @param driverType 数据格式类型
     * @param path       数据源路径
     * @param oguLayer   OguLayer图层对象
     * @param layerName  图层名称
     * @param options    创建选项
     */
    public static void oguLayer2Layer(DataFormatType driverType, String path, OguLayer oguLayer, String layerName, Vector options) {
        initLayer(driverType, path, oguLayer, layerName, options);
        oguFeatures2Layer(driverType, path, oguLayer.getFields(), oguLayer.getFeatures(), layerName);
    }

    /**
     * 将OguLayer写入PostGIS图层
     * <p>
     * 将OguLayer转换并写入PostGIS数据库。
     * 自动配置几何字段名称为SHAPE，FID字段为FID，支持64位FID。
     * 使用批量多线程写入优化性能。
     * </p>
     *
     * @param driverType      数据格式类型
     * @param dbConnBaseModel 数据库连接配置
     * @param oguLayer        OguLayer图层对象
     * @param layerName       图层名称
     * @param options         创建选项（可为null）
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
     * 从数据源读取图层并转换为OguLayer
     * <p>
     * 便捷方法，自动处理数据源的打开和关闭。
     * 支持属性过滤和空间过滤条件。
     * </p>
     *
     * @param driverType       数据格式类型
     * @param path             数据源路径
     * @param layerName        图层名称
     * @param attributeFilter  属性过滤条件（SQL WHERE子句），为null时不过滤
     * @param spatialFilterWkt 空间过滤条件（WKT格式），为null时不过滤
     * @return OguLayer图层对象
     */
    public static OguLayer layer2OguLayer(DataFormatType driverType, String path, String layerName, String attributeFilter, String spatialFilterWkt) {
        DataSource dataSource = OgrUtil.openDataSource(driverType, path);
        Layer layer = OgrUtil.getLayer(dataSource, layerName);
        OguLayer oguLayer = OgrUtil.layer2OguLayer(layer, attributeFilter, spatialFilterWkt);
        OgrUtil.closeDataSource(dataSource);
        return oguLayer;
    }
}
