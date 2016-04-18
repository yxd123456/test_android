package com.hz.helper;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.hz.R;
import com.hz.common.Constans;
import com.hz.util.MultiKeyMap;

/**
 * 地图图标
 */
public class MapIconHelper {
    public static final String TAG = MapIconHelper.class.getSimpleName();

    public static final int KEY_CROSSLINE_START = 10001;//跨越线图标开始
    public static final int KEY_CROSSLINE_END = 10002; //跨越线图标结束
    public static final int KEY_VERTICAL_WELDING = 10003;//立杆图标
    public static final int KEY_TRANSFORMER_CHAMBER = 10004;//变压箱图标
    public static final int KEY_DOOR_METER = 10005;//户表图标
    public static final int KEY_CABLE_PIT = 10006;//电缆井图标
    public static final int KEY_BOX_SWITCH_STATATION = 10007;//箱式开关站图标
    public static final int KEY_SP_SWITCHING_POST = 10008;//开闭所图标
    public static final int KEY_BOX_TYPE_SUBSTATION = 10009;//箱式变压站图标
    public static final int KEY_CROSSLINE_POINT = 10010;//跨越线开始结束点位



    /**
     * 单一实例
     **/
    private static volatile MapIconHelper mInstance;

    /**
     * Context对象引用
     **/
    private Context context;

    /**
     * 生成地图图标视图
     **/
    private View mapIconView;

    /**
     * 根据序号生成的图标
     **/
    private MultiKeyMap<Integer, Integer, BitmapDescriptor> multiKeyMap = new MultiKeyMap<>();


    public static MapIconHelper getInstance() {
        if (mInstance == null) {
            synchronized (MapIconHelper.class) {
                if (mInstance == null) {
                    mInstance = new MapIconHelper();
                }
            }
        }
        return mInstance;
    }

    private MapIconHelper() {
        Log.d("Test",getClass().getSimpleName()+"被调用了");
    }

    public void init(Context appContext) {
        if (this.context != null) {
            throw new RuntimeException("已经初始化，只能初始化一次");
        }
        this.context = appContext.getApplicationContext();
        mapIconView = LayoutInflater.from(context).inflate(R.layout.map_icon, null);
    }


    /**
     * 根据图表的不同状态生成不同颜色文字的图标
     **/
    public BitmapDescriptor generateBitmapWithStatus(int numKey, int attributeStatus) {
        int textColor;
        switch (attributeStatus) {
            case Constans.AttributeStatus.BALI:
                textColor = Color.YELLOW;
                break;
            case Constans.AttributeStatus.NEW:
                textColor = Color.GREEN;
                break;
            case Constans.AttributeStatus.OLD:
                textColor = Color.CYAN;
                break;
            default:
                textColor = Color.WHITE;
                break;
        }
        return MapIconHelper.getInstance().generateBitmapIconByNum(numKey, textColor);
    }


    /**
     * 根据序号和文字颜色生成图标
     **/
    public BitmapDescriptor generateBitmapIconByNum(int numKey, int textColor) {
        int backgroundRes = R.drawable.map_point_red;
        String viewText;
        switch (numKey) {
            case KEY_CROSSLINE_START:
                viewText = "始";
                break;
            case KEY_CROSSLINE_END:
                viewText = "终";
                break;
            case KEY_VERTICAL_WELDING:
                viewText = "杆";
                break;
            case KEY_TRANSFORMER_CHAMBER:
                viewText = "变";
                break;
            case KEY_DOOR_METER:
                viewText = "户";
                break;
            case KEY_CABLE_PIT:
                viewText = "井";
                break;
            case KEY_BOX_SWITCH_STATATION:
                viewText = "箱";
                break;
            case KEY_SP_SWITCHING_POST:
                viewText = "所";
                break;
            case KEY_BOX_TYPE_SUBSTATION:
                viewText = "站";
                break;
            case KEY_CROSSLINE_POINT:
                viewText = "";
                backgroundRes = R.drawable.map_pin;
                break;
            default:
                viewText = numKey + "";
                break;
        }
        return generateMapPointBitmapByText(backgroundRes, numKey, viewText, textColor);
    }

    /***
     * 根据文字生成图标
     **/
    private BitmapDescriptor generateMapPointBitmapByText(@DrawableRes int backgroundRes, int numKey, String viewText, int textColor) {
        BitmapDescriptor bitmapDescriptor = multiKeyMap.get(numKey, textColor);
        if (bitmapDescriptor == null) {
            bitmapDescriptor = createPointBitmapFromView(backgroundRes, viewText, textColor);
            multiKeyMap.put(numKey, textColor, bitmapDescriptor);
        }
        return bitmapDescriptor;
    }

    /**
     * 根据点位视图创建地图图标
     *
     * @param backgroundRes 背景资源
     * @param text          显示文字
     */
    private BitmapDescriptor createPointBitmapFromView(@DrawableRes int backgroundRes, String text, int textColor) {
        TextView textView = ((TextView) mapIconView.findViewById(R.id.id_map_point_text));
        textView.setText(TextUtils.isEmpty(text) ? "" : text);
        textView.setTextColor(textColor);
        mapIconView.findViewById(R.id.id_map_point_background).setBackgroundResource(backgroundRes);
        return BitmapDescriptorFactory.fromView(mapIconView);
    }


    /***
     * 根据点位类型转为点位生成图标的数字
     **/
    public static int convertNumKeyByPointType(int pointType) {
        int iconKey = 0;
        switch (pointType) {
            case Constans.MapAttributeType.VERTICAL_WELDING://立杆图标
                iconKey = MapIconHelper.KEY_VERTICAL_WELDING;
                break;
            case Constans.MapAttributeType.TRANSFORMER_CHAMBER://变压箱图标
                iconKey = MapIconHelper.KEY_TRANSFORMER_CHAMBER;
                break;
            case Constans.MapAttributeType.DOOR_METETR://户表
                iconKey = MapIconHelper.KEY_DOOR_METER;
                break;
            case Constans.MapAttributeType.CABLE_PIT://电缆井
                iconKey = MapIconHelper.KEY_CABLE_PIT;
                break;
            case Constans.MapAttributeType.BOX_SWITCH_STATION://箱式开关站
                iconKey = MapIconHelper.KEY_BOX_SWITCH_STATATION;
                break;
            case Constans.MapAttributeType.SP_SWITCHING_POST://开闭所
                iconKey = MapIconHelper.KEY_SP_SWITCHING_POST;
                break;
            case Constans.MapAttributeType.BOX_TYPE_SUBSTATION://箱式变压站
                iconKey = MapIconHelper.KEY_BOX_TYPE_SUBSTATION;
                break;
        }
        return iconKey;
    }
}
