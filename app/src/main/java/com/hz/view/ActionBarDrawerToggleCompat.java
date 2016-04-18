package com.hz.view;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * 获取actionBarTogger Open事件
 */
public class ActionBarDrawerToggleCompat extends ActionBarDrawerToggle {

    private DrawerLayout.SimpleDrawerListener simpleDrawerListener;

    public static final String TAG = ActionBarDrawerToggleCompat.class.getSimpleName();

    public ActionBarDrawerToggleCompat(Activity activity, DrawerLayout drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    public ActionBarDrawerToggleCompat(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        if (simpleDrawerListener != null) {
            simpleDrawerListener.onDrawerOpened(drawerView);
        }
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        super.onDrawerClosed(drawerView);
        if (simpleDrawerListener != null) {
            simpleDrawerListener.onDrawerClosed(drawerView);
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        super.onDrawerSlide(drawerView, slideOffset);
        if (simpleDrawerListener != null) {
            simpleDrawerListener.onDrawerSlide(drawerView, slideOffset);
        }
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        super.onDrawerStateChanged(newState);
        if (simpleDrawerListener != null) {
            simpleDrawerListener.onDrawerStateChanged(newState);
        }
    }

    public void setSimpleDrawerListener(DrawerLayout.SimpleDrawerListener simpleDrawerListener) {
        this.simpleDrawerListener = simpleDrawerListener;
    }
}
