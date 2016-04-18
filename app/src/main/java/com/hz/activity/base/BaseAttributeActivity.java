package com.hz.activity.base;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.hz.R;
import com.hz.adapter.ProjectGalleryAdapter;
import com.hz.common.Constans;
import com.hz.dialog.PickerListViewDialog;
import com.hz.entity.GalleryListItemEntity;
import com.hz.fragment.ProjectListFragment;
import com.hz.greendao.dao.PointGalleryEntity;
import com.hz.greendao.dao.PointGalleryEntityDao;
import com.hz.greendao.dao.ProjectEntity;
import com.hz.popupwindow.ChooseImagePopupWindow;
import com.hz.popupwindow.GalleryPopupWindow;
import com.hz.util.DateUtil;
import com.hz.helper.StroageHelper;
import com.hz.view.ValidaterEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 地图点位，线条属性编辑超类
 */
public abstract class BaseAttributeActivity extends BaseActivity implements View.OnClickListener, PickerListViewDialog.onPickerDialogHasFocusListener, ChooseImagePopupWindow.onButtonClickListener, GalleryPopupWindow.onOkClickListener {

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    public static final int CAMERA_REQUEST_RESULT = 1; //获取相机拍摄照片数据
    //获取选择经纬度修改数据
    public String mCurrentFilePath;//当前拍摄图片保存路径
    public String mCurrentFileName;//保存当前拍摄的图片名称
    //相册
    public RecyclerView mRecyclerGallery;
    public List<PointGalleryEntity> mGalleryEntityList = new ArrayList<>();
    public ProjectGalleryAdapter mGalleryAdapter;
    public ValidaterEditText mEditAttributeName;//地图属性名称
    public ValidaterEditText mEditAttributeNote;//地图属性备注

    private ChooseImagePopupWindow popupWindow;
    private GalleryPopupWindow galleryPopupWindow;

    private Button mEditAttributeOk = null;
    public ProjectEntity projectEntity;

/**************************************************************************************************/
    //生命周期+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        if (galleryPopupWindow != null && galleryPopupWindow.isShowing()) {
            galleryPopupWindow.dismiss();
        }
    }

    //父类方法+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    //当点击返回键返回的时候要验证输入的正确性并将信息保存到数据库
    @Override
    public void finish() {
        onValidateInputSetUpResultAndFinish(true);
    }
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        Log.d("Test","当前的Activity是"+getClass().getSimpleName()+"BAA");
        onInitView();
        onAnalysisBundleData();
        initGallery();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAMERA_REQUEST_RESULT:
                addGalleryItem(mCurrentFilePath, mCurrentFileName);
                break;
        }
    }

    //接口方法+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    public void pickerDialogHasNotFocus(PickerListViewDialog pickerScrollViewDialog) {
        ValidaterEditText editText = (ValidaterEditText) pickerScrollViewDialog.getPickerDialogBindEditText();
        editText.validateByConfig();
    }
    @Override
    public void onOkClick(List<GalleryListItemEntity> checkStringList) {
        if (checkStringList != null && !checkStringList.isEmpty()) {
            for (GalleryListItemEntity itemEntity : checkStringList) {
                String name = new File(itemEntity.imagePath).getName();
                int lastIndex = name.lastIndexOf(".");
                if (lastIndex <= 0) {
                    continue;
                }
                name = name.substring(0, lastIndex);
                addGalleryItem(itemEntity.imagePath, name);
            }
        }
        galleryPopupWindow.dismiss();
    }
    @Override
    public void onButtonClick(View view, int viewType) {
        switch (viewType) {
            case ChooseImagePopupWindow.VIEW_TYPE_CAMERAS:
                lunchCameraForResult();
                popupWindow.dismiss();
                break;
            case ChooseImagePopupWindow.VIEW_TYPE_IMAGES:
                lunchGalleryToChooseImages();
                popupWindow.dismiss();
                break;
            case ChooseImagePopupWindow.VIEW_TYPE_CANCEL:
                popupWindow.dismiss();
                break;
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_button_editok:
                //当点击确定按钮返回的时候需要验证输入的正确性并设置数据到数据库
                this.onValidateInputSetUpResultAndFinish(true);
                break;
        }
    }

    //选项菜单+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_point_attribute, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_remove:
                onRightMenuClick();
                return true;
            case R.id.menu_item_ok:
                this.onValidateInputSetUpResultAndFinish(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

/**************************************************************************************************/
    /**
     * 解析传入bundle参数
     * */
    public void onAnalysisBundleData() {
        Bundle bundleParam = this.getIntent().getExtras();//获取项目列表传入参数
        projectEntity = (ProjectEntity) bundleParam.getSerializable(ProjectListFragment.PROJECT_OBJ_KEY);
    }
    /**
     * 显示点位名称和点位备注
     * */
    public void setNameAndNoteByBundleData(String name, String note) {
        mEditAttributeName.setText(name);
        mEditAttributeNote.setText(note);
    }
    /**
     * 通过点位类型来修改确定按钮显示内容
     * *
     */
    public void displayOkOrUpdateByAttributeByEditType(int attributeEditType) {
        //确定按钮
        if (attributeEditType == Constans.AttributeEditType.EDIT_TYPE_ADD) {
            mEditAttributeOk.setText(getResources().getString(R.string.editAttributeAdd));
        } else {
            mEditAttributeOk.setText(getResources().getString(R.string.editAttributeUpdate));
        }
    }
    /**
     * 选择一种方式来获取图片
     **/
    public void chooseWayToGetImage() {
        popupWindow = new ChooseImagePopupWindow(this);
        popupWindow.setOnButtonClickListener(this);
        popupWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }
    /**
     * 启动系统摄像头并获取返回值
     * *
     */
    public void lunchCameraForResult() {
        String imageFolderName = DateUtil.genCurrDateStrWithFormat("yyyyMMdd");//根据日期创建的文件夹名称
        mCurrentFileName = DateUtil.genCurrDateStr();//文件名称
        mCurrentFilePath = StroageHelper.getProjectImageFileByDate(this, imageFolderName, mCurrentFileName + ".jpeg").getAbsolutePath();

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCurrentFilePath)));
        startActivityForResult(intent, CAMERA_REQUEST_RESULT);
    }
    /***
     * 启动选择系统图片页面
     */
    public void lunchGalleryToChooseImages() {
        galleryPopupWindow = new GalleryPopupWindow(this);
        galleryPopupWindow.setOnOkClickListener(this);
        galleryPopupWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }
    /**
     * 长按点位图片从数据库删除图片
     */
    public void removeImageFromDb(String imageId) {
        PointGalleryEntityDao pointGalleryEntityDao = this.getDaoSession().getPointGalleryEntityDao();
        //2.移除点位图片信息
        PointGalleryEntity galleryEntity = pointGalleryEntityDao.queryBuilder().where(PointGalleryEntityDao.Properties.ImgId.eq(imageId)).unique();
        if (galleryEntity != null) {
            galleryEntity.setImgRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_REMOVED);
            galleryEntity.setImgNeedToUpload(true);
            pointGalleryEntityDao.updateInTx(galleryEntity);
        }
    }

    public void addGalleryItem(String filePath, String fileName) {
        if (filePath != null) {
            File file = new File(filePath);
            if (file.isFile() && file.length() > 1000) {
                //保存图片到列表
                PointGalleryEntity entity = new PointGalleryEntity();
                entity.setImgId(fileName);
                entity.setImgFrom(Constans.ImageFrom.FILE);
                entity.setImgAddress(filePath);
                mGalleryEntityList.add(mGalleryEntityList.size() - 1, entity);
                mGalleryAdapter.notifyDataSetChanged();
                mRecyclerGallery.smoothScrollToPosition(mGalleryEntityList.size() + 1);
            }
        }
    }
    /**
     * 当右键点击的时候不需要验证输入的正确性
     * */
    public void onRightMenuClick() {
        onBeforeRightIconClick();
        this.onValidateInputSetUpResultAndFinish(false);
    }
    /**
     * 设置参数并返回上一个页面，
     * needCheckInput 设置是否需要检查输入正确性
     */
    private void onValidateInputSetUpResultAndFinish(boolean needCheckInput) {
        if (needCheckInput) {//直接返回 ，确定，修改 --》 需要验证输入
            if (onValidateInputSetUpResult()) {
                onSetUpResult();
                super.finish();
            }
        } else {//删除   --》不需要验证输入
            onSetUpResult();
            super.finish();
        }
    }

    //抽象方法+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 右变按钮被点击前触发
     * 点击删除
     */
    public abstract void onBeforeRightIconClick();
    /**
     * 设置修改以后的参数
     * 点击完成
     */
    public abstract void onSetUpResult();

    /**
     * 验证输入时候正确如果不正确不能退出
     * *
     */
    public abstract boolean onValidateInputSetUpResult();

/**初始化工作****************************************************************************************/
    /**
     * 当初始化视图的时候触发
     * */
    public void onInitView() {
        mEditAttributeName = (ValidaterEditText) findViewById(R.id.id_edit_attributename);
        mEditAttributeNote = (ValidaterEditText) findViewById(R.id.id_edit_attributenote);
        mEditAttributeOk = (Button) findViewById(R.id.id_button_editok);
        mEditAttributeOk.setOnClickListener(this);
    }
    /**
     * 初始化相册信息
     * */
    private void initGallery() {
        initGalleryDatas();
        mRecyclerGallery = (RecyclerView) findViewById(R.id.id_recyclerview_gallery);
        //设置数据源
        mGalleryAdapter = new ProjectGalleryAdapter(this, mGalleryEntityList);
        mRecyclerGallery.setAdapter(mGalleryAdapter);
        //设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerGallery.setLayoutManager(layoutManager);
        mRecyclerGallery.setItemAnimator(new DefaultItemAnimator());
    }
    /**
     * 初始化相册数据
     * *
     */
    private void initGalleryDatas() {
        PointGalleryEntity pointEditGalleryEntity = new PointGalleryEntity();
        pointEditGalleryEntity.setImgId(UUID.randomUUID().toString());
        pointEditGalleryEntity.setImgPointId("zwfpointid");//占位符点位ID
        pointEditGalleryEntity.setImgFrom(Constans.ImageFrom.DRAWABLE);
        pointEditGalleryEntity.setImgAddress(R.drawable.ic_camera_enhance_black_48dp + "");
        mGalleryEntityList.add(pointEditGalleryEntity);
    }
}
