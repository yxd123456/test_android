package com.hz.adapter;

import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hz.R;
import com.hz.activity.ImagePreviewActivity;
import com.hz.activity.base.BaseAttributeActivity;
import com.hz.greendao.dao.PointGalleryEntity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 相册显示适配器
 */
public class ProjectGalleryAdapter extends RecyclerView.Adapter<ProjectGalleryAdapter.GalleryViewHolder> {
    public static final String TAG = ProjectGalleryAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private List<PointGalleryEntity> mGalleryEntityList;
    private WeakReference<BaseAttributeActivity> activityWeakReference;

    public ProjectGalleryAdapter(BaseAttributeActivity pointAttributeActivity, List<PointGalleryEntity> mGalleryEntityList) {
        activityWeakReference = new WeakReference<>(pointAttributeActivity);
        this.mInflater = LayoutInflater.from(pointAttributeActivity);
        this.mGalleryEntityList = mGalleryEntityList;
    }

    //创建ViewHolder
    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.activity_project_gallery_item, null);
        GalleryViewHolder galleryViewHolder = new GalleryViewHolder(view);
        galleryViewHolder.mImageView = (ImageView) view.findViewById(R.id.id_ImageView_image);
        return galleryViewHolder;
    }

    // 将数据绑定至ViewHolder
    @Override
    public void onBindViewHolder(GalleryViewHolder holder, final int position) {
        final PointGalleryEntity entity = mGalleryEntityList.get(position);
        final String imageUrl = entity.getImgFrom() + entity.getImgAddress();
        final ImageView imageView = holder.mImageView;
        //imageloader加载图片

        final BaseAttributeActivity attributeActivity = activityWeakReference.get();

        if (attributeActivity != null) {
            ImageLoader.getInstance().displayImage(imageUrl, imageView);
            //图片长按事件
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d(TAG, "position:" + position + ",size:" + mGalleryEntityList.size());
                    if ((position + 1) != mGalleryEntityList.size()) {
                        removeItemAndNotify(position);
                    } else {
                        final BaseAttributeActivity attributeActivity = activityWeakReference.get();
                        if (attributeActivity != null) {
                            attributeActivity.lunchCameraForResult();
                        }

                    }
                    return true;
                }
            });
        }

        //图片单击事件 预览图片
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BaseAttributeActivity attributeActivity = activityWeakReference.get();
                if (attributeActivity != null) {
                    if ((position + 1) != mGalleryEntityList.size()) {
                        Intent previewIntent = new Intent(attributeActivity, ImagePreviewActivity.class);
                        previewIntent.putExtra(ImagePreviewActivity.IMAGE_ADDRESS, entity.getImgAddress());
                        previewIntent.putExtra(ImagePreviewActivity.IMAGE_FROM, entity.getImgFrom());

                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeScaleUpAnimation(imageView, imageView.getWidth() / 2, imageView.getHeight() / 2, 0, 0);
                        ActivityCompat.startActivity(attributeActivity, previewIntent, optionsCompat.toBundle());
                    } else {
                        attributeActivity.chooseWayToGetImage();
                    }
                }

            }
        });
    }

    /**
     * 移除一个item并刷新视图
     * *
     */
    public void removeItemAndNotify(int position) {
        final BaseAttributeActivity attributeActivity = activityWeakReference.get();
        if (attributeActivity != null) {
            PointGalleryEntity entity = mGalleryEntityList.get(position);
            String imageId = entity.getImgId();
            mGalleryEntityList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mGalleryEntityList.size() - position);
            attributeActivity.removeImageFromDb(imageId);
        }
    }

    //获取总的条目数
    @Override
    public int getItemCount() {
        return mGalleryEntityList.size();
    }

    /**
     * galleryViewHolder
     * *
     */
    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        public GalleryViewHolder(View itemView) {
            super(itemView);
        }
    }
}
