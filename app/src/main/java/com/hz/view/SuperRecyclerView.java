package com.hz.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.hz.R;

/**
 * 实现EmptyView功能
 */
public class SuperRecyclerView extends RecyclerView implements View.OnClickListener {

    public static final String TAG = SuperRecyclerView.class.getSimpleName();
    private int emptyViewId;
    private OnEmptyViewClickListener onEmptyViewClickListener;
    private View emptyView;
    /**
     * 适配器变化监听器
     **/
    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            boolean isEmpty = getAdapter() == null || getAdapter().getItemCount() == 0;
            onRecyclerViewDataChange(isEmpty);
        }
    };

    public SuperRecyclerView(Context context) {
        super(context);
        init(null);
    }

    public SuperRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SuperRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    /**
     * 初始化系统组件
     **/
    private void init(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = this.getContext().obtainStyledAttributes(attrs, R.styleable.SuperRecyclerView);
        emptyViewId = a.getResourceId(R.styleable.SuperRecyclerView_emptyView, 0);
        a.recycle();


        this.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                /*LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    int topPosition = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
                    Log.d(TAG, "onScrolled: " + topPosition + "  " + getAdapter().getItemCount());
                    if (topPosition == 0) {
                        Log.d(TAG, "onScrolled: 继续往下滑动，下拉刷新");
                        return;
                    }


                    int bottomPosition = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
                    if (bottomPosition == getAdapter().getItemCount() - 1) {
                        Log.d(TAG, "onScrolled: 继续往上滑动，上拉加载更多");
                    }
                }
*/
            }
        });
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }
        emptyObserver.onChanged();
    }

    /**
     * 数据源数据改变事件
     **/
    public void onRecyclerViewDataChange(boolean isEmpty) {
        ViewGroup parentView = (ViewGroup) this.getParent();
        if (parentView == null) {
            return;
        }

        ViewGroup parentView2 = (ViewGroup) parentView.getParent();

        if (parentView2 != null) {
            emptyView = parentView2.findViewById(emptyViewId);
        } else {
            emptyView = parentView.findViewById(emptyViewId);
        }

        if (emptyView == null) {
            return;
        }

        emptyView.setOnClickListener(this);

        if (isEmpty) {
            this.setVisibility(GONE);
            emptyView.setVisibility(VISIBLE);
        } else {
            this.setVisibility(VISIBLE);
            emptyView.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (onEmptyViewClickListener != null) {
            onEmptyViewClickListener.onEmptyViewClick(emptyView);
        }
    }

    /**
     * 设置列表看视图点击事件
     **/
    public void setOnEmptyViewClickListener(OnEmptyViewClickListener onEmptyViewClickListener) {
        this.onEmptyViewClickListener = onEmptyViewClickListener;
    }

    /**
     * 空视图点击监听器
     **/
    public interface OnEmptyViewClickListener {
        void onEmptyViewClick(View emptyView);
    }

    /**
     * 获取EmptyView
     **/
    public View getEmptyView() {
        return emptyView;
    }


}
