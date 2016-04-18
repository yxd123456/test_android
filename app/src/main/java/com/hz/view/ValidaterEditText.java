package com.hz.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;

import com.hz.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义验证edittext
 */
public class ValidaterEditText extends AppCompatEditText {

    //正则表达式 不能输入的特殊字符
    public static final String REG_SPECIAL_CHARACTER = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";//查找特殊字符正则


    /*public static final String REG_TOWER_NUM = "^([0-9]{1,}|[0-9]{1,}-[0-9]{1,})#$";*/
    /*public static final String REG_TOWER_NUM = "^[a-zA-Z]{0,1}[0-9]{0,}(-[a-zA-Z]{0,1}[0-9]{0,}){0,}#{0,1}$";*/

    /***
     * 可以匹配的字符
     * a,1,11,1#,11#,a#,a1#,a12#
     * a-a,a-1,1-a,1-1,a-11,11-11,a-a#,a-1#,1-a#,1-1#,
     * *************************************************************
     * ***********************************************************************
     * *
     * *
     * *
     **/
    public static final String REG_TOWER_NUM = "^([a-zA-Z]{1}[0-9]{0,}|[a-zA-Z]{0,1}[0-9]{1,})(-[a-zA-Z]{1}[0-9]{0,}|-[a-zA-Z]{0,1}[0-9]{1,}){0,}#{0,1}$";

    public static final int VALIDATE_NOT_BLANK = 1101;
    public static final int VALIDATE_NOT_CONTACT_SPECIAL_CHARACTOR = 1102;
    public static final int VALIDATE_NOT_TEXT_TOO_LONG = 1103;
    public static final int VALIDATE_NOT_NUM_TOO_LONG = 1104;
    public static final int VALIDATE_NOT_MIN_IS_1 = 1105;
    public static final int VALIDATE_ONLY_NUM_JH_XHX = 1106;
    public static final int VALIDATE_NOT_MAX_IS_10 = 1107;

    public static final int NUM_MAX_LENGTH = 6;//数字字符最大长度
    public static final int NUM_MAX_VALUE = Integer.MAX_VALUE / 2;//数字最大值
    public static final int CHAR_MAX_LENGTH = 1000;//字符最大长度
    private SparseBooleanArray needToValidateMap = new SparseBooleanArray();//待验证的类型

    public ValidaterEditText(Context context) {
        super(context);
        init(null);
    }

    public ValidaterEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ValidaterEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = this.getContext().obtainStyledAttributes(attrs, R.styleable.ValidaterEditText);

        boolean validateNotBlank = a.getBoolean(R.styleable.ValidaterEditText_validateNoteBlank, false);
        boolean validateNotContactSpecialCharactor = a.getBoolean(R.styleable.ValidaterEditText_validateNotContactSpecialCharactor, true);
        boolean validateNotTextToLong = a.getBoolean(R.styleable.ValidaterEditText_validateNotTextToLong, false);
        boolean validateNotNumToLong = a.getBoolean(R.styleable.ValidaterEditText_validateNotNumToLong, false);
        boolean validateNotMinIs1 = a.getBoolean(R.styleable.ValidaterEditText_validateNotMinIs1, false);
        boolean validateOnlyNumJhXhx = a.getBoolean(R.styleable.ValidaterEditText_validateOnlyNumJXhx, false);
        boolean validateNotMaxIs10 = a.getBoolean(R.styleable.ValidaterEditText_validateNotMaxIs10, false);

        needToValidateMap.put(VALIDATE_NOT_BLANK, validateNotBlank);
        needToValidateMap.put(VALIDATE_NOT_CONTACT_SPECIAL_CHARACTOR, validateNotContactSpecialCharactor);
        needToValidateMap.put(VALIDATE_NOT_TEXT_TOO_LONG, validateNotTextToLong);
        needToValidateMap.put(VALIDATE_NOT_NUM_TOO_LONG, validateNotNumToLong);
        needToValidateMap.put(VALIDATE_NOT_MIN_IS_1, validateNotMinIs1);
        needToValidateMap.put(VALIDATE_ONLY_NUM_JH_XHX, validateOnlyNumJhXhx);
        needToValidateMap.put(VALIDATE_NOT_MAX_IS_10, validateNotMaxIs10);

        a.recycle();
    }

    @Override
    public void setError(CharSequence error) {
        setError(error, null);
    }


    @Override
    public void setError(CharSequence error, Drawable icon) {
        Double height = this.getMeasuredHeight() * 0.6;
        Drawable dr = getResources().getDrawable(R.drawable.error);
        if (dr != null) {
            dr.setBounds(0, 0, height.intValue(), height.intValue());
        }
        super.setError(error, dr);
    }

    /**
     * 根据配置文件xml中配置的验证参数进行验证
     * *
     */
    public boolean validateByConfig() {
        //重置错误
        clearError();

        //根据配置验证
        boolean allValid = true;
        for (int i = 0; i < needToValidateMap.size(); i++) {
            int key = needToValidateMap.keyAt(i);
            if (needToValidateMap.get(key)) {
                allValid = validateByKey(key) && allValid;
            }
        }
        return allValid;
    }


    /**
     * 去除某个验证规则
     **/
    public void removeValidate(int validateRule) {
        needToValidateMap.put(validateRule, false);
    }

    /**
     * 重置错误提示
     * *
     */
    public void clearError() {
        super.setError(null, null);
        super.setError(null);
    }

    /**
     * 根据key做不同的输入验证
     * *
     */
    private boolean validateByKey(int key) {
        boolean result = true;
        switch (key) {
            case VALIDATE_NOT_BLANK:
                result = validateNotBlank();
                break;
            case VALIDATE_NOT_CONTACT_SPECIAL_CHARACTOR:
                result = validateNotContactSpecialCharactor();
                break;
            case VALIDATE_NOT_TEXT_TOO_LONG:
                result = validateNotTextToLong();
                break;
            case VALIDATE_NOT_NUM_TOO_LONG:
                result = validateNotNumToLong();
                break;
            case VALIDATE_NOT_MIN_IS_1:
                result = validateNotMinIs1();
                break;
            case VALIDATE_ONLY_NUM_JH_XHX:
                result = validateOnlyNumJhXhx();
                break;
            case VALIDATE_NOT_MAX_IS_10:
                result = validateNotMaxIs10();
                break;
        }
        return result;
    }


    /**
     * 验证输入只能包括数字 1#    1-45#
     * *
     */
    private boolean validateOnlyNumJhXhx() {
        if (!TextUtils.isEmpty(this.getText())) {
            Pattern p = Pattern.compile(REG_TOWER_NUM);
            Matcher m = p.matcher(this.getText());
            if (m.matches()) {
                return true;
            } else {
                this.setError(getContext().getString(R.string.string_validate_input_textonlynumjxhx));
                return false;
            }
        } else {
            return true;
        }
    }


    /**
     * 验证输入不能为空
     * *
     */
    public boolean validateNotBlank() {
        if (!TextUtils.isEmpty(this.getText())) {
            return true;
        } else {
            this.setError(getContext().getString(R.string.string_validate_input_textnotblank));
            return false;
        }
    }

    /*
     * 验证输入不能包括特殊字符
     * */

    public boolean validateNotContactSpecialCharactor() {
      /*  String str = this.getText().toString();
        System.out.println(str);*/
        if (!TextUtils.isEmpty(this.getText())) {
            Pattern p = Pattern.compile(REG_SPECIAL_CHARACTER);
            Matcher m = p.matcher(this.getText());
            if (!m.find()) {
                return true;
            } else {
                this.setError(getContext().getString(R.string.string_validate_input_textnotcontactspecialtext));
                return false;
            }
        } else {
            return true;
        }
    }

    /*
     * 验证输入字符不能太长
     * */

    public boolean validateNotTextToLong() {
        if (!TextUtils.isEmpty(this.getText())) {
            int charCount = this.getText().length();
            if (charCount < CHAR_MAX_LENGTH) {
                return true;
            } else {
                this.setError(getContext().getString(R.string.string_validate_input_textnottolong));
                return false;
            }
        } else {
            return true;
        }
    }

    /*
     * 验证输入数字不能太大
     * <p/>
     * */

    public boolean validateNotNumToLong() {
        String text = this.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            if (text.length() < NUM_MAX_LENGTH) {
                int num = Integer.parseInt(text);
                if (num <= NUM_MAX_VALUE) {
                    return true;
                } else {
                    this.setError(getContext().getString(R.string.string_validate_input_numnottolong));
                    return false;
                }
            } else {
                this.setError(getContext().getString(R.string.string_validate_input_numnottolong));
                return false;
            }

        } else {
            return true;
        }
    }

    /*
     * 验证输入数字不能小于1
     * */

    public boolean validateNotMinIs1() {
        String text = this.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            if (text.length() < NUM_MAX_LENGTH) {
                int num = Integer.parseInt(text);
                if (num >= 1) {
                    return true;
                } else {
                    this.setError(getContext().getString(R.string.string_validate_input_numnotminis1));
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /*
        * 验证输入数字不能大于10
        * */
    private boolean validateNotMaxIs10() {
        String text = this.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            if (text.length() < NUM_MAX_LENGTH) {
                int num = Integer.parseInt(text);
                if (num <= 10) {
                    return true;
                } else {
                    this.setError(getContext().getString(R.string.string_validate_input_numnotmaxis10));
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

}
