package com.hz.entity;

/**
 * 数据同步到服务器返回信息bean
 */
public class ResponseObjWrapperEntity<T> extends ResponseStateEntity{

    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 用户登录返回
     */
    public static class UserLoginWrapperEntity extends ResponseObjWrapperEntity<UserLoginEntity> {
    }
}
