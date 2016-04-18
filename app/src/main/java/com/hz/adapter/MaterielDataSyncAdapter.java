package com.hz.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hz.R;
import com.hz.activity.base.BaseActivity;
import com.hz.common.Constans;
import com.hz.entity.MaterielDataSyncEntity;
import com.hz.fragment.MaterielDataSyncFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * 物料同步列表数据适配器
 */
public class MaterielDataSyncAdapter extends RecyclerView.Adapter<MaterielDataSyncAdapter.MaterialDataSyncViewHolder> {
    public static final String TAG = MaterielDataSyncAdapter.class.getSimpleName();
    private List<MaterielDataSyncEntity> mMaterielDataSyncEntities;
    private LayoutInflater mLayoutInflater;
    private MaterielDataSyncFragment mTargetFragment;

    public MaterielDataSyncAdapter(MaterielDataSyncFragment mTargetFragment, List<MaterielDataSyncEntity> materielListEntities) {
        this.mMaterielDataSyncEntities = materielListEntities;
        this.mTargetFragment = mTargetFragment;
        BaseActivity mTargetActivity = (BaseActivity) mTargetFragment.getActivity();
        this.mLayoutInflater = LayoutInflater.from(mTargetActivity);
    }

    @Override
    public MaterialDataSyncViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.activity_materiel_datasync_item, null);
        MaterialDataSyncViewHolder viewHolder = new MaterialDataSyncViewHolder(view);
        viewHolder.materielName = (TextView) view.findViewById(R.id.id_textview_materielname);
        viewHolder.materielSyncTime = (TextView) view.findViewById(R.id.id_textview_prosynctime);
        viewHolder.materielNum = (TextView) view.findViewById(R.id.id_textview_num);
        viewHolder.syncMateriel = (Button) view.findViewById(R.id.id_button_syncmateriel);
        viewHolder.materielImage = (ImageView) view.findViewById(R.id.id_imageview_leftimage);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MaterialDataSyncViewHolder holder, int position) {
        MaterielDataSyncEntity item = mMaterielDataSyncEntities.get(position);
        holder.syncMateriel.setVisibility(View.INVISIBLE);
        setViewValue(holder, item);
        setViewListner(item, holder, position);
    }

    @Override
    public int getItemCount() {
        return mMaterielDataSyncEntities.size();
    }

    /***
     * 为视图设置监听
     **/
    private void setViewListner(final MaterielDataSyncEntity item, MaterialDataSyncViewHolder viewHolder, final int position) {
        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTargetFragment.startDetailActivity(item);
            }
        });

        viewHolder.syncMateriel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTargetFragment.syncMaterielDatas(item, position);
            }
        });
    }


    /**
     * 为视图赋予值
     **/
    private void setViewValue(MaterialDataSyncViewHolder viewHolder, MaterielDataSyncEntity item) {
        viewHolder.materielName.setText(item.getMaterielName());
        viewHolder.materielSyncTime.setText(new SimpleDateFormat(Constans.DATE_PATTERN_DEFAULT, Locale.CHINA).format(item.getProSyncTime()));
        viewHolder.materielNum.setText(item.getNum() + "个");
        String imageLoaderUrl = Constans.ImageLoaderMark.DRAWABLE + item.getImageId();
        ImageLoader.getInstance().displayImage(imageLoaderUrl, viewHolder.materielImage);
    }


    static class MaterialDataSyncViewHolder extends RecyclerView.ViewHolder {
        public ImageView materielImage;
        public TextView materielName;
        public TextView materielSyncTime;
        public TextView materielNum;
        public Button syncMateriel;
        public View rootView;

        public MaterialDataSyncViewHolder(View itemView) {
            super(itemView);
            this.rootView = itemView;
        }
    }
}
