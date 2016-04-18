package com.hz.entity;

import android.util.Log;

/**
 * 物料详细列表实体
 */
public class MaterielDetailEntity {

    /**
     * 材料名称*
     */
    private String materielName;

    public MaterielDetailEntity(String materielName) {
        this.materielName = materielName;
        Log.d("Test",getClass().getSimpleName()+"被调用了");

    }

    public String getMaterielName() {
        return materielName;
    }

    public void setMaterielName(String materielName) {
        this.materielName = materielName;
    }
}
