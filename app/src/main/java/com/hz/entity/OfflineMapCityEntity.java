package com.hz.entity;

import android.util.Log;

import java.util.List;

/**
 * 离线地图城市对象
 */
public class OfflineMapCityEntity {
    public static final int ITEM_TYPE_PARENT = 0;
    public static final int ITEM_TYPE_CHILD = 1;

    private String cityName; //城市名称
    private int cityId;//城市编码
    private int progress; //下载的进度
    public int size;//离线地图size
    private DownLoadStatus downLoadStatus = DownLoadStatus.INIT;//初始化状态
    private List<OfflineMapCityEntity> childList;//子节点列表
    public boolean expand = false;// 是否展开

    public OfflineMapCityEntity(String cityName, int cityCode, int size) {
        Log.d("Test",getClass().getSimpleName()+"被调用了");

        this.cityName = cityName;
        this.cityId = cityCode;
        this.size = size;
    }

    public DownLoadStatus getDownLoadStatus() {
        return downLoadStatus;
    }

    public void setDownLoadStatus(DownLoadStatus downLoadStatus) {
        this.downLoadStatus = downLoadStatus;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public List<OfflineMapCityEntity> getChildList() {
        return childList;
    }

    public void setChildList(List<OfflineMapCityEntity> childList) {
        this.childList = childList;
    }

    public boolean isExpand() {
        return expand;
    }

    public void setExpand(boolean expand) {
        this.expand = expand;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    /**
     * 判断此节点是父节点
     **/
    public boolean isParentNode() {
        return this.childList != null && this.childList.size() > 0;
    }


    /**
     * 下载的状态：无状态，暂停，正在下载
     *
     * @author zhy
     */
    public enum DownLoadStatus {

        WAIT("等待中"), PAUSE("继续"), DOWNLOADING("下载中"), OK("已下载"),INIT("下载");

        private String text;

        DownLoadStatus(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
