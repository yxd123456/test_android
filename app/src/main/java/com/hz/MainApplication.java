package com.hz;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.mapapi.SDKInitializer;
import com.hz.exception.CrashHandler;
import com.hz.greendao.dao.DaoMaster;
import com.hz.greendao.dao.DaoSession;
import com.hz.helper.DataBaseManagerHelper;
import com.hz.helper.MapIconHelper;
import com.hz.helper.StroageHelper;
import com.hz.util.HttpManager;
import com.hz.util.PackageUtil;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.squareup.leakcanary.LeakCanary;

import java.io.File;

import cn.jpush.android.api.JPushInterface;

/**
 * @author taol
 * @see android.app.Application
 * <p/>
 * *
 */
public class MainApplication extends Application {
    public static final String TAG = MainApplication.class.getSimpleName();
    public DaoMaster daoMaster;//数据库操作全局对象


    @Override
    public void onCreate() {
        super.onCreate();
        initComponents();
    }

    /**
     * 初始化系统组件
     * *
     */
    private void initComponents() {
        if (PackageUtil.isDebugEnable(this)) {
            LeakCanary.install(this);
        }
        //初始化ImageLoader
        initImageLoader();

        //初始化CrashHandler
        CrashHandler.getInstance().init(this);

        //初始化MapSdk
        SDKInitializer.initialize(this.getApplicationContext());

        //初始化 GreenDao
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(this, "ketech-db", null);
        SQLiteDatabase sqLiteDatabase = devOpenHelper.getWritableDatabase();
        daoMaster = new DaoMaster(sqLiteDatabase);

        //初始化 JPush
        JPushInterface.setDebugMode(true);    // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);            // 初始化 JPush

        //初始化 HttpManager
        HttpManager.getInstance().init(this);

        //初始化 DataBaseManagerHelper
        DataBaseManagerHelper.getInstance().init(this);

        /**地图资源初始化**/
        MapIconHelper.getInstance().init(this);
    }

    /**
     * 获取全局DaoSession对象*
     */
    public DaoSession getDaoSession() {
        return this.daoMaster.newSession();
    }


    /**
     * 初始化imageLoader图片加载缓存
     * <p/>
     * **
     */
    private void initImageLoader() {
        File cacheDir = StroageHelper.getProjectImageLoaderCacheDir(this.getApplicationContext());

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .memoryCacheExtraOptions(480, 800) // default = device screen dimensions 内存缓存文件的最大长宽
                .diskCacheExtraOptions(480, 800, null)  // 本地缓存的详细信息(缓存的最大长宽)，最好不要设置这个
                .threadPriority(Thread.NORM_PRIORITY - 2) // default 设置当前线程的优先级
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new WeakMemoryCache()) //可以通过自己的内存缓存实现
                .diskCache(new UnlimitedDiscCache(cacheDir, null, new Md5FileNameGenerator())) // default 可以自定义缓存路径
                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000)) // default
                .imageDecoder(new BaseImageDecoder(true)) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
              /*  .writeDebugLogs()*/ // 打印debug log
                .build();

        ImageLoader.getInstance().init(config);

        /**
         * String imageUri = "http://site.com/image.png"; // from Web
         * String imageUri = "file:///mnt/sdcard/image.png"; // from SD card
         * String imageUri = "content://media/external/audio/albumart/13"; // from content provider
         * String imageUri = "assets://image.png"; // from assets
         * String imageUri = "drawable://" + R.drawable.image; // from drawables (only images, non-9patch)
         * **/
    }
}
