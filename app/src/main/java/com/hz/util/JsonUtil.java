package com.hz.util;

import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hz.common.Constans;
import com.hz.entity.ResponseStateEntity;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * json处理工具
 */
public class JsonUtil {
    public static final String TAG = "JsonUtil";



    /**
     * 获取已经配置好的objectMapper
     * //或略未知的属性
     */
    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constans.DATE_PATTERN_DEFAULT, Locale.CHINA);
        objectMapper.setDateFormat(simpleDateFormat);
        return objectMapper;
    }

    /**
     * 对象转换为json
     *
     * @param object 需要转换为json对象
     *               *
     */
    public static String convertObjToJson(Object object) {
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            return null;
        }
    }


    /**
     * 对象转换为json
     *
     * @param jsonStr   需要转换为obj的对象
     * @param valueType 转换类型                    *
     */
    public static <T> T convertJsonToObj(String jsonStr, Class<T> valueType) {
        try {
            return getObjectMapper().readValue(jsonStr, valueType);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            return null;
        }
    }
}
