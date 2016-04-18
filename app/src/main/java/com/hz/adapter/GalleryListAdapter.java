package com.hz.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.hz.R;
import com.hz.activity.ImagePreviewActivity;
import com.hz.common.Constans;
import com.hz.entity.GalleryListItemEntity;
import com.hz.popupwindow.GalleryPopupWindow;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * 选择图片适配器
 */
public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.GalleryListViewHolder> {
    public static final String TAG = GalleryListAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private List<GalleryListItemEntity> mGalleryEntityList;
    private Context mContext;
    private int cellWidth;
    private GalleryPopupWindow mGalleryPopupWindow;

    public GalleryListAdapter(GalleryPopupWindow galleryPopupWindow, Context context, List<GalleryListItemEntity> mGalleryEntityList, int cellWidth){
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mGalleryPopupWindow = galleryPopupWindow;
        this.mGalleryEntityList = mGalleryEntityList;
        this.cellWidth = cellWidth;
    }

    //创建ViewHolder
    @Override
    public GalleryListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.activity_gallerylist_datapreview_item, null);

        GalleryListViewHolder galleryViewHolder = new GalleryListViewHolder(view);
        galleryViewHolder.mImageView = (ImageView) view.findViewById(R.id.id_ImageView_image);
        galleryViewHolder.mCheckBox = (CheckBox) view.findViewById(R.id.id_checkbox_choose);

        ViewGroup.LayoutParams layoutParams = galleryViewHolder.mImageView.getLayoutParams();
        layoutParams.width = cellWidth;
        layoutParams.height = cellWidth;
        galleryViewHolder.mImageView.setLayoutParams(layoutParams);

        return galleryViewHolder;
    }

    // 将数据绑定至ViewHolder
    @Override
    public void onBindViewHolder(GalleryListViewHolder holder, final int position) {
        final GalleryListItemEntity entity = mGalleryEntityList.get(position);
        String imageUrl = Constans.ImageLoaderMark.FILE + entity.imagePath;
        final ImageView imageView = holder.mImageView;
        ImageLoader.getInstance().displayImage(imageUrl, imageView);


        //图片单击事件 预览图片
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent previewIntent = new Intent(mContext, ImagePreviewActivity.class);
                previewIntent.putExtra(ImagePreviewActivity.IMAGE_ADDRESS, entity.imagePath);
                previewIntent.putExtra(ImagePreviewActivity.IMAGE_FROM, Constans.ImageLoaderMark.FILE);
                mContext.startActivity(previewIntent);
            }
        });

        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckBox) v).setChecked(entity.checked = !entity.checked);
                mGalleryPopupWindow.calculateChecked();
            }
        });
        holder.mCheckBox.setChecked(entity.checked);
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
    public static class GalleryListViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;
        CheckBox mCheckBox;

        public GalleryListViewHolder(View itemView) {
            super(itemView);
        }
    }
}
