package com.znlgis.ogu4j.exception;

/**
 * 格式解析异常
 * <p>
 * 当解析GIS数据格式（如GeoJSON、WKT、Shapefile等）失败时抛出此异常。
 * 可能的原因包括：格式不正确、编码错误、数据损坏等。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
public class FormatParseException extends OguException {

    /**
     * 使用详细消息构造异常
     *
     * @param message 详细消息
     */
    public FormatParseException(String message) {
        super(message);
    }

    /**
     * 使用详细消息和原因构造异常
     *
     * @param message 详细消息
     * @param cause   原因
     */
    public FormatParseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用原因构造异常
     *
     * @param cause 原因
     */
    public FormatParseException(Throwable cause) {
        super(cause);
    }
}
