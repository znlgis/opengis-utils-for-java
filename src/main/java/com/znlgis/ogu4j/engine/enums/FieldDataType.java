package com.znlgis.ogu4j.engine.enums;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 字段数据类型枚举
 * <p>
 * 定义GIS图层字段支持的数据类型，包括整型、浮点型、字符串、二进制、日期时间等。
 * 提供与GDAL字段类型代码的映射关系，支持类型代码和Java类之间的相互转换。
 * </p>
 *
 * @see com.znlgis.ogu4j.engine.model.layer.OguField
 */
@Getter
public enum FieldDataType {
    /**
     * 整型
     */
    INTEGER(new int[]{0}, Integer.class, 0),
    /**
     * 浮点型
     */
    DOUBLE(new int[]{2}, Double.class, 2),
    /**
     * 字符串
     */
    STRING(new int[]{1, 3, 4, 6, 5, 7, 13}, String.class, 4),
    /**
     * 二进制
     */
    BINARY(new int[]{8}, byte[].class, 8),
    /**
     * 日期
     */
    DATE(new int[]{9}, LocalDate.class, 9),
    /**
     * 时间
     */
    TIME(new int[]{10}, LocalTime.class, 10),
    /**
     * 日期时间
     */
    DATETIME(new int[]{11}, LocalDateTime.class, 11),
    /**
     * 长整型
     */
    LONG(new int[]{12}, Long.class, 12);

    /**
     * GDAL字段类型代码
     */
    private final int[] gdalCodes;
    /**
     * 字段类型
     */
    private final Class<?> typeClass;
    /**
     * 默认字段类型代码
     */
    private final int defaultGdalCode;

    /**
     * 构造函数
     *
     * @param gdalCodes       GDAL字段类型代码
     * @param typeClass       字段类型
     * @param defaultGdalCode 默认字段类型代码
     */
    FieldDataType(int[] gdalCodes, Class<?> typeClass, int defaultGdalCode) {
        this.gdalCodes = gdalCodes;
        this.typeClass = typeClass;
        this.defaultGdalCode = defaultGdalCode;
    }

    /**
     * 根据字段类型代码获取字段类型
     *
     * @param gdalCode 字段类型代码
     * @return 字段类型
     */
    public static FieldDataType fieldDataTypeByGdalCode(int gdalCode) {
        for (FieldDataType status : FieldDataType.values()) {
            for (int c : status.getGdalCodes()) {
                if (c == gdalCode) {
                    return status;
                }
            }
        }

        return STRING;
    }

    /**
     * 根据字段类型获取字段类型
     *
     * @param typeClass 字段类型
     * @return 字段类型
     */
    public static FieldDataType fieldDataTypeByTypeClass(Class<?> typeClass) {
        for (FieldDataType status : FieldDataType.values()) {
            if (status.getTypeClass().equals(typeClass)) {
                return status;
            }
        }

        return STRING;
    }
}
