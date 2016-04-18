package com.hz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.hz.R;
import com.hz.activity.base.BaseAttributeActivity;
import com.hz.common.Constans;
import com.hz.common.TowerDetailTerrainEnums;
import com.hz.dialog.PickerListViewDialog;
import com.hz.greendao.dao.ConductorWireEntity;
import com.hz.greendao.dao.ConductorWireEntityDao;
import com.hz.greendao.dao.DaoSession;
import com.hz.greendao.dao.ElectricPoleType;
import com.hz.greendao.dao.ElectricPoleTypeDao;
import com.hz.greendao.dao.EquimentInstallType;
import com.hz.greendao.dao.EquimentInstallTypeDao;
import com.hz.greendao.dao.GeologicalConditionType;
import com.hz.greendao.dao.GeologicalConditionTypeDao;
import com.hz.greendao.dao.MapLineEntity;
import com.hz.greendao.dao.MapLineItemEntity;
import com.hz.greendao.dao.MapPoiEntity;
import com.hz.greendao.dao.TowerType;
import com.hz.greendao.dao.TowerTypeDao;
import com.hz.greendao.dao.TransformerType;
import com.hz.greendao.dao.TransformerTypeDao;
import com.hz.entity.PickerItem;
import com.hz.view.ValidaterEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 地图点位属性编辑页面
 * 立杆，变压箱，户表，电缆井，箱式开关站，开闭所，箱式变压站
 * <p/>
 * *
 */
public class PointAttributeActivity extends BaseAttributeActivity {
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    public static final String TAG = PointAttributeActivity.class.getSimpleName();

    private MapPoiEntity mapObj;
    private MapLineEntity mapLineObj = new MapLineEntity();//批量修改使用

    //点位需要填写的数据信息
    private ValidaterEditText mEditTowerType;//点位杆塔类型
    private ValidaterEditText mEditTransformerType;//点位变压器类型
    private ValidaterEditText mEditElectricPoleType;//点位电杆型号
    private ValidaterEditText mEditElectricPoleTypeCount;//点位电杆数量
    private ValidaterEditText mEditGeologicalConditionsType;//点位土壤质量
    private ValidaterEditText mEditEquipmentInstallationType;//点位设备安装
    private ValidaterEditText mEditLighting;//显示照明表
    private ValidaterEditText mEditPower;//显示动力表
    private ValidaterEditText mEditConnectDoorLength;//显示接户线长数
    private ValidaterEditText mEditPointNum;//点位杆号
    private ValidaterEditText mEditLandFrom;//地形
    private ValidaterEditText mEditSpecificationNumber;//规格线数
    private RadioGroup mEditStatus;//点位是否是新点  0:新   1:旧 2:拔立 需要此属性的点位类型包括 ： 立杆 变压箱 电缆井，箱式开关站，开闭所，箱变
    private TableLayout mEditElectricCableTableLayout;//导线/电缆 属性

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_attribute);
    }
    /**
     * 解析从地图编辑页面传递过来的参数
     */
    @Override
    public void onAnalysisBundleData() {
        super.onAnalysisBundleData();
        //参数
        Bundle bundleParam = this.getIntent().getExtras();
        mapObj = (MapPoiEntity) bundleParam.getSerializable(Constans.POINT_OBJ_KEY);

        if (mapObj == null) {
            return;
        }

        //根据线类型显示不同的标题和控制组件的显示隐藏
        analysisUiTitleAndFieldVisibleByPointType(mapObj.getPointType());
        //根据参数显示修改或者新增
        displayOkOrUpdateByAttributeByEditType(mapObj.getPointEditType());
        //设置tag和文本
        setTextFieldTagAndText(mapObj);
        //根据textfield的tag到数据库查询数据库文本信息
        setTextFieldTextByTextFieldTag(mapObj);

        //获取已有相册数据
        if (mapObj.getPointGalleryLists() != null && mapObj.getPointGalleryLists().size() > 0) {
            mGalleryEntityList.addAll(mapObj.getPointGalleryLists());
        }

    }
    @Override
    public void pickerDialogHasFocus(PickerListViewDialog pickerScrollViewDialog) {
        List<PickerItem> pickerItems = new ArrayList<>();
        String title = "";

        switch (pickerScrollViewDialog.getPickerDialogBindEditTextId()) {
            case R.id.id_edit_towertype:
                TowerTypeDao towerTypeDao = this.getDaoSession().getTowerTypeDao();
                List<TowerType> towerTypeList = towerTypeDao.queryBuilder()
                        .where(TowerTypeDao.Properties.VoltageType.eq(projectEntity.getVoltageType()))
                        .where(TowerTypeDao.Properties.BelongId.eq(projectEntity.getBelongId()))
                        .where(TowerTypeDao.Properties.TerrainId.eq(projectEntity.getTerrainId()))
                        .list();
                for (TowerType towerType : towerTypeList) {
                    pickerItems.add(new PickerItem(towerType.getId() + "", towerType.getBarNameEn()));
                }
                title = getResources().getString(R.string.string_edit_towertype);
                break;
            case R.id.id_edit_transformer:
                TransformerTypeDao transformerTypeDao = this.getDaoSession().getTransformerTypeDao();
                List<TransformerType> transformerTypeList = transformerTypeDao.queryBuilder().list();
                for (TransformerType transformerType : transformerTypeList) {
                    pickerItems.add(new PickerItem(transformerType.getId() + "", transformerType.getCapacity()));
                }
                title = getResources().getString(R.string.string_edit_transformer);
                break;
            case R.id.id_edit_electricpoletype:
                ElectricPoleTypeDao electricPoleTypeDao = this.getDaoSession().getElectricPoleTypeDao();
                List<ElectricPoleType> electricPoleTypeList = electricPoleTypeDao.queryBuilder()
                        .where(ElectricPoleTypeDao.Properties.AreaType.eq(projectEntity.getAreaType()))
                        .where(ElectricPoleTypeDao.Properties.AreaId.eq(projectEntity.getAreaId()))
                        .list();
                for (ElectricPoleType electricPoleType : electricPoleTypeList) {
                    String name = TextUtils.isEmpty(electricPoleType.getMaterialNameEn()) ? "无" : electricPoleType.getMaterialNameEn();
                    pickerItems.add(new PickerItem(electricPoleType.getId() + "", name));
                }
                title = getResources().getString(R.string.string_edit_electricpoletype);
                break;
            case R.id.id_edit_geologicalconditions:
                GeologicalConditionTypeDao geologicalConditionTypeDao = this.getDaoSession().getGeologicalConditionTypeDao();
                List<GeologicalConditionType> geologicalConditionTypeList = geologicalConditionTypeDao.queryBuilder().list();
                for (GeologicalConditionType geologicalConditionType : geologicalConditionTypeList) {
                    pickerItems.add(new PickerItem(geologicalConditionType.getId() + "", geologicalConditionType.getSoilType()));
                }
                title = getResources().getString(R.string.string_edit_geologicalconditions);
                break;
            case R.id.id_edit_equipmentinstallation:
                EquimentInstallTypeDao equimentInstallTypeDao = this.getDaoSession().getEquimentInstallTypeDao();
                List<EquimentInstallType> equimentInstallTypeList = equimentInstallTypeDao.queryBuilder()
                        .where(EquimentInstallTypeDao.Properties.VoltageType.eq(projectEntity.getVoltageType()))
                        .where(EquimentInstallTypeDao.Properties.BelongId.eq(projectEntity.getBelongId()))
                        .where(EquimentInstallTypeDao.Properties.TerrainId.eq(projectEntity.getTerrainId()))
                        .list();
                for (EquimentInstallType equimentInstallType : equimentInstallTypeList) {
                    pickerItems.add(new PickerItem(equimentInstallType.getId() + "", equimentInstallType.getBarNameEn()));
                }
                title = getResources().getString(R.string.string_edit_equipmentinstallation);
                break;
            case R.id.id_edit_landform:
                pickerItems.addAll(TowerDetailTerrainEnums.toPickerList());
                title = getResources().getString(R.string.string_edit_landform);
                break;
            case R.id.id_edit_electriccable_itemmode:
                ConductorWireEntityDao conductorWireEntityDao = this.getDaoSession().getConductorWireEntityDao();
                List<ConductorWireEntity> conductorWireEntityList = conductorWireEntityDao.queryBuilder()
                        .where(ConductorWireEntityDao.Properties.AreaId.eq(projectEntity.getAreaId()))
                        .where(ConductorWireEntityDao.Properties.VoltageType.eq(projectEntity.getVoltageType()))
                        .where(ConductorWireEntityDao.Properties.AreaType.eq(projectEntity.getAreaType()))
                        .list();
                Log.d(TAG, "pickerDialogHasFocus: 导线/电缆 个数：" + conductorWireEntityList.size() + "  " + projectEntity.toString());
                for (ConductorWireEntity conductorWireEntity : conductorWireEntityList) {
                    Log.d(TAG, "pickerDialogHasFocus: " + conductorWireEntity.toString());
                    pickerItems.add(new PickerItem(conductorWireEntity.getId() + "", conductorWireEntity.getMaterialNameEn()));
                }
                title = getResources().getString(R.string.string_wire_electriccable);
                break;
        }

        pickerScrollViewDialog.setPickerDialogDatas(pickerItems);
        pickerScrollViewDialog.show();
        pickerScrollViewDialog.setTitle(title);
    }
    @Override//添加一行
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.id_button_addtablerow:
                TableRow tableRow = createNewTableRow();
                addNewTableRowToTableLayout(tableRow);
                break;
        }
    }
    @Override
    public void onBeforeRightIconClick() {
        mapObj.setPointEditType(Constans.AttributeEditType.EDIT_TYPE_REMOVE);
    }
    @Override
    public void onSetUpResult() {
        String landForm = getString(mEditLandFrom.getTag());
        String electricPoleTypeCountStr = mEditElectricPoleTypeCount.getText().toString();
        int electricPoleTypeCount = TextUtils.isEmpty(electricPoleTypeCountStr) ? 0 : Integer.parseInt(electricPoleTypeCountStr);
        String lighting = mEditLighting.getText().toString();
        String power = mEditPower.getText().toString();
        String connectdoor = mEditConnectDoorLength.getText().toString();
        double coverDepth = calculateCoverDepth(); //计算立杆的埋深
        mGalleryEntityList.remove(mGalleryEntityList.size() - 1);//移除默认图片
        int pointStatus;
        switch (mEditStatus.getCheckedRadioButtonId()) {
            case R.id.id_pointstatus_new:
                pointStatus = Constans.AttributeStatus.NEW;
                break;
            case R.id.id_pointstatus_old:
                pointStatus = Constans.AttributeStatus.OLD;
                break;
            case R.id.id_pointstatus_bali:
                pointStatus = Constans.AttributeStatus.BALI;
                break;
            case R.id.id_pointstatus_none:
                pointStatus = Constans.AttributeStatus.NONE;
                break;
            default:
                pointStatus = Constans.AttributeStatus.NEW;
                break;
        }
        if (mapObj.getPointType() == Constans.MapAttributeType.DOOR_METETR) {//户表无状态
            pointStatus = Constans.AttributeStatus.NONE;
        }

        //设置返回参数
        mapObj.setPointName(mEditAttributeName.getText().toString());
        mapObj.setPointNote(mEditAttributeNote.getText().toString());
        mapObj.setPointTransformerTypeId(getString(mEditTransformerType.getTag()));
        mapObj.setPointElectricPoleTypeId(getString(mEditElectricPoleType.getTag()));
        mapObj.setPointGeologicalConditionsTypeId(getString(mEditGeologicalConditionsType.getTag()));
        mapObj.setPointEquipmentInstallationTypeId(getString(mEditEquipmentInstallationType.getTag()));
        mapObj.setPointTowerTypeId(getString(mEditTowerType.getTag()));
        mapObj.setPointLightingNum(TextUtils.isEmpty(lighting) ? 0 : Integer.parseInt(lighting));
        mapObj.setPointPowerNum(TextUtils.isEmpty(power) ? 0 : Integer.parseInt(power));
        mapObj.setPointConnectDoorNum(TextUtils.isEmpty(connectdoor) ? 0 : Integer.parseInt(connectdoor));
        mapObj.setPointNum(mEditPointNum.getText().toString());
        mapObj.setPointCoverDepth(coverDepth);
        mapObj.setPointGalleryLists(mGalleryEntityList);
        mapObj.setPointStatus(pointStatus);
        mapObj.setPointLandForm(landForm);
        mapObj.setPointElectricPoleTypeCount(electricPoleTypeCount);


        //线信息
        mapLineObj.setLineSpecificationNumber(Integer.parseInt(mEditSpecificationNumber.getText().toString()));
        List<MapLineItemEntity> lineItemEntityList = new ArrayList<>();
        for (TableRow tableRow : findElectricCableTableLayoutChildTableRow()) {
            ValidaterEditText electricCable = (ValidaterEditText) tableRow.findViewById(R.id.id_edit_electriccable_itemmode);
            AppCompatSpinner wiretypeSpinner = (AppCompatSpinner) tableRow.findViewById(R.id.id_edit_spinner_linewiretype);
            AppCompatSpinner lineStatusSpinner = (AppCompatSpinner) tableRow.findViewById(R.id.id_edit_spinner_linestatus);
            ValidaterEditText lineItemNum = (ValidaterEditText) tableRow.findViewById(R.id.id_edittext_itemnum);

            //mode
            String lineItemModeId = getString(electricCable.getTag());

            //wiretype
            String wireTypeStr = getString(wiretypeSpinner.getSelectedItem());
            String wire = this.getResources().getStringArray(R.array.string_edit_linewiretype)[0];
            int wireType = TextUtils.equals(wireTypeStr, wire) ? Constans.LineWireType.WIRE : Constans.LineWireType.ELECTRIC_CABLE;

            //lineStatus
            String lineStatusStr = getString(lineStatusSpinner.getSelectedItem());
            String status = this.getResources().getStringArray(R.array.string_edit_linestatus)[0];
            int lineStatus = TextUtils.equals(lineStatusStr, status) ? Constans.AttributeStatus.NEW : Constans.AttributeStatus.OLD;

            //num
            String lintNumStr = lineItemNum.getText().toString();
            int itemNum = TextUtils.isEmpty(lintNumStr) ? 1 : Integer.parseInt(lintNumStr);

            //hide id
            TextView hideIdTextView = (TextView) tableRow.findViewById(R.id.id_textview_hide_row_id);
            String hideId = hideIdTextView.getText().toString();

            //remove identifier
            int removeIdentifier = (tableRow.getVisibility() == View.VISIBLE) ? Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL : Constans.RemoveIdentified.REMOVE_IDENTIFIED_REMOVED;

            Log.d(TAG, lineItemModeId + "  " + wireTypeStr + "  " + lineStatusStr + "  " + itemNum + "  " + hideId + "  " + removeIdentifier);

            MapLineItemEntity itemEntity = new MapLineItemEntity();
            itemEntity.setLineItemWireType(wireType);
            itemEntity.setLineItemModeId(lineItemModeId);
            itemEntity.setLineItemNum(itemNum);
            itemEntity.setLineItemId(hideId);
            itemEntity.setLineItemStatus(lineStatus);
            itemEntity.setLineItemRemoved(removeIdentifier);
            lineItemEntityList.add(itemEntity);
        }
        mapLineObj.setMapLineItemEntityList(lineItemEntityList);

        //设置bundle
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constans.POINT_OBJ_KEY, mapObj);
        bundle.putSerializable(Constans.LINE_OBJ_KEY, mapLineObj);//批量线/点位信息

        //设置intent
        Intent resultIntent = new Intent();
        resultIntent.putExtras(bundle);

        //设置返回信息
        this.setResult(Constans.RequestCode.POINT_ATTRIBUTE_EDIT_REQUESTCODE, resultIntent);
    }
    @Override
    public boolean onValidateInputSetUpResult() {
        int pointType = mapObj.getPointType();
        List<ValidaterEditText> validateList = new ArrayList<>();
        validateList.add(mEditAttributeName);
        validateList.add(mEditAttributeNote);

        switch (pointType) {
            case Constans.MapAttributeType.TRANSFORMER_CHAMBER://变压箱
                validateList.add(mEditTransformerType);
                break;
            case Constans.MapAttributeType.VERTICAL_WELDING://立杆
                validateList.add(mEditTowerType);
                validateList.add(mEditElectricPoleType);
                validateList.add(mEditElectricPoleTypeCount);
                validateList.add(mEditGeologicalConditionsType);
                validateList.add(mEditEquipmentInstallationType);
                validateList.add(mEditPointNum);
                validateList.add(mEditLandFrom);
                if (mapObj.getPointEditType() == Constans.AttributeEditType.EDIT_TYPE_POINT_BATCHUPDATE) {
                    for (TableRow tableRow : findElectricCableTableLayoutChildTableRow()) {
                        if (tableRow.getVisibility() == View.VISIBLE) {
                            validateList.add((ValidaterEditText) tableRow.findViewById(R.id.id_edit_electriccable_itemmode));
                            validateList.add((ValidaterEditText) tableRow.findViewById(R.id.id_edittext_itemnum));
                        }
                    }
                    validateList.add(mEditSpecificationNumber);
                }
                break;
            case Constans.MapAttributeType.DOOR_METETR://户表
                validateList.add(mEditLighting);
                validateList.add(mEditPower);
                validateList.add(mEditConnectDoorLength);
                break;
        }

        //批量修改点位时点位杆号可以为空
        if (mapObj.getPointEditType() == Constans.AttributeEditType.EDIT_TYPE_POINT_BATCHUPDATE) {
            mEditPointNum.removeValidate(ValidaterEditText.VALIDATE_NOT_BLANK);
            mEditElectricPoleTypeCount.removeValidate(ValidaterEditText.VALIDATE_NOT_MIN_IS_1);
        }

        boolean allValid = true;
        for (ValidaterEditText field : validateList) {
            allValid = field.validateByConfig() && allValid;
        }
        return allValid;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 根据传入的参数修改界面编辑框中的数据和tag信息
     */
    private void setTextFieldTagAndText(MapPoiEntity mapObj) {
        setNameAndNoteByBundleData(mapObj.getPointName(), mapObj.getPointNote());

        mEditLighting.setText(String.valueOf(mapObj.getPointLightingNum()));
        mEditPower.setText(String.valueOf(mapObj.getPointPowerNum()));
        mEditConnectDoorLength.setText(String.valueOf(mapObj.getPointConnectDoorNum()));
        mEditPointNum.setText(mapObj.getPointNum());
        int poleCount = mapObj.getPointElectricPoleTypeCount();
        mEditElectricPoleTypeCount.setText(String.valueOf(poleCount == 0 ? 1 : poleCount));
        if (mapObj.getPointEditType() == Constans.AttributeEditType.EDIT_TYPE_POINT_BATCHUPDATE) {
            mEditElectricPoleTypeCount.setText(String.valueOf(0));
        }
        //显示点位状态
        if (mapObj.getPointStatus() == 0) {
            if (mapObj.getPointEditType() == Constans.AttributeEditType.EDIT_TYPE_POINT_BATCHUPDATE) {
                mapObj.setPointStatus(Constans.AttributeStatus.NONE);
            } else {
                mapObj.setPointStatus(Constans.AttributeStatus.NEW);
            }
        }
        switch (mapObj.getPointStatus()) {
            case Constans.AttributeStatus.NEW:
                mEditStatus.check(R.id.id_pointstatus_new);
                break;
            case Constans.AttributeStatus.OLD:
                mEditStatus.check(R.id.id_pointstatus_old);
                break;
            case Constans.AttributeStatus.BALI:
                mEditStatus.check(R.id.id_pointstatus_bali);
                break;
            case Constans.AttributeStatus.NONE:
                mEditStatus.check(R.id.id_pointstatus_none);
                break;
            default:
                mEditStatus.check(R.id.id_pointstatus_new);
                break;
        }


        //为pickerDialog设置tag以后此dialog在打开后就会显示此tag数据对应的key值
        mEditTowerType.setTag(mapObj.getPointTowerTypeId());
        mEditTransformerType.setTag(mapObj.getPointTransformerTypeId());
        mEditElectricPoleType.setTag(mapObj.getPointElectricPoleTypeId());
        mEditGeologicalConditionsType.setTag(mapObj.getPointGeologicalConditionsTypeId());
        mEditEquipmentInstallationType.setTag(mapObj.getPointEquipmentInstallationTypeId());
        mEditLandFrom.setTag(mapObj.getPointLandForm());
    }
    /**
     * 更新界面显示资源
     */
    private void setTextFieldTextByTextFieldTag(MapPoiEntity mapObj) {
        DaoSession daoSession = this.getDaoSession();
        //在线程中根据传入的输入框ID更新显示tett信息
        //更新杆塔显示信息
        if (mapObj.getPointTowerTypeId() != null && mEditTowerType.getVisibility() == View.VISIBLE) {
            TowerType towerType = daoSession.getTowerTypeDao().queryBuilder().where(TowerTypeDao.Properties.Id.eq(mapObj.getPointTowerTypeId())).unique();
            if (towerType != null) {
                mEditTowerType.setText(towerType.getBarNameEn());
            }
        }
        //变压器容量类型
        if (mapObj.getPointTransformerTypeId() != null && mEditTransformerType.getVisibility() == View.VISIBLE) {
            TransformerType transformerType = daoSession.getTransformerTypeDao().queryBuilder().where(TransformerTypeDao.Properties.Id.eq(mapObj.getPointTransformerTypeId())).unique();
            if (transformerType != null) {
                mEditTransformerType.setText(transformerType.getCapacity());
            }
        }
        //电杆型号类型
        if (mapObj.getPointElectricPoleTypeId() != null && mEditElectricPoleType.getVisibility() == View.VISIBLE) {
            ElectricPoleType electricPoleType = daoSession.getElectricPoleTypeDao().queryBuilder()
                    .where(ElectricPoleTypeDao.Properties.Id.eq(mapObj.getPointElectricPoleTypeId())).unique();
            if (electricPoleType != null) {
                mEditElectricPoleType.setText(electricPoleType.getMaterialNameEn());
            }
        }
        //地质情况类型
        if (mapObj.getPointGeologicalConditionsTypeId() != null && mEditGeologicalConditionsType.getVisibility() == View.VISIBLE) {
            GeologicalConditionType geologicalConditionType = daoSession.getGeologicalConditionTypeDao().queryBuilder().where(GeologicalConditionTypeDao.Properties.Id.eq(mapObj.getPointGeologicalConditionsTypeId())).unique();
            if (geologicalConditionType != null) {
                mEditGeologicalConditionsType.setText(geologicalConditionType.getSoilType());
            }
        }
        //设备安装类型
        if (mapObj.getPointEquipmentInstallationTypeId() != null && mEditEquipmentInstallationType.getVisibility() == View.VISIBLE) {
            EquimentInstallType equimentInstallType = daoSession.getEquimentInstallTypeDao().queryBuilder().where(EquimentInstallTypeDao.Properties.Id.eq(mapObj.getPointEquipmentInstallationTypeId())).unique();
            if (equimentInstallType != null) {
                mEditEquipmentInstallationType.setText(equimentInstallType.getBarNameEn());
            }
        }

        //地形
        if (mapObj.getPointLandForm() != null && mEditLandFrom.getVisibility() == View.VISIBLE) {
            String landform = mapObj.getPointLandForm();
            if (!TextUtils.isEmpty(landform)) {
                mEditLandFrom.setText(TowerDetailTerrainEnums.getByTypeSerial(Integer.parseInt(landform)).getTypeName());
            }

        }
    }
    /**
     * 通过点位类型来显示隐藏ui界面元素 点位编辑框
     */
    private void analysisUiTitleAndFieldVisibleByPointType(final int pointType) {
        //编辑信息
        final View towertype = findViewById(R.id.id_linearlayout_towertype);
        towertype.setVisibility(View.GONE);
        final View transformer = findViewById(R.id.id_linearlayout_transformer);
        transformer.setVisibility(View.GONE);
        final View electricpoletype = findViewById(R.id.id_linearlayout_electricpoletype);
        electricpoletype.setVisibility(View.GONE);
        final View geologicalconditions = findViewById(R.id.id_linearlayout_geologicalconditions);
        geologicalconditions.setVisibility(View.GONE);
        final View equipmentinstallation = findViewById(R.id.id_linearlayout_equipmentinstallation);
        equipmentinstallation.setVisibility(View.GONE);
        final View pointstatus = findViewById(R.id.id_linearlayout_pointstatus);
        pointstatus.setVisibility(View.GONE);
        final View pointstatusBali = findViewById(R.id.id_pointstatus_bali);
        pointstatusBali.setVisibility(View.GONE);
        final View lighting = findViewById(R.id.id_linearlayout_lighting);
        lighting.setVisibility(View.GONE);
        final View power = findViewById(R.id.id_linearlayout_power);
        power.setVisibility(View.GONE);
        final View connectDoor = findViewById(R.id.id_linearlayout_connectdoorlength);
        connectDoor.setVisibility(View.GONE);
        final View pointnum = findViewById(R.id.id_linearlayout_pointnum);
        pointnum.setVisibility(View.GONE);
        final View electricpoletypeCount = findViewById(R.id.id_linearlayout_electricpoletype_count);
        electricpoletypeCount.setVisibility(View.GONE);
        final View landform = findViewById(R.id.id_linearlayout_landform);
        landform.setVisibility(View.GONE);
        final View pointStatusRadioNone = findViewById(R.id.id_pointstatus_none);
        pointStatusRadioNone.setVisibility(View.GONE);
        final View specificationnumber = findViewById(R.id.id_linearlayout_specificationnumber);
        specificationnumber.setVisibility(View.GONE);
        final View electriccable = findViewById(R.id.id_linearlayout_electriccable);
        electriccable.setVisibility(View.GONE);

        final View gallery = findViewById(R.id.idlinearlayout_gallery);
        final View name = findViewById(R.id.id_linearlayout_pointname);
        final View note = findViewById(R.id.id_linearlayout_pointnote);


        ArrayList<View> animateVeiwVisible = new ArrayList<>();

        switch (pointType) {
            case Constans.MapAttributeType.VERTICAL_WELDING://立杆
                if (mapObj.getPointEditType() == Constans.AttributeEditType.EDIT_TYPE_POINT_BATCHUPDATE) {
                    setMDToolBarTitle(R.string.string_batchupdate_verticalwelding);
                    gallery.setVisibility(View.GONE);
                    name.setVisibility(View.GONE);
                    note.setVisibility(View.GONE);
                    pointStatusRadioNone.setVisibility(View.VISIBLE);
                    specificationnumber.setVisibility(View.VISIBLE);
                    electriccable.setVisibility(View.VISIBLE);
                } else {
                    setMDToolBarTitle(R.string.string_verticalwelding);
                }
                animateVeiwVisible.add(pointnum);
                animateVeiwVisible.add(towertype);
                animateVeiwVisible.add(electricpoletype);
                animateVeiwVisible.add(electricpoletypeCount);
                animateVeiwVisible.add(geologicalconditions);
                animateVeiwVisible.add(landform);
                animateVeiwVisible.add(equipmentinstallation);
                animateVeiwVisible.add(pointstatus);
                animateVeiwVisible.add(pointstatusBali);
                break;
            case Constans.MapAttributeType.TRANSFORMER_CHAMBER://变压箱
                setMDToolBarTitle(R.string.string_transformerchamber);
                animateVeiwVisible.add(transformer);
                animateVeiwVisible.add(pointstatus);
                break;
            case Constans.MapAttributeType.DOOR_METETR://户表
                setMDToolBarTitle(R.string.string_doormeter);
                animateVeiwVisible.add(lighting);
                animateVeiwVisible.add(power);
                animateVeiwVisible.add(connectDoor);
                break;
            case Constans.MapAttributeType.CABLE_PIT://电缆井
                setMDToolBarTitle(R.string.string_cablepit);
                animateVeiwVisible.add(pointstatus);
                break;
            case Constans.MapAttributeType.BOX_SWITCH_STATION://箱式开关站
                setMDToolBarTitle(R.string.string_boxswitchstation);
                animateVeiwVisible.add(pointstatus);
                break;
            case Constans.MapAttributeType.SP_SWITCHING_POST://开闭所
                setMDToolBarTitle(R.string.string_spswitchingpost);
                animateVeiwVisible.add(pointstatus);
                break;
            case Constans.MapAttributeType.BOX_TYPE_SUBSTATION://箱式变压站
                setMDToolBarTitle(R.string.string_boxtypesubstation);
                animateVeiwVisible.add(pointstatus);
                break;
        }

        for (View view : animateVeiwVisible) {
            view.setVisibility(View.VISIBLE);
        }
    }
    /**
     * 创建一行
     */
    private TableRow createNewTableRow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        TableRow tableRow = (TableRow) inflater.inflate(R.layout.tablerow_item, null);

        //初始化线模
        ValidaterEditText validaterEditText = (ValidaterEditText) tableRow.findViewById(R.id.id_edit_electriccable_itemmode);
        PickerListViewDialog mWireTypePickerScrollViewDialog = new PickerListViewDialog(this);
        mWireTypePickerScrollViewDialog.setPickerDialogBindEditText(validaterEditText);
        mWireTypePickerScrollViewDialog.setOnPickerDialogHasFocusListener(this);

        //初始化删除本行视图
        View deleteView = tableRow.findViewById(R.id.id_tablerow_deleterow);
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableRow toDeleteTableRow = (TableRow) v.getParent();
                toDeleteTableRow.setVisibility(View.GONE);
            }
        });

        //hide id
        TextView textView = (TextView) tableRow.findViewById(R.id.id_textview_hide_row_id);
        textView.setText(UUID.randomUUID().toString());//set default id
        return tableRow;
    }
    /**
     * 添加一行
     */
    private void addNewTableRowToTableLayout(TableRow tableRow) {
        mEditElectricCableTableLayout.addView(tableRow, mEditElectricCableTableLayout.getChildCount() - 1);
    }
    /**
     * 获取ElectricCable的所有行
     */
    private List<TableRow> findElectricCableTableLayoutChildTableRow() {
        List<TableRow> tableRowList = new ArrayList<>();
        int childCount = mEditElectricCableTableLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View tableRow = mEditElectricCableTableLayout.getChildAt(i);
            if (tableRow != null && tableRow instanceof TableRow) {
                tableRowList.add((TableRow) tableRow);
            }
        }
        return tableRowList;
    }
    /**
     * 计算立杆埋深
     **/
    private double calculateCoverDepth() {
        double coverDepth = 0;

        if (mapObj.getPointType() != Constans.MapAttributeType.VERTICAL_WELDING) {
            return coverDepth;
        }

        String electricPoleTypeId = getString(mEditElectricPoleType.getTag());
        if (TextUtils.isEmpty(electricPoleTypeId)) {
            return coverDepth;
        }

        ElectricPoleType electricPoleType = this.getDaoSession()
                .getElectricPoleTypeDao()
                .queryBuilder()
                .where(
                        ElectricPoleTypeDao.Properties.Id.eq(electricPoleTypeId)
                )
                .unique();

        if (electricPoleType == null) {
            return coverDepth;
        }

        String nameEn = electricPoleType.getMaterialNameEn();
        if (nameEn == null) {
            return coverDepth;
        }
        String strs[] = nameEn.split("-");
        if (strs.length < 2) {
            return coverDepth;
        }
        try {
            double num = Double.valueOf(strs[1]);
            if (num > 0) {
                return num / 10d + 0.7d;
            }
        } catch (Exception e) {
            Log.e(TAG, "onSetUpResult: " + e);
        }
        return coverDepth;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 初始化系统组件相关视图
     */
    @Override
    public void onInitView() {
        super.onInitView();
        setMdToolBar(R.id.id_material_toolbar);
        setMDToolBarBackEnable(true);
        setMDToolBarTitle(R.string.title_activity_point_attribute);

        //点位需要填写的数据信息
        mEditTowerType = (ValidaterEditText) findViewById(R.id.id_edit_towertype);

        mEditTransformerType = (ValidaterEditText) findViewById(R.id.id_edit_transformer);
        mEditElectricPoleType = (ValidaterEditText) findViewById(R.id.id_edit_electricpoletype);
        mEditElectricPoleTypeCount = (ValidaterEditText) findViewById(R.id.id_edit_electricpoletype_count);
        mEditGeologicalConditionsType = (ValidaterEditText) findViewById(R.id.id_edit_geologicalconditions);
        mEditEquipmentInstallationType = (ValidaterEditText) findViewById(R.id.id_edit_equipmentinstallation);
        mEditLandFrom = (ValidaterEditText) findViewById(R.id.id_edit_landform);

        mEditStatus = (RadioGroup) findViewById(R.id.id_edit_pointstatus);
        mEditLighting = (ValidaterEditText) findViewById(R.id.id_edit_lighting);
        mEditPower = (ValidaterEditText) findViewById(R.id.id_edit_power);
        mEditConnectDoorLength = (ValidaterEditText) findViewById(R.id.id_edit_connectdoorlength);

        mEditPointNum = (ValidaterEditText) findViewById(R.id.id_edit_attributenum);


        mEditSpecificationNumber = (ValidaterEditText) findViewById(R.id.id_edit_specificationnumber);
        //tableLayout
        mEditElectricCableTableLayout = (TableLayout) findViewById(R.id.id_tablelayout_electriccable);
        //拉线/导线属性
        Button addNewRowButton = (Button) mEditElectricCableTableLayout.findViewById(R.id.id_button_addtablerow);
        addNewRowButton.setOnClickListener(this);

        //初始化pickerDialog选择数据插件
        PickerListViewDialog mTowerTypePicker = new PickerListViewDialog(this);
        mTowerTypePicker.setPickerDialogBindEditText(mEditTowerType);
        mTowerTypePicker.setOnPickerDialogHasFocusListener(this);

        PickerListViewDialog mTransformerPicker = new PickerListViewDialog(this);
        mTransformerPicker.setPickerDialogBindEditText(mEditTransformerType);
        mTransformerPicker.setOnPickerDialogHasFocusListener(this);

        PickerListViewDialog mElectricPolePicker = new PickerListViewDialog(this);
        mElectricPolePicker.setPickerDialogBindEditText(mEditElectricPoleType);
        mElectricPolePicker.setOnPickerDialogHasFocusListener(this);

        PickerListViewDialog mGeologicalConditionsPicker = new PickerListViewDialog(this);
        mGeologicalConditionsPicker.setPickerDialogBindEditText(mEditGeologicalConditionsType);
        mGeologicalConditionsPicker.setOnPickerDialogHasFocusListener(this);

        PickerListViewDialog mEquipmentInstallationPicker = new PickerListViewDialog(this);
        mEquipmentInstallationPicker.setPickerDialogBindEditText(mEditEquipmentInstallationType);
        mEquipmentInstallationPicker.setOnPickerDialogHasFocusListener(this);

        PickerListViewDialog mLandFormPicker = new PickerListViewDialog(this);
        mLandFormPicker.setPickerDialogBindEditText(mEditLandFrom);
        mLandFormPicker.setOnPickerDialogHasFocusListener(this);
    }

}
