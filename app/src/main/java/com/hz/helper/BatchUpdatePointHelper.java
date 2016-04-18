package com.hz.helper;

import android.text.TextUtils;
import android.util.Log;

import com.hz.activity.MainActivity;
import com.hz.common.Constans;
import com.hz.greendao.dao.MapLineEntity;
import com.hz.greendao.dao.MapLineItemEntity;
import com.hz.greendao.dao.MapPoiEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 批量修改点位信息帮助类
 */
public class BatchUpdatePointHelper {


    //地图点位拉线（临时对象）
    private Map<String, MapPoiEntity> tempBatchAddPointMap = new HashMap<>();//临时存放开始点结束点对象数据

    //临时存放连线的时候点击点位的对象数据
    private MapPoiEntity tempMapPoiEntity = null;

    //临时存放批量线信息
    private List<MapLineEntity> mapLineEntityList = new ArrayList<>();

    private List<MapPoiEntity> mapPoiEntityList = new ArrayList<>();


    public BatchUpdatePointHelper() {
        Log.d("Test",getClass().getSimpleName()+"被调用了");

    }

    /**
     * 添加开始或者结束点位
     **/
    public void put(String key) {
        if (tempMapPoiEntity != null) {
            tempBatchAddPointMap.put(key, tempMapPoiEntity);
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
        return tempBatchAddPointMap.get(key);
    }

    public void remove(String key) {
        tempBatchAddPointMap.remove(key);
    }

    public int size() {
        return tempBatchAddPointMap.size();
    }

    /**
     * 清理临时变量信息
     **/
    public void clear() {
        tempBatchAddPointMap.clear();
        mapLineEntityList.clear();
        mapPoiEntityList.clear();
        tempMapPoiEntity = null;
    }

    public void setTempBatchPointEntity(MapPoiEntity tempMapPoiEntity) {
        this.tempMapPoiEntity = tempMapPoiEntity;
    }

    public List<MapPoiEntity> getMapPoiEntityList() {
        return mapPoiEntityList;
    }

    public void setMapPoiEntityList(List<MapPoiEntity> mapPoiEntityList) {
        this.mapPoiEntityList = mapPoiEntityList;
        fetchConnectLineBy(mapPoiEntityList);
    }


    public List<MapLineEntity> getMapLineEntityList() {
        return mapLineEntityList;
    }

    public void setMapLineEntityList(List<MapLineEntity> mapLineEntityList) {
        this.mapLineEntityList = mapLineEntityList;
    }

    /**
     * 批量处理杆信息，杆号批量排序
     **/
    public void batchUpdatePointResultHandler(MapPoiEntity editPoiEntity) {
        int num = 0;
        for (MapPoiEntity poiItem : mapPoiEntityList) {
            if (editPoiEntity != null) {
                String editPointName = editPoiEntity.getPointName();
                String editPointNum = PointNumHelper.orderPointNum(editPoiEntity.getPointNum(), num);//生成杆塔顺序
                String editPointTowerType = editPoiEntity.getPointTowerTypeId();//杆塔形式
                String editPointElectricPoleType = editPoiEntity.getPointElectricPoleTypeId();//电杆型号
                int editPointElectricPoleTypeCount = editPoiEntity.getPointElectricPoleTypeCount();//电杆数量
                String editPointGeologicalConditionsTypeId = editPoiEntity.getPointGeologicalConditionsTypeId();//地质情况
                String editPointLandForm = editPoiEntity.getPointLandForm();//地形情况
                String editPointEquipmentInstallationTypeId = editPoiEntity.getPointEquipmentInstallationTypeId();//设备安装
                int editPointStatus = editPoiEntity.getPointStatus();//点位状态
                String editPointNote = editPoiEntity.getPointNote();//点位备注

                if (!TextUtils.isEmpty(editPointName)) {
                    poiItem.setPointName(editPointName);
                }
                if (!TextUtils.isEmpty(editPointNum)) {
                    poiItem.setPointNum(editPointNum);
                }
                if (!TextUtils.isEmpty(editPointTowerType)) {
                    poiItem.setPointTowerTypeId(editPointTowerType);
                }
                if (!TextUtils.isEmpty(editPointElectricPoleType)) {
                    poiItem.setPointElectricPoleTypeId(editPointElectricPoleType);
                }
                if (editPointElectricPoleTypeCount != 0) {
                    poiItem.setPointElectricPoleTypeCount(editPointElectricPoleTypeCount);
                }
                if (!TextUtils.isEmpty(editPointGeologicalConditionsTypeId)) {
                    poiItem.setPointGeologicalConditionsTypeId(editPointGeologicalConditionsTypeId);
                }
                if (!TextUtils.isEmpty(editPointLandForm)) {
                    poiItem.setPointLandForm(editPointLandForm);
                }
                if (!TextUtils.isEmpty(editPointEquipmentInstallationTypeId)) {
                    poiItem.setPointEquipmentInstallationTypeId(editPointEquipmentInstallationTypeId);
                }
                if (editPointStatus != Constans.AttributeStatus.NONE) {
                    poiItem.setPointStatus(editPointStatus);
                }
                if (!TextUtils.isEmpty(editPointNote)) {
                    poiItem.setPointNote(editPointNote);
                }
                num++;
            }
        }
        //持久化修改的数据到数据库
        DataBaseManagerHelper.getInstance().updateConnectPoints(mapPoiEntityList);
    }


    /**
     * 批量处理线信息
     **/
    public void batchUpdateLineResultHandle(MapLineEntity lineEntity) {
        int specificationNumber = lineEntity.getLineSpecificationNumber();
        List<MapLineItemEntity> lineItemEntity = lineEntity.getMapLineItemEntityList2();
        if (specificationNumber == 0 && lineItemEntity == null) {
            return;
        }
        if (specificationNumber == 0 && lineItemEntity.size() == 0) {
            return;
        }

        List<MapLineItemEntity> lineItemListNotRemoved = new ArrayList<>();
        for (MapLineItemEntity itemEntity : lineItemEntity) {
            if (itemEntity.getLineItemRemoved() == Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL) {
                lineItemListNotRemoved.add(itemEntity);
            }
        }
        if (specificationNumber == 0 && lineItemListNotRemoved.size() == 0) {
            return;
        }

        for (MapLineEntity mapLineEntity : mapLineEntityList) {
            if (specificationNumber > 0) {
                mapLineEntity.setLineSpecificationNumber(specificationNumber);
            }
            //重新生成ID
            for (MapLineItemEntity itemEntity : lineItemListNotRemoved) {
                itemEntity.setLineItemId(UUID.randomUUID().toString());
                itemEntity.setLineItemLineId(mapLineEntity.getLineId());
            }
            List<MapLineItemEntity> lineItemListOld = mapLineEntity.getMapLineItemEntityList();
            if (lineItemListOld != null && lineItemListOld.size() > 0) {
                for (MapLineItemEntity itemEntity : lineItemListOld) {
                    itemEntity.setLineItemRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_REMOVED);
                }
                lineItemListOld.addAll(lineItemListNotRemoved);
            }
            DataBaseManagerHelper.getInstance().updateInsertConnectLineItems(lineItemListOld);
        }

        DataBaseManagerHelper.getInstance().updateConnectLines(mapLineEntityList);
    }

    /**
     * 根据设置的点位列表到数据库获取点位连接的线列表
     **/
    private void fetchConnectLineBy(List<MapPoiEntity> mapPoiEntityList) {
        MapPoiEntity prePoint = null;
        for (int i = 0; i < mapPoiEntityList.size(); i++) {
            MapPoiEntity mapPoiEntity = mapPoiEntityList.get(i);
            if (prePoint != null) {
                String currentPointId = mapPoiEntity.getPointId();
                String prePointId = prePoint.getPointId();
                MapLineEntity mapLineEntity = DataBaseManagerHelper.getInstance().getLineByLineStartPointIdAndEndPointId(
                        prePointId,
                        currentPointId
                );
                mapLineEntityList.add(mapLineEntity);
            }
            prePoint = mapPoiEntity;
        }
    }

}
