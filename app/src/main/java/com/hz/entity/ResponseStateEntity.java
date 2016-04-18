package com.hz.entity;

/**
 * 网络请求返回值状态
 */
public class ResponseStateEntity {
    public boolean success;

    public boolean sessionExpired;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSessionExpired() {
        return sessionExpired;
    }

    public void setSessionExpired(boolean sessionExpired) {
        this.sessionExpired = sessionExpired;
    }

    @Override
    public String toString() {
        return "ResponseStateEntity{" +
                "success=" + success +
                ", sessionExpired=" + sessionExpired +
                '}';
    }
}
