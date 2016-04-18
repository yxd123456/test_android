package com.hz.helper;

import android.util.Log;

/**
 * 点位杆号排序帮助类
 */
public class PointNumHelper {


    public PointNumHelper() {
        Log.d("Test",getClass().getSimpleName()+"被调用了");
    }

    /**
     * 排序点杆号
     **/
    public static String orderPointNum(String pointNum, int num) {
        if (com.hz.util.TextUtils.isEmpty(pointNum)) {
            return null;
        }
        int last1CharIndex = pointNum.length() - 1;
        int last2CharIndex = last1CharIndex - 1;
        char last1Char = pointNum.charAt(last1CharIndex);
        StringBuilder sb = new StringBuilder();
        //1.以#结束
        if (last1Char == '#') {
            char last2Char = pointNum.charAt(last2CharIndex);
            if (Character.isDigit(last2Char)) {//数字
                sb.append(pointNum.substring(0, last2CharIndex));
                sb.append(Integer.parseInt(last2Char + "") + num);
                sb.append(last1Char);
            } else if (Character.isLetter(last2Char)) {
                sb.append(pointNum.substring(0, last1CharIndex));
                sb.append(num);
                sb.append(last1Char);
            }
        } else if (Character.isDigit(last1Char)) {
            sb.append(pointNum.substring(0, last1CharIndex));
            sb.append(Integer.parseInt(last1Char + "") + num);
        } else if (Character.isLetter(last1Char)) {
            sb.append(pointNum.substring(0, last1CharIndex + 1));
            sb.append(num);
        }
        return sb.toString();
    }

}
