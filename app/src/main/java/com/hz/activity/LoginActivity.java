package com.hz.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.hz.R;
import com.hz.activity.base.BaseActivity;
import com.hz.common.Constans;
import com.hz.dialog.ProgressHUD;
import com.hz.entity.ResponseObjWrapperEntity;
import com.hz.entity.ResponseStateEntity;
import com.hz.util.EncryptUtil;
import com.hz.util.HttpManager;
import com.hz.util.JsonUtil;
import com.hz.util.NetworkManager;
import com.hz.helper.SharedPreferencesHelper;
import com.hz.view.PopupToast;
import com.hz.view.ValidaterEditText;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

/**
 * 登陆系统
 * *
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    public static final String KEY_USERID = "KEY_USERID";
    public static final String KEY_USERNAME = "KEY_USERNAME";
    public static final String KEY_PASSWD = "KEY_PASSWD";
    public static final String KEY_REALNAME = "KEY_REALNAME";//保存用户真实姓名
    public static final String KEY_REGISTRATIONID = "KEY_REGISTRATIONID";
    private ProgressHUD mLoginProgressHUD = null;//登录中状态提示
    private ValidaterEditText mUserName;//用户名输入
    private ValidaterEditText mPassWd;//密码输入

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initComponents();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoginProgressHUD != null && mLoginProgressHUD.isShowing()) {
            mLoginProgressHUD.dismiss();
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_button_login:
                validateLoginInfo();
                break;
        }
    }
    @Override
    public void onBackPressed() {

    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 初始化系统相关信息
     **/
    private void initComponents() {
        initViews();
        displayLoginInfoFromPreferences();
    }
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

    /**
     * 展示配置文件中存储的用户名密码信息
     */
    private void displayLoginInfoFromPreferences() {
        String userName = SharedPreferencesHelper.getUserNameFrom(this);
        String passWd = SharedPreferencesHelper.getUserPassWd(this);

        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(passWd)) {
            mUserName.setText(userName);
            mPassWd.setText(passWd);
        }
    }
    /**
     * 处理登陆输入验证逻辑
     */
    private void validateLoginInfo() {
        //验证用户名密码有没有输入
        if (!mUserName.validateByConfig() || !mPassWd.validateByConfig()) {
            return;
        }

        //检查网络可用性
        if (!NetworkManager.isConnectAvailable(this)) {
            PopupToast.showError(LoginActivity.this, "当前网络不可用，请检查网络后重试");
            return;
        }

        //验证jpush注册id
        String registrationId = SharedPreferencesHelper.getRegistrationId(this);
        if (TextUtils.isEmpty(registrationId)) {
            PopupToast.showError(LoginActivity.this, "Jpush服务未注册成功，请稍后重试");
            return;
        }

        String userName = getString(mUserName.getText());
        String passwd = EncryptUtil.getHmacMD5String(getString(mPassWd.getText()), EncryptUtil.EDAP_SALT);//加密密码
        String loginUrlGetData = String.format(Constans.LOGIN_URL_GETUSERDATA, userName, passwd, registrationId);

        String loginUrl = Constans.LOGIN_URL_LOGIN;

        if (TextUtils.isEmpty(passwd) || TextUtils.isEmpty(loginUrl)) {
            PopupToast.showError(LoginActivity.this, "请稍后重试");
            return;
        }
        mLoginProgressHUD = ProgressHUD.show(this, "登录系统中");
        login(loginUrl, userName, passwd, loginUrlGetData);
    }
    /**
     * 登陆系统
     * @param passwd   密码
     * @param userName 用户名
     * @param loginUrl 登陆系统地址
     */
    private void login(final String loginUrl, final String userName, final String passwd, final String loginUrlGetData) {

        new AsyncTask<String, Long, Integer>() {
            @Override
            protected Integer doInBackground(String... params) {
                //1.登陆系统保存cookie
                RequestBody loginRequestBody = new FormEncodingBuilder()
                        .add("username", userName)
                        .add("password", passwd)
                        .add("remember-me", "true")
                        .build();

                Request loginRequest = new Request.Builder()
                        .url(loginUrl)
                        .addHeader("X-Requested-With", "XMLHttpRequest")
                        .post(loginRequestBody)
                        .build();

                String loginResult = HttpManager.getInstance().addSyncHttpTask(loginRequest);
                if (loginResult != null) {
                    Log.d(TAG, "run: " + loginResult);
                    ResponseStateEntity loginRespState = JsonUtil.convertJsonToObj(loginResult, ResponseStateEntity.class);
                    if (loginRespState == null || !loginRespState.isSuccess()) {
                        return 2000;
                    }
                } else {
                    return 2000;
                }


                //2.获取用户信息保存
                Request getDateRequest = new Request.Builder()
                        .url(loginUrlGetData)
                        .addHeader("X-Requested-With", "XMLHttpRequest")
                        .get()
                        .build();

                String getDataResp = HttpManager.getInstance().addSyncHttpTask(getDateRequest);
                if (getDataResp != null) {
                    if (!saveUserData(getDataResp)) {
                        return 2001;
                    } else {
                        return 2004;
                    }
                } else {
                    return 2003;
                }
            }

            @Override
            protected void onPostExecute(Integer result) {
                mLoginProgressHUD.dismiss();
                switch (result) {
                    case 2000:
                        PopupToast.showError(LoginActivity.this, "登录失败");
                        break;
                    case 2001:
                        PopupToast.showError(LoginActivity.this, "用户信息解析失败");
                        break;
                    case 2003:
                        PopupToast.showError(LoginActivity.this, "获取用户信息失败");
                        break;
                    case 2004:
                        LoginActivity.this.finish();
                        SharedPreferencesHelper.saveNeedToUpdateMaterialDataIdentifier(LoginActivity.this, true);
                        break;
                }
            }
        }.execute();
    }
    /**
     * 根据登陆系统返回值检查登陆结果
     * @param respStr 登陆返回字符串
     */
    private boolean saveUserData(String respStr) {
        Log.d(TAG, "登陆系统返回值:" + respStr);

        String userName = getString(mUserName.getText());
        String passwd = getString(mPassWd.getText());

        ResponseObjWrapperEntity.UserLoginWrapperEntity responseEntity = JsonUtil.convertJsonToObj(respStr, ResponseObjWrapperEntity.UserLoginWrapperEntity.class);

        if (responseEntity != null && responseEntity.isSuccess()) {
            SharedPreferencesHelper.saveLoginInfoToPreferences(this, responseEntity.getData().getId(), responseEntity.getData().getRealname(), userName, passwd);
            return true;
        }
        return false;
    }
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 初始化系统视图
     **/
    private void initViews() {
        setMdToolBar(R.id.id_material_toolbar);
        setMDToolBarBackEnable(false);
        setMDToolBarTitle(R.string.title_activity_login);

        mUserName = (ValidaterEditText) findViewById(R.id.id_edittext_username);
        mPassWd = (ValidaterEditText) findViewById(R.id.id_edittext_passwd);
        Button mLogin = (Button) findViewById(R.id.id_button_login);
        mLogin.setOnClickListener(this);
        ImageView mImageView = (ImageView) findViewById(R.id.id_imageview_login);
        String imageLoaderUrl = Constans.ImageLoaderMark.DRAWABLE + R.drawable.vp_bg_1;
        ImageLoader.getInstance().displayImage(imageLoaderUrl, mImageView);

        findViewById(R.id.id_imageview_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "isRemberMeExpiredSync: " + HttpManager.getInstance().isRemberMeExpiredSync());
                    }
                }).start();
            }
        });
    }

}
