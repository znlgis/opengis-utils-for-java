package com.znlgis.ogu4j.datasource;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.znlgis.ogu4j.common.EncodingUtil;
import com.znlgis.ogu4j.common.NumUtil;
import com.znlgis.ogu4j.common.CrsUtil;
import com.znlgis.ogu4j.enums.FieldDataType;
import com.znlgis.ogu4j.enums.GeometryType;
import com.znlgis.ogu4j.geometry.EsriGeometryUtil;
import com.znlgis.ogu4j.geometry.GeometryConverter;
import com.znlgis.ogu4j.model.layer.OguCoordinate;
import com.znlgis.ogu4j.model.layer.OguFeature;
import com.znlgis.ogu4j.model.layer.OguField;
import com.znlgis.ogu4j.model.layer.OguFieldValue;
import com.znlgis.ogu4j.model.layer.OguLayer;
import com.znlgis.ogu4j.model.layer.OguLayerMetadata;
import lombok.SneakyThrows;
import org.locationtech.jts.geom.*;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 国土TXT坐标文件工具类
 * <p>
 * 提供国土资源部门TXT坐标文件格式的读取和写入功能。
 * TXT文件包含属性描述和地块坐标两个主要部分。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 */
public class GtTxtUtil {
    private GtTxtUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 保存OguLayer为TXT文件
     *
     * @param layer      OguLayer
     * @param txtPath    TXT文件路径
     * @param metadata   元数据信息
     * @param fieldNames 字段名称顺序
     * @param zoneNumber 带号
     */
    @SneakyThrows
    public static void saveTxt(OguLayer layer, String txtPath, OguLayerMetadata metadata, List<String> fieldNames, Integer zoneNumber) {
        // 转换为内部表示并保存
        OguLayerMetadata layerMetadata = prepareMetadata(layer, metadata, zoneNumber);
        List<String> txtLines = new ArrayList<>();

        // 写入扩展信息
        if (layerMetadata.getExtendedInfos() != null && !layerMetadata.getExtendedInfos().isEmpty()) {
            for (OguLayerMetadata.ExtendedInfo extInfo : layerMetadata.getExtendedInfos()) {
                txtLines.add("[" + extInfo.getName() + "]");
                for (Map.Entry<String, String> entry : extInfo.getProperties().entrySet()) {
                    txtLines.add(entry.getKey() + "=" + entry.getValue());
                }
            }
        }

        // 写入属性描述
        txtLines.add("[属性描述]");
        txtLines.add("格式版本号=" + (layerMetadata.getFormatVersion() == null ? "" : layerMetadata.getFormatVersion()));
        txtLines.add("数据产生单位=" + (layerMetadata.getDataSource() == null ? "自然资源部" : layerMetadata.getDataSource()));
        txtLines.add("数据产生日期=" + (layerMetadata.getDataDate() == null ? DatePattern.NORM_DATE_FORMAT.format(DateUtil.date()) : layerMetadata.getDataDate()));
        txtLines.add("坐标系=" + (layerMetadata.getCoordinateSystemName() == null ? "2000国家大地坐标系" : layerMetadata.getCoordinateSystemName()));
        txtLines.add("几度分带=" + (layerMetadata.getZoneDivision() == null ? "3" : layerMetadata.getZoneDivision()));
        txtLines.add("投影类型=" + (layerMetadata.getProjectionType() == null ? "高斯克吕格" : layerMetadata.getProjectionType()));
        txtLines.add("计量单位=" + (layerMetadata.getMeasureUnit() == null ? "米" : layerMetadata.getMeasureUnit()));
        txtLines.add("带号=" + (layerMetadata.getZoneNumber() == null ? String.valueOf(zoneNumber) : layerMetadata.getZoneNumber()));
        txtLines.add("精度=" + (layerMetadata.getPrecision() == null ? "0.01" : layerMetadata.getPrecision()));
        txtLines.add("转换参数=" + (layerMetadata.getTransformParams() == null ? "0,0,0,0,0,0,0" : layerMetadata.getTransformParams()));

        // 处理字段名称
        if (fieldNames == null || fieldNames.isEmpty()) {
            fieldNames = getDefaultFieldNames();
        }

        // 写入地块坐标
        txtLines.add("[地块坐标]");

        Integer wkid = null;
        for (OguFeature feature : layer.getFeatures()) {
            // 处理几何
            List<OguCoordinate> coordinates = new ArrayList<>();
            Geometry geometry = GeometryConverter.wkt2Geometry(feature.getGeometry());
            if (wkid == null) {
                zoneNumber = CrsUtil.getDh(geometry);
                wkid = CrsUtil.getProjectedWkid(zoneNumber);
            }
            geometry = CrsUtil.transform(geometry, layer.getWkid(), wkid);

            if (geometry instanceof Polygonal) {
                List<Polygon> polygons = new ArrayList<>();
                if (geometry instanceof Polygon) {
                    polygons.add((Polygon) geometry);
                } else if (geometry instanceof MultiPolygon multiPolygon) {
                    for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                        polygons.add((Polygon) multiPolygon.getGeometryN(i));
                    }
                }

                int qh = 1;
                for (Polygon polygon : polygons) {
                    List<LinearRing> rings = new ArrayList<>();
                    rings.add(polygon.getExteriorRing());
                    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                        rings.add(polygon.getInteriorRingN(i));
                    }

                    for (LinearRing ring : rings) {
                        int pn = 1;
                        for (Coordinate coordinate : ring.getCoordinates()) {
                            OguCoordinate coord = new OguCoordinate();
                            coord.setPointNumber(String.valueOf(pn));
                            coord.setRingNumber(qh);
                            coord.setY(coordinate.getY());
                            coord.setX(coordinate.getX());
                            coordinates.add(coord);
                            pn++;
                        }
                        qh++;
                    }
                }
            } else {
                throw new RuntimeException("不支持的几何类型");
            }

            // 处理属性值
            List<String> values = new ArrayList<>();
            for (String fieldName : fieldNames) {
                switch (fieldName) {
                    case "JZDS":
                        values.add(String.valueOf(coordinates.size()));
                        break;
                    case "DKMJ":
                        double area = geometry.getArea() / 10000;
                        values.add(NumUtil.getPlainString(area));
                        break;
                    case "JLTXSX":
                        values.add("面");
                        break;
                    default: {
                        if (feature.getAttributes() != null) {
                            Optional<OguFieldValue> optional = feature.getAttributes().stream()
                                    .filter(m -> m.getField() != null && m.getField().getName() != null
                                            && m.getField().getName().equalsIgnoreCase(fieldName)
                                            && ObjectUtil.isNotEmpty(m.getValue())).findFirst();
                            if (optional.isPresent()) {
                                values.add(String.valueOf(optional.get().getValue()));
                            } else {
                                values.add("");
                            }
                        } else {
                            values.add("");
                        }
                    }
                }
            }

            // 写入要素属性行
            txtLines.add(CharSequenceUtil.join(",", values) + ",@");

            // 写入坐标行
            for (OguCoordinate coord : coordinates) {
                txtLines.add(CharSequenceUtil.join(",", coord.getPointNumber(), coord.getRingNumber(),
                        NumUtil.getPlainString(coord.getY()),
                        NumUtil.getPlainString(coord.getX())));
            }
        }

        FileUtil.writeLines(txtLines, txtPath, StandardCharsets.UTF_8);
    }

    /**
     * 加载TXT文件为OguLayer
     *
     * @param txtPath TXT文件路径
     * @param fields  字段定义，如果为null则使用默认定义
     * @return OguLayer
     */
    @SneakyThrows
    public static OguLayer loadTxt(String txtPath, List<OguField> fields) {
        File file = new File(txtPath);
        Charset encoding = EncodingUtil.getFileEncoding(file);

        List<String> txtLines = new ArrayList<>();
        for (String line : FileUtil.readLines(file, encoding)) {
            String trim = line.trim();
            if (CharSequenceUtil.isBlank(trim)) {
                continue;
            }

            if (trim.startsWith("\uFEFF")) {
                trim = trim.substring(1);
            }

            txtLines.add(trim);
        }

        if (txtLines.stream().noneMatch("[属性描述]"::equals)) {
            throw new RuntimeException("缺少[属性描述]");
        }

        if (txtLines.stream().noneMatch("[地块坐标]"::equals)) {
            throw new RuntimeException("缺少[地块坐标]");
        }

        LinkedHashMap<String, List<String>> txtMap = new LinkedHashMap<>();
        String currKey = null;
        for (String txtLine : txtLines) {
            if (txtLine.startsWith("[") && txtLine.endsWith("]")) {
                currKey = txtLine;
                txtMap.put(txtLine, new ArrayList<>());
            } else {
                txtMap.get(currKey).add(txtLine);
            }
        }

        OguLayer layer = new OguLayer();
        layer.setName(FileUtil.getName(txtPath));
        OguLayerMetadata metadata = new OguLayerMetadata();
        List<OguLayerMetadata.ExtendedInfo> extInfos = new ArrayList<>();

        final String[] zoneNumber = {null};

        txtMap.forEach((module, modules) -> {
            if ("[属性描述]".equals(module)) {
                for (String line : modules) {
                    String[] split = line.split("=");
                    if (split.length == 0 || split.length > 2) {
                        throw new RuntimeException("txt文件格式不正确");
                    }

                    String key = null;
                    String value = null;
                    if (split.length == 1) {
                        key = split[0].trim();
                    }
                    if (split.length == 2) {
                        key = split[0].trim();
                        value = split[1].trim();
                    }
                    switch (key) {
                        case "格式版本号":
                            metadata.setFormatVersion(value);
                            break;
                        case "数据产生单位":
                            metadata.setDataSource(value);
                            break;
                        case "数据产生日期":
                            metadata.setDataDate(value);
                            break;
                        case "坐标系":
                            metadata.setCoordinateSystemName(value);
                            break;
                        case "几度分带":
                            metadata.setZoneDivision(value);
                            break;
                        case "投影类型":
                            metadata.setProjectionType(value);
                            break;
                        case "计量单位":
                            metadata.setMeasureUnit(value);
                            break;
                        case "带号":
                            metadata.setZoneNumber(value);
                            zoneNumber[0] = value;
                            break;
                        case "精度":
                            metadata.setPrecision(value);
                            break;
                        case "转换参数":
                            metadata.setTransformParams(value);
                            break;
                        default:
                            throw new RuntimeException("txt文件格式不正确");
                    }
                }
            } else if (!"[地块坐标]".equals(module)) {
                OguLayerMetadata.ExtendedInfo extInfo = new OguLayerMetadata.ExtendedInfo();
                extInfo.setName(module.replace("[", "").replace("]", ""));
                LinkedHashMap<String, String> props = new LinkedHashMap<>();
                for (String line : modules) {
                    String[] split = line.split("=");
                    if (split.length == 0 || split.length > 2) {
                        throw new RuntimeException("txt文件格式不正确");
                    }

                    String key = null;
                    String value = null;
                    if (split.length == 1) {
                        key = split[0].trim();
                    }
                    if (split.length == 2) {
                        key = split[0].trim();
                        value = split[1].trim();
                    }
                    props.put(key, value);
                }
                extInfo.setProperties(props);
                extInfos.add(extInfo);
            }
        });

        metadata.setExtendedInfos(extInfos);
        layer.setMetadata(metadata);

        // 设置默认字段
        if (fields == null) {
            fields = getDefaultFields();
        }
        layer.setFields(fields);

        // 解析地块坐标
        List<String> coordLines = txtMap.get("[地块坐标]");
        List<OguFeature> features = new ArrayList<>();
        LinkedHashMap<String, List<String>> zbMap = new LinkedHashMap<>();
        String currZbKey = null;
        for (String line : coordLines) {
            if (line.endsWith("@")) {
                currZbKey = line;
                zbMap.put(currZbKey, new ArrayList<>());
            } else {
                zbMap.get(currZbKey).add(line);
            }
        }

        final List<OguField> finalFields = fields;
        zbMap.forEach((zbKey, zbLines) -> {
            OguFeature feature = new OguFeature();
            List<String> fieldValues = CharSequenceUtil.split(zbKey, ",");
            List<String> rawValues = fieldValues.subList(0, fieldValues.size() - 1);
            feature.setRawValues(rawValues);

            // 解析坐标
            List<OguCoordinate> coordinates = new ArrayList<>();
            zbLines.forEach(zbLine -> {
                List<String> zbLineList = new ArrayList<>(CharSequenceUtil.split(zbLine, ","));
                if (zbLineList.size() != 4) {
                    throw new RuntimeException("txt坐标点格式不正确，错误行：" + zbLine);
                }
                OguCoordinate coord = new OguCoordinate();
                coord.setPointNumber(zbLineList.get(0));
                coord.setRingNumber(NumberUtil.parseInt(zbLineList.get(1)));
                coord.setY(NumberUtil.parseDouble(zbLineList.get(2)));
                coord.setX(NumberUtil.parseDouble(zbLineList.get(3)));
                coordinates.add(coord);
            });
            feature.setCoordinates(coordinates);

            // 构建几何
            LinkedHashMap<Integer, List<Coordinate>> coordinateMap = new LinkedHashMap<>();
            for (OguCoordinate coord : coordinates) {
                int qh = coord.getRingNumber();
                List<Coordinate> coords = coordinateMap.computeIfAbsent(qh, k -> new ArrayList<>());
                coords.add(new Coordinate(coord.getX(), coord.getY()));
            }

            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING));
            LinearRing shell = null;
            LinearRing[] holes = null;
            if (coordinateMap.size() > 1) {
                holes = new LinearRing[coordinateMap.size() - 1];
            }
            int index = 0;
            for (Map.Entry<Integer, List<Coordinate>> entry : coordinateMap.entrySet()) {
                List<Coordinate> coords = entry.getValue();
                if (!coords.get(0).equals2D(coords.get(coords.size() - 1))) {
                    coords.add(coords.get(0));
                }

                LinearRing ring = geometryFactory.createLinearRing(ArrayUtil.toArray(coords, Coordinate.class));
                if (index == 0) {
                    shell = ring;
                } else {
                    if (holes != null) {
                        holes[index - 1] = ring;
                    }
                }
                index++;
            }

            Polygon polygon = geometryFactory.createPolygon(shell, holes);

            // 设置属性
            List<OguFieldValue> attributes = new ArrayList<>();
            for (int i = 0; i < rawValues.size() && i < finalFields.size(); i++) {
                OguFieldValue fieldValue = new OguFieldValue();
                fieldValue.setField(finalFields.get(i));
                fieldValue.setValue(rawValues.get(i));
                attributes.add(fieldValue);
            }
            feature.setAttributes(attributes);

            feature.setId(IdUtil.simpleUUID());
            int wkid = 4488 + NumberUtil.parseInt(zoneNumber[0]);
            String wkt = polygon.toText();
            feature.setGeometry(EsriGeometryUtil.simplify(wkt, wkid));
            features.add(feature);
        });

        layer.setFeatures(features);

        // 设置图层属性
        int wkid = 4488 + NumberUtil.parseInt(zoneNumber[0]);
        layer.setWkid(wkid);
        layer.setTolerance(CrsUtil.getTolerance(wkid));
        layer.setGeometryType(GeometryType.MULTIPOLYGON);
        layer.setAlias(FileUtil.getName(txtPath));

        layer.validate();
        return layer;
    }

    /**
     * 获取默认字段名称
     */
    private static List<String> getDefaultFieldNames() {
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add("JZDS");
        fieldNames.add("DKMJ");
        fieldNames.add("DKBH");
        fieldNames.add("DKMC");
        fieldNames.add("JLTXSX");
        fieldNames.add("TFH");
        fieldNames.add("DKYT");
        fieldNames.add("DLBM");
        fieldNames.add("TBLX");
        fieldNames.add("DL");
        fieldNames.add("GZQ");
        fieldNames.add("GZH");
        fieldNames.add("BZ");
        return fieldNames;
    }

    /**
     * 获取默认字段定义
     */
    private static List<OguField> getDefaultFields() {
        List<OguField> fields = new ArrayList<>();
        fields.add(new OguField("JZDS", "界址点数", null, FieldDataType.STRING));
        fields.add(new OguField("DKMJ", "地块面积", null, FieldDataType.STRING));
        fields.add(new OguField("DKBH", "地块编号", null, FieldDataType.STRING));
        fields.add(new OguField("DKMC", "地块名称", null, FieldDataType.STRING));
        fields.add(new OguField("JLTXSX", "记录图形属性", null, FieldDataType.STRING));
        fields.add(new OguField("TFH", "图幅号", null, FieldDataType.STRING));
        fields.add(new OguField("DKYT", "地块用途", null, FieldDataType.STRING));
        fields.add(new OguField("DLBM", "地类编码", null, FieldDataType.STRING));
        fields.add(new OguField("TBLX", "图斑类型", null, FieldDataType.STRING));
        fields.add(new OguField("DL", "地类", null, FieldDataType.STRING));
        fields.add(new OguField("GZQ", "改造前平均质量等别", null, FieldDataType.STRING));
        fields.add(new OguField("GZH", "改造后平均质量等别", null, FieldDataType.STRING));
        fields.add(new OguField("BZ", "备注", null, FieldDataType.STRING));
        return fields;
    }

    /**
     * 准备元数据
     */
    private static OguLayerMetadata prepareMetadata(OguLayer layer, OguLayerMetadata source, Integer zoneNumber) {
        OguLayerMetadata metadata = new OguLayerMetadata();

        if (source != null) {
            metadata.setFormatVersion(source.getFormatVersion());
            metadata.setDataSource(source.getDataSource());
            metadata.setDataDate(source.getDataDate());
            metadata.setCoordinateSystemName(source.getCoordinateSystemName());
            metadata.setZoneDivision(source.getZoneDivision());
            metadata.setProjectionType(source.getProjectionType());
            metadata.setMeasureUnit(source.getMeasureUnit());
            metadata.setZoneNumber(source.getZoneNumber());
            metadata.setPrecision(source.getPrecision());
            metadata.setTransformParams(source.getTransformParams());
            metadata.setExtendedInfos(source.getExtendedInfos());
        }

        if (metadata.getZoneNumber() == null && zoneNumber != null) {
            metadata.setZoneNumber(String.valueOf(zoneNumber));
        }

        return metadata;
    }
}
