package com.znlgis.ogu4j.common;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.SystemPropsUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.znlgis.ogu4j.model.GdbGroupModel;

import java.nio.charset.StandardCharsets;

/**
 * GDAL命令行工具类
 * <p>
 * 提供GDAL命令行工具的调用功能，仅包含只能通过命令行实现的功能。
 * 使用前需确保GDAL命令行工具已正确安装和配置。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 *
 * @author znlgis
 * @since 1.0.0
 */
public class GdalCmdUtil {
    /**
     * ogrinfo
     */
    private static final String OGRINFO = "ogrinfo";
    /**
     * -json
     */
    private static final String TOJSON = "-json";

    private GdalCmdUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 执行ogrinfo命令获取返回信息
     *
     * @param gisPath GIS路径
     * @return 返回信息
     */
    private static JSONObject getOgrInfo(String gisPath) {
        SystemPropsUtil.set("GDAL_FILENAME_IS_UTF8", "YES");
        SystemPropsUtil.set("SHAPE_ENCODING", "UTF-8");
        String output = RuntimeUtil.execForStr(StandardCharsets.UTF_8, OGRINFO, TOJSON, gisPath);

        if (CharSequenceUtil.isBlank(output)) {
            throw new RuntimeException("ogrinfo命令执行失败，返回信息为空");
        }

        String jout = null;
        if (!JSONUtil.isTypeJSONObject(output)) {
            jout = output.substring(output.indexOf("{"));
        }

        if (!JSONUtil.isTypeJSONObject(jout)) {
            jout = output.substring(output.indexOf("{"), output.lastIndexOf("}") + 1);
        }

        if (!JSONUtil.isTypeJSONObject(jout)) {
            throw new RuntimeException("ogrinfo命令执行失败，返回信息：" + output);
        }

        return JSON.parseObject(jout);
    }

    /**
     * 获取GDB图层结构
     *
     * @param gdbPath GDB路径
     * @return GDB图层结构
     */
    public static GdbGroupModel getGdbDataStructure(String gdbPath) {
        JSONObject jb = GdalCmdUtil.getOgrInfo(gdbPath);
        return jb.getObject("rootGroup", GdbGroupModel.class);
    }
}
