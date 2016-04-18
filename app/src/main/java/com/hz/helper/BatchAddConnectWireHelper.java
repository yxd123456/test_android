package com.hz.helper;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.map.A;
import com.baidu.platform.comapi.util.f;
import com.hz.activity.MainActivity;
import com.hz.common.Constans;
import com.hz.entity.Lines;
import com.hz.greendao.dao.MapLineEntity;
import com.hz.greendao.dao.MapLineItemEntity;
import com.hz.greendao.dao.MapPoiEntity;
import com.hz.util.okhttp_extend.FileUtil;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

/**
 * 批量添加点位工具类
 */
public class BatchAddConnectWireHelper {
    public static final int CONNECT_WIRE_NUM = 2;//连线点位数量
    private ArrayList<Marker> tempBatchLineMarkers = new ArrayList<>();
    private Polyline tempBatchLines;
    private int num = 0;
    private ArrayList<String> markerIds;

    private DbUtils dbUtils;
    private boolean flag_test = false;

    public BatchAddConnectWireHelper() {
        Log.d("Test",getClass().getSimpleName()+"被调用了");

    }

    /**
     * 清空批量增加连线的点位
     ***/
    public void clear() {
        num = 0;
        for (Marker marker : tempBatchLineMarkers) {
            if (marker != null) {
                marker.remove();
            }
        }
        tempBatchLineMarkers.clear();
        if (tempBatchLines != null) {
            tempBatchLines.remove();
        }
    }

    public int nextNum() {
        num++;
        return num;
    }

    /**
     * 获取当前有多少个点位
     **/
    public int markerSize() {
        return tempBatchLineMarkers.size();
    }

    /**
     * 当一个点位重复点击时，将移除重复点击的点位
     **/
    public void removeTempMarker(Marker marker) {
        if (tempBatchLineMarkers.indexOf(marker) != -1) {
            tempBatchLineMarkers.remove(marker);
            marker.remove();
        }
    }

    /**
     * 添加一个点位
     **/
    public void addBatchLineMarker(Marker batchLineMarker) {
        tempBatchLineMarkers.add(batchLineMarker);
    }

    /**
     * 将批量点位连线
     **/
    public void generateBatchConnectWireLine(MainActivity mainActivity) {
        if (tempBatchLines != null) {
            tempBatchLines.remove();
        }
        if (tempBatchLineMarkers.size() >= 2) {
            ArrayList<LatLng> latLngList = new ArrayList<>();
            for (Marker marker1 : tempBatchLineMarkers) {
                MapPoiEntity mapPoiEntity = (MapPoiEntity) marker1.getExtraInfo().getSerializable(Constans.POINT_OBJ_KEY);
                if (mapPoiEntity == null) {
                    continue;
                }
                latLngList.add(new LatLng(mapPoiEntity.getPointLatitude(), mapPoiEntity.getPointLongitude()));
            }
            tempBatchLines = mainActivity.addMapPolyline(latLngList, Color.YELLOW);
        }
    }

    /**
     * 获取所有连线的点位ID
     **/
    public ArrayList<String> getMarkerIds() {
        ArrayList<String> pointIds = new ArrayList<>();
        for (Marker marker : tempBatchLineMarkers) {
            MapPoiEntity mapPoiEntity = (MapPoiEntity) marker.getExtraInfo().getSerializable(Constans.POINT_OBJ_KEY);
            if (mapPoiEntity == null) {
                continue;
            }
            pointIds.add(mapPoiEntity.getPointId());
        }
        return pointIds;
    }

    /**
     * 批量增加导线/电缆
     **/
    public void handlerBatchAddLine(Context context, MapLineEntity lineEntity, long currentProjectId, long userId) {

        Log.d("KO", "也许花朵也灭的话");
        markerIds = getMarkerIds();

    /*    if(markerIds.size()!=0) {
            FileUtil.write(context, markerIds);

        }else{
            markerIds = FileUtil.read(context);
        }*/

        Log.d("KO", "我知道花开罚了"+markerIds.size());
        String preId = null;
        for (String id : markerIds) {
            Log.d("KO", "markerIds: "+id);
            if (preId != null) {
                MapPoiEntity strartPoint = DataBaseManagerHelper.getInstance().getPointEntityByPointId(id);
                Log.d("KO", "sP"+strartPoint.getPointName());
                MapPoiEntity endPoint = DataBaseManagerHelper.getInstance().getPointEntityByPointId(preId);
                Log.d("KO", "eP"+endPoint.getPointName());
                handlerBatchAddLineItem(strartPoint, endPoint, lineEntity, currentProjectId, userId);
            }
            preId = id;
        }
    }

    public ArrayList<MapLineEntity> handlerBatchAddLine(Context context, MapLineEntity lineEntity, long currentProjectId, long userId, ArrayList<MapLineEntity> list) {

        for (int i = 0; i < list.size(); i++) {
            MapLineEntity entity = lineEntity;
            /*list.remove(i);
            list.add(entity);*/
            list.set(i, entity);
            Log.d("KO", "接近真相"+entity.getLineName()+" "+list.get(i).getLineName());
            handlerBatchAddLineItem(entity, currentProjectId, userId);
        }

        for (int i = 0; i < list.size(); i++) {
            Log.d("KO", "认识真相"+list.get(i).getLineName());
        }

        return list;
    }

    public void handlerBatchAddLine(MapLineEntity lineEntity, long currentProjectId, long userId) {



        markerIds = getMarkerIds();

        String preId = null;
        for (String id : markerIds) {
            Log.d("KO", "markerIds: "+id);
            if (preId != null) {
                MapPoiEntity strartPoint = DataBaseManagerHelper.getInstance().getPointEntityByPointId(id);
                Log.d("KO", "sP"+strartPoint.getPointName());
                MapPoiEntity endPoint = DataBaseManagerHelper.getInstance().getPointEntityByPointId(preId);
                Log.d("KO", "eP"+endPoint.getPointName());
                handlerBatchAddLineItem(strartPoint, endPoint, lineEntity, currentProjectId, userId);
            }
            preId = id;
        }
    }


    /**
     * 处理批量增加导线/电缆 的单条线
     **/
    private void handlerBatchAddLineItem(MapPoiEntity strartPoint, MapPoiEntity endPoint, MapLineEntity editLineEntity, long currentProjectId, long userId) {
        //1.为批量新增的连线中的所有线段重新商城ID防止ID重复插入失效
        for (MapLineItemEntity lineItem : editLineEntity.getMapLineItemEntityList()) {
            lineItem.setLineItemId(UUID.randomUUID().toString());
        }

        //2.获取两个点之间的已有连线，如果有连线用批量的输入值覆盖已有的数据，如果没有新增连线
        MapLineEntity mapLineEntity = DataBaseManagerHelper.getInstance().getLineByLineStartPointIdAndEndPointId(strartPoint.getPointId(), endPoint.getPointId());
        if (mapLineEntity == null) {
            mapLineEntity = new MapLineEntity();
            mapLineEntity.setLineProjId(currentProjectId);
            mapLineEntity.setLineUserId(userId);
            mapLineEntity.setLineId(UUID.randomUUID().toString());//设置线唯一ID
            //线条关联的开始点结束点属性
            mapLineEntity.setLineStartLatitude(strartPoint.getPointLatitude());
            mapLineEntity.setLineStartLongitude(strartPoint.getPointLongitude());
            mapLineEntity.setLineStartPointId(strartPoint.getPointId());
            mapLineEntity.setLineStartPointName(strartPoint.getPointName());

            mapLineEntity.setLineEndLatitude(endPoint.getPointLatitude());
            mapLineEntity.setLineEndLongitude(endPoint.getPointLongitude());
            mapLineEntity.setLineEndPointId(endPoint.getPointId());
            mapLineEntity.setLineEndPointName(endPoint.getPointName());

            ArrayList<MapLineItemEntity> mapLineItemEntities = new ArrayList<>();
            mapLineItemEntities.addAll(editLineEntity.getMapLineItemEntityList());
            mapLineEntity.setMapLineItemEntityList(mapLineItemEntities);
        } else {
            if (mapLineEntity.getMapLineItemEntityList() == null) {
                mapLineEntity.setMapLineItemEntityList(new ArrayList<MapLineItemEntity>());
            } else {
                for (MapLineItemEntity mapLineItemEntity : mapLineEntity.getMapLineItemEntityList()) {
                    mapLineItemEntity.setLineItemRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_REMOVED);
                }
            }
            mapLineEntity.getMapLineItemEntityList().addAll(editLineEntity.getMapLineItemEntityList());
        }

        mapLineEntity.setLineType(Constans.MapAttributeType.WIRE_ELECTRIC_CABLE);
        mapLineEntity.setLineRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL);
        mapLineEntity.setLineName(editLineEntity.getLineName());
        mapLineEntity.setLineNote(editLineEntity.getLineNote());
        mapLineEntity.setLineSpecificationNumber(editLineEntity.getLineSpecificationNumber());


        LatLng startLatlong = new LatLng(mapLineEntity.getLineStartLatitude(), mapLineEntity.getLineStartLongitude());
        LatLng endLatlong = new LatLng(mapLineEntity.getLineEndLatitude(), mapLineEntity.getLineEndLongitude());
        mapLineEntity.setLineLength(DistanceUtil.getDistance(startLatlong, endLatlong));

        DataBaseManagerHelper.getInstance().addOrUpdateOneLineToDb(mapLineEntity);
    }

    private void handlerBatchAddLineItem(MapLineEntity editLineEntity, long currentProjectId, long userId) {
        //1.为批量新增的连线中的所有线段重新商城ID防止ID重复插入失效
        for (MapLineItemEntity lineItem : editLineEntity.getMapLineItemEntityList()) {
            lineItem.setLineItemId(UUID.randomUUID().toString());
        }

        //2.获取两个点之间的已有连线，如果有连线用批量的输入值覆盖已有的数据，如果没有新增连线
        MapLineEntity mapLineEntity =  editLineEntity;
        if (mapLineEntity == null) {
            mapLineEntity = new MapLineEntity();
            mapLineEntity.setLineProjId(currentProjectId);
            mapLineEntity.setLineUserId(userId);
            mapLineEntity.setLineId(UUID.randomUUID().toString());//设置线唯一ID

            ArrayList<MapLineItemEntity> mapLineItemEntities = new ArrayList<>();
            mapLineItemEntities.addAll(editLineEntity.getMapLineItemEntityList());
            mapLineEntity.setMapLineItemEntityList(mapLineItemEntities);
        } else {
            if (mapLineEntity.getMapLineItemEntityList() == null) {
                mapLineEntity.setMapLineItemEntityList(new ArrayList<MapLineItemEntity>());
            } else {
                for (MapLineItemEntity mapLineItemEntity : mapLineEntity.getMapLineItemEntityList()) {
                    mapLineItemEntity.setLineItemRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_REMOVED);
                }
            }
            mapLineEntity.getMapLineItemEntityList().addAll(editLineEntity.getMapLineItemEntityList());
        }

        mapLineEntity.setLineType(Constans.MapAttributeType.WIRE_ELECTRIC_CABLE);
        mapLineEntity.setLineRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL);
        mapLineEntity.setLineName(editLineEntity.getLineName());
        mapLineEntity.setLineNote(editLineEntity.getLineNote());
        mapLineEntity.setLineSpecificationNumber(editLineEntity.getLineSpecificationNumber());


        LatLng startLatlong = new LatLng(mapLineEntity.getLineStartLatitude(), mapLineEntity.getLineStartLongitude());
        LatLng endLatlong = new LatLng(mapLineEntity.getLineEndLatitude(), mapLineEntity.getLineEndLongitude());
        mapLineEntity.setLineLength(DistanceUtil.getDistance(startLatlong, endLatlong));

        DataBaseManagerHelper.getInstance().addOrUpdateOneLineToDb(mapLineEntity);
    }


    /**
     * 当批量连线时点位被点击的操作
     **/
    public void handleOnBatchLine(MainActivity mainActivity, MapPoiEntity editEntity, Marker marker) {
        if (editEntity.getPointType() == Constans.MapAttributeType.BATCH_LINE_POINT) {
            removeTempMarker(marker);
            generateBatchConnectWireLine(mainActivity);
            return;
        }

        MapPoiEntity batchLinePoi = new MapPoiEntity();
        batchLinePoi.setPointType(Constans.MapAttributeType.BATCH_LINE_POINT);
        batchLinePoi.setPointId(editEntity.getPointId());
        batchLinePoi.setPointLatitude(editEntity.getPointLatitude());
        batchLinePoi.setPointLongitude(editEntity.getPointLongitude());

        Marker batchLineMarker = mainActivity.addMapMarkerWithBundle(
                new LatLng(editEntity.getPointLatitude(), editEntity.getPointLongitude()),
                MapIconHelper.getInstance().generateBitmapIconByNum(nextNum(), Color.WHITE),
                batchLinePoi,
                false
        );
        addBatchLineMarker(batchLineMarker);
        generateBatchConnectWireLine(mainActivity);
    }


}
