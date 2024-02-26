package com.example.utils;

/**
 * 一些常量字符串整合
 */
public final class Const {
    //JWT令牌
    public final static String JWT_BLACK_LIST = "jwt:blacklist:";
    public final static String JWT_FREQUENCY = "jwt:frequency:";
    //请求频率限制
    public final static String FLOW_LIMIT_COUNTER = "flow:counter:";
    public final static String FLOW_LIMIT_BLOCK = "flow:block:";
    //邮件验证码
    public final static String VERIFY_EMAIL_LIMIT = "verify:email:limit:";

    public final static String ATTR_CLIENT = "client";
    public final static String VERIFY_EMAIL_DATA = "verify:email:data:";
    public final static String LOGIN_EMAIL_DATA = "login:email:data:";
    public final static String MODIFY_EMAIL_DATA = "modify:email:data:";
    //过滤器优先级
    public final static int ORDER_FLOW_LIMIT = -101;
    public final static int ORDER_CORS = -102;
    //请求自定义属性
    public final static String ATTR_USER_ID = "userId";
    //消息队列
    public final static String MQ_MAIL = "mail";
    //用户角色

    public static final String ATTR_USER_ROLE = "user";
    public final static String ROLE_ADMIN = "admin";
    public final static String ROLE_NORMAL = "user";
}
