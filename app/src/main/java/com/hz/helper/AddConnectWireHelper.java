package com.hz.helper;

import android.util.Log;

import com.hz.activity.MainActivity;
import com.hz.greendao.dao.MapPoiEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * 添加导线/电缆连线辅助类
 */
public class AddConnectWireHelper {

    public AddConnectWireHelper(){
        Log.d("Test",getClass().getSimpleName()+"被调用了");

    }

    //地图点位拉线（临时对象）
    private Map<String, MapPoiEntity> tempAddConnectWireMap = new HashMap<>();//导线/电缆临时存放开始点结束点对象数据

    //临时存放连线的时候点击点位的对象数据
    private MapPoiEntity tempConnectWireEntity = null;


    /**
     * 添加开始或者结束点位
     **/
    public void put(String key) {
        if (tempConnectWireEntity != null) {
            tempAddConnectWireMap.put(key, tempConnectWireEntity);
        }
    }

    /**
     * 检查点位时候够两个
     **/
    public boolean isSizeEnough() {
        return size() == 2;
    }

    /**
     * 检查开始和结束的点位时候相同
     **/
    public boolean isIdEquals() {
        MapPoiEntity startEntity = get(MainActivity.LINE_START_KEY);
        MapPoiEntity endEntity = get(MainActivity.LINE_END_KEY);
        return startEntity.getPointId().equals(endEntity.getPointId());
    }

    public MapPoiEntity get(String key) {
        return tempAddConnectWireMap.get(key);
    }

    public void remove(String key) {
        tempAddConnectWireMap.remove(key);
    }

    public int size() {
        return tempAddConnectWireMap.size();
    }

    /**
     * 清理临时变量信息
     **/
    public void clear() {
        tempAddConnectWireMap.clear();
        tempConnectWireEntity = null;
    }

    public void setTempConnectWireEntity(MapPoiEntity tempConnectWireEntity) {
        this.tempConnectWireEntity = tempConnectWireEntity;
    }

}
