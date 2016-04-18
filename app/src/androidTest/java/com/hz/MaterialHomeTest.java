package com.hz;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;

import com.hz.activity.MaterialHomeActivity;

import java.security.Key;

/**
 * 主页测试
 */
public class MaterialHomeTest extends ActivityInstrumentationTestCase2<MaterialHomeActivity> {


    public MaterialHomeTest() {
        super("com.hz.activity.MaterialHomeActivity", MaterialHomeActivity.class);
    }


    public void testaa() throws Exception {


    }

    public void testClick(){
        MaterialHomeActivity homeActivity = getActivity();

        /*for (int i=0;i<100;i++){
            this.sendKeys(KeyEvent.KEYCODE_BACK);
        }*/


    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
