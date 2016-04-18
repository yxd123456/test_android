package com.hz.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hz.R;
import com.hz.activity.LoginActivity;
import com.hz.activity.MaterialHomeActivity;
import com.hz.adapter.ProjectListAdapter;
import com.hz.common.Constans;
import com.hz.entity.ResponseArrayWrapperEntity;
import com.hz.fragment.base.BaseFragment;
import com.hz.greendao.dao.ProjectEntity;
import com.hz.helper.CommonHelper;
import com.hz.service.MaterielDataSyncService;
import com.hz.util.DensityUtil;
import com.hz.util.HttpManager;
import com.hz.util.JsonUtil;
import com.hz.util.NetworkManager;
import com.hz.helper.SharedPreferencesHelper;
import com.hz.view.PopupToast;
import com.hz.view.SuperRecyclerView;
import com.hz.view.recyclerview.HorizontalDividerItemDecoration;
import com.squareup.okhttp.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 项目列表
 */
public class ProjectListFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, SuperRecyclerView.OnEmptyViewClickListener {
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    public static final String PROJECT_OBJ_KEY = "PROJECT_OBJ_KEY";//传递项目数据
    public static final int REQUESTCODE_TOMAP = 1;//打点 连线
    public static final String TAG = ProjectListFragment.class.getSimpleName();

    public ProjectListAdapter mProjectListAdapter;
    private List<ProjectEntity> mProjectListEntityList = new ArrayList<>();
    private MaterialHomeActivity homeActivity;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View rootView;

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_project_list_fragment, null, false);
        initComponent(rootView);
        return rootView;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        homeActivity = (MaterialHomeActivity) this.getActivity();
        //当AppBarLayout滑动到最下面时启用滑动刷新
        if (this.homeActivity != null && this.homeActivity.getAppBarLayout() != null) {
            this.homeActivity.getAppBarLayout().addOnOffsetChangedListener(onOffsetChangedListener);
        }

    }
    @Override
    public void onDetach() {
        super.onDetach();
        if (this.homeActivity != null && this.homeActivity.getAppBarLayout() != null) {
            this.homeActivity.getAppBarLayout().removeOnOffsetChangedListener(onOffsetChangedListener);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        checkLoginStatus();
    }

    @Override
    public void onRefresh() {
        checkLoginStatus();
    }
    @Override
    public void onEmptyViewClick(View emptyView) {
        checkLoginStatus();
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * AppBar监听
     **/
    private AppBarLayout.OnOffsetChangedListener onOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {
        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            mSwipeRefreshLayout.setEnabled(verticalOffset == 0);
        }
    };
    /**
     * 检查用户的登录状态，如果登录已经失效就重新登录系统
     */
    private void checkLoginStatus() {
        if (!NetworkManager.isConnectAvailable(homeActivity)) {
            mSwipeRefreshLayout.setRefreshing(false);
            PopupToast.showError(homeActivity, "当前网络不可用,请检查网络后重试");
            return;
        }

        HttpManager.getInstance().isRemberMeExpiredASync(new HttpManager.SessionCheckCallBack() {
            @Override
            public void sessionExpired(boolean isExpired) {
                if (!isExpired) {
                    getServerProject2();
                } else {
                    sessinExpiredHandler();
                }
            }
        });
    }
    /**
     * 登录超时处理
     **/
    private void sessinExpiredHandler() {
        rootView.post(new Runnable() {
            @Override
            public void run() {
                PopupToast.showError(homeActivity, "用户登录失效,请重新登陆");
            }
        });
        CommonHelper.toLoginActivity(homeActivity);
    }
    /**
     * 获取服务器端项目数据
     **/
    private void getServerProject2() {
        final Long userId = SharedPreferencesHelper.getUserId(homeActivity);
        Request request = new Request.Builder().url(String.format(Constans.PROJECT_URL, userId)).get().build();
        HttpManager.getInstance().addAsyncHttpTask(request, new HttpManager.HttpTaskCallback() {
            @Override
            public void onFailure(Exception e) {
                mProjectListAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "onFailure: ", e);
                showRetry();
            }

            @Override
            public void onSuccess(String respstr) {
                try {
                    analysisJson(respstr);
                    //Log.d("INEEDTHIS", " "+respstr);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                mProjectListAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
                showLoadSuccess();
            }
        });
    }
    /**
     * 弹出Snackbar显示加载成功
     **/
    private void showLoadSuccess() {
        if (rootView == null) {
            return;
        }
        final Snackbar snackbar = Snackbar.make(rootView, "项目数据加载成功", Snackbar.LENGTH_SHORT);
        snackbar.show();
        snackbar.setAction("ok", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
    }
    /**
     * 显示加载失败并提示重新加载
     **/
    private void showRetry() {
        if (rootView == null) {
            return;
        }
        Snackbar.make(rootView, "项目数据加载失败", Snackbar.LENGTH_LONG).setAction("重试", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLoginStatus();
            }
        }).show();
    }
    /**
     * 项目列表json解析
     */
    private void analysisJson(String jsonStr) throws Exception {
        Log.d(TAG, "analysisJson: " + jsonStr);
        ResponseArrayWrapperEntity.ProjectWrapperEntity projectWrapperEntity = JsonUtil.convertJsonToObj(
                jsonStr,
                ResponseArrayWrapperEntity.ProjectWrapperEntity.class
        );
        if (projectWrapperEntity == null) {
            return;
        }

        //更新材料数据
        if (SharedPreferencesHelper.getNeedToUpdateMaterialDataIdentifier(homeActivity)) {
            homeActivity.startService(new Intent(homeActivity, MaterielDataSyncService.class));
        }

        mProjectListEntityList.clear();
        mProjectListEntityList.addAll(projectWrapperEntity.getData());


        //排序
        Collections.sort(mProjectListEntityList, new Comparator<ProjectEntity>() {
            @Override
            public int compare(ProjectEntity lhs, ProjectEntity rhs) {
                return rhs.getCjsj().compareTo(lhs.getCjsj());
            }
        });
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    private void initComponent(View rootView) {
        //初始化项目列表
        SuperRecyclerView mProjectRecyclerView = (SuperRecyclerView) rootView.findViewById(R.id.id_listview_project_list);
        mProjectListAdapter = new ProjectListAdapter(this, mProjectListEntityList);
        mProjectRecyclerView.setAdapter(mProjectListAdapter);

        //设置布局管理器
        GridLayoutManager layoutManager = new GridLayoutManager(homeActivity, 1);
        mProjectRecyclerView.setLayoutManager(layoutManager);
        mProjectRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //分割线
        mProjectRecyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this.getActivity())
                        .colorResId(R.color.view_seperator_color)
                        .size(DensityUtil.dip2px(this.getActivity(), 1))
                        .build()
        );

        //下拉刷新
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.id_projectlist_swiperefreshlayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        //空视图时点击事件
        mProjectRecyclerView.setOnEmptyViewClickListener(this);
    }

}
