package com.hz.helper;

import android.graphics.Color;
import android.util.Log;

import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.hz.activity.MainActivity;
import com.hz.greendao.dao.MapPoiEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * 增加跨越线工具
 */
public class AddCrossLineHelper {

    public Map<String, MapPoiEntity> tempAddLineMap = new HashMap<>();//跨越线临时存放开始点结束点对象数据
    public Map<String, Marker> tempAddLineMarker = new HashMap<>();//跨越线开始点结束点标记


    public AddCrossLineHelper() {
        Log.d("Test",getClass().getSimpleName()+"被调用了");

    }

    /**
     * 新增一个开始或者结束点位的图标，如果跨越线增加成功就返回true
     *
     * @param lineKey      key
     * @param mainActivity 页面对象
     * @param poiEntity    开始点位数据
     *                     *
     */
    public boolean addStartOrEndPoint(MainActivity mainActivity, String lineKey, MapPoiEntity poiEntity) {
        //清除所有的开始结束点位
        clearAllMarker();
        //添加新增的开始或者结束点位
        tempAddLineMap.put(lineKey, poiEntity);

        //添加marker到地图
        for (String key : tempAddLineMap.keySet()) {
            MapPoiEntity entity = tempAddLineMap.get(key);
            switch (key) {
                case MainActivity.LINE_END_KEY:
                    tempAddLineMarker.put(
                            key,
                            mainActivity.addMapMarker(
                                    new LatLng(entity.getPointLatitude(), entity.getPointLongitude()),
                                    MapIconHelper.getInstance().generateBitmapIconByNum(MapIconHelper.KEY_CROSSLINE_END, Color.WHITE),
                                    true
                            )
                    );
                    break;
                case MainActivity.LINE_START_KEY:
                    tempAddLineMarker.put(
                            key,
                            mainActivity.addMapMarker(
                                    new LatLng(entity.getPointLatitude(), entity.getPointLongitude()),
                                    MapIconHelper.getInstance().generateBitmapIconByNum(MapIconHelper.KEY_CROSSLINE_START, Color.WHITE),
                                    true
                            )
                    );
                    break;
            }
        }
        return tempAddLineMap.size() == 2 && tempAddLineMarker.size() == 2;
    }

    /**
     * 获取开始点位携带的对象
     * *
     */
    public MapPoiEntity getStartPointEntity() {
        return tempAddLineMap.get(MainActivity.LINE_START_KEY);
    }

    /**
     * 获取结束点位携带的对象
     * *
     */
    public MapPoiEntity getEndPointEntity() {
        return tempAddLineMap.get(MainActivity.LINE_END_KEY);
    }

    /**
     * 清除点位信息和图标信息
     * *
     */
    public void clear() {
        clearAllMarker();
        tempAddLineMarker.clear();
        tempAddLineMap.clear();
    }

    /**
     * 清除开始和结束点位的图标资源信息
     * *
     */
    private void clearAllMarker() {
        for (String key : tempAddLineMarker.keySet()) {
            Marker marker = tempAddLineMarker.get(key);
            if (marker != null) {
                marker.remove();
            }
        }
    }

}
