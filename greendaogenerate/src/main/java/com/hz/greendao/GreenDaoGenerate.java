package com.hz.greendao;

import java.io.File;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by long on 2015/8/4.
 */
public class GreenDaoGenerate {

    public static final String PACKAGE_DIR = "com.hz.greendao.dao";
    public static final String CLASS_DIR = "greendaogenerate" + File.separator + "src" + File.separator + "main" + File.separator + "java";
    public static final String PROJECT_DIR = System.getProperty("user.dir") + File.separator + CLASS_DIR;
    public static final int VERSION = 3;


    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(VERSION, PACKAGE_DIR);
        addTowerTypeEntity(schema);//**杆塔形式**//
        addWireTypeEntity(schema);//**拉线类型**//
        addTransformerEntity(schema);//**变压器容量**//
        addGeologicalconditionTypeEntity(schema);//**地质情况**//
        addEquimentInstallEntity(schema);//**设备安装**//
        addElectricPoleEntity(schema);//**电杆型号**//

        addProjectEntity(schema);//**项目列表**//
        addMapPoiEntity(schema);//**项目点位列表**//
        addProjectGalleryEntity(schema);//**项目相册列表**//


        new DaoGenerator().generateAll(schema, PROJECT_DIR);

    }

    /**
     * 项目相册列表*
     */
    private static void addProjectGalleryEntity(Schema schema) {
        Entity note = schema.addEntity("PointGalleryEntity");
        note.addStringProperty("imgId").primaryKey().index().notNull();
        note.addStringProperty("imgPointId").index().notNull();
        note.addStringProperty("imgFrom");
        note.addStringProperty("imgAddress");
        note.addIntProperty("imgRemoved");
    }

    /**
     * 项目点位列表*
     */
    private static void addMapPoiEntity(Schema schema) {
        Entity note = schema.addEntity("MapPoiEntity");
        note.addStringProperty("pointId").primaryKey().index().notNull();
        note.addLongProperty("pointProjId").index().notNull();
        note.addLongProperty("pointUserId").index().notNull();
        note.addIntProperty("pointEditType");
        note.addIntProperty("pointType");
        note.addDoubleProperty("pointLatitude");
        note.addDoubleProperty("pointLongitude");

        note.addStringProperty("pointName");
        note.addStringProperty("pointNote");
        note.addStringProperty("pointTowerTypeId");
        note.addStringProperty("pointWireTypeId");
        note.addStringProperty("pointTransformerTypeId");
        note.addStringProperty("pointElectricPoleTypeId");
        note.addStringProperty("pointGeologicalConditionsTypeId");
        note.addStringProperty("pointEquipmentInstallationTypeId");
        note.addIntProperty("pointRemoved");
    }

    /**
     * 项目列表
     * *
     */
    private static void addProjectEntity(Schema schema) {
        Entity note = schema.addEntity("ProjectEntity");
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
    }

    /**
     * 杆塔形式*
     */
    private static void addTowerTypeEntity(Schema schema) {
        Entity note = schema.addEntity("TowerType");
        note.addIdProperty().primaryKey().index().notNull();
        note.addStringProperty("barNameEn");
        note.addStringProperty("towerMaterialType");
        note.addIntProperty("scbz");
        note.addDateProperty("cjsj");
        note.addDateProperty("updateDate");
    }


    /**
     * 拉线类型*
     */
    private static void addWireTypeEntity(Schema schema) {
        Entity note = schema.addEntity("WireType");
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
        note.addIdProperty().primaryKey().index().notNull();
        note.addStringProperty("barNameEn");
        note.addStringProperty("towerMaterialType");
        note.addStringProperty("scbz");
        note.addDateProperty("cjsj");
        note.addDateProperty("updateDate");
    }

    /**
     * 电杆型号*
     */
    private static void addElectricPoleEntity(Schema schema) {
        Entity note = schema.addEntity("ElectricPoleType");
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
    }
}
