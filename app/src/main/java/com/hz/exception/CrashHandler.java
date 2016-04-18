package com.hz.exception;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

import com.hz.common.Constans;
import com.hz.util.DateUtil;
import com.hz.helper.StroageHelper;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.umeng.analytics.MobclickAgent;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统未捕获异常处理
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    public static final String TAG = CrashHandler.class.getSimpleName();

    // CrashHandler 实例
    private static CrashHandler INSTANCE = new CrashHandler();

    // 程序的 Context 对象
    private Context mContext;

    // 系统默认的 UncaughtException 处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<>();

    /**
     * 保证只有一个 CrashHandler 实例
     */
    private CrashHandler() {
    }

    /**
     * 获取 CrashHandler 实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public void init(Context context) {
        mContext = context.getApplicationContext();

        // 获取系统默认的 UncaughtException 处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        // 设置该 CrashHandler 为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当 UncaughtException 发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, final Throwable ex) {
        MobclickAgent.reportError(mContext,ex);
        new Thread() {
            @Override
            public void run() {
                try {
                    collectDeviceInfo(mContext);
                    File tempFile = saveCrashInfo2File(ex);
                    senderCrashToServer(tempFile);
                } catch (Exception ignored) {
                }

                Looper.prepare();
                Toast toast = Toast.makeText(mContext, "很抱歉，程序出现异常", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();

            }
        }.start();

        if (mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        }
    }

    /**
     * 发送错误日志到服务器
     * *
     */
    private void senderCrashToServer(File tempFile) throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = new FormEncodingBuilder()
                .add("subject", "EDAP-crash-" + DateUtil.genCurrDateStr())
                .add("content", FileUtils.readFileToString(tempFile, "utf-8"))
                .build();
        Request requestPost = new Request.Builder().url(Constans.COLLECT_CRUSH_URL).post(requestBody).build();
        okHttpClient.newCall(requestPost).execute();
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx 上下文
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);

            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (Exception ignored) {
        }

        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                if (field.getType().isArray()) {
                    infos.put(field.getName(), Arrays.toString((Object[]) field.get(null)));
                } else {
                    infos.put(field.getName(), field.get(null).toString());
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 保存错误信息到文件中
     * *
     *
     * @param ex 错误对象
     */
    private File saveCrashInfo2File(Throwable ex) {
        File timestampFile = null;
        //手机设备信息
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        //手机错误信息
        StringWriter stringWriter = new StringWriter();
        PrintWriter printStream = new PrintWriter(stringWriter);
        ex.printStackTrace(printStream);

        try {
            long timestamp = System.currentTimeMillis();
            String time = DateUtil.genCurrDateStr();
            String fileName = "crash-" + time + "-" + timestamp + ".log";


            File crashDir = StroageHelper.getProjectCrashDir(mContext);  //缓存文件夹路径
            timestampFile = new File(crashDir.getAbsolutePath() + File.separator + fileName);

            FileUtils.writeStringToFile(timestampFile, sb.toString() + "\n", Charset.forName("utf-8"));
            FileUtils.writeStringToFile(timestampFile, stringWriter.toString(), "utf-8", true);

        } catch (Exception ignored) {
        }
        return timestampFile;
    }

}
