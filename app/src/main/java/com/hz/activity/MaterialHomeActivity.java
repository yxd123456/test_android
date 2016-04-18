package com.hz.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hz.R;
import com.hz.activity.base.BaseActivity;
import com.hz.common.Constans;
import com.hz.dialog.ProgressHUD;
import com.hz.drawable.UserNameDrawable;
import com.hz.fragment.OffLineMapListFragment;
import com.hz.fragment.ProjectListFragment;
import com.hz.helper.CommonHelper;
import com.hz.helper.SharedPreferencesHelper;
import com.hz.util.HttpManager;
import com.hz.util.NetworkManager;
import com.hz.util.PackageUtil;
import com.hz.view.ActionBarDrawerToggleCompat;
import com.hz.view.PopupToast;
import com.hz.view.RoundImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.Request;

import cn.jpush.android.api.JPushInterface;

/**
 * MaterialDesign风格的首页
 */
public class MaterialHomeActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    //变量常量+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    private ActionBarDrawerToggleCompat mDrawerToggle;
    private DrawerLayout mDrawerLayout;//滑动菜单布局
    private MenuItem mPreMenuItem;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppCompatImageView mToolBarImageView;
    private AppBarLayout mAppBarLayout;
    private Fragment currentFragment;
    private ProgressHUD mCheckLoginProgress = null;//检查登陆状态进度
    private Handler handler = new Handler();
    public static final String TAG = MaterialHomeActivity.class.getSimpleName();

    //生命周期+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setUpDrawerLayout();
        addDefaultFragment();
        checkLogin();
    }
    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(this);

    }
    @Override
    protected void onPause() {
        JPushInterface.onPause(this);
        super.onPause();
    }

    //重写方法+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }
        showBackPressedAlert();
    }
    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_item_about:
                showAbout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     *侧滑菜单的点击事件
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (mPreMenuItem != null) {
            mPreMenuItem.setChecked(false);
        }
        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
        mPreMenuItem = menuItem;
        Fragment toReplaceFragment = null;
        switch (menuItem.getItemId()) {
            case R.id.id_menu_item_home:
                mCollapsingToolbarLayout.setTitle(getString(R.string.title_activity_projectList));
                mToolBarImageView.setImageResource(R.drawable.vp_bg_1);
                mAppBarLayout.setExpanded(true, true);
                toReplaceFragment = new ProjectListFragment();
                break;
          /*  case R.id.id_menu_item_materiel_sync:
                mCollapsingToolbarLayout.setTitle(getString(R.string.string_home_materiel_sync));
                mToolBarImageView.setImageResource(R.drawable.vp_bg_2);
                mAppBarLayout.setExpanded(true, true);
                toReplaceFragment = new MaterielDataSyncFragment();
                break;*/
            case R.id.id_menu_item_localmap:
                mCollapsingToolbarLayout.setTitle(getString(R.string.string_localMap));
                mToolBarImageView.setImageResource(R.drawable.vp_bg_3);
                mAppBarLayout.setExpanded(true, true);
                toReplaceFragment = new OffLineMapListFragment();
                break;
            case R.id.id_menu_item_loginout://退出
                loginOut();
                clearCache();
                break;
        }

        if (toReplaceFragment != null
                && currentFragment != null
                && !TextUtils.equals(currentFragment.getClass().getName(), toReplaceFragment.getClass().getName())) {

            menuItemClick(toReplaceFragment);
            currentFragment = toReplaceFragment;
        }
        return true;
    }

    //主要方法+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     *  设置导航菜单相关属性
     */
    private void setUpDrawerLayout() {
        //滑动菜单总布局
        mDrawerLayout = (DrawerLayout) findViewById(R.id.id_materialhome_drawer_layout);

        //菜单按钮
        mDrawerToggle = new ActionBarDrawerToggleCompat(this, mDrawerLayout, R.string.string_home_title, R.string.string_home_materiel_sync);
        mDrawerToggle.setSimpleDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                String userName = SharedPreferencesHelper.getUserRealName(MaterialHomeActivity.this);

                TextView mUserNameTextView = (TextView) drawerView.findViewById(R.id.id_home_username);
                mUserNameTextView.setText(userName);

                RoundImageView roundImageView = (RoundImageView) drawerView.findViewById(R.id.id_user_image);
                ViewGroup.LayoutParams layoutParams = roundImageView.getLayoutParams();
                roundImageView.setImageDrawable(new UserNameDrawable(userName, layoutParams.width, layoutParams.height, getResources().getDimension(R.dimen.user_image_text_size)));
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        //导航栏
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.id_materialhome_nv_menu);
        mNavigationView.setNavigationItemSelectedListener(this);

        //可折叠ToolBar
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.id_home_collapsingtoolbarlayout);
        mCollapsingToolbarLayout.setTitle(getString(R.string.title_activity_projectList));
        mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.content_background_color));
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.content_background_color));

        setMdToolBar(R.id.id_material_toolbar);

        //工具条图片
        mToolBarImageView = (AppCompatImageView) findViewById(R.id.id_home_toolbarimageview);
        mToolBarImageView.setImageResource(R.drawable.vp_bg_1);

        //AppBarLayout
        mAppBarLayout = (AppBarLayout) findViewById(R.id.id_home_appbarlayout);

        //actionBar 启用左侧按钮
        setMDToolBarBackEnable(true);
    }
    /**
     * 添加系统默认的Fragment
     **/
    private void addDefaultFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.id_fragment_home_content, currentFragment = new ProjectListFragment());
        transaction.commit();
    }
    /**
     * 检查系统登陆状态
     */
    private void checkLogin() {
        if (!NetworkManager.isConnectAvailable(this)) {
            PopupToast.showError(this, "当前网络不可用,请检查网络后重试");
            return;
        }

        mCheckLoginProgress = ProgressHUD.show(this, "检查用户登录状态中");
        HttpManager.getInstance().isRemberMeExpiredASync(new HttpManager.SessionCheckCallBack() {
            @Override
            public void sessionExpired(final boolean isExpired) {
                if (isExpired) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CommonHelper.toLoginActivity(MaterialHomeActivity.this);
                            mCheckLoginProgress.dismiss();
                        }
                    }, 500);
                } else {
                    mCheckLoginProgress.dismiss();
                }
            }
        });
    }

    //次要方法+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 获取appBarLayout
     **/
    public AppBarLayout getAppBarLayout() {
        return mAppBarLayout;
    }
    /**
     * 替换主页fragment信息
     */
    private void menuItemClick(Fragment toReplaceFragment) {
        //替换fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.id_fragment_home_content, toReplaceFragment);
        transaction.commit();
    }
    /***
     * 退出系统
     **/
    private void loginOut() {
        if (NetworkManager.isConnectAvailable(this)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Request request = new Request.Builder()
                            .url(Constans.LOGIN_URL_LOGOUT)
                            .get().build();
                    HttpManager.getInstance().addSyncHttpTask(request);
                    HttpManager.getInstance().removeUserCookie();
                }
            }).start();

            SharedPreferencesHelper.clearPreferencesLoginInfo(this);
            CommonHelper.toLoginActivity(MaterialHomeActivity.this);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("当前网络不可用，请检查网络设置后重试!");
            builder.setTitle("提示");
            builder.setIcon(R.drawable.ic_warning_black_96dp);
            builder.setPositiveButton("确认",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builder.create().show();
        }
    }
    /**
     * 清除ImageLoader 和 OkHttpClient缓存
     **/
    public void clearCache() {
        try {
            HttpManager.getInstance().clear();
            ImageLoader.getInstance().getDiskCache().clear();
        } catch (Exception e) {
            Log.e(TAG, "clearCache: ", e);
        }
    }
    /**
     * 显示退出提示框
     * *
     */
    public void showBackPressedAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("确定要退出吗?");
        builder.setTitle("提示");
        builder.setIcon(R.drawable.ic_warning_black_96dp);
        builder.setPositiveButton("确认",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });

        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }
    private void showAbout() {
        PopupToast.show(this, Gravity.BOTTOM, PackageUtil.isDebugEnable(this) ? "DEBUG" : "RELEASE", PopupToast.CUSTOME);
    }

}
