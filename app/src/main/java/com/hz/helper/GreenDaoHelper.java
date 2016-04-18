package com.hz.helper;

import android.util.Log;

import com.hz.MainApplication;
import com.hz.activity.base.BaseActivity;
import com.hz.common.Constans;
import com.hz.entity.ResponseArrayWrapperEntity;

import de.greenrobot.dao.AbstractDao;

/**
 * 封装greendao操作常用方法
 */
public class GreenDaoHelper {

    public GreenDaoHelper() {
        Log.d("Test",getClass().getSimpleName()+"被调用了");
    }

    /**
     * 根据物料的类型获取不同类型的class对象,物料同步
     *
     * @param materielType 物料类型
     */
    public static Class getResponseWrapperEntityClazzByMaterialType(String materielType) {
        Class clazz = null;

        switch (materielType) {
            case Constans.MaterielType.TOWER_TYPE://杆塔形式
                clazz = ResponseArrayWrapperEntity.TowerTypeWrapperEntity.class;
                break;
            case Constans.MaterielType.WIRE_TYPE://拉线类型
                clazz = ResponseArrayWrapperEntity.WireTypeWrapperEntity.class;
                break;
            case Constans.MaterielType.TRANSFORMER_TYPE://变压器容量
                clazz = ResponseArrayWrapperEntity.TransformerTypeWrapperEntity.class;
                break;
            case Constans.MaterielType.GEOLOGICALCONDITION_TYPE://地质情况
                clazz = ResponseArrayWrapperEntity.GeologicalConditionTypeWrapperEntity.class;
                break;
            case Constans.MaterielType.EQUIMENTINSTALL_TYPE://设备安装
                clazz = ResponseArrayWrapperEntity.EquimentInstallTypeWrapperEntity.class;
                break;
            case Constans.MaterielType.ELECTRICPOLE_TYPE://电杆型号
                clazz = ResponseArrayWrapperEntity.ElectricPoleTypeWrapperEntity.class;
                break;
            case Constans.MaterielType.CONDUCTORWIRE_TYPE://导线,电缆
                clazz = ResponseArrayWrapperEntity.ConductorWireWrapperEntity.class;
                break;
        }
        return clazz;
    }

    /**
     * 根据物料的类型获取不同类型的GreenDao对象,物料同步
     *
     * @param materielType 物料类型
     */
    public static AbstractDao getGreenDaoByMaterialType(MainApplication mainApplication, String materielType) {
        AbstractDao abstractDao = null;
        switch (materielType) {
            case Constans.MaterielType.TOWER_TYPE://杆塔形式
                abstractDao = mainApplication.getDaoSession().getTowerTypeDao();
                break;
            case Constans.MaterielType.WIRE_TYPE://拉线类型
                abstractDao = mainApplication.getDaoSession().getWireTypeDao();
                break;
            case Constans.MaterielType.TRANSFORMER_TYPE://变压器容量
                abstractDao = mainApplication.getDaoSession().getTransformerTypeDao();
                break;
            case Constans.MaterielType.GEOLOGICALCONDITION_TYPE://地质情况
                abstractDao = mainApplication.getDaoSession().getGeologicalConditionTypeDao();
                break;
            case Constans.MaterielType.EQUIMENTINSTALL_TYPE://设备安装
                abstractDao = mainApplication.getDaoSession().getEquimentInstallTypeDao();
                break;
            case Constans.MaterielType.ELECTRICPOLE_TYPE://电杆型号
                abstractDao = mainApplication.getDaoSession().getElectricPoleTypeDao();
                break;
            case Constans.MaterielType.CONDUCTORWIRE_TYPE://导线,电缆
                abstractDao = mainApplication.getDaoSession().getConductorWireEntityDao();
                break;
        }
        return abstractDao;
    }

    /**
     * 根据物料的类型获取不同类型的GreenDao对象,物料同步
     *
     * @param materielType 物料类型
     */
    public static AbstractDao getGreenDaoByMaterialType(BaseActivity baseActivity, String materielType) {
        return getGreenDaoByMaterialType(baseActivity.getMainApplication(),materielType);
    }
}
