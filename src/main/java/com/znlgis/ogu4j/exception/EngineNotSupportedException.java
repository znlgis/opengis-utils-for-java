package com.znlgis.ogu4j.exception;

/**
 * GIS引擎不支持异常
 * <p>
 * 当请求的操作不被指定的GIS引擎支持时抛出此异常。
 * 例如：使用GeoTools引擎处理FileGDB格式（需要GDAL支持）。
 * </p>
 */
public class EngineNotSupportedException extends OguException {

    /**
     * 使用详细消息构造异常
     *
     * @param message 详细消息
     */
    public EngineNotSupportedException(String message) {
        super(message);
    }

    /**
     * 使用详细消息和原因构造异常
     *
     * @param message 详细消息
     * @param cause   原因
     */
    public EngineNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用原因构造异常
     *
     * @param cause 原因
     */
    public EngineNotSupportedException(Throwable cause) {
        super(cause);
    }
}
