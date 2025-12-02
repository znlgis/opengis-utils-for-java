package com.znlgis.ogu4j.utils;

import java.math.BigDecimal;

/**
 * 数字格式化工具类
 * <p>
 * 提供数字格式化功能，如去除科学计数法显示等。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
public class NumUtil {
    private NumUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 去除科学计数法显示
     *
     * @param d 数字
     * @return 去除科学计数法的字符串
     */
    public static String getPlainString(Double d) {
        return BigDecimal.valueOf(d).toPlainString();
    }
}
