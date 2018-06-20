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
    // public static String baseImg = baseUrl + "/";
    // public static String baseImg = BuildConfig.LOG_DEBUG ? "http://122.114.212.120:8097/" : "http://ebringhome.com/";
    public static String baseImg = "http://ebringhome.com/";

    public static String login_sub = baseIp + "/login_sub.rm";                       //登录√
    public static String identify_get = baseIp + "/identify_get.rm";                 //注册验证码√
    public static String register_sub = baseIp + "/register_sub.rm";                 //注册√
    public static String identify_getbyforget = baseIp + "/identify_getbyforget.rm"; //忘记验证码√
    public static String pwd_forget_sub = baseIp + "/pwd_forget_sub.rm";             //忘记密码√
    public static String identify_get2 = baseIp + "/identify_get2.rm";               //第三方登录验证码√

    public static String user_msg_data = baseIp + "/user_msg_data.rm";                       //个人资料√
    public static String userinfo_uploadhead_sub = baseIp + "/userinfo_uploadhead_sub.rm";   //修改头像√
    public static String nickName_change_sub = baseIp + "/nickName_change_sub.rm";           //修改昵称√
    public static String sex_change_sub = baseIp + "/sex_change_sub.rm";                     //修改性别√
    public static String certification_info = baseIp + "/certification_info.rm";             //实名信息√
    public static String certification_sub = baseIp + "/certification_sub.rm";               //实名认证√
    public static String my_order_list = baseIp + "/my_order_list.rm";                       //我的发布√
    public static String grabsingle_order_list = baseIp + "/grabsingle_order_list.rm";       //我的抢单√
    public static String goodsorde_dealis = baseIp + "/goodsorde_dealis.rm";                 //订单详情√
    public static String my_account_info = baseIp + "/my_account_info.rm";                   //我的账户√
    public static String my_commonaddress_list = baseIp + "/my_commonaddress_list.rm";       //地址列表√
    public static String add_commonaddress = baseIp + "/add_commonaddress.rm";               //新增地址√
    public static String delete_commonaddress = baseIp + "/delete_commonaddress.rm";         //删除地址√
    public static String applyodd_data = baseIp + "/applyodd_data.rm";                       //抢单状态√
    public static String add_applyodd = baseIp + "/add_applyodd.rm";                         //申请抢单√
    public static String recharge_balance_request = baseIp + "/recharge_balance_request.rm"; //余额充值√
    public static String check_user_bail = baseIp + "/check_user_bail.rm";                   //保证金判断√
    public static String un_blocked = baseIp + "/un_blocked.rm";                             //保证金解冻√
    public static String recharge_request = baseIp + "/recharge_request.rm";                 //保证金充值√
    public static String recharge_balance = baseIp + "/recharge_balance.rm";                 //保证金余额充值√
    public static String trade_list_data = baseIp + "/trade_list_data.rm";                   //交易列表√
    public static String user_withdraw = baseIp + "/user_withdraw.rm";                       //提现√
    public static String hinvoice_data = baseIp + "/hinvoice_data.rm";                       //发票信息√
    public static String add_invoice = baseIp + "/add_invoice.rm";                           //开发票√

    public static String frist_index_data = baseIp + "/frist_index_data.rm";                   //首页√
    public static String activity_list = baseIp + "/activity_list.rm";                         //首页活动√
    public static String weightprice_list_data = baseIp + "/weightprice_address_list_data.rm"; //商品重量√
    public static String order_commission = baseIp + "/order_commission.rm";                   //获取佣金√
    public static String add_goodsorde = baseIp + "/add_goodsorde.rm";                         //新增订单√
    public static String user_voucer_list = baseIp + "/user_voucer_list.rm.rm";                //优惠券√
    public static String balance_pay_order = baseIp + "/balance_pay_order.rm";                 //余额支付√
    public static String order_pay = baseIp + "/order_pay.rm";                                 //支付√
    public static String add_order_tip_balance = baseIp + "/add_order_tip_balance.rm";         //余额支付小费√
    public static String pay_renew = baseIp + "/pay_renew.rm";                                 //支付小费√
    public static String order_list_data = baseIp + "/order_list_data.rm";                     //订单列表√
    public static String grab_order = baseIp + "/grab_order.rm";                               //抢单√
    public static String complete_order = baseIp + "/complete_order.rm";                       //完成订单√
    public static String evaluate_order = baseIp + "/evaluate_order.rm";                       //评价订单√
    public static String cancel_order = baseIp + "/cancel_order.rm";                           //客户取消√
    public static String customer_cancel_order = baseIp + "/customer_cancel_order.rm";         //客户同意√
    public static String rider_cancel_order = baseIp + "/rider_cancel_order.rm";               //骑手取消√
    public static String rider_deal_cancel_order = baseIp + "/rider_deal_cancel_order.rm";     //骑手同意√

    public static String index_data = baseIp + "/index_data.rm";                               //互助首页√
    public static String cooperation_list = baseIp + "/cooperation_list.rm";                   //互助列表√
    public static String add_cooperation = baseIp + "/add_cooperation.rm";                     //添加互助√
    public static String cooperation_detail = baseIp + "/cooperation_detail.rm";               //互助详情√
    public static String cooperation_collect = baseIp + "/cooperation_collect.rm";             //收藏互助√
    public static String delete_collect_cooperatio = baseIp + "/delete_collect_cooperatio.rm"; //取消互助√
    public static String add_cooperation_collect = baseIp + "/add_cooperation_collect.rm";     //添加评论√
    public static String comment_list = baseIp + "/comment_list.rm";                           //评论列表√
    public static String mycooperation_list = baseIp + "/mycooperation_list.rm";               //我的发布√
    public static String cooperation_collect_list = baseIp + "/cooperation_collect_list.rm";   //我的收藏√
    public static String delete_cooperation = baseIp + "/delete_cooperation.rm";               //删除互助√

    public static String city_name_data = baseIp + "/city_name_data.rm";       //获取省市区√
    public static String area_street2 = baseIp + "/area_street2.rm";           //获取乡镇√
    public static String area_street3 = baseIp + "/area_street3.rm";           //获取区√
    public static String leave_message_sub = baseIp + "/leave_message_sub.rm"; //意见反馈√
    public static String msg_list_data = baseIp + "/msg_list_data.rm";         //消息列表√
    public static String common_problem = baseIp + "/common_problem.rm";       //常见问题√
    public static String help_center = baseIp + "/help_center.rm";             //帮助中心√
    public static String newsnotice_Info = baseIp + "/newsnotice_Info.rm";     //公告详情√
    public static String get_versioninfo = baseIp + "/get_versioninfo.rm";     //版本信息√
    public static String commonphone_list = baseIp + "/commonphone_list.rm";   //常用电话√
    public static String new_user_course = baseIp + "/new_user_course.rm";     //常用电话√

    public static String invite_index = baseImg + "/forend/invite_index.hm";   //分享√
    public static String version = "https://www.pgyer.com/apiv2/app/view";     //版本更新(蒲公英)√
}
