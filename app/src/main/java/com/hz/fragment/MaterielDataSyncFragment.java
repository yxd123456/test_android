package com.hz.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hz.R;
import com.hz.activity.MaterielDetailActivity;
import com.hz.activity.base.BaseActivity;
import com.hz.adapter.MaterielDataSyncAdapter;
import com.hz.common.Constans;
import com.hz.dialog.ProgressHUD;
import com.hz.entity.MaterielDataSyncEntity;
import com.hz.entity.ResponseArrayWrapperEntity;
import com.hz.fragment.base.BaseFragment;
import com.hz.util.DensityUtil;
import com.hz.helper.GreenDaoHelper;
import com.hz.util.HttpManager;
import com.hz.helper.MaterielDataSyncHelper;
import com.hz.view.PopupToast;
import com.hz.view.recyclerview.HorizontalDividerItemDecoration;
import com.squareup.okhttp.Request;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.AbstractDao;

/**
 * 材料数据同步fragment
 */
public class MaterielDataSyncFragment extends BaseFragment {
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    public static final String TAG = MaterielDataSyncFragment.class.getSimpleName();
    private static ProgressHUD mProgressHUD = null;
    private BaseActivity baseActivity;
    private List<MaterielDataSyncEntity> mMaterielListEntityList = new ArrayList<>();
    private MaterielDataSyncAdapter materielDataSyncAdapter = null;
    private MaterielDataSyncHelper materielDataSyncHelper = new MaterielDataSyncHelper();

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseActivity = (BaseActivity) this.getActivity();
        View rootView = inflater.inflate(R.layout.activity_materiel_datasync_list, null, false);
        initViews(rootView);
        return rootView;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 初始化材料同步列表
     */
    private void reInitMaterielDatas() {
        List<MaterielDataSyncEntity> materielSettingEntityList = materielDataSyncHelper.getAllMateriel(baseActivity);

        //获取每个材料的个数
        for (MaterielDataSyncEntity entity : materielSettingEntityList) {
            entity.setNum(getTowerTypeCountByMaterielId(entity.getMaterielId()));
        }

        mMaterielListEntityList.clear();
        mMaterielListEntityList.addAll(materielSettingEntityList);
        materielDataSyncAdapter.notifyDataSetChanged();
    }
    /**
     * 同步材料数据实现
     */
    public void syncMaterielDatas(final MaterielDataSyncEntity item, final int position) {
        mProgressHUD = ProgressHUD.show(this.getActivity(), "获取服务器最新数据中");
        Request request = new Request.Builder().url(item.getServerUrl()).get().build();
        HttpManager.getInstance().addAsyncHttpTask(request, new HttpManager.HttpTaskCallback() {
            @Override
            public void onFailure(Exception e) {
                mProgressHUD.dismiss();
                materielDataSyncAdapter.notifyDataSetChanged();
                PopupToast.showError(MaterielDataSyncFragment.this.baseActivity, "数据同步失败");
            }

            @Override
            public void onSuccess(String respstr) {
                try {
                    //1.解析json为TowerType对象
                    ResponseArrayWrapperEntity wrapperEntity = materielDataSyncHelper.analyJsonToBeanByMaterielId(respstr, item.getMaterielId());
                    if (wrapperEntity != null) {
                        //2.将TowerType对象列表持久化到数据库
                        materielDataSyncHelper.persisentMaterielBeanToDbByMaterielId(baseActivity, wrapperEntity, item.getMaterielId());
                        //3.重新初始化数据源
                        reInitMaterielDatas();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "materielId:" + item.getMaterielId() + ",异常:" + e.toString());
                }
                mProgressHUD.dismiss();
                materielDataSyncAdapter.notifyItemChanged(position);
                PopupToast.showOk(MaterielDataSyncFragment.this.baseActivity, "数据同步成功");
            }
        });
    }
    /**
     * 根据材料的ID获取总数
     * @param materielId 材料ID
     */
    public int getTowerTypeCountByMaterielId(String materielId) {
        long count = 0;
        AbstractDao abstractDao = GreenDaoHelper.getGreenDaoByMaterialType(this.baseActivity, materielId);
        if (abstractDao != null) {
            count = abstractDao.count();
        }
        Log.d(TAG, "materielId:" + materielId + ",count:" + count);
        return (int) count;
    }
    /**
     * 查看材料详细信息页面
     * @param item 材料实体
     */
    public void startDetailActivity(MaterielDataSyncEntity item) {
        Intent materielDetail = new Intent(baseActivity, MaterielDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(Constans.MaterielDetail.MATERIEL_DETAIL_ICON_KEY, item.getImageId());
        bundle.putString(Constans.MaterielDetail.MATERIEL_DETAIL_TITLE_KEY, item.getMaterielName());
        bundle.putString(Constans.MaterielDetail.MATERIEL_DETAIL_TYPE_KEY, item.getMaterielId());
        materielDetail.putExtras(bundle);
        this.startActivity(materielDetail);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 初始化视图组件
     * @param rootView 视图根节点
     */
    private void initViews(View rootView) {
        RecyclerView mMaterielList = (RecyclerView) rootView.findViewById(R.id.id_listview_materials_list);
        materielDataSyncAdapter = new MaterielDataSyncAdapter(this, mMaterielListEntityList);
        mMaterielList.setAdapter(materielDataSyncAdapter);

        //设置布局管理器
        GridLayoutManager layoutManager = new GridLayoutManager(baseActivity, 1);
        mMaterielList.setLayoutManager(layoutManager);
        mMaterielList.setItemAnimator(new DefaultItemAnimator());

        //分割线
        mMaterielList.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this.getActivity())
                        .colorResId(R.color.view_seperator_color)
                        .size(DensityUtil.dip2px(this.getActivity(), 1))
                        .build()
        );

        reInitMaterielDatas();
    }

}
