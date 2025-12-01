# OGU4J - OpenGIS Utils for Java

[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0.0-orange)](https://github.com/znlgis/opengis-utils-for-java)

[English](#english) | [中文](#中文)

---

<a name="中文"></a>
## 中文说明

### 简介

OGU4J（OpenGIS Utils for Java）是一个基于开源GIS库（GeoTools、JTS、GDAL/OGR、ESRI Geometry API）的Java GIS二次开发工具库。它提供了统一的图层模型和便捷的格式转换功能，简化了GIS数据的读取、处理和导出操作。

### 主要特性

- 🗂️ **统一图层模型**：提供简洁的图层、要素、字段抽象，屏蔽底层GIS库差异
- 📁 **多格式支持**：支持Shapefile、GeoJSON、FileGDB、PostGIS、国土TXT坐标文件等格式
- 🔄 **双引擎架构**：支持GeoTools和GDAL/OGR两种引擎，可根据需求灵活切换
- 📐 **几何处理**：基于JTS和ESRI Geometry API提供丰富的几何操作和空间分析功能
- 🌐 **坐标系管理**：内置CGCS2000坐标系支持，提供坐标转换功能
- 🛠️ **实用工具**：提供ZIP压缩/解压、文件编码检测、自然排序等实用工具

### 快速安装

#### Maven

```xml
<repositories>
    <repository>
        <id>osgeo</id>
        <url>https://repo.osgeo.org/repository/release/</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.znlgis.ogu4j</groupId>
    <artifactId>ogu4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 核心图层模型

本库提供了统一的简化图层模型，位于 `com.znlgis.ogu4j.model.layer` 包中：

| 类名 | 说明 |
|------|------|
| **OguLayer** | 统一的GIS图层定义，包含图层名称、坐标系、几何类型、字段定义和要素集合 |
| **OguFeature** | 统一的要素类，包含要素ID、几何信息（WKT格式）和属性值集合 |
| **OguField** | 统一的字段定义类，包含字段名称、别名、数据类型等信息 |
| **OguFieldValue** | 字段值容器，提供便捷的类型转换方法（getStringValue、getIntValue、getDoubleValue等） |
| **OguCoordinate** | 坐标类，支持二维/三维坐标及点号/圈号（用于国土TXT格式） |
| **OguFeatureFilter** | 函数式接口，用于要素过滤 |
| **OguLayerMetadata** | 图层元数据，存储坐标系参数、数据来源、扩展信息等 |

### 使用示例

#### 基本操作

```java
// 从JSON字符串创建OguLayer
OguLayer layer = OguLayer.fromJSON(jsonString);

// 验证图层数据完整性
layer.validate();

// 过滤要素
List<OguFeature> filtered = layer.filter(feature ->
    "北京".equals(feature.getValue("city")));

// 获取要素数量
int count = layer.getFeatureCount();

// 转换为JSON字符串
String json = layer.toJSON();
```

#### 读取要素属性

```java
OguFeature feature = layer.getFeatures().get(0);

// 获取属性值
Object value = feature.getValue("fieldName");

// 获取属性值对象
OguFieldValue fieldValue = feature.getAttribute("fieldName");
String strValue = fieldValue.getStringValue();
Integer intValue = fieldValue.getIntValue();
Double doubleValue = fieldValue.getDoubleValue();

// 设置属性值
feature.setValue("fieldName", newValue);
```

### 图层格式转换

使用 `OguLayerConverter` 进行各种格式间的转换：

#### Shapefile

```java
// 从Shapefile读取（支持属性过滤和空间过滤）
OguLayer layer = OguLayerConverter.fromShapefile(
    shpPath, 
    "NAME = '北京'",           // 属性过滤条件（CQL表达式）
    spatialFilterWkt,          // 空间过滤条件（WKT格式）
    GisEngineType.GEOTOOLS     // 使用的GIS引擎
);

// 保存为Shapefile
OguLayerConverter.toShapefile(layer, shpPath, GisEngineType.GEOTOOLS);
```

#### GeoJSON

```java
// 从GeoJSON读取
OguLayer layer = OguLayerConverter.fromGeoJSON(geojsonPath, GisEngineType.GEOTOOLS);

// 保存为GeoJSON
OguLayerConverter.toGeoJSON(layer, geojsonPath, GisEngineType.GEOTOOLS);
```

#### FileGDB（需要GDAL支持）

```java
// 从FileGDB读取指定图层
OguLayer layer = OguLayerConverter.fromFileGDB(
    gdbPath, 
    "layerName", 
    attributeFilter, 
    spatialFilterWkt, 
    GisEngineType.GDAL
);

// 保存到FileGDB
OguLayerConverter.toFileGDB(layer, gdbPath, "featureDataset", "layerName", GisEngineType.GDAL);
```

#### PostGIS

```java
// 配置数据库连接
DbConnBaseModel dbConn = new DbConnBaseModel();
dbConn.setDbtype("postgis");
dbConn.setHost("localhost");
dbConn.setPort("5432");
dbConn.setDatabase("gisdb");
dbConn.setSchema("public");
dbConn.setUser("postgres");
dbConn.setPasswd("password");

// 从PostGIS读取
OguLayer layer = OguLayerConverter.fromPostGIS(dbConn, "layerName", null, null, GisEngineType.GEOTOOLS);

// 保存到PostGIS
OguLayerConverter.toPostGIS(layer, dbConn, "layerName", GisEngineType.GEOTOOLS);
```

#### 国土TXT坐标文件

```java
// 从TXT文件读取
OguLayer layer = OguLayerConverter.fromTxtFile(txtPath, null);

// 保存为TXT文件
OguLayerMetadata metadata = new OguLayerMetadata();
metadata.setDataSource("自然资源部");
metadata.setCoordinateSystemName("2000国家大地坐标系");
metadata.setZoneDivision("3");
metadata.setProjectionType("高斯克吕格");
metadata.setMeasureUnit("米");

int zoneNumber = 39; // 带号
OguLayerConverter.toTxtFile(layer, txtPath, metadata, null, zoneNumber);
```

### 几何格式转换

使用 `GeometryConverter` 进行几何格式转换：

```java
// WKT <-> JTS Geometry
Geometry geom = GeometryConverter.wkt2Geometry(wkt);
String wkt = GeometryConverter.geometry2Wkt(geom);

// GeoJSON <-> JTS Geometry
Geometry geom = GeometryConverter.geojson2Geometry(geojson);
String geojson = GeometryConverter.geometry2Geojson(geom);

// WKT <-> GeoJSON
String geojson = GeometryConverter.wkt2Geojson(wkt);
String wkt = GeometryConverter.geojson2Wkt(geojson);

// WKT <-> ESRI JSON
String esriJson = GeometryConverter.wkt2EsriJson(wkt, wkid);
String wkt = GeometryConverter.esriJson2Wkt(esriJson);

// GeoJSON <-> ESRI JSON
String esriJson = GeometryConverter.geoJson2EsriJson(wkid, geojson);
String geojson = GeometryConverter.esriJson2GeoJson(esriJson);

// ESRI JSON <-> JTS Geometry
Geometry geom = GeometryConverter.esriJson2Geometry(esriJson);
String esriJson = GeometryConverter.geometry2EsriJson(geom, wkid);
```

### 几何空间分析

#### JTS几何工具（JtsGeometryUtil）

```java
// 空间关系判断
boolean result = JtsGeometryUtil.intersects(geomA, geomB);
boolean result = JtsGeometryUtil.contains(geomA, geomB);
boolean result = JtsGeometryUtil.within(geomA, geomB);
boolean result = JtsGeometryUtil.touches(geomA, geomB);
boolean result = JtsGeometryUtil.crosses(geomA, geomB);
boolean result = JtsGeometryUtil.overlaps(geomA, geomB);
boolean result = JtsGeometryUtil.disjoint(geomA, geomB);

// 空间分析
Geometry buffer = JtsGeometryUtil.buffer(geom, distance);
Geometry intersection = JtsGeometryUtil.intersection(geomA, geomB);
Geometry union = JtsGeometryUtil.union(geomA, geomB);
Geometry difference = JtsGeometryUtil.difference(geomA, geomB);
Geometry symDifference = JtsGeometryUtil.symDifference(geomA, geomB);

// 几何属性
double area = JtsGeometryUtil.area(geom);
double length = JtsGeometryUtil.length(geom);
Geometry centroid = JtsGeometryUtil.centroid(geom);
Geometry interiorPoint = JtsGeometryUtil.interiorPoint(geom);
int dimension = JtsGeometryUtil.dimension(geom);
int numPoints = JtsGeometryUtil.numPoints(geom);
GeometryType geometryType = JtsGeometryUtil.geometryType(geom);
boolean isEmpty = JtsGeometryUtil.isEmpty(geom);

// 几何边界与外包矩形
Geometry boundary = JtsGeometryUtil.boundary(geom);
Geometry envelope = JtsGeometryUtil.envelope(geom);

// 凸包与凹包
Geometry convexHull = JtsGeometryUtil.convexHull(geom);
Geometry concaveHull = JtsGeometryUtil.concaveHull(geom);

// 拓扑验证与简化
TopologyValidationResult validResult = JtsGeometryUtil.isValid(geom);
SimpleGeometryResult simpleResult = JtsGeometryUtil.isSimple(geom);
Geometry simplified = JtsGeometryUtil.simplify(geom, tolerance);
Geometry validated = JtsGeometryUtil.validate(geom);
Geometry densified = JtsGeometryUtil.densify(geom, distance);

// 几何相等判断
boolean equalsExact = JtsGeometryUtil.equalsExact(geomA, geomB);
boolean equalsExactTol = JtsGeometryUtil.equalsExactTolerance(geomA, geomB, tolerance);
boolean equalsNorm = JtsGeometryUtil.equalsNorm(geomA, geomB);
boolean equalsTopo = JtsGeometryUtil.equalsTopo(geomA, geomB);

// 空间关系模式
boolean relateResult = JtsGeometryUtil.relatePattern(geomA, geomB, "T*T***FF*");
String relate = JtsGeometryUtil.relate(geomA, geomB);

// 距离计算
double distance = JtsGeometryUtil.distance(geomA, geomB);
boolean withinDistance = JtsGeometryUtil.isWithinDistance(geomA, geomB, maxDistance);

// 多边形操作
Geometry splitResult = JtsGeometryUtil.splitPolygon(polygon, line);
Geometry polygonized = JtsGeometryUtil.polygonize(geom);
```

#### ESRI几何工具（EsriGeometryUtil）

```java
// 几何创建
Geometry geom = EsriGeometryUtil.createGeometryByWkt(wkt);
Geometry geom = EsriGeometryUtil.createGeometryByGeoJson(geojson);
Geometry geom = EsriGeometryUtil.createGeometryByJson(esriJson);

// 几何输出
String wkt = EsriGeometryUtil.toWkt(geometry);
String geojson = EsriGeometryUtil.toGeoJson(geometry);
String esriJson = EsriGeometryUtil.toEsriJson(wkid, geometry);

// 格式转换
String wkt = EsriGeometryUtil.esriJson2Wkt(esriJson);
String esriJson = EsriGeometryUtil.wkt2EsriJson(wkt);

// 空间关系判断（需要指定坐标系）
boolean result = EsriGeometryUtil.intersects(wktA, wktB, wkid);
boolean result = EsriGeometryUtil.contains(wktA, wktB, wkid);
boolean result = EsriGeometryUtil.within(wktA, wktB, wkid);
boolean result = EsriGeometryUtil.disjoint(wktA, wktB, wkid);
boolean result = EsriGeometryUtil.touches(wktA, wktB, wkid);
boolean result = EsriGeometryUtil.crosses(wktA, wktB, wkid);
boolean result = EsriGeometryUtil.overlaps(wktA, wktB, wkid);
boolean result = EsriGeometryUtil.equals(wktA, wktB, wkid);
boolean result = EsriGeometryUtil.relatePattern(wktA, wktB, wkid, pattern);

// 空间分析
String buffer = EsriGeometryUtil.buffer(wkt, wkid, distance);
String intersection = EsriGeometryUtil.intersection(wktA, wktB, wkid);
String union = EsriGeometryUtil.union(wktList, wkid);
String difference = EsriGeometryUtil.difference(wktA, wktB, wkid);
String symDifference = EsriGeometryUtil.symDifference(wktA, wktB, wkid);
String convexHull = EsriGeometryUtil.convexHull(wkt);
String boundary = EsriGeometryUtil.boundary(wkt);

// 几何属性
double area = EsriGeometryUtil.area(wkt);
double length = EsriGeometryUtil.length(wkt);
String centroid = EsriGeometryUtil.centroid(wkt);
int dimension = EsriGeometryUtil.dimension(wkt);
boolean isEmpty = EsriGeometryUtil.isEmpty(wkt);
double distance = EsriGeometryUtil.distance(wktA, wktB, wkid);
GeometryType geometryType = EsriGeometryUtil.geometryType(wkt);
boolean isSimple = EsriGeometryUtil.isSimple(wkt, wkid);

// 几何简化
String simplified = EsriGeometryUtil.simplify(wkt, wkid);
```

### 坐标系工具（CrsUtil）

位于 `com.znlgis.ogu4j.common` 包中：

```java
// 坐标转换（WKT字符串）
String transformedWkt = CrsUtil.transform(wkt, sourceWkid, targetWkid);

// 坐标转换（JTS Geometry）
Geometry transformed = CrsUtil.transform(geometry, sourceWkid, targetWkid);

// 图层投影转换
OguLayer reprojected = CrsUtil.reproject(layer, targetWkid);

// 获取带号
int zoneNumber = CrsUtil.getDh(geometry);
int zoneNumber = CrsUtil.getDh(wkt);
int zoneNumber = CrsUtil.getDh(projectedWkid);

// 获取几何对应的WKID
Integer wkid = CrsUtil.getWkid(geometry);

// 获取投影坐标系WKID
Integer projectedWkid = CrsUtil.getProjectedWkid(dh);
Integer projectedWkid = CrsUtil.getProjectedWkid(geometry);

// 判断坐标系类型
boolean isProjected = CrsUtil.isProjectedCRS(crs);

// 获取容差
double tolerance = CrsUtil.getTolerance(wkid);

// 获取支持的坐标系列表
Map<Integer, CoordinateReferenceSystem> crsList = CrsUtil.getSupportedCRSList();
```

### 数据模型

#### TopologyValidationResult - 拓扑验证结果

用于封装几何对象拓扑验证的结果，包含验证是否通过、错误位置、错误类型和错误信息：

```java
TopologyValidationResult result = JtsGeometryUtil.isValid(geom);
if (!result.isValid()) {
    System.out.println("错误类型: " + result.getErrorType().getDesc());
    System.out.println("错误位置: " + result.getCoordinate());
    System.out.println("错误信息: " + result.getMessage());
}
```

#### SimpleGeometryResult - 简单几何判断结果

用于封装几何对象简单性检查的结果，简单几何是指不存在自相交或重复点的几何对象：

```java
SimpleGeometryResult result = JtsGeometryUtil.isSimple(geom);
if (!result.isSimple()) {
    System.out.println("非简单点位置: " + result.getNonSimplePts());
}
```

#### TopologyValidationErrorType - 拓扑错误类型

定义几何对象拓扑验证中可能出现的各类错误：

| 错误类型 | 说明 |
|---------|------|
| `ERROR` | 拓扑检查错误 |
| `REPEATED_POINT` | 点重叠 |
| `HOLE_OUTSIDE_SHELL` | 洞在图形外 |
| `NESTED_HOLES` | 洞重叠 |
| `DISCONNECTED_INTERIOR` | 图形内部不连通 |
| `SELF_INTERSECTION` | 自相交 |
| `RING_SELF_INTERSECTION` | 环自相交 |
| `NESTED_SHELLS` | 图形重叠 |
| `DUPLICATE_RINGS` | 环重复 |
| `TOO_FEW_POINTS` | 点太少无法构成有效几何 |
| `INVALID_COORDINATE` | 无效坐标 |
| `RING_NOT_CLOSED` | 环未闭合 |

### API模块概览

| 包名 | 说明 |
|------|------|
| `com.znlgis.ogu4j.model.layer` | 图层模型类（OguLayer、OguFeature、OguField、OguFieldValue、OguCoordinate、OguFeatureFilter、OguLayerMetadata） |
| `com.znlgis.ogu4j.model` | 数据模型类（DbConnBaseModel、GdbGroupModel、TopologyValidationResult、SimpleGeometryResult） |
| `com.znlgis.ogu4j.enums` | 枚举类型（GeometryType、FieldDataType、GisEngineType、DataFormatType、TopologyValidationErrorType） |
| `com.znlgis.ogu4j.geometry` | 几何处理工具（JtsGeometryUtil、EsriGeometryUtil、GeometryConverter） |
| `com.znlgis.ogu4j.datasource` | 数据源工具类（ShpUtil、PostgisUtil、OgrUtil、GeotoolsUtil、GtTxtUtil、OguLayerConverter） |
| `com.znlgis.ogu4j.common` | 通用工具类（CrsUtil、ZipUtil、EncodingUtil、SortUtil、NumUtil、GdalCmdUtil） |

### 实用工具类

#### ZipUtil - ZIP压缩解压工具

```java
// 压缩文件夹
ZipUtil.zip(folder, "output.zip");
ZipUtil.zip(folder, "output.zip", StandardCharsets.UTF_8);

// 解压文件
ZipUtil.unzip("input.zip", destPath);
ZipUtil.unzip("input.zip", destPath, StandardCharsets.UTF_8);
```

#### EncodingUtil - 文件编码检测工具

```java
// 自动检测文件编码
Charset charset = EncodingUtil.getFileEncoding(file);
```

#### SortUtil - 自然排序工具

```java
// 包含数字的字符串自然排序
int result = SortUtil.compareString("第5章", "第10章");  // 返回 -1
```

#### GdalCmdUtil - GDAL命令行工具

```java
// 获取GDB图层结构
GdbGroupModel structure = GdalCmdUtil.getGdbDataStructure(gdbPath);
```

#### NumUtil - 数字格式化工具

```java
// 去除科学计数法显示
String plainString = NumUtil.getPlainString(1.234E10);  // 返回 "12340000000"
```

### 依赖说明

本库主要依赖以下开源库：

| 依赖库 | 版本 | 说明 |
|--------|------|------|
| **JTS** | 1.20.0 | Java拓扑套件，提供几何对象和空间操作 |
| **GeoTools** | 34.1 | Java GIS工具库，提供数据读写和坐标系支持 |
| **ESRI Geometry API** | 2.2.4 | ESRI几何引擎，提供高性能几何运算 |
| **GDAL** | 3.11.0 | 地理空间数据抽象库（可选，用于FileGDB等格式支持） |
| **Hutool** | 5.8.41 | Java工具库，提供便捷的工具方法 |
| **Fastjson2** | 2.0.60 | 高性能JSON处理库 |
| **Zip4j** | 2.11.5 | ZIP压缩解压库 |
| **Lombok** | 1.18.36 | Java注解库，简化代码编写 |

### 环境要求

- **Java**: 17+
- **GDAL**（可选）: 如需使用FileGDB格式或GDAL引擎，需安装GDAL并配置环境变量

---

<a name="english"></a>
## English

### Introduction

OGU4J (OpenGIS Utils for Java) is a Java GIS development toolkit based on open-source GIS libraries (GeoTools, JTS, GDAL/OGR, ESRI Geometry API). It provides a unified layer model and convenient format conversion functions to simplify GIS data reading, processing, and exporting operations.

### Features

- 🗂️ **Unified Layer Model**: Provides simple layer, feature, and field abstractions, hiding the differences of underlying GIS libraries
- 📁 **Multi-format Support**: Supports Shapefile, GeoJSON, FileGDB, PostGIS, and China National Land TXT coordinate files
- 🔄 **Dual Engine Architecture**: Supports both GeoTools and GDAL/OGR engines, switchable based on requirements
- 📐 **Geometry Processing**: Rich geometry operations and spatial analysis based on JTS and ESRI Geometry API
- 🌐 **CRS Management**: Built-in CGCS2000 coordinate system support with coordinate transformation capabilities
- 🛠️ **Utility Tools**: Provides ZIP compression/decompression, file encoding detection, natural sorting, and other utilities

### Installation

#### Maven

```xml
<repositories>
    <repository>
        <id>osgeo</id>
        <url>https://repo.osgeo.org/repository/release/</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.znlgis.ogu4j</groupId>
    <artifactId>ogu4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Layer Model

The library provides a unified simplified layer model in the `com.znlgis.ogu4j.model.layer` package:

| Class | Description |
|-------|-------------|
| **OguLayer** | Unified GIS layer definition with name, CRS, geometry type, fields, and features |
| **OguFeature** | Unified feature class containing ID, geometry (WKT format), and attributes |
| **OguField** | Unified field definition with name, alias, and data type |
| **OguFieldValue** | Field value container with convenient type conversion methods |
| **OguCoordinate** | Coordinate class supporting 2D/3D coordinates with point/ring numbers |
| **OguFeatureFilter** | Functional interface for feature filtering |
| **OguLayerMetadata** | Layer metadata storing CRS parameters, data source, and extended info |

### Quick Start

```java
// Create OguLayer from JSON
OguLayer layer = OguLayer.fromJSON(jsonString);

// Validate layer data integrity
layer.validate();

// Filter features
List<OguFeature> filtered = layer.filter(feature ->
    "Beijing".equals(feature.getValue("city")));

// Convert to JSON
String json = layer.toJSON();
```

### Format Conversion

```java
// Shapefile
OguLayer layer = OguLayerConverter.fromShapefile(shpPath, null, null, GisEngineType.GEOTOOLS);
OguLayerConverter.toShapefile(layer, shpPath, GisEngineType.GEOTOOLS);

// GeoJSON
OguLayer layer = OguLayerConverter.fromGeoJSON(geojsonPath, GisEngineType.GEOTOOLS);
OguLayerConverter.toGeoJSON(layer, geojsonPath, GisEngineType.GEOTOOLS);

// FileGDB (requires GDAL)
OguLayer layer = OguLayerConverter.fromFileGDB(gdbPath, "layerName", null, null, GisEngineType.GDAL);
OguLayerConverter.toFileGDB(layer, gdbPath, "dataset", "layerName", GisEngineType.GDAL);
```

### Requirements

- **Java**: 17+
- **GDAL** (optional): Required for FileGDB format or GDAL engine

### Dependencies

| Library | Version | Description |
|---------|---------|-------------|
| **JTS** | 1.20.0 | Java Topology Suite for geometry objects and spatial operations |
| **GeoTools** | 34.1 | Java GIS toolkit for data I/O and CRS support |
| **ESRI Geometry API** | 2.2.4 | ESRI geometry engine for high-performance geometry operations |
| **GDAL** | 3.11.0 | Geospatial Data Abstraction Library (optional, for FileGDB support) |
| **Hutool** | 5.8.41 | Java utility library for convenient utility methods |
| **Fastjson2** | 2.0.60 | High-performance JSON processing library |
| **Zip4j** | 2.11.5 | ZIP compression/decompression library |
| **Lombok** | 1.18.36 | Java annotation library to simplify code writing |

### Data Models

#### TopologyValidationResult - Topology Validation Result

Encapsulates the result of geometry topology validation, including validation status, error location, error type, and error message:

```java
TopologyValidationResult result = JtsGeometryUtil.isValid(geom);
if (!result.isValid()) {
    System.out.println("Error Type: " + result.getErrorType().getDesc());
    System.out.println("Error Location: " + result.getCoordinate());
    System.out.println("Error Message: " + result.getMessage());
}
```

#### SimpleGeometryResult - Simple Geometry Check Result

Encapsulates the result of geometry simplicity check. A simple geometry has no self-intersections or repeated points:

```java
SimpleGeometryResult result = JtsGeometryUtil.isSimple(geom);
if (!result.isSimple()) {
    System.out.println("Non-simple point locations: " + result.getNonSimplePts());
}
```

#### TopologyValidationErrorType - Topology Error Types

Defines various topology validation error types for geometry objects:

| Error Type | Description |
|------------|-------------|
| `ERROR` | Topology check error |
| `REPEATED_POINT` | Repeated point |
| `HOLE_OUTSIDE_SHELL` | Hole outside shell |
| `NESTED_HOLES` | Nested holes |
| `DISCONNECTED_INTERIOR` | Disconnected interior |
| `SELF_INTERSECTION` | Self-intersection |
| `RING_SELF_INTERSECTION` | Ring self-intersection |
| `NESTED_SHELLS` | Nested shells |
| `DUPLICATE_RINGS` | Duplicate rings |
| `TOO_FEW_POINTS` | Too few points to form valid geometry |
| `INVALID_COORDINATE` | Invalid coordinate |
| `RING_NOT_CLOSED` | Ring not closed |

### API Overview

| Package | Description |
|---------|-------------|
| `com.znlgis.ogu4j.model.layer` | Layer model classes (OguLayer, OguFeature, OguField, OguFieldValue, OguCoordinate, OguFeatureFilter, OguLayerMetadata) |
| `com.znlgis.ogu4j.model` | Data model classes (DbConnBaseModel, GdbGroupModel, TopologyValidationResult, SimpleGeometryResult) |
| `com.znlgis.ogu4j.enums` | Enumerations (GeometryType, FieldDataType, GisEngineType, DataFormatType, TopologyValidationErrorType) |
| `com.znlgis.ogu4j.geometry` | Geometry utilities (JtsGeometryUtil, EsriGeometryUtil, GeometryConverter) |
| `com.znlgis.ogu4j.datasource` | Data source utilities (ShpUtil, PostgisUtil, OgrUtil, GeotoolsUtil, GtTxtUtil, OguLayerConverter) |
| `com.znlgis.ogu4j.common` | Common utilities (CrsUtil, ZipUtil, EncodingUtil, SortUtil, NumUtil, GdalCmdUtil) |

### Utility Classes

#### ZipUtil - ZIP Compression/Decompression

```java
// Compress a folder
ZipUtil.zip(folder, "output.zip");

// Extract a ZIP file
ZipUtil.unzip("input.zip", destPath);
```

#### EncodingUtil - File Encoding Detection

```java
// Auto-detect file encoding
Charset charset = EncodingUtil.getFileEncoding(file);
```

#### SortUtil - Natural Sorting

```java
// Natural string comparison with numbers
int result = SortUtil.compareString("Chapter 5", "Chapter 10");  // returns -1
```

#### NumUtil - Number Formatting

```java
// Remove scientific notation display
String plainString = NumUtil.getPlainString(1.234E10);  // returns "12340000000"
```

#### GdalCmdUtil - GDAL Command Line Tool

```java
// Get GDB layer structure
GdbGroupModel structure = GdalCmdUtil.getGdbDataStructure(gdbPath);
```

### License

This project is licensed under the Apache License 2.0.

### Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
