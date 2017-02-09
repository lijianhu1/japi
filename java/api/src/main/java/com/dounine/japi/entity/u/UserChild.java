package com.dounine.japi.entity.u;

import org.springframework.beans.factory.annotation.Value;

/**
 * Created by huanghuanlai on 2017/1/18.
 */
public class UserChild {

    /**
     * 用户名
     * @reg 这是正则表达式
     * @des 这是描述信息
     */
    @Value("")
    String username;
    /**
     * 用户密码
     */
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}