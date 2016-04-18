package com.hz.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hz.MainApplication;
import com.hz.R;
import com.hz.activity.ProjectDataPreviewActivity;
import com.hz.common.Constans;
import com.hz.entity.ResponseStateEntity;
import com.hz.fragment.ProjectListFragment;
import com.hz.greendao.dao.MapLineEntity;
import com.hz.greendao.dao.MapPoiEntity;
import com.hz.greendao.dao.PointGalleryEntity;
import com.hz.greendao.dao.ProjectEntity;
import com.hz.helper.DataBaseManagerHelper;
import com.hz.helper.SharedPreferencesHelper;
import com.hz.util.HttpManager;
import com.hz.util.JsonUtil;
import com.hz.util.okhttp_extend.progress.ProgressListener;
import com.hz.util.okhttp_extend.progress.ProgressRequestBody;
import com.hz.view.PopupToast;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 项目数据上传service
 *
 * @author long
 */
public class ProjectMapDataUploadService extends Service {
    private static final String TAG = ProjectMapDataUploadService.class.getSimpleName();
    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    private ProjectEntity mProjectEntity;//当前上传的项目
    private boolean isRunning = false;//标志当前是否已经在运行
    private OnGalleryImageUploadListener onGalleryImageUploadListener;
    private String mCurrentUploadImageId;//当前上传的图片ID

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Test",getClass().getSimpleName()+"被调用了");
        if (!isRunning) {
            mProjectEntity = (ProjectEntity) intent.getSerializableExtra(ProjectDataPreviewActivity.KEY_UPLOAD_PROJECTDATA_ENTITY);
            uploadMapDataToService();
            isRunning = true;
            setStartForeground();
        } else {
            Toast.makeText(getMainApplication(), "项目[" + mProjectEntity.getProjectName() + "]数据正在上传中,请稍后", Toast.LENGTH_SHORT).show();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private MainApplication getMainApplication() {
        return (MainApplication) this.getApplication();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        if (onGalleryImageUploadListener != null) {
            onGalleryImageUploadListener = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ProjectCollectDataUploadBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    /**
     * service与activity消息通讯binder
     **/
    public class ProjectCollectDataUploadBinder extends Binder {
        public ProjectMapDataUploadService getTaggetService() {
            return ProjectMapDataUploadService.this;
        }
    }

    public void setOnGalleryImageUploadListener(OnGalleryImageUploadListener onGalleryImageUploadListener) {
        this.onGalleryImageUploadListener = onGalleryImageUploadListener;
    }

    public interface OnGalleryImageUploadListener {
        void onProgress(String imgId, int uploadProgress);

        void onDone(String imgId);
    }

    /**
     * 后台服务前台显示设置
     **/
    private void setStartForeground() {
        Notification notification = new Notification.Builder(ProjectMapDataUploadService.this)
                .setSmallIcon(R.drawable.cloud_upload_64)
                .setContentTitle(mProjectEntity.getProgrammeName())
                .setContentText("项目[" + mProjectEntity.getProjectName() + "]采集数据上传中")
                .setOngoing(true)
                .setProgress(100, 70, true)
                .build();

        startForeground(Constans.NOTIFICATION_ID.ID_UPLOAD_GOREGROUND, notification);
    }

    /**
     * 通知上传成功
     *
     * @param success 标志是否上传成功啦
     */
    public void uploadCompleteNotification(boolean success) {
        Intent intent = new Intent(this, ProjectDataPreviewActivity.class);
        intent.putExtra(ProjectListFragment.PROJECT_OBJ_KEY, mProjectEntity);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(ProjectMapDataUploadService.this)
                .setSmallIcon(R.drawable.icon_message)
                .setContentTitle(mProjectEntity.getProgrammeName())
                .setContentText("项目[" + mProjectEntity.getProjectName() + "]采集数据上传" + (success ? "完毕" : "失败，请稍后重试"))
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{1, 200, 0, 200, 1})
                .setAutoCancel(true)
                .setNumber(1)
                .build();
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_VIBRATE;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Constans.NOTIFICATION_ID.ID_UPLOAD_OK, notification);
    }


    /******************************************
     * 上传项目采集数据逻辑
     ***********************************************/

    /**
     * 上传进度 监听
     **/
    private ProgressListener progressListener = new ProgressListener() {
        int preProgress = 0;

        @Override
        public void onProgress(long currentBytes, long contentLength, boolean done) {
            if (onGalleryImageUploadListener != null && !TextUtils.isEmpty(mCurrentUploadImageId)) {
                if (done) {
                    onGalleryImageUploadListener.onDone(mCurrentUploadImageId);
                    DataBaseManagerHelper.getInstance().updatePointTargetImageUploadStatusByImageId(mCurrentUploadImageId);
                }
                int currentProgress = (int) (100 * currentBytes / contentLength);
                if (currentProgress != preProgress && currentProgress % 10 == 0) {
                    Log.d(TAG, "onProgress: " + currentBytes + "  " + contentLength + "  " + done + "  " + currentProgress);
                    onGalleryImageUploadListener.onProgress(mCurrentUploadImageId, currentProgress);
                    preProgress = currentProgress;
                }
            }
        }
    };

    /**
     * 上传项目采集数据到服务器
     **/
    private void uploadMapDataToService() {
        final long userId = SharedPreferencesHelper.getUserId(this);

        new AsyncTask<String, Long, Integer>() {
            @Override
            protected Integer doInBackground(String... params) {
                boolean success = true;
                try {
                    //上传点位列表信息
                    List<MapPoiEntity> mapPoiEntityList = DataBaseManagerHelper.getInstance().getAllPointByProjectIdContactRemoved(mProjectEntity.getId(), userId);
                    success = postTargetPointsToServer(mapPoiEntityList);
                    if (!success) {
                        return 1000;
                    }

                    //上传采集的线信息
                    List<MapLineEntity> mapLineEntityList = DataBaseManagerHelper.getInstance().getAlllinesByProjectIdContactRemoved(mProjectEntity.getId());
                    for (MapLineEntity mapLineEntity : mapLineEntityList) {
                        mapLineEntity.getMapLineItemEntityList();//查询点位关联的导线/拉线信息
                    }
                    success = postTargetLinesToServer(mapLineEntityList);
                    if (!success) {
                        return 1001;
                    }

                    //上传点位图片信息
                    for (MapPoiEntity poiEntity : mapPoiEntityList) {
                        poiEntity.setPointGalleryLists(DataBaseManagerHelper.getInstance().getPointImagesByPointIdContactRemoved(poiEntity.getPointId()));
                    }
                    success = postTargetImagesToServer(mProjectEntity, mapPoiEntityList);
                    if (!success) {
                        return 1002;
                    }
                } catch (Exception e) {
                    success = false;
                    Log.e(TAG, "上传点位信息异常: ", e);
                } finally {
                    uploadCompleteNotification(success);
                    ProjectMapDataUploadService.this.stopSelf();
                }
                return success ? 1003 : 1004;
            }

            @Override
            protected void onPostExecute(Integer result) {
                switch (result) {
                    case 1000:
                        PopupToast.showError(getMainApplication(), "批量上传点位信息：失败");
                        Log.d(TAG, "批量上传点位信息：失败");
                        break;
                    case 1001:
                        PopupToast.showError(getMainApplication(), "批量上传线信息：失败");
                        Log.d(TAG, "批量上传线信息：失败");
                        break;
                    case 1002:
                        PopupToast.showError(getMainApplication(), "批量上传点位图片信息：失败");
                        Log.d(TAG, "批量上传点位图片信息：失败");
                        break;
                    case 1003:
                        PopupToast.showOk(getMainApplication(), "点位相关数据上传成功关闭服务");
                        Log.d(TAG, "点位相关数据上传成功关闭服务");
                        break;
                    case 1004:
                        PopupToast.showError(getMainApplication(), "点位相关数据上传失败关闭服务");
                        Log.d(TAG, "点位相关数据上传失败关闭服务");
                        break;
                    default:
                        break;
                }
            }
        }.execute();
    }

    /**
     * 上传点位数据到服务端
     * *
     */
    public boolean postTargetPointsToServer(List<MapPoiEntity> entityList) throws Exception {
        String pointJsonList = JsonUtil.convertObjToJson(entityList);
        if (pointJsonList == null) {
            return false;
        }
        RequestBody requestBody = new FormEncodingBuilder().add(Constans.POST_POINTS_KEY, pointJsonList).build();
        Request requestPost = new Request.Builder().url(Constans.POST_POINTS_URL).post(requestBody).build();
        ResponseStateEntity responseStateEntity = HttpManager.getInstance().addSyncHttpTask(requestPost, ResponseStateEntity.class);
        return responseStateEntity != null && responseStateEntity.isSuccess();
    }

    /**
     * 上传项目采集的线信息到服务器
     * *
     */
    private boolean postTargetLinesToServer(List<MapLineEntity> mapLineEntityList) throws Exception {
        String pointJsonList = JsonUtil.convertObjToJson(mapLineEntityList);
        if (pointJsonList == null) {
            return false;
        }
        RequestBody requestBody = new FormEncodingBuilder().add(Constans.POST_LINES_KEY, pointJsonList).build();
        Request requestPost = new Request.Builder().url(Constans.POST_LINES_URL).post(requestBody).build();
        ResponseStateEntity responseStateEntity = HttpManager.getInstance().addSyncHttpTask(requestPost, ResponseStateEntity.class);
        return responseStateEntity != null && responseStateEntity.isSuccess();
    }

    /**
     * 上传一个点位关联的图片到服务器
     * *
     */
    public boolean postOnePointTargetImagesToServer(String galleryProjectId, String galleryPointId, PointGalleryEntity galleryEntity) throws IOException {
        this.mCurrentUploadImageId = galleryEntity.getImgId();

        MultipartBuilder multipartBuilder = new MultipartBuilder().type(MultipartBuilder.FORM);
        File pointImage = new File(galleryEntity.getImgAddress());
        RequestBody fileRequestBody = RequestBody.create(MEDIA_TYPE_JPEG, pointImage);
        multipartBuilder.addFormDataPart(Constans.POST_IMAGE_KEY, pointImage.getName(), fileRequestBody);//图片信息
        multipartBuilder.addFormDataPart(Constans.POST_POINT_ID, galleryPointId);//图片点位ID
        multipartBuilder.addFormDataPart(Constans.POST_PROJ_ID, galleryProjectId);//图片项目ID
        //点位实体json信息
        String pointGalleryJson = JsonUtil.convertObjToJson(galleryEntity);
        if (pointGalleryJson == null) {
            return false;
        }
        multipartBuilder.addFormDataPart(Constans.POST_POINT_GALLERY_ENTITY, pointGalleryJson);//图片实体信息
        RequestBody requestBody = multipartBuilder.build();

        ProgressRequestBody progressRequestBody = new ProgressRequestBody(requestBody, progressListener);
        Request requestPost = new Request.Builder().url(Constans.POST_IMAGES_URL).post(progressRequestBody).build();

        ResponseStateEntity responseStateEntity = HttpManager.getInstance().addSyncHttpTask(requestPost, ResponseStateEntity.class);
        return responseStateEntity != null && responseStateEntity.isSuccess();
    }

    /**
     * 上传点位关联图片到服务器
     * *
     */
    public boolean postTargetImagesToServer(ProjectEntity projectEntity, List<MapPoiEntity> entityList) throws IOException {
        boolean postOnePointImagesResult;
        for (MapPoiEntity poiEntity : entityList) {
            List<PointGalleryEntity> projectGalleryEntityList = poiEntity.getPointGalleryLists();
            if (projectGalleryEntityList == null || projectGalleryEntityList.isEmpty()) {
                continue;
            }
            for (PointGalleryEntity itemGalleryEntity : projectGalleryEntityList) {
                if (!itemGalleryEntity.getImgNeedToUpload()) {
                    continue;
                }
                long start = System.currentTimeMillis();
                Log.d(TAG, "上传点位图片：1张,图片项目ID:" + projectEntity.getId() + ",图片点位ID：" + poiEntity.getPointId() + ",图片实体信息：" + itemGalleryEntity.toString());
                postOnePointImagesResult = postOnePointTargetImagesToServer(projectEntity.getId() + "", poiEntity.getPointId(), itemGalleryEntity);
                Log.d(TAG, "上传一张图片耗时:" + (System.currentTimeMillis() - start) + " 毫秒");
                if (!postOnePointImagesResult) {
                    return false;
                }
            }
        }
        return true;
    }
}
