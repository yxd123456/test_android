package com.hz.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 网络访问工具
 */
public class NetworkManager {
    public static final String NET_TYPE_NO_NETWORK = "no_network";
    public static final String IP_DEFAULT = "0.0.0.0";

    /**
     * 获取当前网络IP地址
     * *
     */
    public static String getIPAddress() {
        try {
            final Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaceEnumeration.hasMoreElements()) {
                final NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
                final Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses();
                while (inetAddressEnumeration.hasMoreElements()) {
                    final InetAddress inetAddress = inetAddressEnumeration.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
            return NetworkManager.IP_DEFAULT;
        } catch (SocketException e) {
            return NetworkManager.IP_DEFAULT;
        }
    }

    /**
     * 检测当前网络是否可用 包括3G和wifi
     *
     * @param mContext 上下文对象
     */
    public static boolean isConnectAvailable(Context mContext) {
        ConnectivityManager conManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    /**
     * 检测当前是否是wifi连接
     *
     * @param mContext 上下文对象
     */
    public static boolean isConnectWifi(Context mContext) {
        ConnectivityManager mConnectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        int netType = -1;
        if (info != null) {
            netType = info.getType();
        }
        return netType == ConnectivityManager.TYPE_WIFI && info.isConnected();
    }

    /**
     * 检测当前是否是手机自带网络连接
     *
     * @param mContext 上下文对象
     */
    public static boolean isConnectMobile(Context mContext) {
        ConnectivityManager mConnectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        //判断网络连接类型，只有在3G或wifi里进行一些数据更新。
        int netType = -1;
        if (info != null) {
            netType = info.getType();
        }
        return netType == ConnectivityManager.TYPE_MOBILE && info.isConnected();
    }

    /**
     * 获取当前网络连接名称
     *
     * @param mContext 上下文对象
     */
    public static String getConnTypeName(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return NET_TYPE_NO_NETWORK;
        } else {
            return networkInfo.getTypeName();
        }
    }
}

