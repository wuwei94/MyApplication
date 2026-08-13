package com.example.william.my.core.retrofit.loading;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.william.my.core.retrofit.R;

/**
 * 加载状态提示 View。
 */
public class LoadingTipView extends LinearLayout implements View.OnClickListener {

    private TextView mTextView;

    public LoadingTipView(Context context) {
        super(context);
        initView(context);
    }

    public LoadingTipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LoadingTipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.basics_layout_loading, this);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setBackgroundColor(Color.WHITE);

        mTextView = findViewById(R.id.loading_textView);
        mTextView.setOnClickListener(this);
        setLoadingTip(Status.loading);
    }

    @Override
    public void onClick(View v) {
        if (onReloadListener != null) {
            onReloadListener.reload();
        }
    }

    public enum Status {
        loading, empty, finish, error
    }

    public void setMessage(String message) {
        mTextView.setText(message);
    }

    public void setLoadingTip(Status status) {
        this.setLoadingTip(status, null);
    }

    public void setLoadingTip(Status status, String message) {
        switch (status) {
            case loading:
                setVisibility(View.VISIBLE);
                setEnabled(false);
                mTextView.setText("加载中……");
                break;
            case empty:
                setVisibility(View.VISIBLE);
                setEnabled(false);
                if (message == null) {
                    mTextView.setText("暂无数据");
                } else {
                    mTextView.setText(message);
                }
                break;
            case finish:
                setVisibility(GONE);
                break;
            case error:
                setVisibility(View.VISIBLE);
                setEnabled(true);
                if (message == null) {
                    mTextView.setText("网络异常，请刷新页面");
                } else {
                    mTextView.setText(message);
                }
                break;
            default:
                break;
        }
    }

    /** 添加覆盖全屏内容的加载提示。 */
    public static LoadingTipView addLoadingTipFullScreen(Activity context) {
        LoadingTipView loadingTipView = new LoadingTipView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, getStatusBarHeight(), 0, 0);
        ((FrameLayout) context.getWindow().getDecorView()).addView(loadingTipView, params);
        return loadingTipView;
    }

    /** 添加避开顶部栏的加载提示。 */
    public static LoadingTipView addLoadingTipWithTopBar(Activity context) {
        LoadingTipView loadingTipView = new LoadingTipView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, getToolBarHeight() + getStatusBarHeight(), 0, 0);
        ((FrameLayout) context.getWindow().getDecorView()).addView(loadingTipView, params);
        return loadingTipView;
    }

    public static int getToolBarHeight() {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (48 * scale + 0.5f);
    }

    public static int getStatusBarHeight() {
        Resources resources = Resources.getSystem();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    private LoadingTipListener onReloadListener;

    public void setOnReloadListener(LoadingTipListener listener) {
        onReloadListener = listener;
    }

    public interface LoadingTipListener {
        /** 重新加载。 */
        void reload();
    }
}
