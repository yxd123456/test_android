package com.hz.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.hz.R;
import com.hz.activity.base.BaseActivity;
import com.hz.view.ZoomImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 图像预览组件
 */
public class ImagePreviewActivity extends BaseActivity {
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    public static final String TAG = ImagePreviewActivity.class.getSimpleName();
    /**
     * 图片地址*
     * @see com.hz.common.Constans.ImageFrom
     */
    public static final String IMAGE_ADDRESS = "IMAGE_ADDRESS";
    /**
     * 图片的来源
     * @see com.hz.common.Constans.ImageFrom
     */
    public static final String IMAGE_FROM = "IMAGE_FROM";

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_imageprevie);
        initComponents();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityCompat.finishAfterTransition(this);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    private void initComponents() {
        setMdToolBar(R.id.id_material_toolbar);
        setMDToolBarBackEnable(true);
        setMDToolBarTitle(R.string.title_activity_imagepreview);
        ZoomImageView zoomImageView = (ZoomImageView) findViewById(R.id.id_zoomimageview_preview);
        String imageAddress = this.getIntent().getStringExtra(IMAGE_ADDRESS);
        String imageFrom = this.getIntent().getStringExtra(IMAGE_FROM);

        Bitmap image = ImageLoader.getInstance().loadImageSync(imageFrom + imageAddress);
        if (image == null) {
            Log.d(TAG, "initComponents: Bitmap为null");
            return;
        }
        zoomImageView.setImageBitmap(image);

        final Toolbar toolbar = this.getMDToolBar();
        Palette.from(image).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int backGroundColor = palette.getLightMutedColor(getResources().getColor(R.color.cadetblue));
                int titleTextColor = palette.getDarkMutedColor(getResources().getColor(R.color.content_background_color));
                toolbar.setBackgroundColor(backGroundColor);
                toolbar.setTitleTextColor(titleTextColor);
            }
        });
    }


}
