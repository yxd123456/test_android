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
import android.widget.TextView;

import com.hz.R;
import com.hz.activity.ImagePreviewActivity;
import com.hz.activity.ProjectDataPreviewActivity;
import com.hz.common.Constans;
import com.hz.greendao.dao.PointGalleryEntity;
import com.hz.view.UploadStatusView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.List;

/**
 * 项目相册预览适配器
 */
public class ProjectGalleryPreviewAdapter extends RecyclerView.Adapter<ProjectGalleryPreviewAdapter.GalleryViewHolder> {
    public static final String TAG = ProjectGalleryPreviewAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private List<PointGalleryEntity> mGalleryEntityList;
    private ProjectDataPreviewActivity projectMapDataPreviewActivity;
    private int cellWidth;

    public ProjectGalleryPreviewAdapter(ProjectDataPreviewActivity projectMapDataPreviewActivity, List<PointGalleryEntity> mGalleryEntityList, int cellWidth) {
        this.projectMapDataPreviewActivity = projectMapDataPreviewActivity;
        this.mInflater = LayoutInflater.from(projectMapDataPreviewActivity);
        this.mGalleryEntityList = mGalleryEntityList;
        this.cellWidth = cellWidth;
    }

    //创建ViewHolder
    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.activity_project_datapreview_item, null);

        GalleryViewHolder galleryViewHolder = new GalleryViewHolder(view);
        galleryViewHolder.mImageView = (ImageView) view.findViewById(R.id.id_ImageView_image);
        galleryViewHolder.mImageSizeKb = (TextView) view.findViewById(R.id.id_textview_imagesizekb);
        galleryViewHolder.mUploadStatusView = (UploadStatusView) view.findViewById(R.id.id_uploadstatusview_status);

        ViewGroup.LayoutParams layoutParams = galleryViewHolder.mImageView.getLayoutParams();
        layoutParams.width = cellWidth;
        layoutParams.height = cellWidth;
        galleryViewHolder.mImageView.setLayoutParams(layoutParams);

        return galleryViewHolder;
    }

    // 将数据绑定至ViewHolder
    @Override
    public void onBindViewHolder(GalleryViewHolder holder, final int position) {
        final PointGalleryEntity entity = mGalleryEntityList.get(position);
        String imageUrl = entity.getImgFrom() + entity.getImgAddress();
        final ImageView imageView = holder.mImageView;
        ImageLoader.getInstance().displayImage(imageUrl, imageView);

        File imageFile = new File(entity.getImgAddress());
        long imagekb = imageFile.length() / 1024;
        holder.mImageSizeKb.setText(imagekb + " kb");

        if (entity.getImgNeedToUpload()) {
            if (entity.getImgUploadProgress() == 0) {
                holder.mUploadStatusView.setProgressToStart();
            } else {
                holder.mUploadStatusView.setProgress(entity.getImgUploadProgress());
            }
        } else {
            holder.mUploadStatusView.setProgressToEnd();
        }

        //已经被删除的图片显示透明
        holder.mImageView.setImageAlpha(
                entity.getImgRemoved() == Constans.RemoveIdentified.REMOVE_IDENTIFIED_REMOVED ? 100 : 255
        );

        //图片单击事件 预览图片
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent previewIntent = new Intent(projectMapDataPreviewActivity, ImagePreviewActivity.class);
                previewIntent.putExtra(ImagePreviewActivity.IMAGE_ADDRESS, entity.getImgAddress());
                previewIntent.putExtra(ImagePreviewActivity.IMAGE_FROM, entity.getImgFrom());

                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeScaleUpAnimation(imageView, imageView.getWidth() / 2, imageView.getHeight() / 2, 0, 0);
                ActivityCompat.startActivity(projectMapDataPreviewActivity, previewIntent, optionsCompat.toBundle());
            }
        });
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
        TextView mImageSizeKb;
        UploadStatusView mUploadStatusView;

        public GalleryViewHolder(View itemView) {
            super(itemView);
        }
    }
}
