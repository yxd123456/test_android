/**
 * 项目名称:	edap
 * 创建时间:	2015年9月23日
 * (C) Copyright KeDu Corporation 2015
 * All Rights Reserved.
 * 注意：本内容仅限于杭州科度科技有限公司内部传阅，禁止外泄以及用于其他的商业目的。
 */
package com.hz.util;

import android.util.Log;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 包名：com.ketech.edap.util <br/>
 * 类名：EncryptUtil.java <br/>
 * 版本：version 1.0 <br/>
 * 作者：ZhangHeng <br/>
 * 描述：加密工具类
 */
public class EncryptUtil {
    public static final String TAG = "EncryptUtil";
    private final static String KEY_MAC = "HmacMD5";
    private final static String UTF_8_CHARSET = "UTF-8";
    public static final String EDAP_SALT = "edap";

    /**
     * 描述：	获取HmacMD5加密值 <br/>
     * 作者：	ZhangHeng
     *
     * @param plainText 密码
     * @param salt      加密机制
     *                                  <br/>
     */
    public static String getHmacMD5String(String plainText, String salt) {
        String sEncodedString = null;
        try {
            SecretKey key = new SecretKeySpec(salt.getBytes(UTF_8_CHARSET), KEY_MAC);
            Mac mac = Mac.getInstance(KEY_MAC);
            mac.init(key);

            byte[] bytes = mac.doFinal(plainText.getBytes(UTF_8_CHARSET));
            StringBuilder hash = new StringBuilder();

            for (byte aByte : bytes) {
                String hex = Integer.toHexString(0xFF & aByte);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            sEncodedString = hash.toString();
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
        return sEncodedString;
    }

    public static void main(String[] args) {
        try {
            System.out.println(EncryptUtil.getHmacMD5String("admin", "edap"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
