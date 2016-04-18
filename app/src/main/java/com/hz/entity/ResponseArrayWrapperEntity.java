package com.hz.entity;

import android.util.Log;

import com.hz.greendao.dao.ConductorWireEntity;
import com.hz.greendao.dao.ElectricPoleType;
import com.hz.greendao.dao.EquimentInstallType;
import com.hz.greendao.dao.GeologicalConditionType;
import com.hz.greendao.dao.ProjectEntity;
import com.hz.greendao.dao.TowerType;
import com.hz.greendao.dao.TransformerType;
import com.hz.greendao.dao.WireType;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据同步请求响应公共属性
 */
public class ResponseArrayWrapperEntity<T> extends ResponseStateEntity {


    private List<T> data = new ArrayList<T>();

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    /**
     * 电杆
     * *
     */
    public static class ElectricPoleTypeWrapperEntity extends ResponseArrayWrapperEntity<ElectricPoleType> {
    }

    /**
     * 设备安装
     */
    public static class EquimentInstallTypeWrapperEntity extends ResponseArrayWrapperEntity<EquimentInstallType> {
    }


    /**
     * 地质情况
     */
    public static class GeologicalConditionTypeWrapperEntity extends ResponseArrayWrapperEntity<GeologicalConditionType> {
    }


    /**
     * 杆塔类型
     */
    public static class TowerTypeWrapperEntity extends ResponseArrayWrapperEntity<TowerType> {

    }


    /**
     * 变压器容量
     */
    public static class TransformerTypeWrapperEntity extends ResponseArrayWrapperEntity<TransformerType> {
    }

    /**
     * 拉线类型
     */
    public static class WireTypeWrapperEntity extends ResponseArrayWrapperEntity<WireType> {
    }


    /**
     * 项目列表
     */
    public static class ProjectWrapperEntity extends ResponseArrayWrapperEntity<ProjectEntity> {
    }

    /**
     * 导线,电缆
     */
    public static class ConductorWireWrapperEntity extends ResponseArrayWrapperEntity<ConductorWireEntity> {
    }


}


