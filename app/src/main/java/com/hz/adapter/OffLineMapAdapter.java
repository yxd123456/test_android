package com.hz.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hz.R;
import com.hz.activity.base.BaseActivity;
import com.hz.entity.OfflineMapCityEntity;
import com.hz.fragment.OffLineMapListFragment;
import com.hz.view.DownLoadIndicator;
import com.hz.view.ExpandDirectionView;
import com.hz.view.progressbar.NumberProgressBar;

import java.util.List;

/**
 * 离线地图数据适配器
 */
public class OffLineMapAdapter extends RecyclerView.Adapter<OffLineMapAdapter.BaseOffLineMapViewHolder> {
    public static final String TAG = OffLineMapAdapter.class.getSimpleName();
    public List<OfflineMapCityEntity> offlineMapCityEntities;
    private LayoutInflater layoutInflater;
    public OffLineMapListFragment offLineMapListFragment;
    public BaseActivity baseActivity;


    public OffLineMapAdapter(OffLineMapListFragment offLineMapListFragment, List<OfflineMapCityEntity> offlineMapCityEntities) {
        this.baseActivity = (BaseActivity) offLineMapListFragment.getActivity();
        this.offLineMapListFragment = offLineMapListFragment;
        this.offlineMapCityEntities = offlineMapCityEntities;
        this.layoutInflater = LayoutInflater.from(this.baseActivity);
    }

    @Override
    public BaseOffLineMapViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == OfflineMapCityEntity.ITEM_TYPE_PARENT) {
            return new ParentOffLineMapViewHolder(layoutInflater.inflate(R.layout.item_recycler_parent, null));
        } else {
            return new ChildOffLineMapViewHolder(layoutInflater.inflate(R.layout.item_recycler_child, null));
        }
    }

    /**
     * 获取此节点是父节点或者子节点
     **/
    @Override
    public int getItemViewType(int position) {
        return offlineMapCityEntities.get(position).isParentNode() ? OfflineMapCityEntity.ITEM_TYPE_PARENT : OfflineMapCityEntity.ITEM_TYPE_CHILD;
    }

    @Override
    public void onBindViewHolder(BaseOffLineMapViewHolder holder, final int position) {
        holder.onBindViewHolder(this, position);
    }

    @Override
    public int getItemCount() {
        return offlineMapCityEntities.size();
    }
    //数据源加载


    /**
     * 离线地图ViewHolder基类
     **/
    public static abstract class BaseOffLineMapViewHolder extends RecyclerView.ViewHolder {
        public BaseOffLineMapViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void onBindViewHolder(final OffLineMapAdapter offLineMapAdapter, final int position);
    }

    /**
     * 子节点ViewHolder
     **/
    public static class ChildOffLineMapViewHolder extends BaseOffLineMapViewHolder {
        public TextView childTextName;
        public TextView childTextSize;
        public Button childButtonDownload;
        public NumberProgressBar childNumberProgressBar;
        public DownLoadIndicator childDownLoadIndicator;
        public View childSeperateLine;
        public View rootView;

        public ChildOffLineMapViewHolder(View itemView) {
            super(itemView);
            this.rootView = itemView;
            this.childTextName = (TextView) itemView.findViewById(R.id.id_textview_child_textname);
            this.childTextSize = (TextView) itemView.findViewById(R.id.id_textview_child_textsize);
            this.childButtonDownload = (Button) itemView.findViewById(R.id.id_button_child_download);
            this.childNumberProgressBar = (NumberProgressBar) itemView.findViewById(R.id.id_progress_child_downloading);
            this.childSeperateLine = itemView.findViewById(R.id.id_seperate_line);
            this.childDownLoadIndicator = (DownLoadIndicator) itemView.findViewById(R.id.id_child_download_indicator);
        }

        @Override
        public void onBindViewHolder(final OffLineMapAdapter offLineMapAdapter, final int position) {
            final OfflineMapCityEntity cityEntity = offLineMapAdapter.offlineMapCityEntities.get(position);
            this.childTextName.setText(cityEntity.getCityName());
            this.childTextSize.setText("地图" + cityEntity.getSize() / 1024 / 1024 + "M");
            this.childButtonDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    offLineMapAdapter.offLineMapListFragment.downLoadMapData(cityEntity, position);
                }
            });
            this.childButtonDownload.setText(cityEntity.getDownLoadStatus().getText());
            if (cityEntity.getDownLoadStatus() == OfflineMapCityEntity.DownLoadStatus.OK) {
                this.childButtonDownload.setEnabled(false);
                this.childButtonDownload.setAlpha(0.4f);
            } else {
                this.childButtonDownload.setEnabled(true);
                this.childButtonDownload.setAlpha(1f);
            }
            if (cityEntity.getDownLoadStatus() == OfflineMapCityEntity.DownLoadStatus.DOWNLOADING ||
                    cityEntity.getDownLoadStatus() == OfflineMapCityEntity.DownLoadStatus.PAUSE ||
                    cityEntity.getDownLoadStatus() == OfflineMapCityEntity.DownLoadStatus.WAIT) {
                this.childNumberProgressBar.setVisibility(View.VISIBLE);
                this.childSeperateLine.setVisibility(View.GONE);
            } else {
                this.childNumberProgressBar.setVisibility(View.GONE);
                this.childSeperateLine.setVisibility(View.VISIBLE);
            }
          /*  WAIT("等待中"), PAUSE("继续"), DOWNLOADING("下载中"), OK("已下载"),INIT("下载");*/
            if (cityEntity.getDownLoadStatus() == OfflineMapCityEntity.DownLoadStatus.INIT) {

                this.childDownLoadIndicator.setDownLoadStatus(DownLoadIndicator.DOWNLOAD_STATUS_INIT);

            } else if (cityEntity.getDownLoadStatus() == OfflineMapCityEntity.DownLoadStatus.PAUSE ||
                    cityEntity.getDownLoadStatus() == OfflineMapCityEntity.DownLoadStatus.WAIT) {

                this.childDownLoadIndicator.setDownLoadStatus(DownLoadIndicator.DOWNLOAD_STATUS_PUSH);

            } else if (cityEntity.getDownLoadStatus() == OfflineMapCityEntity.DownLoadStatus.DOWNLOADING) {

                this.childDownLoadIndicator.setDownLoadStatus(DownLoadIndicator.DOWNLOAD_STATUS_DOWNLOADING);

            } else if (cityEntity.getDownLoadStatus() == OfflineMapCityEntity.DownLoadStatus.OK) {

                this.childDownLoadIndicator.setDownLoadStatus(DownLoadIndicator.DOWNLOAD_STATUS_OK);

            }

            this.childNumberProgressBar.setProgress(cityEntity.getProgress());
        }
    }

    /**
     * 父节点ViewHolder
     **/
    public static class ParentOffLineMapViewHolder extends BaseOffLineMapViewHolder {
        public TextView parentTextName;
        public ExpandDirectionView parentIcon;
        public View parentRootView;

        public ParentOffLineMapViewHolder(View itemView) {
            super(itemView);
            this.parentRootView = itemView;
            parentIcon = (ExpandDirectionView) itemView.findViewById(R.id.id_imageview_parent_icon);
            parentTextName = (TextView) itemView.findViewById(R.id.id_textview_parent_textname);
        }

        @Override
        public void onBindViewHolder(final OffLineMapAdapter offLineMapAdapter, final int position) {
            final ParentOffLineMapViewHolder parentOffLineMapViewHolder = this;
            final OfflineMapCityEntity cityEntity = offLineMapAdapter.offlineMapCityEntities.get(position);
            parentOffLineMapViewHolder.parentTextName.setText(cityEntity.getCityName());
            parentOffLineMapViewHolder.parentIcon
                    .setViewAlpha(0.4f)
                    .setDirection(cityEntity.isExpand() ? ExpandDirectionView.EXPAND_DIRECTION_UP : ExpandDirectionView.EXPAND_DIRECTION_DOWN);

            parentOffLineMapViewHolder.parentRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cityEntity.isExpand()) {
                        int itemCount = cityEntity.getChildList().size();
                        for (int i = 0; i < itemCount; i++) {
                            offLineMapAdapter.offlineMapCityEntities.remove(position + 1);
                        }
                       /* offLineMapAdapter.notifyItemRangeRemoved(position + 1, itemCount);*/
                    } else {
                        List<OfflineMapCityEntity> childList = cityEntity.getChildList();
                        offLineMapAdapter.offlineMapCityEntities.addAll(position + 1, childList);
                      /*  offLineMapAdapter.notifyItemRangeInserted(position + 1, childList.size());*/
                    }
                    cityEntity.setExpand(!cityEntity.isExpand());
                    /*offLineMapAdapter.notifyItemChanged(position);*/
                    offLineMapAdapter.notifyDataSetChanged();
                }
            });
        }

    }
}


