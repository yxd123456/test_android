package com.hz.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hz.R;
import com.hz.entity.PickerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * pickerlistview 适配器
 * *
 */
public class PickerListViewAdapter extends BaseAdapter {
    public static final String TAG = PickerListViewAdapter.class.getSimpleName();
    private PickerItem selectItem;
    private List<PickerItem> pickerDatas = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private Context context;
    private onPickerListSelectListener onPickerListSelectListener;

    public PickerListViewAdapter(List<PickerItem> pickerDatas, Context context, onPickerListSelectListener pickerListSelectListener) {
        this.pickerDatas = pickerDatas;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.onPickerListSelectListener = pickerListSelectListener;
    }

    @Override
    public int getCount() {
        return pickerDatas.size();
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
        final PickerItem item = pickerDatas.get(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.dialog_listview_picker_item, null);
            viewHolder.rootVeiw = convertView;
            viewHolder.pickerText = (TextView) convertView.findViewById(R.id.id_textview_dialog_listview_picker);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.pickerText.setText(item.value);

        if (selectItem != null && item.key.equalsIgnoreCase(selectItem.key)) {
            viewHolder.rootVeiw.setBackgroundColor(context.getResources().getColor(R.color.view_select_background_color));
            onPickerListSelectListener.onPickerListSelect(item);
        } else {
            viewHolder.rootVeiw.setBackgroundColor(context.getResources().getColor(R.color.content_background_color));
        }

        viewHolder.rootVeiw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelected(item);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    public void setSelected(PickerItem pickerItem) {
        this.selectItem = pickerItem;
    }


    public interface onPickerListSelectListener {
        void onPickerListSelect(PickerItem pickerItem);
    }

    static class ViewHolder {
        public View rootVeiw;
        public TextView pickerText;
    }
}
