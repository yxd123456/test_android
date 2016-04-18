package com.hz.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.hz.R;
import com.hz.activity.base.BaseActivity;
import com.hz.adapter.ProjectGalleryPreviewAdapter;
import com.hz.fragment.ProjectListFragment;
import com.hz.greendao.dao.PointGalleryEntity;
import com.hz.greendao.dao.ProjectEntity;
import com.hz.helper.CommonHelper;
import com.hz.helper.DataBaseManagerHelper;
import com.hz.helper.SharedPreferencesHelper;
import com.hz.service.ProjectMapDataUploadService;
import com.hz.util.DensityUtil;
import com.hz.util.DeviceUtils;
import com.hz.util.HttpManager;
import com.hz.util.NetworkManager;
import com.hz.view.PopupToast;

import java.util.ArrayList;
import java.util.List;

/**
 * 采集信息预览页面
 * 启动模式SingleTask
 */
public class ProjectDataPreviewActivity extends BaseActivity {
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    public static final String TAG = ProjectDataPreviewActivity.class.getSimpleName();
    public static final String KEY_UPLOAD_PROJECTDATA_ENTITY = "KEY_UPLOAD_PROJECTDATA_ENTITY";//上传项目数据key

    private TextView mTextViewPointNum, mTextViewLineNum;

    public ProjectEntity mProjectEntity;//从项目列表传入项目数据
    public ProjectGalleryPreviewAdapter projectGalleryPreviewAdapter;
    public Handler mUiHandler = new Handler();//操作uihandler
    private List<PointGalleryEntity> mGalleryEntityList = new ArrayList<>();

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override//初始化
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projectmapdata_preview_layout);
        initComponents();
    }
    @Override//绑定项目上传服务
    protected void onResume() {
        super.onResume();

        Intent projectDataUploadService = new Intent(this, ProjectMapDataUploadService.class);
        this.bindService(projectDataUploadService, projectDataUpLoadConnection, BIND_ADJUST_WITH_ACTIVITY);
    }
    @Override//绑定service获取上传进度
    protected void onDestroy() {
        super.onDestroy();

        unbindService(projectDataUpLoadConnection);
        mUiHandler.removeCallbacksAndMessages(null);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        ProjectEntity newEntity = (ProjectEntity) intent.getSerializableExtra(ProjectListFragment.PROJECT_OBJ_KEY);
        if (newEntity != null) {
            this.setIntent(intent);
            initData();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_project_mapdata_preview, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_upload_mappreviewdata) {
            checkUserPremise();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 检查用户确认是否需要上传
     * 是则触发checkWorkStatus()
     **/
    public void checkUserPremise() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("确定要上传采集信息到服务器吗!");
        builder.setTitle("提示");
        builder.setIcon(R.drawable.ic_cloud_black_48dp);
        builder.setPositiveButton("确认",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        checkWorkStatus();
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
    /**
     * 检查当前的网络和用户登录状态
     * 当状态都正常则触发startUploadService()
     **/
    private void checkWorkStatus() {
        //检测网络状态
        if (!NetworkManager.isConnectAvailable(this)) {
            PopupToast.showError(this, "当前网络不可用,请检查网络状态后继续");
            return;
        }
        //检测用户登录状态
        HttpManager.getInstance().isRemberMeExpiredASync(new HttpManager.SessionCheckCallBack() {
            @Override
            public void sessionExpired(boolean isExpired) {
                if (!isExpired) {
                    startUploadService();
                } else {
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            PopupToast.showError(ProjectDataPreviewActivity.this, "用户登录失效,请重新登陆");
                        }
                    });
                    CommonHelper.toLoginActivity(ProjectDataPreviewActivity.this);
                }
            }
        });
    }
    /**
     * 上传采集数据
     * 启动和绑定服务后触发ServiceConnection的重写方法
     **/
    private void startUploadService() {
        //启动项目采集数据上传服务
        Intent projectDataUploadService = new Intent(ProjectDataPreviewActivity.this, ProjectMapDataUploadService.class);
        projectDataUploadService.putExtra(KEY_UPLOAD_PROJECTDATA_ENTITY, mProjectEntity);
        startService(projectDataUploadService);
        //绑定项目上传服务
        this.bindService(projectDataUploadService, projectDataUpLoadConnection, BIND_ADJUST_WITH_ACTIVITY);
    }
    /**
     * 项目采集数据上传服务连接
     **/
    private ServiceConnection projectDataUpLoadConnection = new ServiceConnection() {

        //图库图片上传监听
        ProjectMapDataUploadService.OnGalleryImageUploadListener onGalleryImageUploadListener = new ProjectMapDataUploadService.OnGalleryImageUploadListener() {
            @Override
            public void onProgress(String imgId, int uploadProgress) {
                for (int i = 0; i < mGalleryEntityList.size(); i++) {
                    PointGalleryEntity galleryEntity = mGalleryEntityList.get(i);
                    if (TextUtils.equals(galleryEntity.getImgId(), imgId)) {
                        galleryEntity.setImgUploadProgress(uploadProgress);
                        final int position = i;
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                projectGalleryPreviewAdapter.notifyItemChanged(position);
                            }
                        });
                        break;
                    }
                }
            }
            @Override
            public void onDone(String imgId) {
            }
        };

        ProjectMapDataUploadService bindService;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ProjectMapDataUploadService.ProjectCollectDataUploadBinder binder = (ProjectMapDataUploadService.ProjectCollectDataUploadBinder) service;
            bindService = binder.getTaggetService();
            bindService.setOnGalleryImageUploadListener(onGalleryImageUploadListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (bindService != null) {
                bindService.setOnGalleryImageUploadListener(null);
            }
        }
    };

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    private void initComponents() {
        initView();
        initData();
    }
    private void initData() {
        mProjectEntity = (ProjectEntity) this.getIntent().getSerializableExtra(ProjectListFragment.PROJECT_OBJ_KEY);
        if (mProjectEntity == null) {
            return;
        }

        long userId = SharedPreferencesHelper.getUserId(this);
        mGalleryEntityList.clear();
        mGalleryEntityList.addAll(DataBaseManagerHelper.getInstance().getAllProjectImagesByProjectId(mProjectEntity.getId(), userId));
        if (projectGalleryPreviewAdapter != null) {
            projectGalleryPreviewAdapter.notifyDataSetChanged();
        }
        //TODO 显示点位列表和杆列表
      /*  long pointNum = DataBaseManagerHelper.getInstance().getProjectPointCountByProjectId(mProjectEntity.getId(), userId);
        long lineNum = DataBaseManagerHelper.getInstance().getProjectLineCountByProjectId(mProjectEntity.getId(), userId);
        mTextViewPointNum.setText(pointNum + "个");
        mTextViewLineNum.setText(lineNum + "条");*/
    }
    private void initView() {
        setMdToolBar(R.id.id_material_toolbar);
        setMDToolBarBackEnable(true);
        setMDToolBarTitle(R.string.title_activity_projectmapdata_preview);
        /*getMDToolBar().setVisibility(View.GONE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar_md_projectmapdata_preview);
        toolbar.setTitle(R.string.title_activity_projectmapdata_preview);
        setSupportActionBar(toolbar);
        setMDToolBarBackEnable(true);*/

    /*    mTextViewPointNum = (TextView) findViewById(R.id.id_textview_numofpoints);
        mTextViewLineNum = (TextView) findViewById(R.id.id_textview_numoflines);*/

        int width = DeviceUtils.getScreenWidth(this); // 屏幕宽度（像素）
        int horCellNum = width / DensityUtil.dip2px(this, 150);
        int cellWidth = width / horCellNum - DensityUtil.dip2px(this, 2) * horCellNum;

        RecyclerView mProjectGalleryRecyclerView = (RecyclerView) findViewById(R.id.id_recyclerview_projectgallerylist);
        projectGalleryPreviewAdapter = new ProjectGalleryPreviewAdapter(this, mGalleryEntityList, cellWidth);
        mProjectGalleryRecyclerView.setAdapter(projectGalleryPreviewAdapter);

        //设置布局管理器
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(horCellNum, StaggeredGridLayoutManager.VERTICAL);
        mProjectGalleryRecyclerView.setLayoutManager(layoutManager);
        mProjectGalleryRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

}


