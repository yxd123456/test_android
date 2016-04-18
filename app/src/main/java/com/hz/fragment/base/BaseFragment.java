package com.hz.fragment.base;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;

/**
 * 基础Fragment
 */
public class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Test", "当前的Fragment是"+getClass().getSimpleName());
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
    }

}
