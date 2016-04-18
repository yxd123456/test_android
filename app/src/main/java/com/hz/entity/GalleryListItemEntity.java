package com.hz.entity;

import android.util.Log;

/**
 * 系统相册列表
 */
public class GalleryListItemEntity {

    public String imagePath;
    public String imageName;
    public boolean checked = false;

    public GalleryListItemEntity(String imagePath, String imageName) {
        Log.d("Test",getClass().getSimpleName()+"被调用了");

        this.imagePath = imagePath;
        this.imageName = imageName;
    }

}
