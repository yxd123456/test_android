package com.hz.helper;

import android.text.TextUtils;
import android.util.Log;

import com.hz.MainApplication;
import com.hz.common.Constans;
import com.hz.greendao.dao.MapLineEntity;
import com.hz.greendao.dao.MapLineEntityDao;
import com.hz.greendao.dao.MapLineItemEntity;
import com.hz.greendao.dao.MapLineItemEntityDao;
import com.hz.greendao.dao.MapPoiEntity;
import com.hz.greendao.dao.MapPoiEntityDao;
import com.hz.greendao.dao.PointGalleryEntity;
import com.hz.greendao.dao.PointGalleryEntityDao;
import com.hz.greendao.dao.ProjectEntity;
import com.hz.greendao.dao.ProjectEntityDao;
import com.hz.greendao.dao.WireType;
import com.hz.greendao.dao.WireTypeDao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * 集中管理数据库操作
 */
public class DataBaseManagerHelper {

    public static final String TAG = "DataBaseManagerHelper";
    private MainApplication mainApplication;

    private volatile static DataBaseManagerHelper dataBaseManager;

    private DataBaseManagerHelper() {
        Log.d("Test",getClass().getSimpleName()+"被调用了");

    }

    public static DataBaseManagerHelper getInstance() {
        if (dataBaseManager == null) {
            synchronized (DataBaseManagerHelper.class) {
                if (dataBaseManager == null) {
                    dataBaseManager = new DataBaseManagerHelper();
                }
            }
        }
        return dataBaseManager;
    }

    public void init(MainApplication application) {
        if (mainApplication != null) {
            throw new RuntimeException("HttpManager不能重复初始化");
        }
        this.mainApplication = application;
    }

    /**
     * 根据点位ID获取点位相关的图片数据 不包括删除的点位
     *
     * @param pointId 点位ID
     *                *
     */
    public List<PointGalleryEntity> getPointImagesByPointId(String pointId) {
        PointGalleryEntityDao pointGalleryEntityDao = mainApplication.getDaoSession().getPointGalleryEntityDao();
        QueryBuilder<PointGalleryEntity> pointGalleryEntityQueryBuilder = pointGalleryEntityDao.queryBuilder();
        pointGalleryEntityQueryBuilder.where(PointGalleryEntityDao.Properties.ImgPointId.eq(pointId));
        pointGalleryEntityQueryBuilder.where(PointGalleryEntityDao.Properties.ImgRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));
        return pointGalleryEntityQueryBuilder.list();
    }

    /**
     * 根据点位ID获取点位相关的图片数据 包括删除的数据
     *
     * @param pointId 点位ID
     *                *
     */
    public List<PointGalleryEntity> getPointImagesByPointIdContactRemoved(String pointId) {
        PointGalleryEntityDao pointGalleryEntityDao = mainApplication.getDaoSession().getPointGalleryEntityDao();
        QueryBuilder<PointGalleryEntity> pointGalleryEntityQueryBuilder = pointGalleryEntityDao.queryBuilder();
        pointGalleryEntityQueryBuilder.where(PointGalleryEntityDao.Properties.ImgPointId.eq(pointId));
        return pointGalleryEntityQueryBuilder.list();
    }

    /**
     * 根据项目ID获取项目ID相关线信息 不包含移除的点位
     *
     * @param currentProjectId 项目ID
     *                         *
     */
    public List<MapLineEntity> getAlllinesByProjectId(long currentProjectId) {
        List<MapLineEntity> tempLineEntityList = new ArrayList<>();//地图线集合
        MapLineEntityDao mapLineEntityDao = mainApplication.getDaoSession().getMapLineEntityDao();
        QueryBuilder<MapLineEntity> lineEntityQueryBuilder = mapLineEntityDao.queryBuilder();
        lineEntityQueryBuilder.where(MapLineEntityDao.Properties.LineProjId.eq(currentProjectId));
        lineEntityQueryBuilder.where(MapLineEntityDao.Properties.LineRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));
        tempLineEntityList.addAll(lineEntityQueryBuilder.list());
        return tempLineEntityList;
    }

    /**
     * 根据项目ID获取项目ID相关线信息 包含已经被移除的点位
     *
     * @param currentProjectId 项目ID
     *                         *
     */
    public List<MapLineEntity> getAlllinesByProjectIdContactRemoved(long currentProjectId) {
        List<MapLineEntity> tempLineEntityList = new ArrayList<>();//地图线集合
        MapLineEntityDao mapLineEntityDao = mainApplication.getDaoSession().getMapLineEntityDao();
        QueryBuilder<MapLineEntity> lineEntityQueryBuilder = mapLineEntityDao.queryBuilder();
        lineEntityQueryBuilder.where(MapLineEntityDao.Properties.LineProjId.eq(currentProjectId));
        tempLineEntityList.addAll(lineEntityQueryBuilder.list());
        return tempLineEntityList;
    }


    /**
     * 获取数据库中所有的导线/电缆
     *
     * @param currentProjectId 项目ID
     * @param userId           用户ID
     */
    public List<MapLineEntity> getAllElectricCableLine(long currentProjectId, long userId) {
        List<MapLineEntity> tempLineEntityList = new ArrayList<>();//地图线集合
        MapLineEntityDao mapLineEntityDao = mainApplication.getDaoSession().getMapLineEntityDao();
        QueryBuilder<MapLineEntity> lineEntityQueryBuilder = mapLineEntityDao.queryBuilder();
        lineEntityQueryBuilder.where(MapLineEntityDao.Properties.LineProjId.eq(currentProjectId));
        lineEntityQueryBuilder.where(MapLineEntityDao.Properties.LineUserId.eq(userId));
        lineEntityQueryBuilder.where(MapLineEntityDao.Properties.LineRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));
        lineEntityQueryBuilder.where(MapLineEntityDao.Properties.LineType.eq(Constans.MapAttributeType.WIRE_ELECTRIC_CABLE));
        tempLineEntityList.addAll(lineEntityQueryBuilder.list());
        return tempLineEntityList;
    }

    /**
     * 根据项目ID获取所有项目的地图点位 不包括已经删除的点位
     *
     * @param currentProjectId 项目ID
     *                         *
     */
    public List<MapPoiEntity> getAllPointByProjectId(long currentProjectId) {
        List<MapPoiEntity> tempPoiEntityList = new ArrayList<>();//地图点位集合
        //根据项目ID获取项目ID相关点位信息
        MapPoiEntityDao mapPoiEntityDao = mainApplication.getDaoSession().getMapPoiEntityDao();
        QueryBuilder<MapPoiEntity> queryBuilder = mapPoiEntityDao.queryBuilder();
        queryBuilder.where(MapPoiEntityDao.Properties.PointProjId.eq(currentProjectId));
        queryBuilder.where(MapPoiEntityDao.Properties.PointRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));
        tempPoiEntityList.addAll(queryBuilder.list());
        return tempPoiEntityList;
    }

    /**
     * 根据项目ID获取所有项目的地图点位 包括已经删除的点位
     *
     * @param currentProjectId 项目ID
     * @param userId           用户ID                       *
     */
    public List<MapPoiEntity> getAllPointByProjectIdContactRemoved(long currentProjectId, long userId) {
        List<MapPoiEntity> tempPoiEntityList = new ArrayList<>();//地图点位集合
        //根据项目ID获取项目ID相关点位信息
        MapPoiEntityDao mapPoiEntityDao = mainApplication.getDaoSession().getMapPoiEntityDao();
        QueryBuilder<MapPoiEntity> queryBuilder = mapPoiEntityDao.queryBuilder();
        queryBuilder.where(MapPoiEntityDao.Properties.PointProjId.eq(currentProjectId));
        queryBuilder.where(MapPoiEntityDao.Properties.PointUserId.eq(userId));
        tempPoiEntityList.addAll(queryBuilder.list());
        return tempPoiEntityList;
    }

    /**
     * 获取点位对象根据点位的ID
     *
     * @param pointId 点位ID
     *                *
     */
    public MapPoiEntity getPointEntityByPointId(String pointId) {
        MapPoiEntityDao mapPoiEntityDao = mainApplication.getDaoSession().getMapPoiEntityDao();
        QueryBuilder<MapPoiEntity> queryBuilder = mapPoiEntityDao.queryBuilder();
        queryBuilder.where(MapPoiEntityDao.Properties.PointId.eq(pointId));
        queryBuilder.where(MapPoiEntityDao.Properties.PointRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));
        return queryBuilder.unique();
    }

    /**
     * 将点位信息和点位图片从数据库移除
     *
     * @param pointId 点位ID
     *                *
     */
    public void removePointsAndTargetGalleryFromDb(String pointId) {
        if (TextUtils.isEmpty(pointId)) {//修改点位的删除
            return;
        }

        //1.移除点位信息
        MapPoiEntityDao mapPoiEntityDao = mainApplication.getDaoSession().getMapPoiEntityDao();
        MapPoiEntity poiEntity = mapPoiEntityDao.queryBuilder().where(MapPoiEntityDao.Properties.PointId.eq(pointId)).unique();
        if (poiEntity == null) {
            return;
        }
        poiEntity.setPointRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_REMOVED);
        poiEntity.setPointNeedToUpload(true);
        poiEntity.setPointUpdateDate(new Date());
        mapPoiEntityDao.update(poiEntity);

        //2.移除点位图片信息
        PointGalleryEntityDao pointGalleryEntityDao = mainApplication.getDaoSession().getPointGalleryEntityDao();
        List<PointGalleryEntity> galleryEntityList = pointGalleryEntityDao.queryBuilder().where(PointGalleryEntityDao.Properties.ImgPointId.eq(pointId)).list();
        if (galleryEntityList == null || galleryEntityList.size() < 0) {
            return;
        }
        for (PointGalleryEntity entity : galleryEntityList) {
            entity.setImgRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_REMOVED);
            entity.setImgNeedToUpload(true);
            entity.setImgUploadProgress(0);
        }
        pointGalleryEntityDao.updateInTx(galleryEntityList);
    }

    /**
     * 将点位信息保存到数据库
     *
     * @param entity 需要保存到数据库的实体
     *               *
     */
    public void addOrUpdateOnePointsToDb(MapPoiEntity entity) {
        //设置带保存到数据库对象数据
        entity.setPointRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL);
        entity.setPointNeedToUpload(true);
        entity.setPointUpdateDate(new Date());

        //保存或修改点位数据到数据库
        MapPoiEntityDao mapPoiEntityDao = mainApplication.getDaoSession().getMapPoiEntityDao();
        mapPoiEntityDao.insertOrReplaceInTx(entity);

        //保存点位关联的图片信息
        List<PointGalleryEntity> galleryLists = entity.getPointGalleryLists();
        if (galleryLists == null || galleryLists.size() < 0) {
            return;
        }
        //保存或修改点位数据到数据库
        for (PointGalleryEntity pge : galleryLists) {
            pge.setImgPointId(entity.getPointId());
            pge.setImgRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL);
            pge.setImgNeedToUpload(true);
            pge.setImgUploadProgress(0);
        }
        PointGalleryEntityDao projectGalleryEntityDao = mainApplication.getDaoSession().getPointGalleryEntityDao();
        projectGalleryEntityDao.insertOrReplaceInTx(galleryLists);

    }


    /**
     * 将线信息保存到数据库
     *
     * @param mapLineEntity 待保存到数据库的线对象
     *                      *
     */
    public void addOrUpdateOneLineToDb(MapLineEntity mapLineEntity) {
        mapLineEntity.setLineRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL);
        mapLineEntity.setLineNeedToUpload(true);

        MapLineEntityDao mapLineEntityDao = mainApplication.getDaoSession().getMapLineEntityDao();
        mapLineEntityDao.insertOrReplaceInTx(mapLineEntity);

        List<MapLineItemEntity> itemEntityList = mapLineEntity.getMapLineItemEntityList();
        if (itemEntityList == null || itemEntityList.size() <= 0) {
            return;
        }
        for (MapLineItemEntity mapLineItemEntity : itemEntityList) {
            mapLineItemEntity.setLineItemLineId(mapLineEntity.getLineId());
        }

        MapLineItemEntityDao mapLineItemEntityDao = mainApplication.getDaoSession().getMapLineItemEntityDao();
        mapLineItemEntityDao.insertOrReplaceInTx(itemEntityList);
    }


    /**
     * 修改点位关联的点位名称，和开始结束点位经纬度
     *
     * @param pointId        点位ID
     * @param pointName      点位名称
     * @param pointLatitude  点位经度
     * @param pointLongitude 点位纬度
     *                       *
     */
    public void updatePointTargetLineNameAndLatitude(String pointId, String pointName, Double pointLatitude, Double pointLongitude) {
        MapLineEntityDao mapLineEntityDao = mainApplication.getDaoSession().getMapLineEntityDao();
        //修改点位关联的开始点位名称和经纬度
        QueryBuilder<MapLineEntity> startPointTargetLineBuilder = mapLineEntityDao.queryBuilder();
        startPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));//没有被删除
        startPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineType.eq(Constans.MapAttributeType.WIRE_ELECTRIC_CABLE));//导线电缆
        startPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineStartPointId.eq(pointId));
        List<MapLineEntity> startPointTargetLineEntity = startPointTargetLineBuilder.list();
        if (startPointTargetLineEntity != null && startPointTargetLineEntity.size() > 0) {
            for (int i = 0; i < startPointTargetLineEntity.size(); i++) {
                MapLineEntity lineEntity = startPointTargetLineEntity.get(i);
                lineEntity.setLineStartPointName(pointName);
                lineEntity.setLineStartLatitude(pointLatitude);
                lineEntity.setLineStartLongitude(pointLongitude);
                lineEntity.setLineNeedToUpload(true);
            }
            mapLineEntityDao.updateInTx(startPointTargetLineEntity);
        }


        //修改点位关联的结束点位的名称和经纬度
        QueryBuilder<MapLineEntity> endPointTargetLineBuilder = mapLineEntityDao.queryBuilder();
        endPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));//没有被删除
        endPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineType.eq(Constans.MapAttributeType.WIRE_ELECTRIC_CABLE));//导线电缆
        endPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineEndPointId.eq(pointId));
        List<MapLineEntity> endPointTargetLineEntity = endPointTargetLineBuilder.list();
        if (endPointTargetLineEntity != null && endPointTargetLineEntity.size() > 0) {
            for (int i = 0; i < endPointTargetLineEntity.size(); i++) {
                MapLineEntity lineEntity = endPointTargetLineEntity.get(i);
                lineEntity.setLineEndPointName(pointName);
                lineEntity.setLineEndLatitude(pointLatitude);
                lineEntity.setLineEndLongitude(pointLongitude);
                lineEntity.setLineNeedToUpload(true);
            }
            mapLineEntityDao.updateInTx(endPointTargetLineEntity);
        }
    }

    /**
     * 根据点位ID删除点位关联的线/电缆信息
     *
     * @param pointId 点位ID
     *                *
     */
    public void removePointTargetLines(String pointId) {
        if (TextUtils.isEmpty(pointId)) {//修改线的删除
            return;
        }
        MapLineEntityDao mapLineEntityDao = mainApplication.getDaoSession().getMapLineEntityDao();
        //修改点位关联的开始点位名称和经纬度
        QueryBuilder<MapLineEntity> startPointTargetLineBuilder = mapLineEntityDao.queryBuilder();
        startPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));//没有被删除
        startPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineType.eq(Constans.MapAttributeType.WIRE_ELECTRIC_CABLE));//导线电缆
        startPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineStartPointId.eq(pointId));
        List<MapLineEntity> startPointTargetLineEntity = startPointTargetLineBuilder.list();
        if (startPointTargetLineEntity != null && startPointTargetLineEntity.size() > 0) {
            for (int i = 0; i < startPointTargetLineEntity.size(); i++) {
                MapLineEntity lineEntity = startPointTargetLineEntity.get(i);
                lineEntity.setLineRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_REMOVED);
                lineEntity.setLineNeedToUpload(true);
            }
            mapLineEntityDao.updateInTx(startPointTargetLineEntity);
        }


        //修改点位关联的结束点位的名称和经纬度
        QueryBuilder<MapLineEntity> endPointTargetLineBuilder = mapLineEntityDao.queryBuilder();
        endPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));//没有被删除
        endPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineType.eq(Constans.MapAttributeType.WIRE_ELECTRIC_CABLE));//导线电缆
        endPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineEndPointId.eq(pointId));
        List<MapLineEntity> endPointTargetLineEntity = endPointTargetLineBuilder.list();
        if (endPointTargetLineEntity != null && endPointTargetLineEntity.size() > 0) {
            for (int i = 0; i < endPointTargetLineEntity.size(); i++) {
                MapLineEntity lineEntity = endPointTargetLineEntity.get(i);
                lineEntity.setLineRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_REMOVED);
                lineEntity.setLineNeedToUpload(true);
            }
            mapLineEntityDao.updateInTx(endPointTargetLineEntity);
        }
    }

    /**
     * 根据开始点和结束点点位ID查找有没有重复的线
     *
     * @param lineStartPointId 开始点位ID
     * @param lineEndPointId   结束点位ID
     *                         *
     */
    public MapLineEntity getLineByLineStartPointIdAndEndPointId(String lineStartPointId, String lineEndPointId) {
        MapLineEntityDao mapLineEntityDao = mainApplication.getDaoSession().getMapLineEntityDao();
        QueryBuilder<MapLineEntity> startPointTargetLineBuilder = mapLineEntityDao.queryBuilder();
        startPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));//没有被删除
        startPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineType.eq(Constans.MapAttributeType.WIRE_ELECTRIC_CABLE));//导线电缆
        startPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineStartPointId.eq(lineStartPointId));
        startPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineEndPointId.eq(lineEndPointId));
        MapLineEntity startPointTargetLineEntity = startPointTargetLineBuilder.unique();
        if (startPointTargetLineEntity != null) {
            return startPointTargetLineEntity;
        }

        QueryBuilder<MapLineEntity> endPointTargetLineBuilder = mapLineEntityDao.queryBuilder();
        endPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));//没有被删除
        endPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineType.eq(Constans.MapAttributeType.WIRE_ELECTRIC_CABLE));//导线电缆
        endPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineStartPointId.eq(lineEndPointId));
        endPointTargetLineBuilder.where(MapLineEntityDao.Properties.LineEndPointId.eq(lineStartPointId));
        MapLineEntity endPointTargetLineEntity = endPointTargetLineBuilder.unique();
        if (endPointTargetLineEntity != null) {
            return endPointTargetLineEntity;
        }
        return null;
    }

    /**
     * 根据项目ID 获取项目的所有点位
     *
     * @param projId 项目ID
     * @param userId 用户ID
     */
    public long getPointCoundByPointProjIdContactRemoved(long userId, long projId) {
        MapPoiEntityDao mapPoiEntityDao = mainApplication.getDaoSession().getMapPoiEntityDao();
        QueryBuilder<MapPoiEntity> queryBuilder = mapPoiEntityDao.queryBuilder();
        queryBuilder.where(MapPoiEntityDao.Properties.PointProjId.eq(projId));
        queryBuilder.where(MapPoiEntityDao.Properties.PointUserId.eq(userId));
        return queryBuilder.count();
    }

    /**
     * 获取所有的线
     *
     * @param currentProjId 项目ID
     *                      *
     */
    public List<MapLineEntity> getAllLinesByProjId(long currentProjId) {
        QueryBuilder<MapLineEntity> mapLineEntityQueryBuilder = mainApplication.getDaoSession().getMapLineEntityDao().queryBuilder();
        mapLineEntityQueryBuilder.where(MapLineEntityDao.Properties.LineProjId.eq(currentProjId));
        mapLineEntityQueryBuilder.where(MapLineEntityDao.Properties.LineRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));
        mapLineEntityQueryBuilder.orderAsc(MapLineEntityDao.Properties.LineName);
        return mapLineEntityQueryBuilder.list();
    }

    /**
     * 根据线ID移除线
     *
     * @param lineId 线ID
     *               *
     */
    public void removeLineByLineId(String lineId) {
        if (TextUtils.isEmpty(lineId)) {
            return;
        }
        QueryBuilder<MapLineEntity> mapLineEntityQueryBuilder = mainApplication.getDaoSession().getMapLineEntityDao().queryBuilder();
        mapLineEntityQueryBuilder.where(MapLineEntityDao.Properties.LineId.eq(lineId));
        MapLineEntity mapLineEntity = mapLineEntityQueryBuilder.unique();
        if (mapLineEntity == null) {
            return;
        }
        mapLineEntity.setLineRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_REMOVED);
        mapLineEntity.setLineNeedToUpload(true);
        mainApplication.getDaoSession().getMapLineEntityDao().updateInTx(mapLineEntity);

        List<MapLineItemEntity> itemEntityList = mapLineEntity.getMapLineItemEntityList();
        if (itemEntityList == null || itemEntityList.size() < 0) {
            return;
        }
        for (MapLineItemEntity itemEntity : itemEntityList) {
            itemEntity.setLineItemRemoved(Constans.RemoveIdentified.REMOVE_IDENTIFIED_REMOVED);
        }
        mainApplication.getDaoSession().getMapLineItemEntityDao().updateInTx(itemEntityList);
    }

    /**
     * 获取同一类型的最近一个数据
     *
     * @param pointType             点位类型
     * @param currentProjectId      点位项目ID
     * @param userIdFromPreferences 点位用户ID
     */
    public MapPoiEntity getpointEntityByPointType(Integer pointType, long currentProjectId, long userIdFromPreferences) {
        MapPoiEntityDao mapPoiEntityDao = mainApplication.getDaoSession().getMapPoiEntityDao();
        QueryBuilder<MapPoiEntity> mapPoiEntityQueryBuilder = mapPoiEntityDao.queryBuilder();
        mapPoiEntityQueryBuilder.where(MapPoiEntityDao.Properties.PointType.eq(pointType));
        mapPoiEntityQueryBuilder.where(MapPoiEntityDao.Properties.PointProjId.eq(currentProjectId));
        mapPoiEntityQueryBuilder.where(MapPoiEntityDao.Properties.PointUserId.eq(userIdFromPreferences));
        mapPoiEntityQueryBuilder.where(MapPoiEntityDao.Properties.PointRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));
        mapPoiEntityQueryBuilder.orderDesc(MapPoiEntityDao.Properties.PointUpdateDate);//根据修改时间排序 获取最新的数据
        List<MapPoiEntity> mapPoiEntityList = mapPoiEntityQueryBuilder.limit(1).list();
        return (mapPoiEntityList != null && !mapPoiEntityList.isEmpty()) ? mapPoiEntityList.get(0) : null;
    }

    /**
     * 获取一个项目中的所有图片信息
     *
     * @param projId 项目ID
     * @param userId 用户ID
     */
    public List<PointGalleryEntity> getAllProjectImagesByProjectId(long projId, long userId) {
        List<PointGalleryEntity> pointGalleryEntityList = new ArrayList<>();
        List<MapPoiEntity> mapPoiEntityList = getAllPointByProjectIdContactRemoved(projId, userId);
        for (MapPoiEntity poiEntity : mapPoiEntityList) {
            pointGalleryEntityList.addAll(getPointImagesByPointIdContactRemoved(poiEntity.getPointId()));
        }
        return pointGalleryEntityList;
    }

    /**
     * 获取一个项目中的点位个数
     *
     * @param projId 项目ID
     * @param userId 用户ID
     */
    public long getProjectPointCountByProjectId(long projId, long userId) {
        MapPoiEntityDao mapPoiEntityDao = mainApplication.getDaoSession().getMapPoiEntityDao();
        QueryBuilder<MapPoiEntity> mapPoiEntityQueryBuilder = mapPoiEntityDao.queryBuilder();
        mapPoiEntityQueryBuilder.where(MapPoiEntityDao.Properties.PointProjId.eq(projId));
        mapPoiEntityQueryBuilder.where(MapPoiEntityDao.Properties.PointUserId.eq(userId));
        return mapPoiEntityQueryBuilder.count();
    }

    /**
     * 获取一个项目中的线个数
     *
     * @param projId 项目ID
     * @param userId 用户ID
     */
    public long getProjectLineCountByProjectId(long projId, long userId) {
        MapLineEntityDao mapPoiEntityDao = mainApplication.getDaoSession().getMapLineEntityDao();
        QueryBuilder<MapLineEntity> mapPoiEntityQueryBuilder = mapPoiEntityDao.queryBuilder();
        mapPoiEntityQueryBuilder.where(MapLineEntityDao.Properties.LineProjId.eq(projId));
        mapPoiEntityQueryBuilder.where(MapLineEntityDao.Properties.LineUserId.eq(userId));
        return mapPoiEntityQueryBuilder.count();
    }

    /**
     * 更新图片的上传状态为已经上传
     *
     * @param mCurrentUploadImageId 图片ID
     **/
    public void updatePointTargetImageUploadStatusByImageId(String mCurrentUploadImageId) {
        if (TextUtils.isEmpty(mCurrentUploadImageId)) {
            return;
        }
        PointGalleryEntityDao pointGalleryEntityDao = mainApplication.getDaoSession().getPointGalleryEntityDao();
        QueryBuilder<PointGalleryEntity> pointGalleryEntityQueryBuilder = pointGalleryEntityDao.queryBuilder();
        pointGalleryEntityQueryBuilder.where(PointGalleryEntityDao.Properties.ImgId.eq(mCurrentUploadImageId));
        List<PointGalleryEntity> pointGalleryEntityList = pointGalleryEntityQueryBuilder.limit(1).list();
        if (pointGalleryEntityList != null && pointGalleryEntityList.size() > 0) {
            for (PointGalleryEntity pointGalleryEntity : pointGalleryEntityList) {
                pointGalleryEntity.setImgUploadProgress(100);
                pointGalleryEntity.setImgNeedToUpload(false);
            }
            pointGalleryEntityDao.updateInTx(pointGalleryEntityList);
        }
    }

    /**
     * 保存项目数据到数据库，在没有网络的时候使用
     **/
    public void saveProjectDataToDb(List<ProjectEntity> data) {
        if (data != null && data.size() >= 0) {
            //清空原有数据
            ProjectEntityDao projectEntityDao = mainApplication.getDaoSession().getProjectEntityDao();
            projectEntityDao.deleteAll();

            //保存新数据
            projectEntityDao.insertInTx(data);
        }
    }

    /**
     * 获取项目列表
     **/
    public List<ProjectEntity> getProjectList() {
        List<ProjectEntity> projectEntityListReturn = new ArrayList<>();
        ProjectEntityDao projectEntityDao = mainApplication.getDaoSession().getProjectEntityDao();
        List<ProjectEntity> projectEntityList = projectEntityDao.queryBuilder().list();
        if (projectEntityList != null && projectEntityList.size() > 0) {
            projectEntityListReturn.addAll(projectEntityList);
        }
        return projectEntityListReturn;
    }

    /**
     * 根据wireTypeId 获取wireTypeName
     **/
    public String getLineWireTypeNameById(String lineWireTypeId) {
        QueryBuilder<WireType> wireTypeQueryBuilder = mainApplication.getDaoSession().getWireTypeDao().queryBuilder();
        List<WireType> lineEntities = wireTypeQueryBuilder.where(WireTypeDao.Properties.Id.eq(lineWireTypeId)).limit(1).list();

        if (lineEntities != null && lineEntities.size() >= 1) {
            return lineEntities.get(0).getCrossingLineName();
        }
        return "";
    }

    /***
     * 更新跨越线 开始点/结束点 经纬度
     **/
    public void updateCrossLinePointLocation(String lineId, int pointType, double dragEndLatitude, double dragEndLongitude, long projId, long userId) {
        MapLineEntityDao mapLineEntityDao = mainApplication.getDaoSession().getMapLineEntityDao();
        //1.获取线数据中所有开始点经纬度和传入数据相同的 更新相应跨越线经纬度
        QueryBuilder<MapLineEntity> crossLineStartQueryBuilder = mapLineEntityDao.queryBuilder();
        crossLineStartQueryBuilder.where(MapLineEntityDao.Properties.LineRemoved.eq(Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL));
        crossLineStartQueryBuilder.where(MapLineEntityDao.Properties.LineProjId.eq(projId));
        crossLineStartQueryBuilder.where(MapLineEntityDao.Properties.LineUserId.eq(userId));
        crossLineStartQueryBuilder.where(MapLineEntityDao.Properties.LineId.eq(lineId));
        List<MapLineEntity> crossLineStartEntity = crossLineStartQueryBuilder.list();
        if (crossLineStartEntity == null) {
            return;
        }
        if (crossLineStartEntity.size() <= 0) {
            return;
        }
        for (MapLineEntity mapLineEntity : crossLineStartEntity) {
            if (pointType == Constans.MapAttributeType.CROSS_LINE_START_POINT) {
                mapLineEntity.setLineStartLatitude(dragEndLatitude);
                mapLineEntity.setLineStartLongitude(dragEndLongitude);
            } else if (pointType == Constans.MapAttributeType.CROSS_LINE_END_POINT) {
                mapLineEntity.setLineEndLatitude(dragEndLatitude);
                mapLineEntity.setLineEndLongitude(dragEndLongitude);
            }
        }
        mapLineEntityDao.updateInTx(crossLineStartEntity);
    }

    /**
     * 持久化批量修改的点位信息到数据库
     *
     * @param mapPoiEntityList 批量修改的点位集合
     **/
    public void updateConnectPoints(List<MapPoiEntity> mapPoiEntityList) {
        MapPoiEntityDao mapPoiEntityDao = mainApplication.getDaoSession().getMapPoiEntityDao();
        mapPoiEntityDao.updateInTx(mapPoiEntityList);
    }

    /**
     * 更新连线关联线信息
     *
     * @param mapLineEntityList 批量修改的线信息
     **/
    public void updateConnectLines(List<MapLineEntity> mapLineEntityList) {
        MapLineEntityDao mapLineEntityDao = mainApplication.getDaoSession().getMapLineEntityDao();
        mapLineEntityDao.updateInTx(mapLineEntityList);
    }

    /**
     * 更新连线的每条线关联的导线电缆
     **/
    public void updateInsertConnectLineItems(List<MapLineItemEntity> lineItemListOld) {
        MapLineItemEntityDao mapLineItemEntityDao = mainApplication.getDaoSession().getMapLineItemEntityDao();
        mapLineItemEntityDao.insertOrReplaceInTx(lineItemListOld);
    }
}
