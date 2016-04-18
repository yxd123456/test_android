package com.hz.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hz.R;
import com.hz.entity.MaterielDetailEntity;

import java.util.List;

/**
 * 物料详细列表数据适配器
 */
public class MaterielDetailListAdapter extends BaseAdapter {
    private List<MaterielDetailEntity> mMaterielDetailListEntities;
    private LayoutInflater mLayoutInflater;

    public MaterielDetailListAdapter(Activity materielDetailListActivity, List<MaterielDetailEntity> materielDetailListEntities) {
        Log.d("Test",getClass().getSimpleName()+"被调用了");

        this.mMaterielDetailListEntities = materielDetailListEntities;
        this.mLayoutInflater = LayoutInflater.from(materielDetailListActivity);
    }

    @Override
    public int getCount() {
        return mMaterielDetailListEntities.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MaterielDetailEntity item = mMaterielDetailListEntities.get(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.activity_materiel_detail_item, null);
            viewHolder.materielName = (TextView) convertView.findViewById(R.id.id_textview_materieldetailname);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.materielName.setText(item.getMaterielName());
        Log.d("INEEDTHIS", item.getMaterielName());
        return convertView;
    }

    static class ViewHolder {
        public TextView materielName;
    }
}
