package com.znlgis.ogu4j.exception;

/**
 * 图层验证异常
 * <p>
 * 当图层数据验证失败时抛出此异常。
 * 可能的原因包括：缺少必需的属性（如几何类型、图层名称、坐标系等）。
 * </p>
 */
public class LayerValidationException extends OguException {

    /**
     * 使用详细消息构造异常
     *
     * @param message 详细消息
     */
    public LayerValidationException(String message) {
        super(message);
    }

    /**
     * 使用详细消息和原因构造异常
     *
     * @param message 详细消息
     * @param cause   原因
     */
    public LayerValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用原因构造异常
     *
     * @param cause 原因
     */
    public LayerValidationException(Throwable cause) {
        super(cause);
    }
}
