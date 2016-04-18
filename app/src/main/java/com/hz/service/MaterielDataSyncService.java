package com.hz.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.hz.MainApplication;
import com.hz.common.Constans;
import com.hz.entity.MaterielDataSyncEntity;
import com.hz.entity.ResponseArrayWrapperEntity;
import com.hz.greendao.dao.ProjectEntity;
import com.hz.helper.GreenDaoHelper;
import com.hz.util.HttpManager;
import com.hz.helper.MaterielDataSyncHelper;
import com.hz.util.NetworkManager;
import com.hz.helper.SharedPreferencesHelper;
import com.hz.view.PopupToast;
import com.squareup.okhttp.Request;

import java.util.List;

/**
 * 材料数据同步服务
 */
public class MaterielDataSyncService extends Service {
    public static final String TAG = MaterielDataSyncService.class.getSimpleName();
    public MainApplication mainApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Test",getClass().getSimpleName()+"被调用了");
        mainApplication = (MainApplication) this.getApplication();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //检测网络状态
                    if (!NetworkManager.isConnectAvailable(mainApplication)) {
                        PopupToast.showError(mainApplication, "当前网络不可用操作将不会执行");
                        return;
                    }

                    //检测用户登录状态
                    if (HttpManager.getInstance().isRemberMeExpiredSync()) {
                        PopupToast.showError(mainApplication, "当前用户未登录或者登录已经超时");
                        return;
                    }

                    List<ProjectEntity> projectEntitys = getUserProjectList();
                    if (projectEntitys == null || projectEntitys.size() <= 0) {
                        return;
                    }

                    MaterielDataSyncHelper materielDataSyncHelper = new MaterielDataSyncHelper();
                    List<MaterielDataSyncEntity> materielSettingEntityList = materielDataSyncHelper.getAllMateriel(mainApplication);
                    //从网络获取每个材料的材料信息并保存到数据库
                    for (MaterielDataSyncEntity entity : materielSettingEntityList) {
                        for (ProjectEntity projectEntity : projectEntitys) {
                            Log.d(TAG,
                                    "获取材料数据:" + entity.getMaterielName() +
                                            ",项目ID:" + projectEntity.getId() +
                                            ",项目名称:" + projectEntity.getProjectName()
                            );
                            Class clazz = GreenDaoHelper.getResponseWrapperEntityClazzByMaterialType(entity.getMaterielId());
                            String url = entity.getServerUrl();

                            if (!entity.getMaterielId().equals(Constans.MaterielType.GEOLOGICALCONDITION_TYPE) &&
                                    !entity.getMaterielId().equals(Constans.MaterielType.TRANSFORMER_TYPE) &&
                                    !entity.getMaterielId().equals(Constans.MaterielType.WIRE_TYPE)) {

                                url = url + projectEntity.getId();
                            }

                            Request request = new Request.Builder().url(url).get().build();
                            ResponseArrayWrapperEntity wrapperEntity = (ResponseArrayWrapperEntity) HttpManager.getInstance().addSyncHttpTask(
                                    request,
                                    clazz
                            );
                            if (wrapperEntity != null) {
                                //2.将TowerType对象列表持久化到数据库
                                materielDataSyncHelper.persisentMaterielBeanToDbByMaterielId(mainApplication, wrapperEntity, entity.getMaterielId());
                            }

                            if (entity.getMaterielId().equals(Constans.MaterielType.GEOLOGICALCONDITION_TYPE) ||
                                    entity.getMaterielId().equals(Constans.MaterielType.TRANSFORMER_TYPE) ||
                                    entity.getMaterielId().equals(Constans.MaterielType.WIRE_TYPE)) {
                                break;
                            }
                        }

                    }
                } catch (Exception e) {
                    Log.e(TAG, "run: ", e);
                } finally {
                    Log.d(TAG, "run: 后台任务运行完成，停止服务");
                    SharedPreferencesHelper.saveNeedToUpdateMaterialDataIdentifier(
                            mainApplication.getApplicationContext(),
                            false
                    );//设置不需要更新材料数据

                    MaterielDataSyncService.this.stopSelf();
                }
            }
        }).start();
    }

    /**
     * 获取用户项目列表
     **/
    private List<ProjectEntity> getUserProjectList() {
        final Long userId = SharedPreferencesHelper.getUserId(mainApplication);
        Request projectListrequest = new Request.Builder().url(String.format(Constans.PROJECT_URL, userId)).get().build();
        ResponseArrayWrapperEntity.ProjectWrapperEntity projectListEntity = HttpManager.getInstance().addSyncHttpTask(
                projectListrequest,
                ResponseArrayWrapperEntity.ProjectWrapperEntity.class
        );
        if (projectListEntity != null) {
            return projectListEntity.getData();
        }
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
