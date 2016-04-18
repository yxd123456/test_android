package com.hz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.hz.R;
import com.hz.activity.base.BaseActivity;
import com.hz.activity.base.BaseMapActivity;
import com.hz.common.Constans;
import com.hz.fragment.ProjectListFragment;
import com.hz.greendao.dao.MapLineEntity;
import com.hz.greendao.dao.MapLineItemEntity;
import com.hz.greendao.dao.MapPoiEntity;
import com.hz.greendao.dao.ProjectEntity;
import com.hz.helper.AddConnectWireHelper;
import com.hz.helper.AddCrossLineHelper;
import com.hz.helper.BatchAddConnectWireHelper;
import com.hz.helper.BatchUpdatePointHelper;
import com.hz.helper.DataBaseManagerHelper;
import com.hz.helper.FoundConnectPointsHelper;
import com.hz.helper.MapIconHelper;
import com.hz.helper.SharedPreferencesHelper;
import com.hz.sensor.listener.OrientationEventListener;
import com.hz.util.SharedPreferencesUtils;
import com.hz.view.PopupToast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends BaseMapActivity implements View.OnClickListener, OrientationEventListener.onOrientationChangeListener, BaiduMap.OnMapTouchListener {

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    public static final String TAG = MainActivity.class.getSimpleName();
    //线起始点控制
    public static final String LINE_START_KEY = "LINE_START_KEY";
    public static final String LINE_END_KEY = "LINE_END_KEY";
    private static boolean SINGGLE_LINE_CLICK = false;

    private ArrayList<Polyline> list_polyline = new ArrayList<Polyline>();
    private ArrayList<MapLineEntity> list_mle = new ArrayList<MapLineEntity>();

    private AddCrossLineHelper addCrossLineHelper = new AddCrossLineHelper();//连接跨越线工具类
    private AddConnectWireHelper addConnectWireHelper = new AddConnectWireHelper();//添加导线/电缆连线辅助类
    private BatchAddConnectWireHelper batchAddConnectWireHelper = new BatchAddConnectWireHelper();//批量添加导线辅助类
    private BatchUpdatePointHelper batchUpdatePointHelper = new BatchUpdatePointHelper();//批量修改点位信息辅助类

    private boolean isConnectWire = false;//标志是否正在连接跨越线
    private boolean isBatchLine = false;//标志是否在批量添加导线/电缆
    private boolean isBatchPoint = false;//标志当前是否在批量修改点位信息

    //地图操作控件
    private AppCompatImageView mLocationImaveView;
    private FrameLayout mEditPointChooseBar;//地图添加点位操作视图
    private FrameLayout mEditLineChooseBar;//地图添加跨越线操作视图
    private FrameLayout mEditLineConnectBar;//地图连线操作视图
    private FrameLayout mEditLineBatchConnectBar;//地图批量连线操作视图
    private FrameLayout mEditBatchPointChooseBar;//地图批量修改点位操作工具栏

    private ProjectEntity projectEntity;
    public long currentProjectId;//当前项目ID

    private OrientationEventListener orientationEventListener;
    /**
     * 方向传感器X方向的值
     */
    public int mXDirection = 0;
    /**
     * 定位精度
     **/
    public float mCurrentAccracy = 0;
    /**
     * 主线程Handler
     **/
    private Handler uiHandler = new Handler();
    /**
     * 后台线程Handler
     **/
    private Handler dataHandler;
    /**
     * 后台线程
     **/
    private HandlerThread dataThread;
    /**
     * 批量删除
     */
    private Button btn_delete;
    /**
     * 批量修改
     */
    private Button btn_change_all;

    private boolean flag_delete = false;
    public static boolean flag_change = false;
    private ArrayList<MapLineEntity> list_new_mle = new ArrayList<>();

    public static boolean FLAG_DELETE_SELECT = false;
    //private List<MapLineEntity> tempLineEntityList;
    //private MapLineEntity entity;
    private List<MapLineEntity> tempLineEntityList;

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override//-->initComponents();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        initComponents();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //跨越线开始结束点位
        addCrossLineHelper.clear();
        addConnectWireHelper.clear();
        orientationEventListener.stop();
        if (dataHandler != null) {
            dataHandler.removeCallbacksAndMessages(null);
        }
        if (dataThread != null) {
            dataThread.quit();
        }
        if (uiHandler != null) {
            uiHandler.removeCallbacksAndMessages(null);
        }
        System.gc();
    }

    //父类
    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        Log.d("Do", "onActivityResult");
        postToShowProgressHudWidthText("更新地图元素中...");

        dataHandler.post(new Runnable() {
            @Override
            public void run() {
                //1.根据点位类型判断  --新增：将数据保存到点位列表中,--修改：根据点位flag(唯一标识)更新对应点位数据信息
                switch (requestCode) {
                    case Constans.RequestCode.POINT_ATTRIBUTE_EDIT_REQUESTCODE://点位信息编辑返回标志
                        //更新点位信息
                        handlerPointEditResult(data);
                        //清除地图上所有的overLay
                        bdClear();
                        //2.更新地图点位图标，点位拉线，点位bundle信息
                        addMapPointMarkers(currentProjectId);
                        //重新在地图上打上线信息
                        // TODO: 2016/4/15

                        addMapLines();

                        if(flag_change){
                            flag_change = false;
                            BaseActivity.list_id.clear();
                            list_new_mle.clear();
                            list_polyline.clear();
                            list_id.clear();
                            tempLineEntityList.clear();
                        }
                        FLAG_DELETE_SELECT = false;
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //重置地图操作栏
                                clearMapOperateBar();
                                postToRemoveProgressHud();
                            }
                        });
                        break;
                    case Constans.RequestCode.LINE_ATTRIBUTE_EDIT_REQUESTCODE:
                        //更新拉线信息
                        handlerLineEditResult(data);
                        //清除地图上所有的overLay
                        bdClear();
                        //2.更新地图点位图标，点位拉线，点位bundle信息
                        addMapPointMarkers(currentProjectId);
                        //重新在地图上打上线信息
                        // TODO: 2016/4/15

                        addMapLines();

                        if(flag_change){
                            flag_change = false;
                            BaseActivity.list_id.clear();
                            list_new_mle.clear();
                            list_polyline.clear();
                            list_id.clear();
                            tempLineEntityList.clear();
                        }
                        FLAG_DELETE_SELECT = false;
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //重置地图操作栏
                                clearMapOperateBar();
                                postToRemoveProgressHud();
                            }
                        });
                        break;
                }

            }
        });
    }
    @Override
    public void onInitMapOption() {
        Log.d("Do", "onInitMapOption");
        super.onInitMapOption();
        //设置定位模式
        updateLocationModel(MyLocationConfiguration.LocationMode.NORMAL);
        mBaiduMap.setOnMapTouchListener(this);
    }
    @Override
    public void onMapLoaded() {
        super.onMapLoaded();
        Log.d("Do", "onMapLoaded");
        dataHandler.post(new Runnable() {
            @Override
            public void run() {
                addMapPointMarkers(currentProjectId);//更新地图点位信息
                addMapLines();//更新地图线信息
            }
        });
    }

    //接口
    @Override//处理地图上各种点击事件
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_btn_change_all:
                if(!flag_change){
                    flag_change = true;
                    startToBatchConnectLine();
                }

                break;
            case R.id.id_btn:
                if(!flag_delete) {
                    Toast.makeText(this, "开启批量删除", Toast.LENGTH_SHORT).show();
                    flag_delete = true;
                }else{
                    Toast.makeText(this, "关闭批量删除", Toast.LENGTH_SHORT).show();
                    flag_delete = false;
                }
                flag_delete = true;
                break;
            case R.id.id_ImageView_location://地图我的位置
                Log.d("Do", "id_ImageView_location");
                switchLocationMode();
                break;
            case R.id.id_ImageView_zoomin://地图放大
                Log.d("Do", "id_ImageView_zoomin");
                bdAnimateMapStatus(MapStatusUpdateFactory.zoomIn());
                break;
            case R.id.id_ImageView_zoomout://地图缩小
                Log.d("Do", "id_ImageView_zoomout");
                bdAnimateMapStatus(MapStatusUpdateFactory.zoomOut());
                break;
            case R.id.id_ImageView_point://地图开始选择点位
                Log.d("Do", "WSSW");
                startToChoosePoint();
                break;
            case R.id.id_ImageView_batch_point://地图批量修改点位信息
                Log.d("Do", "id_ImageView_batch_point");
                startToBatchPoint();
                break;
            case R.id.id_ImageView_crossline://开始画跨越线
                Log.d("Do", "id_ImageView_crossline");
                startToChooseCrossLine();
                break;
            case R.id.id_ImageView_line://连接点位之间的线条
                Log.d("Do", "id_ImageView_line");
                startToConnectLine();
                break;
            case R.id.id_ImageView_batchline://批量连接点位之间的线条
                Log.d("Do", "id_ImageView_batchline");
                startToBatchConnectLine();
                break;
            case R.id.id_ImageView_removeall://清除地图上的其他元素
                Log.d("Do", "id_ImageView_removeall");
                clearMapOperateBar();
                break;
            case R.id.id_button_verticalwelding://立杆
                Log.d("Do", "id_button_verticalwelding");
                checkMapZoomToEditPointAttribute(Constans.MapAttributeType.VERTICAL_WELDING);
                break;
            case R.id.id_button_transformerchamber://变压箱
                Log.d("Do", "id_button_transformerchamber");
                checkMapZoomToEditPointAttribute(Constans.MapAttributeType.TRANSFORMER_CHAMBER);
                break;
            case R.id.id_button_doormeter://户表
                Log.d("Do", "id_button_doormeter");
                checkMapZoomToEditPointAttribute(Constans.MapAttributeType.DOOR_METETR);
                break;
            case R.id.id_button_cablepit://电缆井
                Log.d("Do", "id_button_cablepit");
                checkMapZoomToEditPointAttribute(Constans.MapAttributeType.CABLE_PIT);
                break;
            case R.id.id_button_boxswitchstation://箱式开关站
                Log.d("Do", "id_button_boxswitchstation");
                checkMapZoomToEditPointAttribute(Constans.MapAttributeType.BOX_SWITCH_STATION);
                break;
            case R.id.id_button_spswitchingpost://开闭所
                Log.d("Do", "id_button_spswitchingpost");
                checkMapZoomToEditPointAttribute(Constans.MapAttributeType.SP_SWITCHING_POST);
                break;
            case R.id.id_button_boxtypesubstation://箱式变压站
                Log.d("Do", "id_button_boxtypesubstation");

                checkMapZoomToEditPointAttribute(Constans.MapAttributeType.BOX_TYPE_SUBSTATION);
                break;
            case R.id.id_button_addcancel://点编辑取消
                Log.d("Do", "id_button_addcancel");

                addCancel();
                break;
            case R.id.id_button_addcancel2://跨越线取消
                Log.d("Do", "id_button_addcancel2");

                addCancel();
                break;
            case R.id.id_button_addcancel3://连线取消
                Log.d("Do", "id_button_addcancel3");
                addCancel();
                break;
            case R.id.id_button_addcancel4://批量连线取消
                Log.d("Do", "id_button_addcancel4");
                addCancel();
                break;
            case R.id.id_button_addcancel5://批量修改点位信息取消
                Log.d("Do", "id_button_addcancel5");
                addCancel();
                break;
            case R.id.id_ImageView_maptype://地图类型切换
                Log.d("Do", "id_ImageView_maptype");

                mBaiduMap.setMapType((mBaiduMap.getMapType() == BaiduMap.MAP_TYPE_NORMAL) ? BaiduMap.MAP_TYPE_SATELLITE : BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.id_button_line_start://跨越线开始
                Log.d("Do", "id_button_line_start");

                addStartOrEndCrossLineMarker(LINE_START_KEY);
                break;
            case R.id.id_button_line_end://跨越线结束
                Log.d("Do", "id_button_line_end");

                addStartOrEndCrossLineMarker(LINE_END_KEY);
                break;
            case R.id.id_batchpoint_startpoint://批量修改点位开始
                Log.d("Do", "id_batchpoint_startpoint");

                addBatchUpdatePointStartOrEnd(LINE_START_KEY);
                break;
            case R.id.id_batchpoint_endpoint://批量修改点位结束
                Log.d("Do", "id_batchpoint_endpoint");

                addBatchUpdatePointStartOrEnd(LINE_END_KEY);
                break;
            case R.id.id_connectwire_startpoint://设置为开始点(导线/电缆)
                Log.d("Do", "id_connectwire_startpoint");

                chooseStartOrEndConnectWireLineBundle(LINE_START_KEY);
                break;
            case R.id.id_connectwire_endpoint://设置为开始点(导线/电缆)
                Log.d("Do", "id_connectwire_endpoint");

                chooseStartOrEndConnectWireLineBundle(LINE_END_KEY);
                break;
            case R.id.id_button_batchline_ok://批量连线确定
                Log.d("Do", "id_button_batchline_ok");
                if(flag_change){
                    // TODO: 2016/4/18 选中多条线段
                    //list_mle.get(0).setLineEditType(Constans.AttributeEditType.EDIT_TYPE_LINE_BATCHADD_C);
                    list_mle.get(0).getMapLineItemEntityList();
                    list_mle.get(0).setLineEditType(Constans.AttributeEditType.EDIT_TYPE_EDIT);
                    //list_mle.get(0).setLineType(Constans.MapAttributeType.WIRE_ELECTRIC_CABLE);//导线.电缆
                    toEditLineAttributeActivity(list_mle.get(0));
                   /* entity.getMapLineItemEntityList();
                    entity.setLineEditType(Constans.AttributeEditType.EDIT_TYPE_EDIT);
                    toEditLineAttributeActivity(entity);*/

                    return;
                }

                if (batchAddConnectWireHelper.markerSize() >= BatchAddConnectWireHelper.CONNECT_WIRE_NUM) {
                    MapLineEntity mapLineEntity = new MapLineEntity();
                    //线条编辑属性
                    mapLineEntity.setLineEditType(Constans.AttributeEditType.EDIT_TYPE_LINE_BATCHADD);
                    mapLineEntity.setLineType(Constans.MapAttributeType.WIRE_ELECTRIC_CABLE);//导线.电缆
                    toEditLineAttributeActivity(mapLineEntity);
                }
                break;
        }
    }
    @Override//当方向改变后，重新设置用户的定位信息
    public void onOrientationChange(float x) {
        Log.d("Do", "onOrientationChange");

        if (mMapView == null || mBaiduMap == null || userLocLatLng == null) {
            return;
        }
        mXDirection = (int) x;
        Log.d(TAG, "onOrientationChange: " + x);
        // 构造定位数据
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(mCurrentAccracy)
                .direction(mXDirection)// 此处设置开发者获取到的方向信息，顺时针0-360
                .latitude(userLocLatLng.latitude)
                .longitude(userLocLatLng.longitude).build();
        // 设置定位数据
        bdSetMyLocationData(locData);
    }
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        Log.d("Do", "onReceiveLocation");

        if (bdLocation == null || mMapView == null || mBaiduMap == null) {
            return;
        }
        int locType = bdLocation.getLocType();
        if (locType == BDLocation.TypeCriteriaException
                || locType == BDLocation.TypeNetWorkException
                || locType == BDLocation.TypeNone
                || locType == BDLocation.TypeOffLineLocationFail
                || locType == BDLocation.TypeOffLineLocationNetworkFail
                || locType == BDLocation.TypeServerError) {//error

            bdLocation = mLocationClient.getLastKnownLocation();
        }

        //修改地图位置信息
        MyLocationData locationData = new MyLocationData.Builder()
                .accuracy(bdLocation.getRadius())
                .direction(mXDirection)
                .latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude())
                .build();
        bdSetMyLocationData(locationData);
        mCurrentAccracy = bdLocation.getRadius();

        String text = "latitude:" + bdLocation.getLatitude()
                + "\n longitude:" + bdLocation.getLongitude()
                + "\n accuracy:" + bdLocation.getRadius()
                + "\n mXDirection:" + mXDirection
                + "\n locationMode:" + bdCurrentLocationMode()
                + "\n LocType:" + bdLocation.getLocType();

        text = text + "\nCoorType:" + bdLocation.getCoorType() + "\nNetworkLocationType:" + bdLocation.getNetworkLocationType();
        Log.d(TAG, text);
        if (latLongTextView != null) {
            latLongTextView.setText(text);
            latLongTextView.setLines(6);

            if (bdCurrentLocationMode() == MyLocationConfiguration.LocationMode.COMPASS) {
                latLongTextView.setVisibility(View.VISIBLE);
            } else {
                latLongTextView.setVisibility(View.GONE);
            }
        }

        if (locType == BDLocation.TypeNetWorkLocation
                || locType == BDLocation.TypeGpsLocation
                || locType == BDLocation.TypeOffLineLocation) {//ok

            userLocLatLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());

            if (bdCurrentLocationMode() == MyLocationConfiguration.LocationMode.NORMAL) {
                updateLocationModel(MyLocationConfiguration.LocationMode.NORMAL);
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(userLocLatLng, bdGetMaxZoomLevel() - 2);
                bdAnimateMapStatus(mapStatusUpdate);
            } else if (bdCurrentLocationMode() == MyLocationConfiguration.LocationMode.COMPASS) {
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(userLocLatLng, bdGetMaxZoomLevel() - 1);
                bdAnimateMapStatus(mapStatusUpdate);
                requestLocation();
            } else if (bdCurrentLocationMode() == MyLocationConfiguration.LocationMode.FOLLOWING) {
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(userLocLatLng, bdGetMaxZoomLevel());
                bdAnimateMapStatus(mapStatusUpdate);
                requestLocation();
            }
        } else if (locType == BDLocation.TypeCriteriaException
                || locType == BDLocation.TypeNetWorkException
                || locType == BDLocation.TypeNone
                || locType == BDLocation.TypeOffLineLocationFail
                || locType == BDLocation.TypeOffLineLocationNetworkFail
                || locType == BDLocation.TypeServerError) {//error

            PopupToast.show(this, Gravity.BOTTOM, "定位失败 " + locType, PopupToast.CUSTOME);
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(userLocLatLng, bdGetMaxZoomLevel());
            bdAnimateMapStatus(mapStatusUpdate);
            requestLocation();
        }
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("Do", "onMarkerClick");

        //在打点中不能连线，不能点击点位
        if (isPointing) {
            return false;
        }
        if (marker == null || marker.getExtraInfo() == null) {
            return false;
        }
        Serializable markerObj = marker.getExtraInfo().getSerializable(Constans.POINT_OBJ_KEY);
        if (markerObj == null) {
            return false;
        }
        MapPoiEntity editEntity = (MapPoiEntity) markerObj;


        //点位是跨越线开始或者结束的虚拟点位,不能用来连线
        if (editEntity.getPointType() == Constans.MapAttributeType.CROSS_LINE_START_POINT ||
                editEntity.getPointType() == Constans.MapAttributeType.CROSS_LINE_END_POINT) {
            return false;
        }

        //在连线中 点击后弹出infofWindow
        if (isConnectWire) {
            showInfoWindowFromPointLatLong(editEntity);
            return true;
        }

        //如果当前是在批量连接导线点击后添加文字标注
        if (isBatchLine) {
            batchAddConnectWireHelper.handleOnBatchLine(this, editEntity, marker);
            return true;
        }

        //如果当前是在批量修改点位
        if (isBatchPoint) {
            if (editEntity.getPointType() == Constans.MapAttributeType.VERTICAL_WELDING) {
                handlerBatchUpdatePoint(editEntity);
            } else {
                PopupToast.show(this, Gravity.BOTTOM, "只有立杆可以批量修改", PopupToast.CUSTOME);
            }
            return true;
        }

        //非连线中弹出编辑页面
        toEditPointAttributeActivity(marker.getExtraInfo(), Constans.AttributeEditType.EDIT_TYPE_EDIT, editEntity.getPointType());
        return true;
    }
    @Override//触发updateLocationModel，修改当前定位方式
    public void onTouch(MotionEvent motionEvent) {
        //Log.d("Do", "onTouch");

        if (mBaiduMap == null || motionEvent == null || motionEvent.getAction() != MotionEvent.ACTION_UP) {
            return;
        }
        if (bdCurrentLocationMode() != MyLocationConfiguration.LocationMode.NORMAL) {
            updateLocationModel(MyLocationConfiguration.LocationMode.NORMAL);
        }
    }
    @Override//当标签拖拽完毕后
    public void onMarkerDragEnd(final Marker marker) {
        Log.d("Do", "onMarkerDragEnd");

        dataHandler.post(new Runnable() {
            @Override
            public void run() {
                postToShowProgressHudWidthText("更新地图元素中...");
                markerDragHandler(marker);
                postToRemoveProgressHud();
            }
        });
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {// TODO: 2016/4/18 点击单线段进行编辑
        Log.d("Do", "onPolylineClick");

        MapLineEntity entity = (MapLineEntity) polyline.getExtraInfo().getSerializable(Constans.LINE_OBJ_KEY);

        if(flag_change){
            // TODO: 2016/4/13
            String tag = UUID.randomUUID().toString();
            DataBaseManagerHelper.getInstance().removeLineByLineId(entity.getLineId());
            entity.setLineId(tag);
            DataBaseManagerHelper.getInstance().addOrUpdateOneLineToDb(entity);
            list_mle.add(entity);
            addId(entity.getLineId());
            polyline.setColor(Color.RED);
            list_polyline.add(polyline);
            log("KO", "tag初始化："+tag);
            return true;
        } /*else {
            addId("test_id");
            SINGGLE_LINE_CLICK = true;
        }*/

        if(flag_delete){
            DataBaseManagerHelper.getInstance().removeLineByLineId(entity.getLineId());
            //清除地图上所有的overLay
            bdClear();
            //2.更新地图点位图标，点位拉线，点位bundle信息
            addMapPointMarkers(currentProjectId);
            //重新在地图上打上线信息
            addMapLines();
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    //重置地图操作栏
                    clearMapOperateBar();
                    postToRemoveProgressHud();
                }
            });
            return true;
        }

        if (entity != null) {
            entity.getMapLineItemEntityList();
            entity.setLineEditType(Constans.AttributeEditType.EDIT_TYPE_EDIT);
            toEditLineAttributeActivity(entity);
        }
        return true;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 开始选择跨越线
     */
    private void startToChooseCrossLine() {
        isConnectWire = false;
        isBatchLine = false;
        isBatchPoint = false;
        mEditLineChooseBar.setVisibility(View.VISIBLE);
        mEditPointChooseBar.setVisibility(View.GONE);
        mLocationImaveView.setVisibility(View.GONE);
        mEditLineConnectBar.setVisibility(View.GONE);
        mEditLineBatchConnectBar.setVisibility(View.GONE);
        mEditBatchPointChooseBar.setVisibility(View.GONE);
        addConnectWireHelper.clear();
        batchAddConnectWireHelper.clear();
        batchUpdatePointHelper.clear();
        addMapCenterImageView();
        updateLocationModel(MyLocationConfiguration.LocationMode.NORMAL);
        MapStatusUpdate zoomToMin2 = MapStatusUpdateFactory.zoomTo(bdGetMaxZoomLevel());
        bdAnimateMapStatus(zoomToMin2);
        bdHideInfoWindow();
    }
    /**
     * 开始连接点位之间的线
     */
    private void startToConnectLine() {
        isConnectWire = true;
        isBatchLine = false;
        isBatchPoint = false;
        mEditLineConnectBar.setVisibility(View.VISIBLE);
        mEditPointChooseBar.setVisibility(View.GONE);
        mLocationImaveView.setVisibility(View.GONE);
        mEditLineChooseBar.setVisibility(View.GONE);
        mEditLineBatchConnectBar.setVisibility(View.GONE);
        mEditBatchPointChooseBar.setVisibility(View.GONE);
        addCrossLineHelper.clear();
        batchAddConnectWireHelper.clear();
        batchUpdatePointHelper.clear();
        addConnectWireHelper.clear();
        removeMapCenterImageView();
        updateLocationModel(MyLocationConfiguration.LocationMode.NORMAL);
    }
    /**
     * 开始批量连接点位之间的线
     */
    private void startToBatchConnectLine() {
        isConnectWire = false;
        isBatchLine = true;
        isBatchPoint = false;
        mEditLineBatchConnectBar.setVisibility(View.VISIBLE);
        AppCompatButton btn = (AppCompatButton) mEditLineBatchConnectBar.findViewById(R.id.id_button_addcancel4);
        if(flag_change){
            btn.setText(R.string.giveup_change);
        } else {
            btn.setText(R.string.string_addcancel_batchline);
        }
        mEditPointChooseBar.setVisibility(View.GONE);
        mLocationImaveView.setVisibility(View.GONE);
        mEditLineChooseBar.setVisibility(View.GONE);
        mEditLineConnectBar.setVisibility(View.GONE);
        mEditBatchPointChooseBar.setVisibility(View.GONE);
        addConnectWireHelper.clear();
        addCrossLineHelper.clear();
        batchAddConnectWireHelper.clear();
        batchUpdatePointHelper.clear();
        removeMapCenterImageView();
        updateLocationModel(MyLocationConfiguration.LocationMode.NORMAL);
        bdHideInfoWindow();
    }
    /**
     * 开始选择地图打点点位
     */
    private void startToChoosePoint() {
        isConnectWire = false;
        isBatchLine = false;
        isBatchPoint = false;
        mEditPointChooseBar.setVisibility(View.VISIBLE);
        mLocationImaveView.setVisibility(View.GONE);
        mEditLineChooseBar.setVisibility(View.GONE);
        mEditLineConnectBar.setVisibility(View.GONE);
        mEditLineBatchConnectBar.setVisibility(View.GONE);
        mEditBatchPointChooseBar.setVisibility(View.GONE);
        addCrossLineHelper.clear();
        addConnectWireHelper.clear();
        batchAddConnectWireHelper.clear();
        batchUpdatePointHelper.clear();
        addMapCenterImageView();//在地图中间添加一个标签
        updateLocationModel(MyLocationConfiguration.LocationMode.NORMAL);
        MapStatusUpdate zoomToMin = MapStatusUpdateFactory.zoomTo(bdGetMaxZoomLevel());
        bdAnimateMapStatus(zoomToMin);
        bdHideInfoWindow();
    }
    /**
     * 开始批量修改点位
     **/
    private void startToBatchPoint() {
        isConnectWire = false;
        isBatchLine = false;
        isBatchPoint = true;
        mEditBatchPointChooseBar.setVisibility(View.VISIBLE);
        mEditPointChooseBar.setVisibility(View.GONE);
        mLocationImaveView.setVisibility(View.GONE);
        mEditLineChooseBar.setVisibility(View.GONE);
        mEditLineConnectBar.setVisibility(View.GONE);
        mEditLineBatchConnectBar.setVisibility(View.GONE);
        addConnectWireHelper.clear();
        addCrossLineHelper.clear();
        batchAddConnectWireHelper.clear();
        batchUpdatePointHelper.clear();
        removeMapCenterImageView();
        updateLocationModel(MyLocationConfiguration.LocationMode.NORMAL);
        bdHideInfoWindow();
    }
    /**
     * 清除地图上的其他元素
     */
    private void clearMapOperateBar() {
        isConnectWire = false;
        isBatchLine = false;
        isBatchPoint = false;
        mLocationImaveView.setVisibility(View.VISIBLE);
        mEditLineChooseBar.setVisibility(View.GONE);
        mEditPointChooseBar.setVisibility(View.GONE);
        mEditLineConnectBar.setVisibility(View.GONE);
        mEditLineBatchConnectBar.setVisibility(View.GONE);
        mEditBatchPointChooseBar.setVisibility(View.GONE);
        addConnectWireHelper.clear();
        addCrossLineHelper.clear();
        batchAddConnectWireHelper.clear();
        batchUpdatePointHelper.clear();
        removeMapCenterImageView();
        updateLocationModel(MyLocationConfiguration.LocationMode.NORMAL);
        bdHideInfoWindow();
    }
    /**
     * 检查地图状态，设置地图视图显示隐藏。并跳转到相应的新增点位页面
     */
    private void checkMapZoomToEditPointAttribute(int attributeType) {
        if (!checkIsMaxZoomMap()) {
            return;
        }
        Bundle bundle = getMapCenterImageViewBundle();
        if (bundle == null) {
            return;
        }
        toEditPointAttributeActivity(bundle, Constans.AttributeEditType.EDIT_TYPE_ADD, attributeType);
    }
    /**
     * 编辑取消
     */
    private void addCancel() {
        for (Polyline polyline : list_polyline){
            polyline.setColor(Color.WHITE);
        }
        flag_change = false;
        clearMapOperateBar();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(getMapCenterLatLongFormScreen(), bdGetMaxZoomLevel() - 2);
        bdAnimateMapStatus(mapStatusUpdate);
    }
    /**
     * 批量修改点位 设置为开始点或者结束点
     * @param lineKey 开始或者结束点位键
     */
    private void addBatchUpdatePointStartOrEnd(String lineKey) {
        bdHideInfoWindow();

        batchUpdatePointHelper.put(lineKey);
        if (!batchUpdatePointHelper.isSizeEnough()) {
            return;
        }
        //设置传入编辑线属性页面属性值
        MapPoiEntity startEntity = batchUpdatePointHelper.get(LINE_START_KEY);
        MapPoiEntity endEntity = batchUpdatePointHelper.get(LINE_END_KEY);

        //如果开始点和结束点ID相同
        if (batchUpdatePointHelper.isIdEquals()) {
            batchUpdatePointHelper.remove(lineKey);
            return;
        }

        List<MapPoiEntity> lineConnectPointEntitys = FoundConnectPointsHelper.getLineConnectPointFrom(
                startEntity.getPointId(),
                endEntity.getPointId(),
                currentProjectId,
                SharedPreferencesHelper.getUserId(this)
        );
        if (lineConnectPointEntitys == null || lineConnectPointEntitys.size() == 0) {
            PopupToast.showError(this, "两个点位之间没有线连接,请重新选择起始点");
            batchUpdatePointHelper.clear();
            return;
        }
        for (MapPoiEntity poiEntity : lineConnectPointEntitys) {
            if (poiEntity.getPointType() != Constans.MapAttributeType.VERTICAL_WELDING) {
                PopupToast.showError(this, "两个点位之间有非立杆点位,请重新选择起始点");
                batchUpdatePointHelper.clear();
                return;
            }
        }
        batchUpdatePointHelper.setMapPoiEntityList(lineConnectPointEntitys);

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constans.POINT_OBJ_KEY, new MapPoiEntity());
        toEditPointAttributeActivity(bundle, Constans.AttributeEditType.EDIT_TYPE_POINT_BATCHUPDATE, Constans.MapAttributeType.VERTICAL_WELDING);
    }
    /**
     * 选择导线/电缆的 开始点或者结束点
     * @param lineKey 开始或者结束点位键
     */
    private void chooseStartOrEndConnectWireLineBundle(String lineKey) {
        bdHideInfoWindow();

        addConnectWireHelper.put(lineKey);
        if (!addConnectWireHelper.isSizeEnough()) {
            return;
        }
        //设置传入编辑线属性页面属性值
        MapPoiEntity startEntity = addConnectWireHelper.get(LINE_START_KEY);
        MapPoiEntity endEntity = addConnectWireHelper.get(LINE_END_KEY);

        //如果开始点和结束点ID相同
        if (addConnectWireHelper.isIdEquals()) {
            addConnectWireHelper.remove(lineKey);
            return;
        }

        MapLineEntity mapLineEntity = new MapLineEntity();
        //线条编辑属性
        mapLineEntity.setLineEditType(Constans.AttributeEditType.EDIT_TYPE_ADD);
        mapLineEntity.setLineType(Constans.MapAttributeType.WIRE_ELECTRIC_CABLE);//导线.电缆

        //线条关联的开始点结束点属性
        mapLineEntity.setLineStartLatitude(startEntity.getPointLatitude());
        mapLineEntity.setLineStartLongitude(startEntity.getPointLongitude());
        mapLineEntity.setLineStartPointId(startEntity.getPointId());
        mapLineEntity.setLineStartPointName(startEntity.getPointName());

        mapLineEntity.setLineEndLatitude(endEntity.getPointLatitude());
        mapLineEntity.setLineEndLongitude(endEntity.getPointLongitude());
        mapLineEntity.setLineEndPointId(endEntity.getPointId());
        mapLineEntity.setLineEndPointName(endEntity.getPointName());

        toEditLineAttributeActivity(mapLineEntity);
    }
    /**
     * 根据跨越线类型添加跨越线
     * @param lineKey key
     */
    private void addStartOrEndCrossLineMarker(String lineKey) {
        Bundle bundle = getMapCenterImageViewBundle();
        if (bundle == null) {
            return;
        }
        MapPoiEntity poiEntity = (MapPoiEntity) bundle.getSerializable(Constans.POINT_OBJ_KEY);

        boolean addCrossLineOk = addCrossLineHelper.addStartOrEndPoint(this, lineKey, poiEntity);

        if (!addCrossLineOk) {//返回true代表跨越线添加成功
            return;
        }
        MapPoiEntity startEntity = addCrossLineHelper.getStartPointEntity();
        MapPoiEntity endEntity = addCrossLineHelper.getEndPointEntity();

        MapLineEntity mapLineEntity = new MapLineEntity();
        mapLineEntity.setLineEditType(Constans.AttributeEditType.EDIT_TYPE_ADD);
        mapLineEntity.setLineType(Constans.MapAttributeType.CROSS_LINE);//跨越线
        mapLineEntity.setLineStartLatitude(startEntity.getPointLatitude());
        mapLineEntity.setLineStartLongitude(startEntity.getPointLongitude());
        mapLineEntity.setLineEndLatitude(endEntity.getPointLatitude());
        mapLineEntity.setLineEndLongitude(endEntity.getPointLongitude());

        toEditLineAttributeActivity(mapLineEntity);
    }

    /**
     * 更新地图线信息
     */
    private void addMapLines() {

        tempLineEntityList = DataBaseManagerHelper.getInstance().getAlllinesByProjectId(currentProjectId);
        log("KO", "?3");
        if(!FLAG_DELETE_SELECT) {
            log("KO", "?1");
            if (list_new_mle != null && list_new_mle.size() != 0) {
                log("KO", "?2");
                for (MapLineEntity lineEntity : tempLineEntityList) {
                    log("KO", lineEntity.getLineId() + "********************************");
                    for (String tag : BaseActivity.list_id) {
                        log("KO", tag + "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                        if (lineEntity.getLineId().equals(tag)) {// TODO: 修改所选线条的信息
                            String lineName = (String) SharedPreferencesUtils.getParam(MainActivity.this, LineAttributeActivity.LINE_NAME, list_new_mle.get(0).getLineName());
                            log("KO", lineName + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            // TODO: 更新规格线数
                            int Line_Specification_Number = (int) SharedPreferencesUtils.getParam(MainActivity.this, LineAttributeActivity.Line_Specification_Number, list_new_mle.get(0).getLineSpecificationNumber());
                            log("dddddddddddddddd"+Line_Specification_Number);
                            lineEntity.setLineSpecificationNumber(Line_Specification_Number);
                            lineEntity.setLineName(lineName);
                            List<MapLineItemEntity> mapLineItemEntityList = lineEntity.getMapLineItemEntityList();
                            if (mapLineItemEntityList != null && mapLineItemEntityList.size() > 0) {
                                for (MapLineItemEntity lineItemEntity : mapLineItemEntityList) {

                                }
                            }


                            if (flag_change) {
                                DataBaseManagerHelper.getInstance().removeLineByLineId(lineEntity.getLineId());
                            }
                            DataBaseManagerHelper.getInstance().addOrUpdateOneLineToDb(lineEntity);
                        }
                    }
                }
            }
        }


        for (MapLineEntity lineEntity : tempLineEntityList) {
            //添加新点位
            if (lineEntity == null) {
                continue;
            }
            LatLng startLatlong = new LatLng(lineEntity.getLineStartLatitude(), lineEntity.getLineStartLongitude());
            LatLng endLatlong = new LatLng(lineEntity.getLineEndLatitude(), lineEntity.getLineEndLongitude());

            int color = Color.BLACK;
            String text = "text";
            switch (lineEntity.getLineType()) {
                case Constans.MapAttributeType.CROSS_LINE://跨越线
                    color = Color.BLUE;
                    text = DataBaseManagerHelper.getInstance().getLineWireTypeNameById(lineEntity.getLineWireTypeId());
                    addCrossLinePoints(lineEntity);
                    break;
                case Constans.MapAttributeType.WIRE_ELECTRIC_CABLE://导线电缆
                    color = convertLineColorByLineItems(lineEntity.getMapLineItemEntityList());
                    text = Constans.DECIMALFORMAT_M.format(DistanceUtil.getDistance(startLatlong, endLatlong));
                    break;
            }

            addMapPolylineWithBundle(Arrays.asList(startLatlong, endLatlong), color, lineEntity);
            addMapPolylineCenterText(startLatlong, endLatlong, text);
        }
    }


    /**
     * 根据线的个数和新旧显示不同的颜色
     **/
    private int convertLineColorByLineItems(List<MapLineItemEntity> mapLineItemEntityList) {
        int lineColor = Color.WHITE;
        if (mapLineItemEntityList == null || mapLineItemEntityList.size() == 0) {
            return lineColor;
        }
        //过滤已经被删除的线
        List<MapLineItemEntity> notRemoveLineItemEntityList = new ArrayList<>();
        for (MapLineItemEntity itemEntity : mapLineItemEntityList) {
            if (itemEntity.getLineItemRemoved() == Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL) {
                notRemoveLineItemEntityList.add(itemEntity);
            }
        }

        if (notRemoveLineItemEntityList.size() == 0) {
            return lineColor;
        }

        switch (notRemoveLineItemEntityList.size()) {
            case 1:
                MapLineItemEntity itemEntity0 = notRemoveLineItemEntityList.get(0);
                if (itemEntity0.getLineItemStatus() == Constans.AttributeStatus.NEW) {
                    lineColor = Color.GREEN;
                } else {
                    lineColor = Color.CYAN;
                }
                break;
            case 2:
                MapLineItemEntity itemEntity1 = notRemoveLineItemEntityList.get(0);
                MapLineItemEntity itemEntity2 = notRemoveLineItemEntityList.get(1);
                int status1 = itemEntity1.getLineItemStatus();
                int status2 = itemEntity2.getLineItemStatus();

                if (status1 == Constans.AttributeStatus.NEW && status2 == Constans.AttributeStatus.NEW) {
                    lineColor = Color.GREEN;
                } else if (status1 == Constans.AttributeStatus.OLD && status2 == Constans.AttributeStatus.OLD) {
                    lineColor = Color.CYAN;
                } else {
                    lineColor = Color.parseColor("#9BCD9B");//淡绿色
                }
                break;
            default:
                lineColor = Color.RED;
                break;
        }
        return lineColor;
    }
    /***
     * 添加跨越线两侧的点位
     * @param lineEntity 跨越线关联的线对象信息
     **/
    private void addCrossLinePoints(MapLineEntity lineEntity) {
        //跨越线开始点
        MapPoiEntity crossLineStart = new MapPoiEntity(
                lineEntity.getLineId(),
                lineEntity.getLineStartLatitude(),
                lineEntity.getLineStartLongitude(),
                Constans.MapAttributeType.CROSS_LINE_START_POINT
        );
        BitmapDescriptor bitmapDescriptorStart = MapIconHelper.getInstance().generateBitmapIconByNum(MapIconHelper.KEY_CROSSLINE_POINT, Color.WHITE);
        addMapPoiMarkerWithObj(crossLineStart, bitmapDescriptorStart);

        //跨越线结束点
        MapPoiEntity crossLineEnd = new MapPoiEntity(
                lineEntity.getLineId(),
                lineEntity.getLineEndLatitude(),
                lineEntity.getLineEndLongitude(),
                Constans.MapAttributeType.CROSS_LINE_END_POINT
        );
        BitmapDescriptor bitmapDescriptorEnd = MapIconHelper.getInstance().generateBitmapIconByNum(MapIconHelper.KEY_CROSSLINE_POINT, Color.WHITE);
        addMapPoiMarkerWithObj(crossLineEnd, bitmapDescriptorEnd);
    }
    /**
     * 打开新的activity页面选择线属性
     * @param mapLineEntity 线属性
     */
    public void toEditLineAttributeActivity(MapLineEntity mapLineEntity) {// TODO: 2016/4/18 前往编辑属性
        //修改导线/电缆属性时 如果是重复添加的 则修改
        if (mapLineEntity.getLineEditType() == Constans.AttributeEditType.EDIT_TYPE_ADD && mapLineEntity.getLineType() == Constans.MapAttributeType.WIRE_ELECTRIC_CABLE) {
            //如果两点之间已经有一条线
            MapLineEntity lineEntity = DataBaseManagerHelper.getInstance().getLineByLineStartPointIdAndEndPointId(mapLineEntity.getLineStartPointId(), mapLineEntity.getLineEndPointId());
            if (lineEntity != null) {
                lineEntity.getMapLineItemEntityList();//查询线关联的线item信息
                mapLineEntity = lineEntity;
                mapLineEntity.setLineEditType(Constans.AttributeEditType.EDIT_TYPE_EDIT);
            }
        }
        Bundle bundle = new Bundle();
        mapLineEntity.setLineProjId(currentProjectId);
        mapLineEntity.setLineUserId(SharedPreferencesHelper.getUserId(this));
        bundle.putSerializable(Constans.LINE_OBJ_KEY, mapLineEntity);
        Intent intent = new Intent(this, LineAttributeActivity.class);
        //传入项目数据
        bundle.putSerializable(ProjectListFragment.PROJECT_OBJ_KEY, projectEntity);
        intent.putExtras(bundle);
        startActivityForResult(intent, Constans.RequestCode.LINE_ATTRIBUTE_EDIT_REQUESTCODE);
    }
    public void toEditLineAttributeActivity(ArrayList<MapLineEntity> list) {
        //修改导线/电缆属性时 如果是重复添加的 则修改
        for (MapLineEntity mapLineEntity:list
             ) {
            if (mapLineEntity.getLineEditType() == Constans.AttributeEditType.EDIT_TYPE_ADD && mapLineEntity.getLineType() == Constans.MapAttributeType.WIRE_ELECTRIC_CABLE) {
                //如果两点之间已经有一条线
                MapLineEntity lineEntity = DataBaseManagerHelper.getInstance().getLineByLineStartPointIdAndEndPointId(mapLineEntity.getLineStartPointId(), mapLineEntity.getLineEndPointId());
                if (lineEntity != null) {
                    lineEntity.getMapLineItemEntityList();//查询线关联的线item信息
                    mapLineEntity = lineEntity;
                    mapLineEntity.setLineEditType(Constans.AttributeEditType.EDIT_TYPE_EDIT);
                }
            }
        }
        for (MapLineEntity mapLineEntity:list
             ) {
            mapLineEntity.setLineProjId(currentProjectId);
            mapLineEntity.setLineUserId(SharedPreferencesHelper.getUserId(this));
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constans.LINE_OBJ_KEY_TEST, list_mle);
        Intent intent = new Intent(this, LineAttributeActivity.class);
        //传入项目数据
        bundle.putSerializable(ProjectListFragment.PROJECT_OBJ_KEY, projectEntity);
        intent.putExtras(bundle);
        startActivityForResult(intent, Constans.RequestCode.LINE_ATTRIBUTE_EDIT_REQUESTCODE);
    }
    /**
     * 打开新的activity页面选择点位属性
     * @param bundle        传入点位编辑activity的参数
     * @param pointEditType 地图编辑类型 新增或者是修改
     * @param pointType     点位的类型信息,立杆，变压箱，户表，电缆井，箱式开关站，开闭所，箱式变压站
     */
    private void toEditPointAttributeActivity(Bundle bundle, int pointEditType, int pointType) {
        MapPoiEntity editEntity = (MapPoiEntity) bundle.getSerializable(Constans.POINT_OBJ_KEY);
        if (editEntity == null) {
            return;
        }
        editEntity.setPointEditType(pointEditType);
        editEntity.setPointType(pointType);
        editEntity.setPointUserId(SharedPreferencesHelper.getUserId(this));
        editEntity.setPointProjId(currentProjectId);

        switch (pointEditType) {
            case Constans.AttributeEditType.EDIT_TYPE_EDIT: //修改marker属性时，根据点位的ID查询出对应的图库信息
                editEntity.setPointGalleryLists(DataBaseManagerHelper.getInstance().getPointImagesByPointId(editEntity.getPointId()));
                break;
            case Constans.AttributeEditType.EDIT_TYPE_ADD: //新增marker时，根据类型在库中查找同类的属性并填入 （根据时间排序获取最新的一条数据）
                MapPoiEntity prePoiEntity = DataBaseManagerHelper.getInstance().getpointEntityByPointType(editEntity.getPointType(), currentProjectId, SharedPreferencesHelper.getUserId(this));
                if (prePoiEntity != null) {
                    editEntity.setPointName(prePoiEntity.getPointName());
                    editEntity.setPointNote(prePoiEntity.getPointNote());
                    editEntity.setPointLightingNum(prePoiEntity.getPointLightingNum());
                    editEntity.setPointPowerNum(prePoiEntity.getPointPowerNum());
                    editEntity.setPointConnectDoorNum(prePoiEntity.getPointConnectDoorNum());
                    editEntity.setPointTransformerTypeId(prePoiEntity.getPointTransformerTypeId());
                    editEntity.setPointStatus(prePoiEntity.getPointStatus());
                    editEntity.setPointTowerTypeId(prePoiEntity.getPointTowerTypeId());
                    editEntity.setPointGeologicalConditionsTypeId(prePoiEntity.getPointGeologicalConditionsTypeId());
                    editEntity.setPointEquipmentInstallationTypeId(prePoiEntity.getPointEquipmentInstallationTypeId());
                    editEntity.setPointElectricPoleTypeId(prePoiEntity.getPointElectricPoleTypeId());
                    editEntity.setPointNum(prePoiEntity.getPointNum());
                    editEntity.setPointLandForm(prePoiEntity.getPointLandForm());
                    editEntity.setPointElectricPoleTypeCount(prePoiEntity.getPointElectricPoleTypeCount());

                    PopupToast.show(this, Gravity.BOTTOM, getResources().getString(R.string.string_fillsamedata_frompre), PopupToast.CUSTOME);
                }
                break;
        }

        //传入项目数据
        bundle.putSerializable(ProjectListFragment.PROJECT_OBJ_KEY, projectEntity);
        Intent intent = new Intent(this, PointAttributeActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, Constans.RequestCode.POINT_ATTRIBUTE_EDIT_REQUESTCODE);
    }

    private void postToShowProgressHudWidthText(final String title) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                showProgressHudWidthText(title);
            }
        });
    }

    private void postToRemoveProgressHud() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                removeProgressHud();
            }
        });
    }
    /***
     * 地图图标拖动处理
     **/
    private void markerDragHandler(Marker marker) {
        MapPoiEntity poiEntity = (MapPoiEntity) marker.getExtraInfo().getSerializable(Constans.POINT_OBJ_KEY);
        if (poiEntity == null) {
            return;
        }
        double dragEndLatitude = marker.getPosition().latitude;
        double dragEndLongitude = marker.getPosition().longitude;

        Intent dragEndIntent = new Intent();
        dragEndIntent.putExtra(Constans.POINT_OBJ_KEY, poiEntity);


        if (poiEntity.getPointType() == Constans.MapAttributeType.CROSS_LINE_START_POINT        //跨越线开始虚拟点位
                || poiEntity.getPointType() == Constans.MapAttributeType.CROSS_LINE_END_POINT) {//跨越线结束虚拟点位
            DataBaseManagerHelper.getInstance().updateCrossLinePointLocation(
                    poiEntity.getPointId(),
                    poiEntity.getPointType(),//虚拟点位类型 跨越线开始或者结束
                    dragEndLatitude,//点位拖动后经度
                    dragEndLongitude,//点位拖动后纬度
                    currentProjectId,//当前项目ID
                    SharedPreferencesHelper.getUserId(this)//当前用户ID
            );
        } else {//真实点位
            poiEntity.setPointEditType(Constans.AttributeEditType.EDIT_TYPE_EDIT);
            poiEntity.setPointLatitude(dragEndLatitude);
            poiEntity.setPointLongitude(dragEndLongitude);
            handlerPointEditResult(dragEndIntent);
        }

        //清除地图上所有的overLay
        bdClear();
        //2.更新地图点位图标，点位拉线，点位bundle信息
        addMapPointMarkers(currentProjectId);
        //重新在地图上打上线信息
        addMapLines();
        //清除批量添加线的临时数据
        batchAddConnectWireHelper.clear();
    }
    /**
     * 线编辑返回信息处理
     * @param data 处理线编辑返回的数据
     */
    private void handlerLineEditResult(Intent data) {
        if (data == null) {
            return;
        }
        //MapLineEntity lineEntity = (MapLineEntity) data.getExtras().getSerializable(Constans.LINE_OBJ_KEY);
        MapLineEntity lineEntity = null;
        if(!flag_change||FLAG_DELETE_SELECT){
            lineEntity = (MapLineEntity) data.getExtras().getSerializable(Constans.LINE_OBJ_KEY);
        } else {
            lineEntity = list_mle.get(0);
        }
        if (lineEntity == null) {
            return;
        }
        int editType = lineEntity.getLineEditType();
        Log.d("KO", "开放的接口 "+editType);
        switch (editType) {
            case Constans.AttributeEditType.EDIT_TYPE_ADD: {//新增线信息是要设置点位的唯一标记信息
                Log.d("KO", "开放的接口 "+1);
                lineEntity.setLineProjId(currentProjectId);
                lineEntity.setLineId(UUID.randomUUID().toString());//设置线唯一ID
                DataBaseManagerHelper.getInstance().addOrUpdateOneLineToDb(lineEntity);
                break;
            }
            case Constans.AttributeEditType.EDIT_TYPE_EDIT: {//修改线时替换信息
                Log.d("KO", "开放的接口 "+2);
                DataBaseManagerHelper.getInstance().addOrUpdateOneLineToDb(lineEntity);
                break;
            }
            case Constans.AttributeEditType.EDIT_TYPE_LINE_BATCHADD_C: {//修改线时替换信息
                list_new_mle.clear();
                list_new_mle.addAll(batchAddConnectWireHelper.handlerBatchAddLine(this, lineEntity, currentProjectId, SharedPreferencesHelper.getUserId(this), list_mle));
                break;
            }
            case Constans.AttributeEditType.EDIT_TYPE_REMOVE: {//移除线
                DataBaseManagerHelper.getInstance().removeLineByLineId(lineEntity.getLineId());
                break;
            }
            case Constans.AttributeEditType.EDIT_TYPE_REMOVE_SELECT: {//移除线
                log("KO", "计划B");
                    for (MapLineEntity line : tempLineEntityList) {
                        log("KO", line.getLineId()+"&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                        for (String tag : BaseActivity.list_id){
                            log("KO", tag + "***************************");
                            if(line.getLineId().equals(tag)){
                                DataBaseManagerHelper.getInstance().removeLineByLineId(line.getLineId());
                                log("KO", "计划C");
                            }
                        }
                    }

                break;
            }
            case Constans.AttributeEditType.EDIT_TYPE_LINE_BATCHADD://批量添加点位返回
                Log.d("KO", "开放的接口 "+4);// TODO: 2016/4/14  
                batchAddConnectWireHelper.handlerBatchAddLine(lineEntity, currentProjectId, SharedPreferencesHelper.getUserId(this));
                break;
        }
    }
    /**
     * 点位编辑返回信息处理
     * @param data 点编辑返回的数据
     */
    private void handlerPointEditResult(Intent data) {
        if (data == null) {
            return;
        }
        MapPoiEntity poiEntity = (MapPoiEntity) data.getExtras().getSerializable(Constans.POINT_OBJ_KEY);
        if (poiEntity == null) {
            return;
        }
        int editType = poiEntity.getPointEditType();
        String bundlePointId = poiEntity.getPointId();

        switch (editType) {
            case Constans.AttributeEditType.EDIT_TYPE_ADD: {//新增点位信息是要设置点位的唯一标记信息
                poiEntity.setPointId(UUID.randomUUID().toString());//生成点位唯一标识
                DataBaseManagerHelper.getInstance().addOrUpdateOnePointsToDb(poiEntity);
                break;
            }
            case Constans.AttributeEditType.EDIT_TYPE_EDIT: {//修改点位时替换信息
                DataBaseManagerHelper.getInstance().addOrUpdateOnePointsToDb(poiEntity);
                DataBaseManagerHelper.getInstance().updatePointTargetLineNameAndLatitude(poiEntity.getPointId(), poiEntity.getPointName(), poiEntity.getPointLatitude(), poiEntity.getPointLongitude());
                break;
            }
            case Constans.AttributeEditType.EDIT_TYPE_REMOVE: {//移除点位
                DataBaseManagerHelper.getInstance().removePointsAndTargetGalleryFromDb(bundlePointId);
                DataBaseManagerHelper.getInstance().removePointTargetLines(bundlePointId);
                break;
            }
            case Constans.AttributeEditType.EDIT_TYPE_POINT_BATCHUPDATE: {//批量修改点位
                batchUpdatePointHelper.batchUpdatePointResultHandler(poiEntity);
                MapLineEntity lineEntity = (MapLineEntity) data.getExtras().getSerializable(Constans.LINE_OBJ_KEY);
                if (lineEntity == null) {
                    return;
                }
                batchUpdatePointHelper.batchUpdateLineResultHandle(lineEntity);
                break;
            }
        }
    }
    /**
     * 修改当前定位方式
     */
    public void updateLocationModel(MyLocationConfiguration.LocationMode locationMode) {
        if (locationMode != null) {
            bdSetMyLocationConfigeration(new MyLocationConfiguration(locationMode, true, null));
            if (locationMode == MyLocationConfiguration.LocationMode.NORMAL) {
                PopupToast.show(this, Gravity.BOTTOM, "切换定位模式到 正常", PopupToast.CUSTOME);
                if (mLocationImaveView != null) {
                    mLocationImaveView.setBackgroundResource(R.drawable.main_icon_location);
                }
                setBdMapLocationEnable(false);
                if (orientationEventListener != null) {
                    orientationEventListener.stop();
                }
                twenceAnimateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().overlook(0).build()));
            } else if (locationMode == MyLocationConfiguration.LocationMode.FOLLOWING) {
                PopupToast.show(this, Gravity.BOTTOM, "切换定位模式到 跟随", PopupToast.CUSTOME);
                if (mLocationImaveView != null) {
                    mLocationImaveView.setBackgroundResource(R.drawable.main_icon_follow);
                }
                setBdMapLocationEnable(true);
                requestLocation();
                if (orientationEventListener != null) {
                    orientationEventListener.start();
                }
            } else if (locationMode == MyLocationConfiguration.LocationMode.COMPASS) {
                PopupToast.show(this, Gravity.BOTTOM, "切换定位模式到 罗盘", PopupToast.CUSTOME);
                if (mLocationImaveView != null) {
                    mLocationImaveView.setBackgroundResource(R.drawable.main_icon_compass);
                }
                setBdMapLocationEnable(true);
                requestLocation();
                if (orientationEventListener != null) {
                    orientationEventListener.start();
                }
            }
        }
    }
    /***
     * 切换地图定位模式
     * order : normal --> folling --> compass
     * 切换百度地图定位模式
     **/
    public void switchLocationMode() {
        MyLocationConfiguration.LocationMode currentLocationMode = bdCurrentLocationMode();
        if (currentLocationMode == MyLocationConfiguration.LocationMode.NORMAL) {
            updateLocationModel(MyLocationConfiguration.LocationMode.FOLLOWING);
        } else if (currentLocationMode == MyLocationConfiguration.LocationMode.FOLLOWING) {
            updateLocationModel(MyLocationConfiguration.LocationMode.COMPASS);
        } else if (currentLocationMode == MyLocationConfiguration.LocationMode.COMPASS) {
            updateLocationModel(MyLocationConfiguration.LocationMode.NORMAL);
        }
    }
    /**
     * 处理批量修改点位信息
     **/
    private void handlerBatchUpdatePoint(MapPoiEntity editEntity) {
        bdHideInfoWindow();

        //保存临时点位对象数据
        batchUpdatePointHelper.setTempBatchPointEntity(editEntity);

        //加载infoWindow对象页面视图
        LayoutInflater inflater = LayoutInflater.from(this);
        View infoWindowView = inflater.inflate(R.layout.infowindow_map_batchpoint, null);
        infoWindowView.findViewById(R.id.id_batchpoint_startpoint).setOnClickListener(this);
        infoWindowView.findViewById(R.id.id_batchpoint_endpoint).setOnClickListener(this);

        //显示infoWindow对象
        InfoWindow infoWindow = new InfoWindow(infoWindowView, new LatLng(editEntity.getPointLatitude(), editEntity.getPointLongitude()), -30);
        bdShowInfoWindow(infoWindow);
    }
    /**
     * 在弹出infoWindow的位置显示InfoWindow窗口
     * @param editEntity *
     */
    private void showInfoWindowFromPointLatLong(MapPoiEntity editEntity) {
        bdHideInfoWindow();

        //保存临时点位对象数据
        addConnectWireHelper.setTempConnectWireEntity(editEntity);

        //加载infoWindow对象页面视图
        LayoutInflater inflater = LayoutInflater.from(this);
        View infoWindowView = inflater.inflate(R.layout.infowindow_map_connectwire, null);
        infoWindowView.findViewById(R.id.id_connectwire_startpoint).setOnClickListener(this);
        infoWindowView.findViewById(R.id.id_connectwire_endpoint).setOnClickListener(this);

        //显示infoWindow对象
        InfoWindow infoWindow = new InfoWindow(infoWindowView, new LatLng(editEntity.getPointLatitude(), editEntity.getPointLongitude()), -30);
        bdShowInfoWindow(infoWindow);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 初始化系统视图组件信息
     */
    private void initComponents() {
        //获取项目列表传入参数
        projectEntity = (ProjectEntity) this.getIntent().getSerializableExtra(ProjectListFragment.PROJECT_OBJ_KEY);
        currentProjectId = projectEntity.getId();
        //初始化视图
        initViews();

        orientationEventListener = new OrientationEventListener(this);
        orientationEventListener.setOnOrientationChangeListener(this);

        dataThread = new HandlerThread("dataThread");
        dataThread.start();
        dataHandler = new Handler(dataThread.getLooper());
    }
    /**
     * 初始化系统相关视图组件信息
     */
    private void initViews() {
        btn_change_all = (Button) findViewById(R.id.id_btn_change_all);
        btn_change_all.setOnClickListener(this);
        btn_delete = (Button)findViewById(R.id.id_btn);
        btn_delete.setOnClickListener(this);
        mLocationImaveView = (AppCompatImageView) findViewById(R.id.id_ImageView_location);
        mLocationImaveView.setOnClickListener(this);
        mEditLineChooseBar = (FrameLayout) findViewById(R.id.id_edit_line_choose);
        mEditLineChooseBar.findViewById(R.id.id_button_addcancel2).setOnClickListener(this);//准备编辑点位跨越线属性取消
        mEditPointChooseBar = (FrameLayout) findViewById(R.id.id_edit_point_choose);
        mEditPointChooseBar.findViewById(R.id.id_button_addcancel).setOnClickListener(this);//准备编辑点位属性取消
        mEditLineConnectBar = (FrameLayout) findViewById(R.id.id_edit_connectline);
        mEditLineConnectBar.findViewById(R.id.id_button_addcancel3).setOnClickListener(this);//连线取消
        mEditLineBatchConnectBar = (FrameLayout) findViewById(R.id.id_edit_batchconnectline);
        mEditLineBatchConnectBar.findViewById(R.id.id_button_addcancel4).setOnClickListener(this);//连线取消
        mEditBatchPointChooseBar = (FrameLayout) findViewById(R.id.id_edit_batchpoint);
        mEditBatchPointChooseBar.findViewById(R.id.id_button_addcancel5).setOnClickListener(this);//批量修改点位连线取消

        findViewById(R.id.id_button_line_start).setOnClickListener(this);//跨越线开始
        findViewById(R.id.id_button_line_end).setOnClickListener(this);//跨越线结束
        findViewById(R.id.id_button_verticalwelding).setOnClickListener(this);//立杆按钮
        findViewById(R.id.id_button_transformerchamber).setOnClickListener(this);//变压箱
        findViewById(R.id.id_button_doormeter).setOnClickListener(this);//户表按钮
        findViewById(R.id.id_button_cablepit).setOnClickListener(this);//电缆井
        findViewById(R.id.id_button_boxswitchstation).setOnClickListener(this);//箱式开关站
        findViewById(R.id.id_button_spswitchingpost).setOnClickListener(this);//开闭所
        findViewById(R.id.id_button_boxtypesubstation).setOnClickListener(this);//箱式变压站
        findViewById(R.id.id_ImageView_zoomin).setOnClickListener(this);//地图缩放
        findViewById(R.id.id_ImageView_zoomout).setOnClickListener(this);//地图缩放
        findViewById(R.id.id_ImageView_point).setOnClickListener(this);//地图打点
        findViewById(R.id.id_ImageView_batch_point).setOnClickListener(this);//地图批量打点
        findViewById(R.id.id_ImageView_crossline).setOnClickListener(this);//跨越线
        findViewById(R.id.id_ImageView_line).setOnClickListener(this);//地图连线
        findViewById(R.id.id_ImageView_removeall).setOnClickListener(this);//地图清除视图
        findViewById(R.id.id_ImageView_maptype).setOnClickListener(this);//地图类型切换
        findViewById(R.id.id_ImageView_batchline).setOnClickListener(this);//批量连线
        findViewById(R.id.id_button_batchline_ok).setOnClickListener(this);//批量连线确定
    }

}