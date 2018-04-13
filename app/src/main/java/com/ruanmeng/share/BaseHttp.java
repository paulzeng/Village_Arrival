package com.ruanmeng.share;

import com.ruanmeng.village_arrival.BuildConfig;

/**
 * 项目名称：Village_Arrival
 * 创建人：小卷毛
 * 创建时间：2018-04-10 09:06
 */
public class BaseHttp {

    private static String baseUrl = BuildConfig.API_HOST;
    private static String baseIp = baseUrl + "/api";
    public static String baseImg = baseUrl + "/";

    public static String login_sub = baseIp + "/login_sub.rm"; //登录√
    public static String identify_get = baseIp + "/identify_get.rm"; //注册验证码√
    public static String register_sub = baseIp + "/register_sub.rm"; //注册√
    public static String identify_getbyforget = baseIp + "/identify_getbyforget.rm"; //忘记验证码√
    public static String pwd_forget_sub = baseIp + "/pwd_forget_sub.rm"; //忘记密码√
}
