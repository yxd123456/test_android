package com.hz.helper;

import android.text.TextUtils;
import android.util.Log;

import com.hz.greendao.dao.MapLineEntity;
import com.hz.greendao.dao.MapPoiEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 找到两个点位之间的连线点位帮助类
 */
public class FoundConnectPointsHelper {

    public FoundConnectPointsHelper() {

        Log.d("Test",getClass().getSimpleName()+"被调用了");

    }

    public static final String TAG = FoundConnectPointsHelper.class.getSimpleName();

    /**
     * 获取两个立杆点位之间有线段连接的点位列表
     *
     * @param pointIdStart     开始点位ID
     * @param pointIdEnd       结束点位ID
     * @param currentProjectId 当前项目ID
     * @param currentUserId    当前用户ID
     **/
    public static List<MapPoiEntity> getLineConnectPointFrom(String pointIdStart, String pointIdEnd, Long currentProjectId, Long currentUserId) {
        List<MapPoiEntity> connectMapPoiEntityList = new ArrayList<>();
        List<String> connectPointIds = new ArrayList<>();
        Log.d(TAG, "getLineConnectPointFrom:  " + pointIdStart + "  " + pointIdEnd);
        //1.获取当前项目中所有的导线/电缆
        List<MapLineEntity> mapLineEntitys = DataBaseManagerHelper.getInstance().getAllElectricCableLine(
                currentProjectId,
                currentUserId
        );


        //3.递归查找两个点之间的连线的路径
        connectPointIds.add(pointIdStart);
        recurrenceToFoundConnectPoints(mapLineEntitys, pointIdStart, null, pointIdEnd, connectPointIds);

        //4.根据连线ID列表获取连线点位对象
        for (String pointId : connectPointIds) {
            MapPoiEntity poiEntity = DataBaseManagerHelper.getInstance().getPointEntityByPointId(pointId);
            Log.d(TAG, "getLineConnectPointFrom: num:" + poiEntity.getPointNum() + "    pointId:" + poiEntity.getPointId());
            connectMapPoiEntityList.add(poiEntity);
        }
        return connectMapPoiEntityList;
    }


    /**
     * 递归遍历两个点
     *
     * @param mapLineEntitys  地图中所有拉线/导线对象
     * @param pointIdStart    开始点位ID
     * @param prePointId      当前点位的上一个点位ID
     * @param pointIdEnd      结束点位ID
     * @param connectPointIds 连线结果点位ID列表
     **/
    private static void recurrenceToFoundConnectPoints(List<MapLineEntity> mapLineEntitys, String pointIdStart, String prePointId, String pointIdEnd, List<String> connectPointIds) {
        List<String> pointTargetPointIds = getStartPointTargetPointIds(mapLineEntitys, pointIdStart, prePointId);
        if (pointTargetPointIds.size() == 0) {
            //当前路线已经到终点清理掉当前线路上的所有点位
            connectPointIds.clear();
            return;
        }
        if (pointTargetPointIds.size() == 1) {//在无分支的时候点位直接加入主干分支
            String pointId = pointTargetPointIds.get(0);
            connectPointIds.add(pointId);
            if (TextUtils.equals(pointId, pointIdEnd)) {
                Log.d(TAG, "recurrenceToFoundConnectPoints: " + connectPointIds);
                return;
            }
            recurrenceToFoundConnectPoints(mapLineEntitys, pointId, pointIdStart, pointIdEnd, connectPointIds);
        } else {//在有多个分支的情况下新建每个分支的点位列表结合并将每个分支点位添加进去，最后
            boolean branchCoundNotConnect = false;
            for (String pointId : pointTargetPointIds) {
                //// 递归结束找到两个点之间连线的所有点
                if (TextUtils.equals(pointId, pointIdEnd)) {
                    connectPointIds.add(pointId);
                    Log.d(TAG, "recurrenceToFoundConnectPoints: " + connectPointIds);
                    return;
                }
                List<String> branchPointIds = new ArrayList<>();
                //将当前点位加入寻找线路点位列表
                branchPointIds.add(pointId);
                recurrenceToFoundConnectPoints(mapLineEntitys, pointId, pointIdStart, pointIdEnd, branchPointIds);
                Log.d(TAG, "recurrenceToFoundConnectPoints: 分支个数： " + branchPointIds);
                if (branchPointIds.size() > 0) {
                    connectPointIds.addAll(branchPointIds);
                    return;
                } else {
                    branchCoundNotConnect = true;
                }
            }
            if (branchCoundNotConnect) {
                connectPointIds.clear();
            }
        }
    }

    /**
     * 获取包含点位ID的所有线
     *
     * @param mapLineEntitys 项目中所有线对象
     * @param pointIdStart   开始点位ID
     * @param prePointId     当前点位的上一个点位ID
     **/
    private static List<String> getStartPointTargetPointIds(List<MapLineEntity> mapLineEntitys, String pointIdStart, String prePointId) {
        List<String> targetLines = new ArrayList<>();
        for (MapLineEntity item : mapLineEntitys) {
            if (prePointId != null) {
                if (TextUtils.equals(item.getLineStartPointId(), prePointId) || TextUtils.equals(item.getLineEndPointId(), prePointId)) {
                    continue;
                }
            }
            if (TextUtils.equals(item.getLineStartPointId(), pointIdStart)) {
                targetLines.add(item.getLineEndPointId());
            }
            if (TextUtils.equals(item.getLineEndPointId(), pointIdStart)) {
                targetLines.add(item.getLineStartPointId());
            }
        }
        return targetLines;
    }
}
