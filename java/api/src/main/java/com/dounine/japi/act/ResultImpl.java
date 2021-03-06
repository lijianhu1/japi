package com.dounine.japi.act;

/**
 * Created by lake on 17-2-24.
 */
public class ResultImpl<T> implements Result<T> {
    /**
     * 状态码
     */
    private int code;
    /**
     * 错误消息
     */
    private String msg;
    /**
     * 返回数据
     */
    private T data;

    public ResultImpl(){}

    public ResultImpl(String msg){
        this.msg = msg;
    }
    public ResultImpl(String msg,T data){
        this.msg = msg;
        this.data = data;
    }

    @Override
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
