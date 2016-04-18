package com.hz.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.hz.R;
import com.hz.activity.base.BaseActivity;
import com.hz.adapter.MaterielDetailListAdapter;
import com.hz.common.Constans;
import com.hz.entity.MaterielDetailEntity;
import com.hz.greendao.dao.ConductorWireEntity;
import com.hz.greendao.dao.ConductorWireEntityDao;
import com.hz.greendao.dao.ElectricPoleType;
import com.hz.greendao.dao.ElectricPoleTypeDao;
import com.hz.greendao.dao.EquimentInstallType;
import com.hz.greendao.dao.EquimentInstallTypeDao;
import com.hz.greendao.dao.GeologicalConditionType;
import com.hz.greendao.dao.GeologicalConditionTypeDao;
import com.hz.greendao.dao.TowerType;
import com.hz.greendao.dao.TowerTypeDao;
import com.hz.greendao.dao.TransformerType;
import com.hz.greendao.dao.TransformerTypeDao;
import com.hz.greendao.dao.WireType;
import com.hz.greendao.dao.WireTypeDao;

import java.util.ArrayList;
import java.util.List;

/**
 * 物料详细信息activity
 * *
 */
public class MaterielDetailActivity extends BaseActivity {
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    public static final String TAG = MaterielDetailActivity.class.getSimpleName();
    private List<MaterielDetailEntity> mMaterielDetailListEntityList = new ArrayList<>();
    private BaseAdapter mMaterielDetaillListAdapter = null;
    public Handler mUiHandler = new Handler();//操作uihandler
    public Handler mDataHandler = null;//后台任务handler
    public HandlerThread handlerThread;

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_materiel_detail);
        initComponents();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDataHandler.removeCallbacksAndMessages(null);
        mUiHandler.removeCallbacksAndMessages(null);
        handlerThread.quit();
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 根据材料的类型初始化不同类型的材料详细数据
     */
    private void initDateByMaterielType(final String materielType) {
        mDataHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    //1.清空原有数据列表
                    mMaterielDetailListEntityList.clear();
                    //2.根据材料类型从数据库获取数据并保存在数据源中
                    queryMaterielsByType(materielType);
                    //通知listview更新视图数据
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mMaterielDetaillListAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    Log.d(TAG, "异常信息:" + e.toString());
                }
            }
        });
    }
    /**
     * 根据不同的材料类型初始化不同的材料数据
     */
    private void queryMaterielsByType(String materielType) {
        switch (materielType) {
            case Constans.MaterielType.TOWER_TYPE:
                TowerTypeDao towerTypeDao = this.getDaoSession().getTowerTypeDao();
                List<TowerType> towerTypeList = towerTypeDao.queryBuilder().list();
                if (towerTypeList.size() > 0) {
                    printListObj(towerTypeList);
                    for (TowerType towerType : towerTypeList) {
                        mMaterielDetailListEntityList.add(new MaterielDetailEntity(towerType.getBarNameEn()));
                    }
                }
                break;
            case Constans.MaterielType.WIRE_TYPE:
                WireTypeDao wireTypeDao = this.getDaoSession().getWireTypeDao();
                List<WireType> wireTypeList = wireTypeDao.queryBuilder().list();
                if (wireTypeList.size() > 0) {
                    printListObj(wireTypeList);
                    for (WireType wireType : wireTypeList) {
                        mMaterielDetailListEntityList.add(new MaterielDetailEntity(wireType.getCrossingLineName()));
                    }
                }
                break;
            case Constans.MaterielType.TRANSFORMER_TYPE:
                TransformerTypeDao transformerTypeDao = this.getDaoSession().getTransformerTypeDao();
                List<TransformerType> transformerTypeList = transformerTypeDao.queryBuilder().list();
                if (transformerTypeList.size() > 0) {
                    printListObj(transformerTypeList);
                    for (TransformerType transformerType : transformerTypeList) {
                        mMaterielDetailListEntityList.add(new MaterielDetailEntity(transformerType.getCapacity()));
                    }
                }
                break;
            case Constans.MaterielType.GEOLOGICALCONDITION_TYPE:
                GeologicalConditionTypeDao geologicalConditionTypeDao = this.getDaoSession().getGeologicalConditionTypeDao();
                List<GeologicalConditionType> geologicalConditionTypeList = geologicalConditionTypeDao.queryBuilder().list();
                if (geologicalConditionTypeList.size() > 0) {
                    printListObj(geologicalConditionTypeList);
                    for (GeologicalConditionType geologicalConditionType : geologicalConditionTypeList) {
                        mMaterielDetailListEntityList.add(new MaterielDetailEntity(geologicalConditionType.getSoilType()));
                    }
                }
                break;
            case Constans.MaterielType.EQUIMENTINSTALL_TYPE:
                EquimentInstallTypeDao equimentInstallTypeDao = this.getDaoSession().getEquimentInstallTypeDao();
                List<EquimentInstallType> equimentInstallTypeList = equimentInstallTypeDao.queryBuilder().list();
                if (equimentInstallTypeList.size() > 0) {
                    printListObj(equimentInstallTypeList);
                    for (EquimentInstallType equimentInstallType : equimentInstallTypeList) {
                        mMaterielDetailListEntityList.add(new MaterielDetailEntity(equimentInstallType.getBarNameEn()));
                    }
                }
                break;
            case Constans.MaterielType.ELECTRICPOLE_TYPE:
                ElectricPoleTypeDao electricPoleTypeDao = this.getDaoSession().getElectricPoleTypeDao();
                List<ElectricPoleType> electricPoleTypeList = electricPoleTypeDao.queryBuilder().list();
                if (electricPoleTypeList.size() > 0) {
                    printListObj(electricPoleTypeList);
                    for (ElectricPoleType electricPoleType : electricPoleTypeList) {
                        String name = TextUtils.isEmpty(electricPoleType.getMaterialNameEn()) ? "无" : electricPoleType.getMaterialNameEn();
                        mMaterielDetailListEntityList.add(new MaterielDetailEntity(name));
                    }
                }
                break;
            case Constans.MaterielType.CONDUCTORWIRE_TYPE:
                ConductorWireEntityDao conductorWireEntityDao = this.getDaoSession().getConductorWireEntityDao();
                List<ConductorWireEntity> conductorWireEntityList = conductorWireEntityDao.queryBuilder().list();
                if (conductorWireEntityList.size() > 0) {
                    printListObj(conductorWireEntityList);
                    for (ConductorWireEntity conductorWireEntity : conductorWireEntityList) {
                        String name = TextUtils.isEmpty(conductorWireEntity.getMaterialNameEn()) ? "无" : conductorWireEntity.getMaterialNameEn();
                        mMaterielDetailListEntityList.add(new MaterielDetailEntity(name));
                    }
                }
                break;
        }

    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 初始化系统组件信息
     */
    private void initComponents() {
        setMdToolBar(R.id.id_material_toolbar);
        setMDToolBarBackEnable(true);


        handlerThread = new HandlerThread("dataHandlerThread");
        handlerThread.start();
        mDataHandler = new Handler(handlerThread.getLooper());

        /**解析传入参数**/
        Bundle bundle = this.getIntent().getExtras();
        int iconResId = bundle.getInt(Constans.MaterielDetail.MATERIEL_DETAIL_ICON_KEY);
        String title = bundle.getString(Constans.MaterielDetail.MATERIEL_DETAIL_TITLE_KEY);
        String materielType = bundle.getString(Constans.MaterielDetail.MATERIEL_DETAIL_TYPE_KEY);

        setMDToolBarTitle(title + "列表");
        /**初始化组件信息**/
        ListView mMaterielDetailList = (ListView) findViewById(R.id.id_listview_materialdetail_list);
        mMaterielDetaillListAdapter = new MaterielDetailListAdapter(this, mMaterielDetailListEntityList);
        mMaterielDetailList.setAdapter(mMaterielDetaillListAdapter);

        /**从数据库获取对应数据**/
        initDateByMaterielType(materielType);
    }
    private void printListObj(List objectList) {
        for (Object object : objectList) {
            Log.d(TAG, "printListObj: " + object.toString());
        }
    }
}
