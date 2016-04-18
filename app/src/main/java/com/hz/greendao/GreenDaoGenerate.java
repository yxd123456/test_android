package com.hz.greendao;

import java.io.File;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

/**
 * 生成数据库操作类
 */
public class GreenDaoGenerate {

    public static final String PACKAGE_DIR = "com.hz.greendao.dao";
    public static final String CLASS_DIR = "app" + File.separator + "src" + File.separator + "main" + File.separator + "java";
    public static final String PROJECT_DIR = System.getProperty("user.dir") + File.separator + CLASS_DIR;
    public static final int VERSION = 3;


    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(VERSION, PACKAGE_DIR);
        schema.enableKeepSectionsByDefault();

        addWireTypeEntity(schema);//**跨越线类型**//
        addTransformerEntity(schema);//**变压器容量**//
        addGeologicalconditionTypeEntity(schema);//**地质情况**//

        addTowerTypeEntity(schema);//**杆塔形式**//
        addEquimentInstallEntity(schema);//**设备安装**//
        addElectricPoleEntity(schema);//**电杆型号**//
        addConductorWireEntity(schema);//**导线,电缆**//

        addProjectEntity(schema);//**项目列表**//
        addMapPoiEntity(schema);//**项目点位列表**//
        addMapLineEntity(schema);//**项目线列表**//
        addProjectGalleryEntity(schema);//**项目相册列表**//

        new DaoGenerator().generateAll(schema, PROJECT_DIR);
    }


    /**
     * 项目相册列表*
     */
    private static void addProjectGalleryEntity(Schema schema) {
        Entity note = schema.addEntity("PointGalleryEntity");
        note.implementsSerializable();
        note.addStringProperty("imgId").primaryKey().index().notNull();
        note.addStringProperty("imgPointId").index().notNull();
        note.addStringProperty("imgFrom");
        note.addStringProperty("imgAddress");
        note.addIntProperty("imgUploadProgress").index().notNull();//图片上传进度
        note.addIntProperty("imgRemoved").index().notNull();//@see RemoveIdentified
        note.addBooleanProperty("imgNeedToUpload").index().notNull();//true 需要  false 不需要
    }

    /**
     * 项目点位列表*
     */
    private static void addMapPoiEntity(Schema schema) {
        Entity note = schema.addEntity("MapPoiEntity");
        note.implementsSerializable();
        //点位（立杆 ，变压箱，户表，电缆井，箱式开关站，开闭所，箱式变压站）公用属性
        note.addStringProperty("pointId").primaryKey().index().notNull();
        note.addLongProperty("pointProjId").index().notNull();
        note.addLongProperty("pointUserId").index().notNull();
        note.addIntProperty("pointEditType").index().notNull();//@see AttributeEditType
        note.addIntProperty("pointType").index().notNull(); //@see MapAttributeType
        note.addDoubleProperty("pointLatitude").index().notNull();
        note.addDoubleProperty("pointLongitude").index().notNull();
        note.addStringProperty("pointName");
        note.addStringProperty("pointNote");
        note.addIntProperty("pointRemoved").index().notNull();//@see RemoveIdentified
        note.addBooleanProperty("pointNeedToUpload").index().notNull();//true 需要  false 不需要
        note.addDateProperty("pointUpdateDate").index().notNull();//点位修改时间 在新增点位时自动补充三个点位数据

        //户表专有属性
        note.addIntProperty("pointLightingNum").index().notNull();//照明表
        note.addIntProperty("pointPowerNum").index().notNull();//动力表
        note.addIntProperty("pointConnectDoorNum").index().notNull();//接户线长数

        //变压箱/器 立杆 专有属性
        note.addStringProperty("pointTransformerTypeId");//变压器容量

        //点位状态 @see AttributeStatus 0:新   1:旧  2:拔立  需要此属性的点位类型包括 ： 立杆 变压箱 电缆井，箱式开关站，开闭所，箱变
        note.addIntProperty("pointStatus").index().notNull();//点位状态

        //立杆专有属性
        note.addStringProperty("pointTowerTypeId");//杆塔型号
        note.addStringProperty("pointGeologicalConditionsTypeId");//地质情况
        note.addStringProperty("pointEquipmentInstallationTypeId");//设备安装
        note.addStringProperty("pointElectricPoleTypeId");//电杆型号
        note.addIntProperty("pointElectricPoleTypeCount").index().notNull();//电杆数量
        note.addStringProperty("pointLandForm");//地形情况
        note.addStringProperty("pointNum");//杆号 eg:#12
        note.addDoubleProperty("pointCoverDepth");//杆埋深   红色框里的值/10+0.7
    }

    /**
     * 项目线列表*
     */
    private static void addMapLineEntity(Schema schema) {
        // 拉线（导线/电缆） 和  导线电缆是一对多的关系
        Entity item = schema.addEntity("MapLineItemEntity");//两个点位之间的多条导线或者电缆
        item.implementsSerializable();
        item.addStringProperty("lineItemId").primaryKey().index().notNull();
        item.addStringProperty("lineItemModeId").index(); //线型 @see ConductorWireEntity  下拉选择
        item.addIntProperty("lineItemWireType").index().notNull();//导线拉线类型  @see LineWireType    （导线或者拉线）
        item.addIntProperty("lineItemNum").index().notNull();//线条数*/
        item.addIntProperty("lineItemStatus").index().notNull();//0:新  1：旧   @see AttributeStatus
        item.addIntProperty("lineItemRemoved").index().notNull();// @see RemoveIdentified
        Property lineItemLineId = item.addStringProperty("LineItemLineId").notNull().index().getProperty();//外键指向线表


        //线    两点之间包含多个导线和电缆
        Entity note = schema.addEntity("MapLineEntity");
        note.implementsSerializable();
        //跨越线，导线/电缆 公用属性
        note.addStringProperty("lineId").primaryKey().index().notNull();
        note.addLongProperty("lineProjId").index().notNull();
        note.addLongProperty("lineUserId").index().notNull();
        note.addIntProperty("lineEditType").index().notNull();//新增，修改，删除 @see AttributeEditType
        note.addIntProperty("lineType").index().notNull();//线类型 跨越线，导线/电缆 @see MapAttributeType
        note.addStringProperty("lineName");
        note.addStringProperty("lineNote");
        note.addIntProperty("lineRemoved").index().notNull();// @see RemoveIdentified
        note.addBooleanProperty("lineNeedToUpload").index().notNull();//true 需要  false 不需要
        note.addDoubleProperty("lineStartLatitude").index().notNull();//开始结束点经纬度
        note.addDoubleProperty("lineStartLongitude").index().notNull();//开始结束点经纬度
        note.addDoubleProperty("lineEndLatitude").index().notNull();//开始结束点经纬度
        note.addDoubleProperty("lineEndLongitude").index().notNull();//开始结束点经纬度

        //跨越线专有属性
        note.addStringProperty("lineWireTypeId");//跨越线属性  跨越线类型

        //导线/电缆专有属性
        note.addStringProperty("lineStartPointId");//开始点ID
        note.addStringProperty("lineEndPointId");//结束点ID
        note.addStringProperty("lineStartPointName");//开始点名称 用于快速访问
        note.addStringProperty("lineEndPointName");//结束点名称 用于快速访问
        note.addDoubleProperty("lineLength");//导线/电缆 长度
        note.addIntProperty("lineSpecificationNumber").index().notNull();//规格线数 >1

        note.addToMany(item, lineItemLineId);
    }

    /**
     * 项目列表
     * *
     */
    private static void addProjectEntity(Schema schema) {
        Entity note = schema.addEntity("ProjectEntity");
        note.implementsSerializable();
        note.addIdProperty().primaryKey().index().notNull();
        note.addStringProperty("projectName");
        note.addStringProperty("programmeName");
        note.addIntProperty("projectLeader");
        note.addStringProperty("cjsj");
        note.addIntProperty("picId");
        note.addIntProperty("scbz");
        note.addIntProperty("projectCreator");
        note.addStringProperty("status");
        note.addStringProperty("projectNum");

        //项目材料库
        note.addIntProperty("voltageType");
        note.addIntProperty("terrainId");
        note.addIntProperty("belongId");

        //项目材料
        note.addIntProperty("areaType");
        note.addIntProperty("areaId");
    }

    /**
     * 杆塔形式*
     */
    private static void addTowerTypeEntity(Schema schema) {
        Entity note = schema.addEntity("TowerType");
        note.implementsSerializable();
        note.addIdProperty().primaryKey().index().notNull();
        note.addStringProperty("barNameEn");
        note.addStringProperty("towerMaterialType");
        note.addIntProperty("scbz");
        note.addDateProperty("cjsj");
        note.addDateProperty("updateDate");
        //项目归属依据
        note.addIntProperty("voltageType");
        note.addIntProperty("terrainId");
        note.addIntProperty("belongId");
    }


    /**
     * 拉线类型*
     */
    private static void addWireTypeEntity(Schema schema) {
        Entity note = schema.addEntity("WireType");
        note.implementsSerializable();
        note.addIdProperty().primaryKey().index().notNull();
        note.addStringProperty("crossingLineName");
        note.addStringProperty("scbz");
        note.addDateProperty("cjsj");
        note.addDateProperty("updateDate");
    }

    /**
     * 变压器容量*
     */
    private static void addTransformerEntity(Schema schema) {
        Entity note = schema.addEntity("TransformerType");
        note.implementsSerializable();
        note.addIdProperty().primaryKey().index().notNull();
        note.addStringProperty("capacity");
        note.addStringProperty("scbz");
        note.addDateProperty("cjsj");
        note.addDateProperty("updateDate");
    }

    /**
     * 地质情况*
     */
    private static void addGeologicalconditionTypeEntity(Schema schema) {
        Entity note = schema.addEntity("GeologicalConditionType");
        note.implementsSerializable();
        note.addIdProperty().primaryKey().index().notNull();
        note.addStringProperty("soilType");
        note.addStringProperty("scbz");
        note.addDateProperty("cjsj");
        note.addDateProperty("updateDate");
    }

    /**
     * 设备安装*
     */
    private static void addEquimentInstallEntity(Schema schema) {
        Entity note = schema.addEntity("EquimentInstallType");
        note.implementsSerializable();
        note.addIdProperty().primaryKey().index().notNull();
        note.addStringProperty("barNameEn");
        note.addStringProperty("towerMaterialType");
        note.addStringProperty("scbz");
        note.addDateProperty("cjsj");
        note.addDateProperty("updateDate");
        //项目归属依据
        note.addIntProperty("voltageType");
        note.addIntProperty("terrainId");
        note.addIntProperty("belongId");
    }

    /**
     * 电杆型号*
     */
    private static void addElectricPoleEntity(Schema schema) {
        Entity note = schema.addEntity("ElectricPoleType");
        note.implementsSerializable();
        note.addIdProperty().primaryKey().index().notNull();
        note.addStringProperty("materialNum");
        note.addStringProperty("materialDetail");
        note.addStringProperty("materialUnit");
        note.addStringProperty("materialNameEn");
        note.addStringProperty("materialType");
        note.addStringProperty("materialWeight");
        note.addStringProperty("materialDrawing");
        note.addStringProperty("materialTechnical");
        note.addStringProperty("scbz");
        note.addDateProperty("cjsj");
        note.addDateProperty("updateDate");
        //项目归属依据
        note.addIntProperty("areaType");
        note.addIntProperty("areaId");
    }

    /**
     * 导线,电缆
     */
    private static void addConductorWireEntity(Schema schema) {
        Entity note = schema.addEntity("ConductorWireEntity");
        note.implementsSerializable();
        note.addIdProperty().primaryKey().index().notNull();
        note.addStringProperty("materialNum");
        note.addStringProperty("materialDetail");
        note.addStringProperty("materialUnit");
        note.addStringProperty("materialNameEn");
        note.addStringProperty("materialType");
        note.addStringProperty("scbz");
        note.addDateProperty("cjsj");
        note.addStringProperty("materialWeight");
        note.addStringProperty("materialDrawing");
        note.addStringProperty("materialTechnical");
        note.addDateProperty("updateDate");
        //项目归属依据
        note.addIntProperty("areaType");
        note.addIntProperty("areaId");
        note.addIntProperty("voltageType");

    }
}
