package com.github.sylphlike.gateway.common.enums;


import com.github.sylphlike.framework.norm.RCode;

/**
 * <p>  time 19/11/2020 16:26  星期四 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 *
 * @author Gopal.pan
 * @version 1.0.0
 */

public enum GReply implements RCode {

    // ------------ * 99900 - 99999 网关错误码区间 * ------------ //
    GATEWAY_NOT_SUPPORT_REQUEST                ("G00X999011","不支持的请求类型",""),
    GATEWAY_INVALID_ADDRESS                    ("G00X999012","请求路径不符合安全规范",""),
    GATEWAY_NO_PARAM                           ("G00X999013","请求体中未携带参数",""),
    GATEWAY_ANALYTIC_ERROR                     ("G00X999014","解析业务系统数据异常",""),

    GATEWAY_SECURITY_NO_SIGNATURE              ("G00X999021", "签名认证失败",""),
    GATEWAY_TOKEN_LOST                         ("G00X999022", "令牌丢失",""),
    GATEWAY_TOKEN_INVALID                      ("G00X999023", "令牌无效",""),
    GATEWAY_TOKEN_HAS_EXPIRED                  ("G00X999024", "令牌已过期",""),
    GATEWAY_TOKEN_AUTH_FAIL                    ("G00X999025", "令牌认证失败",""),

    GATEWAY_DECRYPTION_WRONG_FORMAT            ("G00X999026", "参数格式不标准",""),
    GATEWAY_DECRYPTION_FAILED                  ("G00X999027", "参数解密失败",""),
    GATEWAY_NO_ACCESS                          ("G00X999029", "无访问权限",""),
    GATEWAY_UNSUPPORTED_ACCESS_TYPE            ("G00X999030", "不支持的访问类型",""),

    GATEWAY_SERVICE_FAIL                       ("G00X999082","服务响应失败",""),
    GATEWAY_REPLAY_TIMEOUT                     ("G00X999083","服务响应超时",""),

    GATEWAY_SERVICE_UNAVAILABLE                ("G00X999094","无可用业务系统提供服务",""),
    GATEWAY_SERVICE_MALFUNCTION                ("G00X999095","网络故障",""),
    GATEWAY_EXECUTE_ERROR                      ("G00X999099","网关内部未知异常",""),
    ;

    private final String code;
    private final String msg;
    private final String subMsg;

    GReply(String code, String msg, String subMsg) {
        this.code = code;
        this.msg = msg;
        this.subMsg = subMsg;
    }


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public String getSubMsg() {
        return subMsg;
    }
}
