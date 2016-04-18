package com.hz.popupwindow;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hz.R;
import com.hz.adapter.GalleryListAdapter;
import com.hz.entity.GalleryListItemEntity;
import com.hz.util.DensityUtil;
import com.hz.util.DeviceUtils;
import com.hz.view.SuperRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择图片
 */
public class GalleryPopupWindow extends PopupWindow implements View.OnTouchListener, PopupWindow.OnDismissListener, View.OnClickListener {

    public static final String TAG = GalleryPopupWindow.class.getSimpleName();
    private View rootView;
    private TextView mTextViewCount;
    private GalleryListAdapter mGalleryListAdapter;
    private List<GalleryListItemEntity> mGalleryEntityList = new ArrayList<>();
    private HandlerThread mHandlerThread;
    private Handler mDataHandler;
    private Handler mUiHandler = new Handler();
    private int count = 0;
    private onOkClickListener onOkClickListener;

    public GalleryPopupWindow(Context context) {
        super(context);
        Log.d("Test",getClass().getSimpleName()+"被调用了");
        mHandlerThread = new HandlerThread("queryImageThread");
        mHandlerThread.start();
        mDataHandler = new Handler(mHandlerThread.getLooper());


        initView(context);
        initPopwindowAttributes();
        initGalleryList();
    }


    private void initPopwindowAttributes() {
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.popupwindow_animation);
        this.setBackgroundDrawable(new ColorDrawable(0xb0000000)); //设置背景为透明，如果不设置的话默认值有边框
        this.setOnDismissListener(this);
    }

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        rootView = inflater.inflate(R.layout.popupwindow_gallery, null);
        mTextViewCount = (TextView) rootView.findViewById(R.id.id_textview_count);
        rootView.findViewById(R.id.id_popupwindow_ok).setOnClickListener(this);
        rootView.setOnTouchListener(this);
        setContentView(rootView);

        int windowHeight = DeviceUtils.getScreenHeight(context);
        int windowWidth = DeviceUtils.getScreenWidth(context);

        View containerView = rootView.findViewById(R.id.id_popupwindow_container);
        ViewGroup.LayoutParams layoutParams = containerView.getLayoutParams();
        layoutParams.height = (int) (windowHeight * 0.7);
        containerView.setLayoutParams(layoutParams);


        int horCellNum = windowWidth / DensityUtil.dip2px(rootView.getContext(), 150);
        int cellWidth = windowWidth / horCellNum - DensityUtil.dip2px(rootView.getContext(), 2) * horCellNum;

        SuperRecyclerView galleryListSuperRecyclerView = (SuperRecyclerView) rootView.findViewById(R.id.id_recyclerview_gallerylist);
        mGalleryListAdapter = new GalleryListAdapter(this, rootView.getContext(), mGalleryEntityList, cellWidth);
        galleryListSuperRecyclerView.setAdapter(mGalleryListAdapter);

        //设置布局管理器
        GridLayoutManager layoutManager = new GridLayoutManager(
                rootView.getContext(),
                horCellNum,
                LinearLayoutManager.HORIZONTAL,
                false
        );
        galleryListSuperRecyclerView.setLayoutManager(layoutManager);
        galleryListSuperRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /***
     * 获取系统相册
     **/
    private void initGalleryList() {
        mDataHandler.post(new Runnable() {
            @Override
            public void run() {

                queryImages();
            }
        });
    }

    private void queryImages() {
        Cursor imageQueryCursor = null;
        try {
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver contentResolver = rootView.getContext().getContentResolver();
            imageQueryCursor = contentResolver.query(
                    uri,
                    null,
                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/jpeg", "image/png"},
                    MediaStore.Images.Media.DATE_MODIFIED + " desc"
            );

            if (imageQueryCursor != null) {
                while (imageQueryCursor.moveToNext()) {
                    String imagePath = imageQueryCursor.getString(imageQueryCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    String imageName = imageQueryCursor.getString(imageQueryCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    Log.d(TAG, "run: " + imagePath);
                    mGalleryEntityList.add(new GalleryListItemEntity(imagePath, imageName));
                    count++;
                    if (count % 100 == 0) {
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mGalleryListAdapter.notifyDataSetChanged();
                                mTextViewCount.setText(formatSelect(count, 0));
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "run: " + e);
        } finally {
            if (imageQueryCursor != null) {
                imageQueryCursor.close();
            }
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mGalleryListAdapter.notifyDataSetChanged();
                    mTextViewCount.setText(formatSelect(count, 0));
                }
            });
        }

    }

    private String formatSelect(int imageCount, int selection) {
        return "一共" + imageCount + "张图片，已选择" + selection + "张";
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        View containerView = rootView.findViewById(R.id.id_popupwindow_container);
        int height = containerView.getTop();
        int y = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (y < height) {
                dismiss();
            }
        }
        return true;
    }


    @Override
    public void onDismiss() {
        mHandlerThread.quit();
        mDataHandler.removeCallbacksAndMessages(null);
        mUiHandler.removeCallbacksAndMessages(null);
    }

    public void calculateChecked() {
        mDataHandler.post(new Runnable() {
            @Override
            public void run() {
                int checkCoun = 0;
                for (GalleryListItemEntity itemEntity : mGalleryEntityList) {
                    if (itemEntity.checked) {
                        checkCoun++;
                    }
                }
                final int finalCheckCoun = checkCoun;
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTextViewCount.setText(formatSelect(count, finalCheckCoun));
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_popupwindow_ok:
                if (onOkClickListener != null) {
                    List<GalleryListItemEntity> checkStringList = new ArrayList<>();
                    for (GalleryListItemEntity itemEntity : mGalleryEntityList) {
                        if (itemEntity.checked) {
                            checkStringList.add(itemEntity);
                        }
                    }
                    onOkClickListener.onOkClick(checkStringList);
                }
                break;
        }
    }

    public interface onOkClickListener {
        void onOkClick(List<GalleryListItemEntity> checkStringList);
    }

    public void setOnOkClickListener(GalleryPopupWindow.onOkClickListener onOkClickListener) {
        this.onOkClickListener = onOkClickListener;
    }
}
