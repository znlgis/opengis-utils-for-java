package com.znlgis.ogu4j.common;

import cn.hutool.core.io.CharsetDetector;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 文件编码检测工具类
 * <p>
 * 提供文件编码自动检测功能，支持UTF-8、GBK、GB2312、GB18030等常见编码。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 *
 * @author znlgis
 * @since 1.0.0
 */
public class EncodingUtil {
    private EncodingUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取文件编码,默认GBK
     *
     * @param file 文件
     * @return 编码
     */
    public static Charset getFileEncoding(File file) {
        Charset[] charsets = new Charset[]{
                StandardCharsets.UTF_8,
                Charset.forName("GBK"),
                Charset.forName("GB2312"),
                Charset.forName("GB18030")};

        Charset charset = CharsetDetector.detect(file, charsets);
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        return charset;
    }
}
