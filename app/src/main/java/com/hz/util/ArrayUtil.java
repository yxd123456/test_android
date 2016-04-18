package com.hz.util;

import android.text.TextUtils;

import java.io.File;
import java.util.Arrays;

/**
 * 数组工具
 */
public class ArrayUtil {


    /**
     * new String[]{"taolong","wangdan","zhangsan","lishi"} ==>  edap\taolong\wangdan\zhangsan\lishi
     *
     * @param split 分隔符
     * @param strs  字符数组
     *              *
     */
    public static String join(String[] strs, String split) {
        StringBuilder sb = new StringBuilder();
        if (strs == null || split == null || strs.length <= 0) {
            return null;
        }

        for (int i = 0, count = strs.length; i < count; i++) {
            sb.append(strs[i]);
            if (i != (count - 1)) {
                sb.append(split);
            }
        }
        return sb.toString();
    }

    /**
     * 数组是否包含字符串
     **/
    public static boolean contains(String[] strings, String string) {
        if (strings == null || strings.length <= 0 || string == null) {
            return false;
        }

        for (String str : strings) {
            if (TextUtils.equals(str, string)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 合并两个数组
     *
     * @param strs1 数组1
     * @param strs2 数组2
     */
    public static String[] contact(String[] strs1, String[] strs2) {
        if (strs1 == null || strs2 == null) {
            return null;
        }
        String[] result = Arrays.copyOf(strs1, strs1.length + strs2.length);
        System.arraycopy(strs2, 0, result, strs1.length, strs2.length);
        return result;
    }

    /**
     * 添加字符串到数组
     *
     * @param strs1 数组1
     * @param str   添加到数组
     */
    public static String[] contact(String[] strs1, String str) {
        return contact(strs1, new String[]{str});
    }

    public static void main(String[] args) {
        String[] strings = new String[]{"taolong", "wangdan", "zhangsan", "lishi"};
        String[] strings2 = new String[]{"taolong", "wangdan", "zhangsan", "lishi"};

        System.out.println(ArrayUtil.join(ArrayUtil.contact(strings, strings2), File.separator));
    }
}
