package com.hz.util;

import android.text.format.DateFormat;

import com.hz.common.Constans;

import java.util.Calendar;
import java.util.Locale;

/**
 * 时间操作工具
 */
public class DateUtil {

    /**
     * 生成当前日期字符串
     * *
     */
    public static String genCurrDateStr() {
        return genCurrDateStrWithFormat(Constans.DATE_PATTERN_LONG);
    }

    /**
     * 生成当前日期字符串
     * *
     */
    public static String genCurrDateStrWithFormat(String formate) {
        return DateFormat.format(formate, Calendar.getInstance(Locale.CHINA)) + "";
    }
}
