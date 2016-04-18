package com.hz.activity.base;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.hz.R;
import com.hz.broadcast.MapSdkReceiver;
import com.hz.common.Constans;
import com.hz.dialog.ProgressHUD;
import com.hz.greendao.dao.MapPoiEntity;
import com.hz.helper.DataBaseManagerHelper;
import com.hz.helper.MapIconHelper;
import com.hz.util.ArrayUtil;
import com.hz.util.DensityUtil;
import com.hz.util.NetworkManager;
import com.hz.view.PopupToast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 百度地图操作基类
 */
public abstract class BaseMapActivity extends BaseActivity implements BaiduMap.OnMarkerClickListener, BaiduMap.OnPolylineClickListener, BaiduMap.OnMapStatusChangeListener, BDLocationListener, BaiduMap.OnMapLoadedCallback, BaiduMap.OnMarkerDragListener {
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    public static final String TAG = BaseMapActivity.class.getSimpleName();

    public MapView mMapView = null;//百度地图视图对象
    public BaiduMap mBaiduMap = null;//百度地图操作对象
    public LocationClient mLocationClient = null;//百度地图定位操作
    public LatLng userLocLatLng = null;//保存当前最新定位经纬度信息

    public boolean isPointing = false;//当前正在打点
    private boolean isFirstIn = true;//第一次进入地图居中显示所有点位

    //百度地图注册回调*
    private MapSdkReceiver mapSdkReceiver = new MapSdkReceiver();
    private float currentMapZoomLevel = 0;//获取当前地图缩放层级
    private ProgressHUD mProgressHUD = null;//加载进度条

    public Handler mMapStatusHandler = new Handler();

    public ImageView mapCenterImageView;
    public TextView latLongTextView;

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Test","当前的Activity是"+getClass().getSimpleName()+"BMA");

        mapSdkReceiver.registerMapSdkReceiver(this);
        showProgressHudWidthText(getResources().getString(R.string.string_initbaidumaping));
    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        mMapStatusHandler.removeCallbacksAndMessages(null);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapSdkReceiver.unregisterReceiver(this);
        setBdMapLocationEnable(false);
        removeProgressHud();

        if (mLocationClient != null) {
            if (mLocationClient.isStarted()) {
                mLocationClient.stop();
            }
            mLocationClient.unRegisterLocationListener(this);
        }

        removeMapCenterImageView();
        mMapView.onDestroy();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mMapView = (MapView) findViewById(R.id.bmapView);
        if (mMapView == null) {
            throw new RuntimeException("必须有ID为:bmapView,的com.baidu.mapapi.map.MapView地图对象");
        }
        initMapAndLocatinClient();
        addCurrentLatLongTextView();
    }
    //方法回调事件方法
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
    @Override
    public boolean onPolylineClick(Polyline polyline) {
        return false;
    }
    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {
        currentMapZoomLevel = mapStatus.zoom;
    }
    @Override
    public void onMapStatusChange(MapStatus mapStatus) {
        currentMapZoomLevel = mapStatus.zoom;
    }
    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        currentMapZoomLevel = mapStatus.zoom;
    }
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
    }
    @Override
    public void onMapLoaded() {
      /*  removeProgressHud();*/
        Log.d(TAG, "onMapLoaded: ");
    }
    @Override
    public void onMarkerDrag(Marker marker) {

    }
    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 显示加载框
     **/
    public void showProgressHudWidthText(final String text) {
        mProgressHUD = ProgressHUD.show(this, text);
    }
    /***
     * 移除加载框
     **/
    public void removeProgressHud() {
        if (mProgressHUD != null) {
            mProgressHUD.dismiss();
        }
    }
    /**
     * @see MapView
     */
    //自定义事件方法
    /**
     * 初始化百度地图和百度地图定位客户端参数信息
     */
    private void initMapAndLocatinClient() {
        this.onInitMapOption();
        this.onInitMapLocationClientOption();
    }
    /**
     * 初始化百度地图
     */
    public void onInitMapOption() {
        mMapView.showZoomControls(false);
        mMapView.showScaleControl(false);
        mBaiduMap = mMapView.getMap();

        mBaiduMap.setOnMapStatusChangeListener(this);
        mBaiduMap.setOnMarkerClickListener(this);
        mBaiduMap.setOnPolylineClickListener(this);
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        mBaiduMap.getUiSettings().setCompassEnabled(true);
        mBaiduMap.getUiSettings().setOverlookingGesturesEnabled(true);//关闭俯视手势
        mBaiduMap.setOnMapLoadedCallback(this);
        mBaiduMap.setOnMarkerDragListener(this);
    }
    /**
     * 初始化百度地图定位组件
     */
    private void onInitMapLocationClientOption() {
        //百度地图定位相关参数设置
        LocationClientOption clientOption = new LocationClientOption();
        clientOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        clientOption.setOpenGps(true);
        clientOption.setCoorType(Constans.COORTYPE);
        clientOption.setScanSpan(Constans.SCANSPAN);
        clientOption.setIsNeedAddress(true);
        clientOption.setIsNeedLocationDescribe(true);
        clientOption.setNeedDeviceDirect(true);

        //百度地图定位客户端
        mLocationClient = new LocationClient(this.getApplicationContext());
        mLocationClient.registerLocationListener(this);
        mLocationClient.setLocOption(clientOption);
    }
    /**
     * 设置用户位置信息
     **/
    public void bdSetMyLocationData(MyLocationData myLocationData) {
        if (mMapView == null || mBaiduMap == null || myLocationData == null) {
            return;
        }
        mBaiduMap.setMyLocationData(myLocationData);
    }
    /**
     * 设置定位模式配置
     **/
    public void bdSetMyLocationConfigeration(MyLocationConfiguration var1) {
        if (mMapView == null || mBaiduMap == null || var1 == null) {
            return;
        }
        mBaiduMap.setMyLocationConfigeration(var1);
    }
    /**
     * 获取百度地图的当前定位模式
     **/
    public MyLocationConfiguration.LocationMode bdCurrentLocationMode() {
        if (mMapView != null && mBaiduMap != null) {
            return mBaiduMap.getLocationConfigeration().locationMode;
        } else {
            return MyLocationConfiguration.LocationMode.NORMAL;
        }
    }
    /**
     * 定位百度地图到某一个文字
     **/
    public void bdAnimateMapStatus(MapStatusUpdate mapStatusUpdate) {
        if (mMapView == null || mBaiduMap == null || mapStatusUpdate == null) {
            return;
        }
        try {
            mBaiduMap.animateMapStatus(mapStatusUpdate);
        } catch (Exception ignored) {
        }

    }
    /**
     * 隐藏infoWindow
     **/
    public void bdHideInfoWindow() {
        if (mMapView == null || mBaiduMap == null) {
            return;
        }
        mBaiduMap.hideInfoWindow();
    }
    /**
     * 显示infoWindow
     **/
    public void bdShowInfoWindow(InfoWindow infoWindow) {
        if (mMapView == null || mBaiduMap == null || infoWindow == null) {
            return;
        }
        mBaiduMap.showInfoWindow(infoWindow);
    }
    /**
     * 获取百度地图最大缩放数
     **/
    public float bdGetMaxZoomLevel() {
        if (mMapView != null && mBaiduMap != null) {
            return mBaiduMap.getMaxZoomLevel();
        } else {
            return 0;
        }
    }
    /***
     * 清除百度地图
     **/
    public void bdClear() {
        if (mMapView == null || mBaiduMap == null) {
            return;
        }
        mBaiduMap.clear();
    }
    /**
     * 在百度地图上添加overlay
     ***/
    public Overlay bdAddOverLay(OverlayOptions overlayOptions) {
        if (mMapView == null || mBaiduMap == null || overlayOptions == null) {
            return null;
        }
        return mBaiduMap.addOverlay(overlayOptions);
    }
    /***
     * 百度地图定位是否开启
     **/
    public boolean bdIsMyLocationEnabled() {
        return !(mMapView == null || mBaiduMap == null) && mBaiduMap.isMyLocationEnabled();
    }
    /***
     * 设置是否开启百度地图定位
     **/
    public void bdSetMyLocationEnabled(boolean enable) {
        if (mMapView == null || mBaiduMap == null) {
            return;
        }
        mBaiduMap.setMyLocationEnabled(enable);
    }

    /**
     * 更新地图上采集的点位信息
     * 包括 立杆,变压箱，户表，电缆井，箱式开关站，开闭所，箱式变压站
     */
    public void addMapPointMarkers(long currentProjectId, String... excludeIds) {
        List<MapPoiEntity> poiEntityList = new ArrayList<>();
        List<MapPoiEntity> tempPoiEntityList = DataBaseManagerHelper.getInstance().getAllPointByProjectId(currentProjectId);

        //过滤点位
        for (MapPoiEntity mapPoiEntity : tempPoiEntityList) {
            if (excludeIds != null && excludeIds.length > 0) {
                if (!ArrayUtil.contains(excludeIds, mapPoiEntity.getPointId())) {
                    poiEntityList.add(mapPoiEntity);
                }
            } else {
                poiEntityList.add(mapPoiEntity);
            }
        }

        Log.d(TAG, "当前项目一共有：" + poiEntityList.size() + " 个点位");

        for (MapPoiEntity entity : poiEntityList) {
            //如果点位没有被删除
            if (entity.getPointRemoved() == Constans.RemoveIdentified.REMOVE_IDENTIFIED_NORMAL) {
                pointWithEntityAndStatus(entity, entity.getPointStatus());//点
                if (entity.getPointType() == Constans.MapAttributeType.VERTICAL_WELDING) {
                    addMapPointAnnotationText(new LatLng(entity.getPointLatitude(), entity.getPointLongitude()), entity.getPointNum());//点上标注
                }
            }
        }
        Log.d(TAG, "isFirstIn:" + isFirstIn);
        centerWithMarkers(poiEntityList);
    }
    /**
     * 根据点位不同的状态在地图上显示不同的点位
     **/
    private void pointWithEntityAndStatus(MapPoiEntity entity, int status) {
        //根据不同的点位类型在地图上打上不同的点
        int iconKey = MapIconHelper.convertNumKeyByPointType(entity.getPointType());
        addMapPoiMarkerWithObj(entity, MapIconHelper.getInstance().generateBitmapWithStatus(iconKey, status));
    }
    /**
     * 根据已有的点位进行居中地图
     **/
    private void centerWithMarkers(List<MapPoiEntity> poiEntityList) {
        //将所有点位居中显示
        if (isFirstIn) {
            isFirstIn = false;

            if (poiEntityList.size() >= 2) {//多个点位时以点位的中点居中地图
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (MapPoiEntity entity : poiEntityList) {
                    builder.include(new LatLng(entity.getPointLatitude(), entity.getPointLongitude()));
                }
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngBounds(builder.build());
                twenceAnimateMapStatus(mapStatusUpdate);
            } else if (poiEntityList.size() == 1) {//一个点位时以点位居中地图
                MapPoiEntity entity = poiEntityList.get(0);
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(
                        new LatLng(entity.getPointLatitude(), entity.getPointLongitude()),
                        bdGetMaxZoomLevel()
                );
                twenceAnimateMapStatus(mapStatusUpdate);
            } else {//没有点位时先启动定位后再以当前用户的位置居中地图
                setBdMapLocationEnable(true);
            }
        }
    }
    /**
     * 两次更新地图位置
     **/
    public void twenceAnimateMapStatus(final MapStatusUpdate mapStatusUpdate) {
        bdAnimateMapStatus(mapStatusUpdate);
        mMapStatusHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    bdAnimateMapStatus(mapStatusUpdate);
                } catch (Exception ignored) {
                } finally {
                    removeProgressHud();
                }
            }
        }, 200);
    }
    /**
     * 更新地图上点位所携带信息
     * @param poiEditEntity 点位所携带的数据
     * @param markerIcon    点位图标
     */
    public Marker addMapPoiMarkerWithObj(MapPoiEntity poiEditEntity, BitmapDescriptor markerIcon) {
        LatLng latLng = new LatLng(poiEditEntity.getPointLatitude(), poiEditEntity.getPointLongitude());
        return addMapMarkerWithBundle(latLng, markerIcon, poiEditEntity, true);
    }
    /**
     * 请求定位
     * 在网络可用的时候使用网络定位，网络不可用的时候使用离线定位
     ***/
    public void requestLocation() {
        if (mLocationClient == null) {
            return;
        }
        if (NetworkManager.isConnectAvailable(this)) {
            mLocationClient.requestLocation();
            PopupToast.show(this, Gravity.BOTTOM, "使用网络定位中......", PopupToast.CUSTOME);
        } else {
            mLocationClient.requestOfflineLocation();
            PopupToast.show(this, Gravity.BOTTOM, "当前网络不可用,正在使用离线定位中......", PopupToast.CUSTOME);
        }
    }
    /**
     * 开启和关闭百度地图定位和地图定位图层
     */
    public void setBdMapLocationEnable(boolean enable) {
        //启用百度地图定位
        if (enable) {
            if (mLocationClient != null && !mLocationClient.isStarted()) {
                mLocationClient.start();
            }
            if (!bdIsMyLocationEnabled()) {
                bdSetMyLocationEnabled(true);
            }
            Log.d(TAG, "开启百度地图定位，并开启百度地图定位图层");
        } else {
            if (mLocationClient != null && mLocationClient.isStarted()) {
                mLocationClient.stop();
            }
            if (bdIsMyLocationEnabled()) {
                bdSetMyLocationEnabled(false);
            }
            Log.d(TAG, "停止百度地图定位，并关闭百度地图定位图层");
        }
    }
    /**
     * 获取屏幕中心点的地图坐标
     */
    public LatLng getMapCenterLatLongFormScreen() {
        return mBaiduMap.getProjection().fromScreenLocation(getMapCenterPoint());
    }
    /**
     * 获取屏幕中心点坐标
     **/
    public Point getMapCenterPoint() {
        int top = mMapView.getTop();
        int bottom = mMapView.getBottom();
        int left = mMapView.getLeft();
        int right = mMapView.getRight();
        return new Point((right - left) / 2, (bottom - top) / 2);
    }
    /**
     * 在地图中间添加视图
     ***/
    public void addMapCenterImageView() {
        removeMapCenterImageView();

        MapViewLayoutParams layoutParams = new MapViewLayoutParams
                .Builder()
                .width(DensityUtil.dip2px(this, 35))
                .height(DensityUtil.dip2px(this, 35))
                .layoutMode(MapViewLayoutParams.ELayoutMode.absoluteMode)
                .point(getMapCenterPoint())
                .build();

        mapCenterImageView = new ImageView(this);
        mapCenterImageView.setLayoutParams(layoutParams);
        mapCenterImageView.setImageResource(R.drawable.map_point_red);

        if (mMapView == null) {
            return;
        }
        isPointing = true;
        mMapView.addView(mapCenterImageView);
    }

    public void addCurrentLatLongTextView() {
        MapViewLayoutParams layoutParams = new MapViewLayoutParams
                .Builder()
                .width(ViewGroup.LayoutParams.WRAP_CONTENT)
                .height(ViewGroup.LayoutParams.WRAP_CONTENT)
                .layoutMode(MapViewLayoutParams.ELayoutMode.absoluteMode)
                .point(new Point(300, 300))
                .build();

        latLongTextView = new TextView(this);
        latLongTextView.setLayoutParams(layoutParams);
        latLongTextView.setTextColor(Color.RED);

        if (mMapView == null) {
            return;
        }
        mMapView.addView(latLongTextView);
    }
    /**
     * 移除屏幕中心视图
     **/
    public void removeMapCenterImageView() {
        if (mapCenterImageView != null) {
            if (mMapView == null) {
                return;
            }
            isPointing = false;
            mMapView.removeView(mapCenterImageView);
        }
    }
    /**
     * 获取屏幕中心点位携带经纬度信息
     **/
    public Bundle getMapCenterImageViewBundle() {
        LatLng mapCenterLatlng = getMapCenterLatLongFormScreen();
        if (mapCenterLatlng == null) {
            return null;
        }

        Bundle bundle = new Bundle();
        MapPoiEntity mapPoiEntity = new MapPoiEntity();
        mapPoiEntity.setPointLatitude(mapCenterLatlng.latitude);
        mapPoiEntity.setPointLongitude(mapCenterLatlng.longitude);
        bundle.putSerializable(Constans.POINT_OBJ_KEY, mapPoiEntity);
        return bundle;
    }
    /**
     * 在地图上添加图标信息并设置携带的信息
     * @param markerLatLong 图标经纬度信息
     * @param markerIcon    点位图标
     * @param markerExtras  点位携带数据
     */
    public Marker addMapMarkerWithBundle(LatLng markerLatLong, BitmapDescriptor markerIcon, Serializable markerExtras, boolean dragable) {
        if (markerLatLong == null || markerIcon == null || markerExtras == null) {
            return null;
        }

        Marker mMarkerPoint = addMapMarker(markerLatLong, markerIcon, dragable);
        if (mMarkerPoint == null) {
            return null;
        }

        //添加intent信息
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constans.POINT_OBJ_KEY, markerExtras);

        mMarkerPoint.setExtraInfo(bundle);
        return mMarkerPoint;
    }
    /**
     * 在地图上添加图标信息
     * @param markerLatLong 图标经纬度信息
     * @param markerIcon    点位图标
     */
    public Marker addMapMarker(LatLng markerLatLong, BitmapDescriptor markerIcon, boolean dragable) {
        MarkerOptions opa = new MarkerOptions()
                .position(markerLatLong)
                .icon(markerIcon)
                .perspective(true)//开启进大远小效果
                .zIndex(999999)
                .draggable(dragable)
                .period(36);//设置多少帧刷新一次图片资源，Marker动画的间隔时间，值越小动画越快
        Overlay overlay = bdAddOverLay(opa);
        if (overlay == null) {
            return null;
        }
        return (Marker) overlay;
    }
    /**
     * 在地图上添加线信息
     * @param lineExtras 线携带的信息
     * @param color      线颜色
     * @param pots       线条点位列表
     */
    public Polyline addMapPolylineWithBundle(List<LatLng> pots, int color, Serializable lineExtras) {
        if (pots == null || lineExtras == null) {
            return null;
        }
        Polyline polyline = addMapPolyline(pots, color);
        if (polyline == null) {
            return null;
        }

        //添加intent信息
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constans.LINE_OBJ_KEY, lineExtras);

        polyline.setExtraInfo(bundle);
        return polyline;
    }
    /**
     * 在地图上线中点添加线长度信息
     * @param startLatlong 开始点经纬度
     * @param endLatlong   结束点经纬度
     */
    public void addMapPolylineCenterText(LatLng startLatlong, LatLng endLatlong, String text) {
        if (startLatlong == null || endLatlong == null) {
            return;
        }
        //获取中间点的坐标
        LatLng center = new LatLngBounds.Builder().include(startLatlong).include(endLatlong).build().getCenter();
        TextOptions textOptions = new TextOptions()
                .position(center)
                .bgColor(getResources().getColor(R.color.content_background_color))
                .fontSize((int) getResources().getDimension(R.dimen.map_line_text_size))
                .align(TextOptions.ALIGN_CENTER_HORIZONTAL,TextOptions.ALIGN_BOTTOM)
                .text(text);

        bdAddOverLay(textOptions);
    }
    /**
     * 在点位的上方添加标注
     * @param latlong 经纬度
     * @param text   标注文字
     */
    public void addMapPointAnnotationText(LatLng latlong, String text) {
        if (latlong == null || text == null) {
            return;
        }
        //获取中间点的坐标
        TextOptions textOptions = new TextOptions()
                .position(latlong)
                .bgColor(getResources().getColor(R.color.content_background_color))
                .fontSize((int) getResources().getDimension(R.dimen.map_line_text_size))
                .fontColor(Color.RED)
                .align(TextOptions.ALIGN_LEFT,TextOptions.ALIGN_TOP)
                .text(text);

        bdAddOverLay(textOptions);
    }
    /**
     * 在地图上添加线信息
     * @param color 线颜色
     * @param pots  线条点位列表
     */
    public Polyline addMapPolyline(List<LatLng> pots, int color) {
        OverlayOptions polygonOption = new PolylineOptions().points(pots).color(color);
        Overlay overlay = bdAddOverLay(polygonOption);
        if (overlay == null) {
            return null;
        }
        return (Polyline) overlay;
    }
    /**
     * 获取当前系统的缩放
     */
    public float getCurrentMapZoomLevel() {
        return currentMapZoomLevel;
    }
    /**
     * 检查当前地图是否是最大缩放
     */
    public boolean checkIsMaxZoomMap() {
        if (bdGetMaxZoomLevel() == getCurrentMapZoomLevel()) {
            return true;
        }
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(getMapCenterLatLongFormScreen(), bdGetMaxZoomLevel());
        bdAnimateMapStatus(mapStatusUpdate);
        return false;
    }

}