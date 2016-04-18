package com.hz.entity;


import android.util.Log;

public class PickerItem {
    public String key;//传入后台的数据
    public String value;//显示的数据

    public PickerItem() {
    }

    public PickerItem(String key, String value) {
        Log.d("Test",getClass().getSimpleName()+"被调用了");

        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "PickerItem{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
