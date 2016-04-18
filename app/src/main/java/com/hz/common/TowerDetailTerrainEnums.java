/**
 * 项目名称:	edap
 * 创建时间:	2016年1月13日
 * (C) Copyright ZUFE Corporation 2016
 * All Rights Reserved.
 * 注意：本内容仅限于杭州科度科技有限公司内部传阅，禁止外泄以及用于其他的商业目的。
 */
package com.hz.common;

import com.hz.entity.PickerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 包名：com.ketech.edap.enums <br/>
 * 类名：TowerDetailTerrainEnums.java <br/>
 * 版本：version 1.0 <br/>
 * 作者：LouZhihang <br/>
 * 描述：地形
 */
public enum TowerDetailTerrainEnums {

    /**
     * 丘陵
     */
    HILL(1, "丘陵"),

    /**
     * 高山
     */
    MOUNTAIN(2, "高山"),

    /**
     * 平原
     */
    PLAIN(3, "平原");


    /**
     * 构造方法.
     *
     * @param typeSerial
     * @param typeName
     */
    private TowerDetailTerrainEnums(Integer typeSerial, String typeName) {
        this.typeSerial = typeSerial;
        this.typeName = typeName;
    }

    /**
     * 类型序号
     */
    private Integer typeSerial;

    /**
     * 模块名称
     */
    private String typeName;

    public Integer getTypeSerial() {
        return typeSerial;
    }

    public void setTypeSerial(Integer typeSerial) {
        this.typeSerial = typeSerial;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }


    /**
     * 根据序号获取对象
     **/
    public static TowerDetailTerrainEnums getByTypeSerial(Integer typeSerial) {
        for (TowerDetailTerrainEnums terrainEnums : TowerDetailTerrainEnums.values()) {
            if (typeSerial.equals(terrainEnums.getTypeSerial())) {
                return terrainEnums;
            }
        }
        return TowerDetailTerrainEnums.HILL;
    }

    /**
     * 转为为Picker列表
     **/
    public static List<PickerItem> toPickerList() {
        List<PickerItem> pickerItemList = new ArrayList<>();
        for (TowerDetailTerrainEnums terrainEnums : TowerDetailTerrainEnums.values()) {
            pickerItemList.add(new PickerItem(String.valueOf(terrainEnums.getTypeSerial()), terrainEnums.getTypeName()));
        }
        return pickerItemList;
    }

}
