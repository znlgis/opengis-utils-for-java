package com.znlgis.ogu4j.datasource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.znlgis.ogu4j.common.EncodingUtil;
import com.znlgis.ogu4j.model.layer.OguField;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shapefile工具类
 * <p>
 * 提供Shapefile文件的校验、编码检测和字段名称格式化功能。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 *
 * @author znlgis
 * @since 1.0.0
 */
public class ShpUtil {
    /**
     * 格式化字段名
     *
     * @param fields 字段列表
     */
    public static void formatFieldName(List<OguField> fields) {
        for (OguField field : fields) {
            if (field.getName().length() > 10) {
                String name = field.getName().substring(0, 10);
                String finalName = name;
                Optional<OguField> optional = fields.stream().filter(f -> f.getName().equals(finalName)).findFirst();
                if (optional.isPresent()) {
                    if (optional.get().getName().matches(".*_\\d$")) {
                        name = field.getName().substring(0, 8) + "_" +
                                (Integer.parseInt(optional.get().getName().substring(9)) + 1);
                    } else {
                        name = field.getName().substring(0, 8) + "_1";
                    }
                }
                field.setName(name);
            }
        }
    }

    /**
     * 检查shp文件编码及必要文件
     *
     * @param shpPath shp文件路径
     * @return 编码
     */
    public static Charset check(String shpPath) {
        List<String> shpFiles = CollUtil.newArrayList(".shp", ".shx", ".dbf", ".prj");
        List<String> qs = new ArrayList<>();
        for (String shpFile : shpFiles) {
            File file = null;
            String filePath = shpPath.substring(0, shpPath.lastIndexOf(".")) + shpFile;
            if (FileUtil.exist(filePath)) {
                file = new File(filePath);
            } else {
                filePath = shpPath.substring(0, shpPath.lastIndexOf(".")) + shpFile.toUpperCase();
                if (FileUtil.exist(filePath)) {
                    file = new File(filePath);
                }
            }

            if (file == null) {
                qs.add(shpFile);
            }
        }

        if (!qs.isEmpty()) {
            throw new RuntimeException("缺少必要文件：" + CharSequenceUtil.join(",", qs));
        }

        Charset shpCharset = null;
        File cpgFile = null;
        String cpgPath = shpPath.substring(0, shpPath.lastIndexOf(".")) + ".cpg";
        if (FileUtil.exist(cpgPath)) {
            cpgFile = new File(cpgPath);
        } else {
            cpgPath = shpPath.substring(0, shpPath.lastIndexOf(".")) + ".CPG";
            if (FileUtil.exist(cpgPath)) {
                cpgFile = new File(cpgPath);
            }
        }

        if (cpgFile != null) {
            Charset cpgCharset = EncodingUtil.getFileEncoding(cpgFile);
            String cpgString = FileUtil.readString(cpgFile, cpgCharset);
            try {
                shpCharset = Charset.forName(cpgString.trim());
            } catch (Exception e) {
                throw new RuntimeException("CPG文件保存的编码格式错误");
            }
        }

        if (shpCharset == null) {
            String dbfPath = shpPath.substring(0, shpPath.lastIndexOf(".")) + ".dbf";
            if (!FileUtil.exist(dbfPath)) {
                dbfPath = shpPath.substring(0, shpPath.lastIndexOf(".")) + ".DBF";
            }

            byte[] bs = FileUtil.readBytes(dbfPath);
            if (bs != null && bs.length >= 30) {
                byte b = bs[29];
                if (b == 0x4d) {
                    shpCharset = Charset.forName("GBK");
                }
            }
        }

        if (shpCharset == null) {
            shpCharset = StandardCharsets.UTF_8;
        }

        return shpCharset;
    }

    /**
     * 工具类禁止实例化。
     * <p>
     * 所有功能均通过静态方法提供，实例化此类没有意义。
     * </p>
     */
    private ShpUtil() {
        // 防止外部 new
    }
}
