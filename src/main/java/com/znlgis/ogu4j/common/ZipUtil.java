package com.znlgis.ogu4j.common;

import lombok.SneakyThrows;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.nio.charset.Charset;

/**
 * ZIP压缩工具类
 * <p>
 * 提供ZIP文件的压缩和解压功能，支持自定义编码。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 *
 * @author znlgis
 * @since 1.0.0
 */
public class ZipUtil {
    private ZipUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 压缩文件夹,默认GBK
     *
     * @param folderToAdd 文件夹
     * @param destZipPath 压缩包路径
     */
    @SneakyThrows
    public static void zip(File folderToAdd, String destZipPath) {
        try (ZipFile zipFile = new ZipFile(destZipPath)) {
            zipFile.setCharset(Charset.forName("GBK"));
            zipFile.addFolder(folderToAdd);
        }
    }

    /**
     * 压缩文件夹
     *
     * @param folderToAdd 文件夹
     * @param destZipPath 压缩包路径
     * @param charset     编码
     */
    @SneakyThrows
    public static void zip(File folderToAdd, String destZipPath, Charset charset) {
        try (ZipFile zipFile = new ZipFile(destZipPath)) {
            zipFile.setCharset(charset);
            zipFile.addFolder(folderToAdd);
        }
    }

    /**
     * 解压文件,默认GBK
     *
     * @param zipPath  压缩包路径
     * @param destPath 解压路径
     */
    @SneakyThrows
    public static void unzip(String zipPath, String destPath) {
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            zipFile.setCharset(Charset.forName("GBK"));
            zipFile.extractAll(destPath);
        }
    }

    /**
     * 解压文件,默认GBK
     *
     * @param zipPath  压缩包文件
     * @param destPath 解压路径
     */
    @SneakyThrows
    public static void unzip(File zipPath, String destPath) {
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            zipFile.setCharset(Charset.forName("GBK"));
            zipFile.extractAll(destPath);
        }
    }

    /**
     * 解压文件
     *
     * @param zipPath  压缩包路径
     * @param destPath 解压路径
     * @param charset  编码
     */
    @SneakyThrows
    public static void unzip(String zipPath, String destPath, Charset charset) {
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            zipFile.setCharset(charset);
            zipFile.extractAll(destPath);
        }
    }

    /**
     * 解压文件
     *
     * @param zipPath  压缩包文件
     * @param destPath 解压路径
     * @param charset  编码
     */
    @SneakyThrows
    public static void unzip(File zipPath, String destPath, Charset charset) {
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            zipFile.setCharset(charset);
            zipFile.extractAll(destPath);
        }
    }
}
