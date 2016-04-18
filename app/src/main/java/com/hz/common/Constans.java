package com.hz.common;

import java.text.DecimalFormat;

/**
 * 保存系统常量
 */
public class Constans {
    public static final String ROOT_DICTORY = "edap";
    public static final String SERVER_ADDRESS = "http://121.196.232.182:8080/edap";
    //public static final String SERVER_ADDRESS = "http://121.41.35.167:80/edap";//服务端地址
    /*public static final String SERVER_ADDRESS = "http://192.168.0.13:8080/edap";//张恒服务端地址*/
    //public static final String SERVER_ADDRESS = "http://192.168.0.98:8080/edap";//楼志航服务端地址
    public static final String POST_POINTS_URL = SERVER_ADDRESS + "/towerDetail/addOnePointTowerDetail.html";//上传点位集合信息
    public static final String POST_IMAGES_URL = SERVER_ADDRESS + "/photoGallery/upload.html";//批量上传图片
    public static final String POST_LINES_URL = SERVER_ADDRESS + "/towerDetail/addOneLineDetail.html";//批量上传线
    public static final String PROJECT_URL = SERVER_ADDRESS + "/outWorkProjectManager/queryProjectByUserId.html?userId=%s";//项目列表请求 param: userId
    public static final String LOGIN_URL_GETUSERDATA = SERVER_ADDRESS + "/userManager/userLogin.html?userName=%s&userPassword=%s&registerid=%s";//获取用户信息 userName=?&userPassword=?&registerid=?
    public static final String LOGIN_URL_LOGIN = SERVER_ADDRESS + "/login";//登录系统
    public static final String LOGIN_URL_LOGOUT = SERVER_ADDRESS + "/loginOut.html";//登出系统
    public static final String CHECK_SESSION_EXPIRED = Constans.SERVER_ADDRESS + "/access/checkRemeberMeExpired.html";
    public static final String COLLECT_CRUSH_URL = SERVER_ADDRESS + "/mail/sendMailFromMobile.html";//收集手机错误日志信息
    public static final String DATE_PATTERN_LONG = "yyyy-MM-dd-HH-mm-ss-sss";//时间格式化长
    public static final String DATE_PATTERN_DEFAULT = "yyyy-MM-dd HH:mm:ss";//时间格式化长
    public static final String COORTYPE = "bd09ll";//百度地图地理位置编码
    public static final int SCANSPAN = 1000;//设置百度地图定位间隔
    public static final String POINT_OBJ_KEY = "POINT_OBJ_KEY";//地图点位信息编辑key值
    public static final String LINE_OBJ_KEY = "LINE_OBJ_KEY";//地图线信息编辑key值
    public static final String POST_POINTS_KEY = "points"; // key
    public static final String POST_IMAGE_KEY = "file"; //key
    public static final String POST_LINES_KEY = "lines"; //key
    public static final String POST_POINT_ID = "pointId";//图片关联点位ID key
    public static final String POST_POINT_GALLERY_ENTITY = "gallery";//图片关联项目ID key
    public static final String POST_PROJ_ID = "projectId";//图片关联项目ID key

    public static final String[] PROJECT_IMAGE_DIR = new String[]{"projectImages"};//项目图片存放路径
    public static final String[] PROJECT_OKHTTPCLIENT_DIR = new String[]{"okHttpClient", "Cache"};//项目OKHTTP缓存路径
    public static final String[] PROJECT_IMAGELOADER_DIR = new String[]{"imageloader", "Cache"};//项目imageloader缓存路径
    public static final String[] PROJECT_CRASH_DIR = new String[]{"crash"};//项目错误日志缓存路径
    public static final DecimalFormat DECIMALFORMAT_LATLONG = new DecimalFormat("#.00000");//经纬度默认格式化长度
    public static final DecimalFormat DECIMALFORMAT_M = new DecimalFormat("#");//长度格式化
    public static final String LINE_OBJ_KEY_TEST = "test_ye" ;

    /**
     * 地图元素类型*
     */
    public static class MapAttributeType {
        //点
        public static final int VERTICAL_WELDING = 1;//立杆
        public static final int TRANSFORMER_CHAMBER = 3;//变压箱
        public static final int DOOR_METETR = 4;//户表
        public static final int CABLE_PIT = 5;//电缆井
        public static final int BOX_SWITCH_STATION = 6;//箱式开关站
        public static final int SP_SWITCHING_POST = 7;//开闭所
        public static final int BOX_TYPE_SUBSTATION = 8;//箱式变压站

        //跨越线开始或结束点位类型(用于跨越线拖动)
        public static final int CROSS_LINE_START_POINT = 10;//跨越线开始点位
        public static final int CROSS_LINE_END_POINT = 11;//跨越线结束点位

        public static final int BATCH_LINE_POINT = 12;//批量添加点位 临时 点位类型


        //线
        public static final int CROSS_LINE = 2;//跨越线
        public static final int WIRE_ELECTRIC_CABLE = 9;//导线/电缆
    }

    /**
     * 线类型分类  导线/电缆
     */
    public static class LineWireType {
        public static final int WIRE = 0;//导线
        public static final int ELECTRIC_CABLE = 1;//电缆
    }

    /**
     * 点位类型
     **/
    public static class AttributeStatus {
        public static final int NEW = 0;//新点
        public static final int OLD = 1;//旧点
        public static final int BALI = 2;//拔立
        public static final int NONE = -1;//无状态
    }

    /**
     * 点位，拉线 编辑属性*
     */
    public static class AttributeEditType {
        //editType 1:新增，0:编辑 ，2：移除
        public static final int EDIT_TYPE_REMOVE = 2;//移除点位
        public static final int EDIT_TYPE_ADD = 1;//新增的点位
        public static final int EDIT_TYPE_EDIT = 0;//编辑点位
        public static final int EDIT_TYPE_LINE_BATCHADD = 3;//批量新增线编辑类型
        public static final int EDIT_TYPE_POINT_BATCHUPDATE = 4;//批量修改点位编辑类型
        public static final int EDIT_TYPE_LINE_BATCHADD_C = 15;
        public static final int EDIT_TYPE_REMOVE_SELECT = 172;
    }

    /**
     * android startActivityForResult requestCode标记*
     */
    public static class RequestCode {
        public static final int POINT_ATTRIBUTE_EDIT_REQUESTCODE = 0;//地图标记编辑requestCode
        public static final int LINE_ATTRIBUTE_EDIT_REQUESTCODE = 1;//地图线编辑requestCode
    }

    /**
     * 从材料同步列表页面到材料详细页面两个参数key
     * *
     */
    public static class MaterielDetail {
        public static final String MATERIEL_DETAIL_ICON_KEY = "materielIcon";
        public static final String MATERIEL_DETAIL_TITLE_KEY = "materielTitle";
        public static final String MATERIEL_DETAIL_TYPE_KEY = "materielTypeTitle";
    }

    /**
     * 移除标志
     * *
     */
    public static class RemoveIdentified {
        public static final int REMOVE_IDENTIFIED_NORMAL = 1;//1 未删除
        public static final int REMOVE_IDENTIFIED_REMOVED = 0;//0 删除
    }

    /**
     * 项目状态
     **/
    public static class ProjectStatus {
        public static final int UN_DONE = 1;//外业采集中
        public static final int HAS_DONE = 2;//外业采集完成
        public static final int HAS_AUDIT = 3;//采集数据审核完成
        public static final int MATERIAL_DONE = 4;//采集数据材料已整理
    }

    /**
     * 材料类型标记
     * *
     */
    public static class MaterielType {
        public static final String TOWER_TYPE = "TOWER_TYPE";//杆塔形式
        public static final String WIRE_TYPE = "WIRE_TYPE";//拉线类型
        public static final String TRANSFORMER_TYPE = "TRANSFORMER_TYPE";//变压器容量
        public static final String GEOLOGICALCONDITION_TYPE = "GEOLOGICALCONDITION_TYPE";//地质情况
        public static final String EQUIMENTINSTALL_TYPE = "EQUIMENTINSTALL_TYPE";//设备安装
        public static final String ELECTRICPOLE_TYPE = "ELECTRICPOLE_TYPE";//电杆型号
        public static final String CONDUCTORWIRE_TYPE = "CONDUCTORWIRE_TYPE";//导线,电缆
    }

    /**
     * 材料类型标记,服务地址
     * *
     */
    public static class MaterielTypeURL {
        public static final String TOWER_TYPE_URL = SERVER_ADDRESS + "/towerMaterial/queryTowerMaterialTypeMobile.html?towerMaterialType=6&proId=";//杆塔形式
        public static final String WIRE_TYPE_URL = SERVER_ADDRESS + "/crossingLineManager/crossingLineMobile.html";//拉线类型 | 跨越线
        public static final String TRANSFORMER_TYPE_URL = SERVER_ADDRESS + "/voltageTransformerManager/queryVoltageTransformerManagerMobile.html";//变压器容量
        public static final String GEOLOGICALCONDITION_TYPE_URL = SERVER_ADDRESS + "/soilManager/querySoilMobile.html";//地质情况
        public static final String EQUIMENTINSTALL_TYPE_URL = SERVER_ADDRESS + "/towerMaterial/queryTowerMaterialTypeMobile.html?towerMaterialType=5&proId=";//设备安装
        public static final String ELECTRICPOLE_TYPE_URL = SERVER_ADDRESS + "/material/queryMaterialsMobile.html?materialType=2&proId=";//电杆型号
        public static final String CONDUCTORWIRE_TYPE_URL = SERVER_ADDRESS + "/material/queryWireWayMaterial.html?proId=";//导线,电缆
    }

    /**
     * 加载各种路径下的标示
     * *
     */
    public static class ImageLoaderMark {
        public static final String DRAWABLE = "drawable://";// from drawables (only images, non-9patch)
        public static final String ASSETS = "assets://";// from assets
        public static final String CONTENT = "content://";// from content provider
        public static final String FILE = "file://";// from SD card
        public static final String HTTP = "http://";// from Web
    }

    /**
     * 图片来源
     * *
     */
    public static class ImageFrom extends ImageLoaderMark {

    }

    /**
     * notificatinID
     **/
    public static class NOTIFICATION_ID {
        public static final int ID_UPLOAD_GOREGROUND = 1101;//采集数据上传中
        public static final int ID_UPLOAD_OK = 1102;//采集数据上传完成
    }
}


