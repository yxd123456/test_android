package com.hz.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.hz.R;
import com.hz.adapter.PickerListViewAdapter;
import com.hz.util.DeviceUtils;
import com.hz.view.ClearAbleEditText;
import com.hz.entity.PickerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 在dialog中选择listview数据
 */
public class PickerListViewDialog extends AppCompatDialog implements View.OnClickListener, PickerListViewAdapter.onPickerListSelectListener, View.OnTouchListener, ClearAbleEditText.OnTextChangeListener {
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    public static final String TAG = PickerListViewDialog.class.getSimpleName();
    public static final int PICKER_OK_ID = R.id.id_picker_ok;//确定按钮ID
    public static final int PICKER_CANCEL_ID = R.id.id_picker_cancel;//取消按钮ID
    public static final String PICKER_ITEM_DEFAULT_KEY = "-1";

    private List<PickerItem> pickerDatas = new ArrayList<>();//pickerView数据源
    private List<PickerItem> pickerDatasOragin = new ArrayList<>();//保存pickerView原始数据源
    private PickerItem currentSelectItem;//保存当前选中的数据
    private PickerListViewAdapter mPickerListViewAdapter;

    private EditText bindEditText;//pickerDialog绑定EditText
    private ClearAbleEditText mClearEdittext;//可以清空输入输入框
    private ListView mPickerListview;

    private onPickerDialogHasFocusListener pickerDialogHasFocusListener;//当EditText获取焦点后触发事件

    public PickerListViewDialog(Context context) {
        super(context);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_listview_picker);
        initComponent();
        this.setTitle(R.string.title_dialog_picker);
    }
    @Override
    public void show() {
        super.show();
        Log.d("Test",getClass().getSimpleName()+"被调用了");

        mPickerListViewAdapter = new PickerListViewAdapter(pickerDatas, this.getContext(), this);
        mPickerListview.setAdapter(mPickerListViewAdapter);

        //如果pickerview的数据源为空，者设置一个默认值
        if (pickerDatas == null || pickerDatas.size() == 0) {
            PickerItem pickerItem = new PickerItem();
            pickerItem.value = this.getContext().getString(R.string.string_pickerdialog_nothavedata);
            pickerItem.key = PICKER_ITEM_DEFAULT_KEY;
            pickerDatas.add(pickerItem);
        }

        //获取上次选中的id 如果不为空 则设置为默认选中的id
        String key = this.getPickerDialogBindEditTextTagValue();
        if (key != null) {
            mPickerListViewAdapter.setSelected(new PickerItem(key, ""));
        }

        mPickerListViewAdapter.notifyDataSetChanged();
        if (key != null) {
            //滑动到指定的位置
            for (int i = 0; i < pickerDatas.size(); i++) {
                PickerItem pickerItem = pickerDatas.get(i);
                if (pickerItem.key.equalsIgnoreCase(key)) {
                    Log.d(TAG, "smoothScrollToPosition ----> " + i);
                    mPickerListview.smoothScrollToPosition(i);
                    break;
                }
            }
        }

        if (mClearEdittext != null) {
            mClearEdittext.setText("");
        }
    }
    @Override
    public void onTextChange(String newText) {
        Log.d(TAG, newText);
        if (pickerDatasOragin.size() <= 0) {
            return;
        }

        if (TextUtils.isEmpty(newText)) {
            pickerDatas.clear();
            pickerDatas.addAll(pickerDatasOragin);
        } else {
            pickerDatas.clear();
            for (PickerItem pickerItem : pickerDatasOragin) {
                if (pickerItem.value.toLowerCase().contains(newText.toLowerCase())) {
                    pickerDatas.add(pickerItem);
                }
            }
        }
        mPickerListViewAdapter.notifyDataSetChanged();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case PickerListViewDialog.PICKER_OK_ID:
                if (!checkCurrentSelectItemIsDefault() && currentSelectItem != null) {
                    bindEditText.setText(currentSelectItem.value);
                    bindEditText.setTag(currentSelectItem.key);
                    Log.d(TAG, "ok:" + currentSelectItem);
                }
                break;
            case PickerListViewDialog.PICKER_CANCEL_ID:
                if (!checkCurrentSelectItemIsDefault() && currentSelectItem != null) {
                    Log.d(TAG, "cancel:" + currentSelectItem);
                    bindEditText.setText("");
                    bindEditText.setTag("");
                    currentSelectItem = null;
                }
                break;
        }
        pickerDialogHasFocusListener.pickerDialogHasNotFocus(this);
        this.dismiss();
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (v.getId() == this.getPickerDialogBindEditTextId()) {
                pickerDialogHasFocusListener.pickerDialogHasFocus(this);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    @Override
    public void onPickerListSelect(PickerItem pickerItem) {
        Log.d(TAG, "onPickerListSelect------>" + pickerItem.toString());
        currentSelectItem = pickerItem;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    /**
     * 初始化pickerView组件
     */
    private void initComponent() {
        int width = DeviceUtils.getScreenWidth(getContext()); // 屏幕宽度（像素）
        int height = DeviceUtils.getScreenHeight(getContext());
        int size = Math.min(width, height);

        View containerView = findViewById(R.id.id_dialog_picker_container);
        ViewGroup.LayoutParams layoutParams = containerView.getLayoutParams();
        layoutParams.width = (int) (size * 0.7);
        layoutParams.height = (width > height) ? (int) (size * 0.6) : (int) (size * 0.9);
        containerView.setLayoutParams(layoutParams);

        this.setCanceledOnTouchOutside(true);
        this.setCancelable(true);
        getWindow().setWindowAnimations(R.style.picker_listview_dialog_topin_topout_animation);

        mPickerListview = (ListView) findViewById(R.id.id_pickerlistview);
        Button mPickerOk = (Button) findViewById(R.id.id_picker_ok);
        mPickerOk.setOnClickListener(this);
        Button mPickerCancel = (Button) findViewById(R.id.id_picker_cancel);
        mPickerCancel.setOnClickListener(this);

        mClearEdittext = (ClearAbleEditText) findViewById(R.id.id_clearableedittext_dialog_listview);
        mClearEdittext.setOnTextChangeListener(this);
    }
    /**
     * 设置picker的数据源
     */
    public PickerListViewDialog setPickerDialogDatas(List<PickerItem> datas) {
        pickerDatas.clear();
        pickerDatas.addAll(datas);
        pickerDatasOragin.clear();
        pickerDatasOragin.addAll(datas);
        return this;
    }

    private boolean checkCurrentSelectItemIsDefault() {
        return currentSelectItem != null && currentSelectItem.key.equalsIgnoreCase(PICKER_ITEM_DEFAULT_KEY);
    }
    /**
     * 获取pickerDialo绑定的EditText的Id
     */
    public int getPickerDialogBindEditTextId() {
        return this.bindEditText.getId();
    }
    /**
     * 获取pickerDialo绑定的EditText
     */
    public EditText getPickerDialogBindEditText() {
        return this.bindEditText;
    }
    /**
     * 设置picker绑定的EditText
     */
    public PickerListViewDialog setPickerDialogBindEditText(EditText bildEditText) {
        this.bindEditText = bildEditText;
        bindEditText.setOnTouchListener(this);
        return this;
    }
    /**
     * 获取pickerDialo绑定的EditText的tag值
     */
    public String getPickerDialogBindEditTextTagValue() {
        Object tag = this.bindEditText.getTag();
        if (tag != null) {
            return tag.toString();
        } else {
            return null;
        }
    }
    /**
     * 为pickerView设置获取焦点监听器
     */
    public PickerListViewDialog setOnPickerDialogHasFocusListener(onPickerDialogHasFocusListener onPickerDialogHasFocusListener) {
        this.pickerDialogHasFocusListener = onPickerDialogHasFocusListener;
        return this;
    }
    /**
     * pickerDialog获取输入焦点触发事件*
     */
    public interface onPickerDialogHasFocusListener {
        void pickerDialogHasFocus(PickerListViewDialog pickerScrollViewDialog);

        void pickerDialogHasNotFocus(PickerListViewDialog pickerScrollViewDialog);
    }

}
