package com.hz.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hz.R;
import com.hz.activity.MainActivity;
import com.hz.activity.ProjectDataPreviewActivity;
import com.hz.activity.base.BaseActivity;
import com.hz.common.Constans;
import com.hz.fragment.ProjectListFragment;
import com.hz.greendao.dao.ProjectEntity;
import com.hz.helper.DataBaseManagerHelper;
import com.hz.helper.SharedPreferencesHelper;
import com.hz.view.PopupToast;
import com.hz.view.ProjectStatusView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 项目列表数据适配器
 */
public class ProjectListAdapter extends RecyclerView.Adapter<ProjectListAdapter.ProjectListViewHolder> {
    public static final String TAG = ProjectListAdapter.class.getSimpleName();
    private List<ProjectEntity> projectListEntityList;
    private LayoutInflater layoutInflater;
    private int selectItem = -1;
    private WeakReference<BaseActivity> baseActivityWeakReference;

    public ProjectListAdapter(ProjectListFragment projectListFragment, List<ProjectEntity> projectListEntityList) {
        this.baseActivityWeakReference = new WeakReference<>((BaseActivity) projectListFragment.getActivity());
        this.projectListEntityList = projectListEntityList;
        this.layoutInflater = LayoutInflater.from(projectListFragment.getActivity());
    }

    @Override
    public ProjectListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.activity_project_list_item, null);
        ProjectListViewHolder galleryViewHolder = new ProjectListViewHolder(view);
        galleryViewHolder.programmeNameTextView = (TextView) view.findViewById(R.id.id_textview_programmeName);
        galleryViewHolder.projectNameTextView = (TextView) view.findViewById(R.id.id_textview_projectName);
        galleryViewHolder.cjsjTextView = (TextView) view.findViewById(R.id.id_textview_cjsj);
        galleryViewHolder.projectDataPreview = (Button) view.findViewById(R.id.id_button_previewprojectdata);
        galleryViewHolder.projectImage = (ImageView) view.findViewById(R.id.id_imageview_leftimage);
        galleryViewHolder.projectStatusView = (ProjectStatusView) view.findViewById(R.id.id_view_projectstatus);
        return galleryViewHolder;
    }

    @Override
    public void onBindViewHolder(ProjectListViewHolder holder, int position) {
        ProjectEntity item = projectListEntityList.get(position);
        setViewValue(holder, item);
        setViewListener(holder, item);
        if (item.getId() == selectItem) {
            BaseActivity baseActivity = baseActivityWeakReference.get();
            if (baseActivity != null) {
                holder.rootView.setBackgroundColor(baseActivity.getResources().getColor(R.color.view_select_background_color));
            }

        } else {
            holder.rootView.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        return projectListEntityList.size();
    }

    private void setViewListener(final ProjectListViewHolder viewHolder, final ProjectEntity item) {
        //去打点
        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelection((int) item.getId());

                BaseActivity baseActivity = baseActivityWeakReference.get();
                if (baseActivity != null) {
                    Intent mainActivity = new Intent(baseActivity, MainActivity.class);
                    mainActivity.putExtra(ProjectListFragment.PROJECT_OBJ_KEY, item);
                    baseActivity.startActivityForResult(mainActivity, ProjectListFragment.REQUESTCODE_TOMAP);
                    viewHolder.rootView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    },300);

                }
            }
        });
        //提交点位和图片
        viewHolder.projectDataPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onButtonClick: viewHolder.projectDataPreview");

                BaseActivity baseActivity = baseActivityWeakReference.get();
                if (baseActivity != null) {
                    long userId = SharedPreferencesHelper.getUserId(baseActivity);
                    long pointCount = DataBaseManagerHelper.getInstance().getProjectPointCountByProjectId(item.getId(), userId);
                    long lineCount = DataBaseManagerHelper.getInstance().getProjectLineCountByProjectId(item.getId(), userId);
                    if (pointCount + lineCount == 0) {
                        String title = "当前项目[" + item.getProjectName() + "]无采集数据,无法查看";
                        PopupToast.show(baseActivity, Gravity.BOTTOM,title,PopupToast.CUSTOME);
                        return;
                    }

                    Intent intent = new Intent(baseActivity, ProjectDataPreviewActivity.class);
                    intent.putExtra(ProjectListFragment.PROJECT_OBJ_KEY, item);
                    baseActivity.startActivity(intent);
                }
            }
        });
    }

    private void setViewValue(ProjectListViewHolder viewHolder, ProjectEntity item) {
        viewHolder.programmeNameTextView.setText(item.getProgrammeName());
        viewHolder.projectNameTextView.setText(item.getProjectName());
        viewHolder.cjsjTextView.setText(item.getCjsj());
        String imageLoaderUrl = Constans.ImageLoaderMark.DRAWABLE + R.drawable.vp_bg_1;
        BaseActivity baseActivity = baseActivityWeakReference.get();
        if (baseActivity != null) {
            ImageLoader.getInstance().displayImage(imageLoaderUrl, viewHolder.projectImage);
        }


        switch (Integer.parseInt(item.getStatus())) {
            case Constans.ProjectStatus.UN_DONE:
                viewHolder.projectStatusView.setBackGroundColor(R.color.white);
                viewHolder.projectStatusView.setBorderColor(R.color.project_status_undone_background);
                viewHolder.projectStatusView.setFillPercent(0.25f);
                viewHolder.projectStatusView.setFillColor(R.color.project_status_undone_fillcolor);
                break;
            case Constans.ProjectStatus.HAS_DONE:
                viewHolder.projectStatusView.setBackGroundColor(R.color.white);
                viewHolder.projectStatusView.setBorderColor(R.color.project_status_hasdone_background);
                viewHolder.projectStatusView.setFillPercent(0.5f);
                viewHolder.projectStatusView.setFillColor(R.color.project_status_hasdone_fillcolor);
                break;
            case Constans.ProjectStatus.HAS_AUDIT:
                viewHolder.projectStatusView.setBackGroundColor(R.color.white);
                viewHolder.projectStatusView.setBorderColor(R.color.project_status_hasaudio_background);
                viewHolder.projectStatusView.setFillPercent(0.75f);
                viewHolder.projectStatusView.setFillColor(R.color.project_status_hasaudio_fillcolor);
                break;
            case Constans.ProjectStatus.MATERIAL_DONE:
                viewHolder.projectStatusView.setBackGroundColor(R.color.white);
                viewHolder.projectStatusView.setBorderColor(R.color.project_status_materialdone_background);
                viewHolder.projectStatusView.setFillPercent(1f);
                viewHolder.projectStatusView.setFillColor(R.color.project_status_materialdone_fillcolor);
                break;
            default:
                viewHolder.projectStatusView.setBackGroundColor(R.color.white);
                viewHolder.projectStatusView.setBorderColor(R.color.white);
                viewHolder.projectStatusView.setFillPercent(0f);
                viewHolder.projectStatusView.setFillColor(R.color.white);
                break;
        }
    }

    public void setSelection(int selectItem) {
        this.selectItem = selectItem;
    }

    public static class ProjectListViewHolder extends RecyclerView.ViewHolder {
        public TextView programmeNameTextView;
        public TextView projectNameTextView;
        public TextView cjsjTextView;
        public Button projectDataPreview;
        public ImageView projectImage;
        public View rootView;
        public ProjectStatusView projectStatusView;

        public ProjectListViewHolder(View itemView) {
            super(itemView);
            this.rootView = itemView;
        }
    }
}
