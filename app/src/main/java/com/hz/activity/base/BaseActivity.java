package com.hz.activity.base;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.hz.MainApplication;
import com.hz.debug.hv.ViewServer;
import com.hz.greendao.dao.DaoSession;
import com.hz.util.PackageUtil;
import com.hz.util.WindowsUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

/**
 * activity 基类
 */
public class BaseActivity extends AppCompatActivity {
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    public static final String TAG = BaseActivity.class.getSimpleName();
    public boolean useTranslucentStatusAndNavigation = false;//设置是否使用导航栏透明显示,设置此属性必须在super.onCreate()方法之前
    private Toolbar mMDToolBar;//Material Design 全局工具条
    public static ArrayList<String> list_id = new ArrayList<>();
    private int num = 0;

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Test", "当前的Activity是"+getClass().getSimpleName());
        if (useTranslucentStatusAndNavigation) {
            WindowsUtil.setTranslucentStatusAndNavigation(this);
        }
        if (PackageUtil.isDebugEnable(this)) {
            ViewServer.get(this).addWindow(this);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
        MobclickAgent.onResume(this);

        if (PackageUtil.isDebugEnable(this)) {
            ViewServer.get(this).setFocusedWindow(this);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
        MobclickAgent.onPause(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (PackageUtil.isDebugEnable(this)) {
            ViewServer.get(this).removeWindow(this);
        }
        System.gc();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null && item.getItemId() == android.R.id.home) {
            onLeftIconClick();
        }
        return super.onOptionsItemSelected(item);
    }
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

    public void point(){
        Log.d("Point", (num++)+"");
    }

    public void addId(String str){
        list_id.add(str);
    }

    public void removeAll(){
        list_id.clear();
    }

    public String random(){
        return ((int)(Math.random()*10))+((int)(Math.random()*100))+((int)(Math.random()*1000))+"";
    }

    public void log(String tag,String str){
        Log.d(tag, str);
    }
    public void log(String str){
        if(str == null){
            Log.d("KO", "空值");
        }
        Log.d("KO", str);
    }
    public void toast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
    /**
     * 将object转换为string
     * *
     */
    public String getString(Object obj) {
        return (obj == null) ? null : obj.toString();
    }
    /**
     * 获取全局DaoSession对象*
     */
    public DaoSession getDaoSession() {
        return this.getMainApplication().getDaoSession();
    }
    /**
     * 获取全局mainApplicatin对象*
     */
    public MainApplication getMainApplication() {
        return (MainApplication) this.getApplication();
    }
    public void onLeftIconClick() {
        this.finish();
    }
    /**
     * 获取系统工具条
     * *
     */
    public Toolbar getMDToolBar() {
        return mMDToolBar;
    }
    /***
     * 设置系统工具条
     **/
    public Toolbar setMdToolBar(@IdRes int resId) {
        Toolbar mHomeToolBar = (Toolbar) findViewById(resId);
        setSupportActionBar(mHomeToolBar);
        this.mMDToolBar = mHomeToolBar;
        return mHomeToolBar;
    }
    /**
     * 设置显示返回按钮
     **/
    public Toolbar setMDToolBarBackEnable(boolean enable) {
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeButtonEnabled(enable);
            ab.setDisplayHomeAsUpEnabled(enable);
        }
        return mMDToolBar;
    }
    /**
     * 设置系统工具条标题
     * *
     */
    public Toolbar setMDToolBarTitle(@StringRes int resId) {
        setMDToolBarTitle(getResources().getString(resId));
        return mMDToolBar;
    }
    /**
     * 设置系统工具条标题
     * *
     */
    public Toolbar setMDToolBarTitle(String title) {
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(title);
        }
        return mMDToolBar;
    }
}
