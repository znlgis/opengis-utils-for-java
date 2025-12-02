package com.znlgis.ogu4j.exception;

/**
 * 数据源连接异常
 * <p>
 * 当连接GIS数据源（如Shapefile、PostGIS数据库、FileGDB等）失败时抛出此异常。
 * 可能的原因包括：文件不存在、数据库连接失败、权限不足等。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
public class DataSourceException extends OguException {

    /**
     * 使用详细消息构造异常
     *
     * @param message 详细消息
     */
    public DataSourceException(String message) {
        super(message);
    }

    /**
     * 使用详细消息和原因构造异常
     *
     * @param message 详细消息
     * @param cause   原因
     */
    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用原因构造异常
     *
     * @param cause 原因
     */
    public DataSourceException(Throwable cause) {
        super(cause);
    }
}
