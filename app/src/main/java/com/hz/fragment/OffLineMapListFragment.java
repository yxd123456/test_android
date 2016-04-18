package com.hz.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.hz.R;
import com.hz.activity.base.BaseActivity;
import com.hz.adapter.OffLineMapAdapter;
import com.hz.entity.OfflineMapCityEntity;
import com.hz.fragment.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 离线地图fragment
 */
public class OffLineMapListFragment extends BaseFragment implements MKOfflineMapListener {
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    protected static final String TAG = OffLineMapListFragment.class.getSimpleName();
    private MKOfflineMap mOfflineMap;//离线地图功能
    private List<OfflineMapCityEntity> offlineMapCityEntities = new ArrayList<>(); //离线地图的数据
    private List<Integer> mCityCodes = new ArrayList<>(); //目前加入下载队列的城市
    private OffLineMapAdapter mOffLineMapAdapter;
    private View rootView;
    public Handler mUiHandler = new Handler();//操作uihandler
    public Handler mDataHandler = null;//后台任务handler
    public HandlerThread handlerThread;

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_offlinemap_manager, null, false);
        initComponents();
        return rootView;
    }
    @Override
    public void onDestroyView() {
        mOfflineMap.destroy();
        super.onDestroyView();
        mDataHandler.removeCallbacksAndMessages(null);
        mUiHandler.removeCallbacksAndMessages(null);
        handlerThread.quit();
    }

    @Override
    public void onGetOfflineMapState(int type, int cityCode) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: // 离线地图下载更新事件类型
                MKOLUpdateElement update = mOfflineMap.getUpdateInfo(cityCode);
                postToCalcDownProgress(update, cityCode);
                break;
            case MKOfflineMap.TYPE_NEW_OFFLINE: // 有新离线地图安装
                Log.d(TAG, "onGetOfflineMapState: " + type + "  " + cityCode);
                break;
            case MKOfflineMap.TYPE_VER_UPDATE: // 版本更新提示
                Log.d(TAG, "onGetOfflineMapState: " + type + "  " + cityCode);
                break;
        }
        Log.d(TAG, "onGetOfflineMapState: " + type + "" + cityCode);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 设置离线地图数据
     **/
    public void setUpOffLineMapData() {
        mDataHandler.post(new Runnable() {
            @Override
            public void run() {
                initOffLineMapData();//初始化ListView数据
            }
        });
    }
    /**
     * 刷新指定位置数据
     * @param position 位置
     **/
    private void postToItemInsert(final int position) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOffLineMapAdapter != null) {
                    mOffLineMapAdapter.notifyItemInserted(position);
                }
            }
        });
    }
    /**
     * 刷新数据
     **/
    private void postToRefreshAll() {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOffLineMapAdapter != null) {
                    mOffLineMapAdapter.notifyDataSetChanged();
                }
            }
        });
    }
    /**
     * 下载离线地图数据
     * @param cityEntity 带现在的地图对象数据
     * @param position   位置
     **/
    public void downLoadMapData(OfflineMapCityEntity cityEntity, int position) {
        int cityId = cityEntity.getCityId();
        if (mCityCodes.contains(cityId)) {
            mCityCodes.remove(Integer.valueOf(cityId));
            mOfflineMap.pause(cityId);
        } else {
            mCityCodes.add(cityId);
            mOfflineMap.start(cityId);
        }
        cityEntity.setDownLoadStatus(OfflineMapCityEntity.DownLoadStatus.WAIT);//新增下载后
        mOffLineMapAdapter.notifyItemChanged(position);
    }
    /**
     * 初始化离线地图数据
     */
    private void initOffLineMapData() {
        int i = 0;
        List<MKOLUpdateElement> allUpdateInfo = mOfflineMap.getAllUpdateInfo();  // 获得所有已经下载的城市列表

        List<MKOLSearchRecord> offlineCityList = mOfflineMap.getOfflineCityList(); // 获得所有热门城市
        for (MKOLSearchRecord record : offlineCityList) {   // 设置所有数据的状态
            OfflineMapCityEntity cityBean = new OfflineMapCityEntity(record.cityName, record.cityID, record.size);
            List<OfflineMapCityEntity> childList = new ArrayList<>();//保存子节点信息列表
            if (record.childCities != null && record.childCities.size() > 0) {//如果此节点还有子节点设置子节点信息
                for (MKOLSearchRecord childRecord : record.childCities) {
                    OfflineMapCityEntity childCityBean = new OfflineMapCityEntity(childRecord.cityName, childRecord.cityID, childRecord.size);
                    childCityBean.setProgress(calItemProgressAndStatus(childCityBean, allUpdateInfo));//设置节点下载进度信息
                    childList.add(childCityBean);
                }
            } else {
                cityBean.setProgress(calItemProgressAndStatus(cityBean, allUpdateInfo));//设置节点下载进度信息
            }
            cityBean.setChildList(childList);
            offlineMapCityEntities.add(cityBean);

            postToItemInsert(i);
            i++;
        }

        postToRefreshAll();
    }
    /**
     * 为cityBean计算进度 和下载状态
     */
    public int calItemProgressAndStatus(OfflineMapCityEntity cityBean, List<MKOLUpdateElement> allUpdateInfo) {
        int pro = 0;
        if (allUpdateInfo == null) {
            return pro;
        }
        for (MKOLUpdateElement ele : allUpdateInfo) {
            if (ele.cityID == cityBean.getCityId()) {
                cityBean.setProgress(ele.ratio);
                switch (cityBean.getProgress()) {
                    case 0:
                        cityBean.setDownLoadStatus(OfflineMapCityEntity.DownLoadStatus.INIT);
                        break;
                    case 100:
                        cityBean.setDownLoadStatus(OfflineMapCityEntity.DownLoadStatus.OK);
                        break;
                    default:
                        cityBean.setDownLoadStatus(OfflineMapCityEntity.DownLoadStatus.PAUSE);
                        break;
                }
                pro = ele.ratio;
                break;
            }
        }
        return pro;
    }
    /**
     * 刷新指定itme数据
     * @param position 城市位置
     */
    public void postToRefreshItemByPosition(final int position) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOffLineMapAdapter != null) {
                    mOffLineMapAdapter.notifyItemChanged(position);
                }
            }
        });
    }

    private void setStatusAndProgress(OfflineMapCityEntity childBean, MKOLUpdateElement update) {
        if (update.ratio % 5 != 0) {//减少刷新频率
            return;
        }

        childBean.setProgress(update.ratio);
        childBean.setDownLoadStatus(childBean.getProgress() == 100 ?
                OfflineMapCityEntity.DownLoadStatus.OK :
                OfflineMapCityEntity.DownLoadStatus.DOWNLOADING);


        //获取城市position 并刷新指定item
        for (int i = 0; i < offlineMapCityEntities.size(); i++) {
            if (offlineMapCityEntities.get(i).getCityId() == childBean.getCityId()) {
                Log.d(TAG, "setStatusAndProgress: " + childBean.getCityName());
                postToRefreshItemByPosition(i);
                break;
            }
        }
    }
    /**
     * 计算进度条进度
     */
    private void postToCalcDownProgress(final MKOLUpdateElement update, final int cityCode) {
        mDataHandler.post(new Runnable() {
            @Override
            public void run() {
                //设置进度
                for (OfflineMapCityEntity bean : offlineMapCityEntities) {
                    if (bean.getChildList() != null) {//设置当前下载为子节点的进度显示
                        for (OfflineMapCityEntity childBean : bean.getChildList()) {
                            if (childBean.getCityId() == cityCode) {
                                setStatusAndProgress(childBean, update);
                                break;
                            }
                        }
                    }
                    if (bean.getCityId() == cityCode) {//设置当前下载为组时的进度显示
                        setStatusAndProgress(bean, update);
                        break;
                    }

                }

                Log.e(TAG, "城市名称：" + update.cityName + " ,下载进度：" + update.ratio);
            }
        });
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    private void initComponents() {
        handlerThread = new HandlerThread("dataHandlerThread");
        handlerThread.start();
        mDataHandler = new Handler(handlerThread.getLooper());

        initOfflineMap();//初始化离线地图对象
        initListView();//初始化ListView
        setUpOffLineMapData();
    }
    /**
     * 初始化离线地图
     */
    private void initOfflineMap() {
        mOfflineMap = new MKOfflineMap();
        mOfflineMap.init(this); // 设置监听
    }
    /**
     * 初始化数据列表
     **/
    private void initListView() {
        RecyclerView mOffLineRecyclerView = (RecyclerView) rootView.findViewById(R.id.id_expandablelistview_offlinelist);
        mOffLineMapAdapter = new OffLineMapAdapter(this, offlineMapCityEntities);
        mOffLineRecyclerView.setAdapter(mOffLineMapAdapter);


        //设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        mOffLineRecyclerView.setLayoutManager(layoutManager);
        mOffLineRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}
