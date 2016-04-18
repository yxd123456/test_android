package com.hz.entity;

import android.util.Log;

import java.util.Date;

/**
 * 物料同步列表对象
 */
public class MaterielDataSyncEntity {

    /**
     * 物料唯一标示*
     */
    private String materielId;

    /**
     * 物料名称*
     */
    private String materielName;

    /**
     * 物料上次同步时间*
     */
    private Date proSyncTime;

    /**
     * 此物料个数*
     */
    private int num;

    /**
     * 此物料图标*
     */
    private int imageId;

    /**
     * 此物料服务请求地址
     */
    private String serverUrl;

    public MaterielDataSyncEntity() {
    }

    public MaterielDataSyncEntity(String materielId, String materielName, Date proSyncTime, int num, int imageId) {
        Log.d("Test",getClass().getSimpleName()+"被调用了");

        this.materielId = materielId;
        this.materielName = materielName;
        this.proSyncTime = proSyncTime;
        this.num = num;
        this.imageId = imageId;
    }

    public MaterielDataSyncEntity(String materielId, String materielName, Date proSyncTime, int num, int imageId, String serverUrl) {
        Log.d("Test",getClass().getSimpleName()+"被调用了");

        this.materielId = materielId;
        this.materielName = materielName;
        this.proSyncTime = proSyncTime;
        this.num = num;
        this.imageId = imageId;
        this.serverUrl = serverUrl;
    }

    public String getMaterielId() {
        return materielId;
    }

    public void setMaterielId(String materielId) {
        this.materielId = materielId;
    }

    public String getMaterielName() {
        return materielName;
    }

    public void setMaterielName(String materielName) {
        this.materielName = materielName;
    }

    public Date getProSyncTime() {
        return proSyncTime;
    }

    public void setProSyncTime(Date proSyncTime) {
        this.proSyncTime = proSyncTime;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
