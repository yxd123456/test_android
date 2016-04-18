package com.hz.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.hz.common.Constans;
import com.hz.entity.ResponseStateEntity;
import com.hz.helper.StroageHelper;
import com.hz.util.okhttp_extend.cookie.PersistentCookieStore;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.util.concurrent.TimeUnit;

/**
 * http操作管理
 *
 * @author long
 */
public class HttpManager {
    public static final String TAG = HttpManager.class.getSimpleName();
    private volatile static HttpManager httpManager;
    private OkHttpClient okHttpClient;//网络访问全局对象
    private Context appContext;//ApplicationContext
    private static Handler handler = new Handler(Looper.getMainLooper());

    private HttpManager() {

    }

    public static HttpManager getInstance() {
        if (httpManager == null) {
            synchronized (HttpManager.class) {
                if (httpManager == null) {
                    httpManager = new HttpManager();
                }
            }
        }
        return httpManager;
    }

    /**
     * 只能初始化一次
     *
     * @param context AppContext对象
     **/
    public void init(Context context) {
        if (appContext != null) {
            throw new RuntimeException("HttpManager不能重复初始化");
        }
        appContext = context.getApplicationContext();
        initOKHttpClient();
    }

    /**
     * 销毁内部所有的残留消息
     **/
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
    }

    public void clear() {
        try {
            okHttpClient.getCache().evictAll();
        } catch (Exception ignored) {
        }
    }

    /**
     * 初始化okHttpClient
     * <p/>
     * *
     */
    private void initOKHttpClient() {
        File cacheDir = StroageHelper.getProjectOkHttpClientCacheDir(appContext);
        okHttpClient = new OkHttpClient();
        Cache cache = new Cache(cacheDir, 100 * 1024 * 1024);
        okHttpClient.setCache(cache);
        okHttpClient.setConnectTimeout(10, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(20, TimeUnit.SECONDS);
        okHttpClient.setWriteTimeout(30, TimeUnit.SECONDS);
        okHttpClient.setCookieHandler(new CookieManager(new PersistentCookieStore(appContext), CookiePolicy.ACCEPT_ALL));
    }


    /**
     * 查看session是否过期
     * true : 已经过期
     * false : 未过期
     **/
    public boolean isRemberMeExpiredSync() {
        Request request = new Request.Builder()
                .url(Constans.CHECK_SESSION_EXPIRED)
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .get().build();
        String resp = HttpManager.getInstance().addSyncHttpTask(request);

        if (resp == null) {
            return true;
        }

        ResponseStateEntity state = JsonUtil.convertJsonToObj(resp, ResponseStateEntity.class);
        Log.d(TAG, "isRemberMeExpiredSync: " + state);
        return state != null && state.isSessionExpired();
    }

    /**
     * 异步检查session是否失效
     **/
    public void isRemberMeExpiredASync(@NonNull final SessionCheckCallBack sessionCheckCallBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sessionCheckCallBack.sessionExpired(isRemberMeExpiredSync());
            }
        }).start();
    }

    /**
     * 移除用户登录cookie信息
     **/
    public void removeUserCookie() {
        if (okHttpClient == null) {
            return;
        }
        CookieManager cookieManager = (CookieManager) okHttpClient.getCookieHandler();
        if (cookieManager == null) {
            return;
        }
        CookieStore cookieStore = cookieManager.getCookieStore();
        if (cookieStore != null) {
            cookieStore.removeAll();
        }
    }


    /**
     * 添加异步网络任务l
     *
     * @param request          请求对象
     * @param responseCallback 返回值处理
     *                         *
     */
    private void addAsyncOkHttpTask(Request request, Callback responseCallback) {
        okHttpClient.newCall(request).enqueue(responseCallback);
    }

    /**
     * 添加同步网络任务l
     *
     * @param request 请求对象
     *                *
     */
    private Response addSyncOkHttpTask(Request request) throws IOException {
        return okHttpClient.newCall(request).execute();
    }


    /**
     * 添加异步网络任务l
     *
     * @param request  请求对象
     * @param callback 返回值处理
     *                 *
     */
    public void addAsyncHttpTask(final Request request, final HttpTaskCallback callback) {
        addAsyncOkHttpTask(request, new Callback() {
            @Override
            public void onFailure(Request request, final IOException e) {
                postFailure(callback, e);
            }

            @Override
            public void onResponse(final Response response) {
                Log.d(TAG, "请求返回值:" + response);
                if (response != null && response.isSuccessful()) {
                    String respStr = null;
                    boolean result = true;
                    try {
                        respStr = response.body().string();
                        Log.d(TAG, "onResponse: 返回值：" + respStr);
                    } catch (final Exception e) {
                        result = false;
                        postFailure(callback, e);
                    } finally {
                        if (result && respStr != null) {
                            postSuccess(callback, respStr);
                        }
                    }
                } else {
                    postFailure(callback, new RuntimeException("response == null || !response.isSuccessful()"));
                }
            }
        });
    }

    public void postFailure(final HttpTaskCallback callback, final Exception e) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFailure(e);
            }
        });
    }

    public void postSuccess(final HttpTaskCallback callback, final String respStr) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(respStr);
            }
        });
    }

    /**
     * 异步请求回调
     * *
     */
    public interface HttpTaskCallback {
        void onFailure(Exception e);

        void onSuccess(String respstr);
    }

    /**
     * 检查session是否失效
     **/
    public interface SessionCheckCallBack {
        void sessionExpired(boolean isExpired);
    }


    /**********************************************************同步请求**********************************************************/

    /**
     * 添加同步网络任务,在主线程执行会报错
     *
     * @param request 请求对象
     *                *
     */
    public String addSyncHttpTask(final Request request) {
        Log.d(TAG, "同步请求request为：" + request);
        String respStr = null;
        try {
            Response response = addSyncOkHttpTask(request);
            Log.d(TAG, "同步请求响应response为：" + response);
            if (response != null && response.isSuccessful()) {
                respStr = response.body().string();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return respStr;
    }

    /**
     * 添加同步网络任务,在主线程执行会报错
     *
     * @param request   请求对象
     * @param valueType 返回值class类型
     *                  *
     */
    public <T> T addSyncHttpTask(final Request request, Class<T> valueType) {
        try {
            String respStr = addSyncHttpTask(request);
            if (respStr != null) {
                return JsonUtil.convertJsonToObj(respStr, valueType);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }


}
