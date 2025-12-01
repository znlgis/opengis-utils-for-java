package com.znlgis.ogu4j.exception;

/**
 * OGU异常基类
 * <p>
 * 所有OGU4J库抛出的自定义异常的基类。
 * 提供统一的异常处理机制，支持消息传递和异常链。
 * </p>
 */
public class OguException extends Exception {

    /**
     * 使用详细消息构造异常
     *
     * @param message 详细消息
     */
    public OguException(String message) {
        super(message);
    }

    /**
     * 使用详细消息和原因构造异常
     *
     * @param message 详细消息
     * @param cause   原因
     */
    public OguException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用原因构造异常
     *
     * @param cause 原因
     */
    public OguException(Throwable cause) {
        super(cause);
    }
}
