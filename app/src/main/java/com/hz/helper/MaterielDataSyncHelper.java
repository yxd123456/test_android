package com.hz.helper;

import android.content.Context;
import android.util.Log;

import com.hz.MainApplication;
import com.hz.R;
import com.hz.activity.base.BaseActivity;
import com.hz.common.Constans;
import com.hz.entity.MaterielDataSyncEntity;
import com.hz.entity.ResponseArrayWrapperEntity;
import com.hz.util.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.dao.AbstractDao;

/**
 * 材料数据同步工具类
 */
public class MaterielDataSyncHelper {

    public MaterielDataSyncHelper() {
        Log.d("Test",getClass().getSimpleName()+"被调用了");
    }

    /**
     * 获取所有的材料列表
     **/
    public List<MaterielDataSyncEntity> getAllMateriel(Context context) {
        List<MaterielDataSyncEntity> materielSettingEntityList = new ArrayList<>();
        MaterielDataSyncEntity towerType = new MaterielDataSyncEntity();
        towerType.setMaterielId(Constans.MaterielType.TOWER_TYPE);
        towerType.setMaterielName(context.getResources().getString(R.string.string_edit_towertype));
        towerType.setProSyncTime(new Date());
        towerType.setImageId(R.drawable.tower_type);
        towerType.setServerUrl(Constans.MaterielTypeURL.TOWER_TYPE_URL);
        materielSettingEntityList.add(towerType);

        MaterielDataSyncEntity wireType = new MaterielDataSyncEntity();
        wireType.setMaterielId(Constans.MaterielType.WIRE_TYPE);
        wireType.setMaterielName(context.getResources().getString(R.string.string_edit_wiretype));
        wireType.setProSyncTime(new Date());
        wireType.setImageId(R.drawable.tower_type);
        wireType.setServerUrl(Constans.MaterielTypeURL.WIRE_TYPE_URL);
        materielSettingEntityList.add(wireType);

        MaterielDataSyncEntity transformerType = new MaterielDataSyncEntity();
        transformerType.setMaterielId(Constans.MaterielType.TRANSFORMER_TYPE);
        transformerType.setMaterielName(context.getResources().getString(R.string.string_edit_transformer));
        transformerType.setProSyncTime(new Date());
        transformerType.setImageId(R.drawable.tower_type);
        transformerType.setServerUrl(Constans.MaterielTypeURL.TRANSFORMER_TYPE_URL);
        materielSettingEntityList.add(transformerType);

        MaterielDataSyncEntity geologicalConditionType = new MaterielDataSyncEntity();
        geologicalConditionType.setMaterielId(Constans.MaterielType.GEOLOGICALCONDITION_TYPE);
        geologicalConditionType.setMaterielName(context.getResources().getString(R.string.string_edit_geologicalconditions));
        geologicalConditionType.setProSyncTime(new Date());
        geologicalConditionType.setImageId(R.drawable.tower_type);
        geologicalConditionType.setServerUrl(Constans.MaterielTypeURL.GEOLOGICALCONDITION_TYPE_URL);
        materielSettingEntityList.add(geologicalConditionType);

        MaterielDataSyncEntity equimentInstallType = new MaterielDataSyncEntity();
        equimentInstallType.setMaterielId(Constans.MaterielType.EQUIMENTINSTALL_TYPE);
        equimentInstallType.setMaterielName(context.getResources().getString(R.string.string_edit_equipmentinstallation));
        equimentInstallType.setProSyncTime(new Date());
        equimentInstallType.setImageId(R.drawable.tower_type);
        equimentInstallType.setServerUrl(Constans.MaterielTypeURL.EQUIMENTINSTALL_TYPE_URL);
        materielSettingEntityList.add(equimentInstallType);


        MaterielDataSyncEntity electricPoleType = new MaterielDataSyncEntity();
        electricPoleType.setMaterielId(Constans.MaterielType.ELECTRICPOLE_TYPE);
        electricPoleType.setMaterielName(context.getResources().getString(R.string.string_edit_electricpoletype));
        electricPoleType.setProSyncTime(new Date());
        electricPoleType.setImageId(R.drawable.tower_type);
        electricPoleType.setServerUrl(Constans.MaterielTypeURL.ELECTRICPOLE_TYPE_URL);
        materielSettingEntityList.add(electricPoleType);

        MaterielDataSyncEntity conductorWire = new MaterielDataSyncEntity();
        conductorWire.setMaterielId(Constans.MaterielType.CONDUCTORWIRE_TYPE);
        conductorWire.setMaterielName(context.getResources().getString(R.string.string_edit_cnductorwire));
        conductorWire.setProSyncTime(new Date());
        conductorWire.setImageId(R.drawable.tower_type);
        conductorWire.setServerUrl(Constans.MaterielTypeURL.CONDUCTORWIRE_TYPE_URL);
        materielSettingEntityList.add(conductorWire);
        return materielSettingEntityList;
    }

    /**
     * 将json解析为javabean对象
     *
     * @param string     json字符串
     * @param materielId 材料ID
     */
    public ResponseArrayWrapperEntity analyJsonToBeanByMaterielId(String string, String materielId) throws IOException {
        ResponseArrayWrapperEntity entity = null;
        Class clazz = GreenDaoHelper.getResponseWrapperEntityClazzByMaterialType(materielId);
        if (clazz != null) {
            entity = (ResponseArrayWrapperEntity) JsonUtil.convertJsonToObj(string, clazz);
        }
        return entity;
    }

    /**
     * 持久化java对象到数据库
     *
     * @param wrapperEntity 材料实体对象超类
     * @param materielId    材料ID
     */
    public void persisentMaterielBeanToDbByMaterielId(MainApplication mainApplication, ResponseArrayWrapperEntity wrapperEntity, String materielId) {
        if (wrapperEntity != null && wrapperEntity.getData() != null && wrapperEntity.getData().size() > 0) {
            AbstractDao materielTypeDao = GreenDaoHelper.getGreenDaoByMaterialType(mainApplication, materielId);
            if (materielTypeDao != null) {
                /*materielTypeDao.deleteAll();*/
                materielTypeDao.insertOrReplaceInTx(wrapperEntity.getData());
            }
        }
    }

    /**
     * 持久化java对象到数据库
     *
     * @param wrapperEntity 材料实体对象超类
     * @param materielId    材料ID
     */
    public void persisentMaterielBeanToDbByMaterielId(BaseActivity baseActivity, ResponseArrayWrapperEntity wrapperEntity, String materielId) {
        this.persisentMaterielBeanToDbByMaterielId(baseActivity.getMainApplication(), wrapperEntity, materielId);
    }
}
