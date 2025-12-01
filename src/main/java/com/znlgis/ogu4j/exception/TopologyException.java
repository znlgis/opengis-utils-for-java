package com.znlgis.ogu4j.exception;

/**
 * 空间拓扑异常
 * <p>
 * 当几何对象的空间拓扑关系存在问题时抛出此异常。
 * 可能的原因包括：自相交、无效坐标、环未闭合等拓扑错误。
 * </p>
 */
public class TopologyException extends OguException {

    /**
     * 使用详细消息构造异常
     *
     * @param message 详细消息
     */
    public TopologyException(String message) {
        super(message);
    }

    /**
     * 使用详细消息和原因构造异常
     *
     * @param message 详细消息
     * @param cause   原因
     */
    public TopologyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用原因构造异常
     *
     * @param cause 原因
     */
    public TopologyException(Throwable cause) {
        super(cause);
    }
}
