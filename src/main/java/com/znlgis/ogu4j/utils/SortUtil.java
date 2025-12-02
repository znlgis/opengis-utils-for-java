package com.znlgis.ogu4j.utils;

import cn.hutool.core.text.CharSequenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串自然排序工具类
 * <p>
 * 提供包含数字的字符串自然排序功能，如"第5章" &lt; "第10章"。
 * 所有方法均为静态方法，无需实例化即可使用。
 * </p>
 *
 * @author znlgis
 * @version 1.0.0
 * @since 1.0.0
 */
public class SortUtil {
    private static final Pattern SPLITSTRINGPATTERN = Pattern.compile("(\\D+)|(\\d+)");
    private static final Pattern ISNUMPATTERN = Pattern.compile("\\d+");

    private SortUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 包含数字的字符串进行比较（按照从小到大排序）
     *
     * @param string1 字符串1，如：第5章第100节课
     * @param string2 字符串2，如：第5章第10节课
     * @return 比较结果，0：相等，-1：string1小于string2，1：string1大于string2
     */
    public static int compareString(String string1, String string2) {
        //拆分两个字符串
        List<String> lstString1 = splitString(string1);
        List<String> lstString2 = splitString(string2);
        //依次对比拆分出的每个值
        int index = 0;
        while (true) {
            if (lstString1.equals(lstString2)) {
                return 0;
            }
            String str1 = index < lstString1.size() ? lstString1.get(index) : "";
            String str2 = index < lstString2.size() ? lstString2.get(index) : "";
            //字符串相等则继续判断下一组数据
            if (str1.equals(str2)) {
                index++;
                continue;
            }
            //是纯数字，比较数字大小
            if (isNum(str1) && isNum(str2)) {
                return Integer.parseInt(str1) < Integer.parseInt(str2) ? -1 : 1;
            }

            return CharSequenceUtil.compare(str1, str2, true);
        }
    }

    /**
     * 拆分字符串
     * 输入：第5章第100节课
     * 返回：[第,5,章第,100,节课]
     *
     * @param string 字符串
     * @return 拆分后的字符串列表
     */
    private static List<String> splitString(String string) {
        Matcher matcher = SPLITSTRINGPATTERN.matcher(string);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    /**
     * 是否是纯数字
     *
     * @param string 字符串
     * @return 是否是纯数字
     */
    private static Boolean isNum(String string) {
        return ISNUMPATTERN.matcher(string).matches();
    }
}
