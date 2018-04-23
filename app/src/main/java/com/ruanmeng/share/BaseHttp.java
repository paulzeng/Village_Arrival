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

    public static String login_sub = baseIp + "/login_sub.rm";                       //登录√
    public static String identify_get = baseIp + "/identify_get.rm";                 //注册验证码√
    public static String register_sub = baseIp + "/register_sub.rm";                 //注册√
    public static String identify_getbyforget = baseIp + "/identify_getbyforget.rm"; //忘记验证码√
    public static String pwd_forget_sub = baseIp + "/pwd_forget_sub.rm";             //忘记密码√

    public static String user_msg_data = baseIp + "/user_msg_data.rm";                     //个人资料√
    public static String userinfo_uploadhead_sub = baseIp + "/userinfo_uploadhead_sub.rm"; //修改头像√
    public static String nickName_change_sub = baseIp + "/nickName_change_sub.rm";         //修改昵称√
    public static String sex_change_sub = baseIp + "/sex_change_sub.rm";                   //修改性别√
    public static String certification_info = baseIp + "/certification_info.rm";           //实名信息√
    public static String certification_sub = baseIp + "/certification_sub.rm";             //实名认证√
    public static String my_order_list = baseIp + "/my_order_list.rm";                     //我的发布√
    public static String goodsorde_dealis = baseIp + "/goodsorde_dealis.rm";               //订单详情√
    public static String my_account_info = baseIp + "/my_account_info.rm";                 //我的账户√
    public static String my_commonaddress_list = baseIp + "/my_commonaddress_list.rm";     //地址列表√
    public static String add_commonaddress = baseIp + "/add_commonaddress.rm";             //新增地址√
    public static String delete_commonaddress = baseIp + "/delete_commonaddress.rm";       //删除地址√

    public static String frist_index_data = baseIp + "/frist_index_data.rm"; //首页√
    public static String weightprice_list_data = baseIp + "/weightprice_address_list_data.rm"; //商品重量√
    public static String order_commission = baseIp + "/order_commission.rm"; //获取佣金√
    public static String add_goodsorde = baseIp + "/add_goodsorde.rm"; //新增订单√
    public static String balance_pay_order = baseIp + "/balance_pay_order.rm"; //余额支付√
}
