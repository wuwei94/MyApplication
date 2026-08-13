package com.example.william.my.core.retrofit.loading;

import androidx.lifecycle.Observer;

import com.example.william.my.core.retrofit.response.RetrofitResponse;

import java.util.Collection;

/**
 * 将 LiveData 业务响应同步到加载提示 View 的 Observer。
 */
public abstract class LoadingTipObserver<T> implements Observer<RetrofitResponse<T>> {

    private final LoadingTipView mLoadingTipView;

    public LoadingTipObserver(LoadingTipView loadingTipView) {
        this.mLoadingTipView = loadingTipView;
    }

    public LoadingTipObserver(LoadingTipView loadingTipView, String message) {
        this.mLoadingTipView = loadingTipView;
        if (this.mLoadingTipView != null) {
            this.mLoadingTipView.setMessage(message);
        }
    }

    @Override
    public void onChanged(RetrofitResponse<T> retrofitResponse) {
        if (retrofitResponse == null) {
            return;
        }
        switch (retrofitResponse.getCode()) {
            case RetrofitResponse.LOADING:
                if (mLoadingTipView != null) {
                    mLoadingTipView.setLoadingTip(LoadingTipView.Status.loading);
                }
                break;
            case RetrofitResponse.SUCCESS:
                if (mLoadingTipView != null) {
                    mLoadingTipView.setLoadingTip(isEmpty(retrofitResponse.getData())
                            ? LoadingTipView.Status.empty
                            : LoadingTipView.Status.finish);
                }
                onResponse(retrofitResponse.getData());
                break;
            default:
                if (!onFailure(retrofitResponse.getMessage()) && mLoadingTipView != null) {
                    mLoadingTipView.setLoadingTip(LoadingTipView.Status.error, retrofitResponse.getMessage());
                }
                break;
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean isEmpty(Object object) {
        if (object == null) {
            return true;
        }
        if (object instanceof Collection) {
            return ((Collection) object).isEmpty();
        }
        return false;
    }

    /** 业务响应成功时返回数据。 */
    protected abstract void onResponse(T response);

    /**
     * 业务响应失败时返回错误消息。
     *
     * @return `false` 表示显示默认错误提示
     */
    @SuppressWarnings("SameReturnValue")
    protected boolean onFailure(String message) {
        return false;
    }
}
