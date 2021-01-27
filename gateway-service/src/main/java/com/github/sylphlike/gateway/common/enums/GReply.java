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
    GATEWAY_NOT_SUPPORT_REQUEST                (99911,"不支持的请求类型"),
    GATEWAY_INVALID_ADDRESS                    (99912,"请求路径不符合安全规范"),
    GATEWAY_NO_PARAM                           (99913,"请求体中未携带参数"),
    GATEWAY_ANALYTIC_ERROR                     (99914,"解析业务系统数据异常"),


    GATEWAY_SECURITY_NO_SIGNATURE              (99921, "签名认证失败"),

    GATEWAY_TOKEN_LOST                         (99922, "令牌丢失"),
    GATEWAY_TOKEN_INVALID                      (99923, "令牌无效"),
    GATEWAY_TOKEN_HAS_EXPIRED                  (99924, "令牌已过期"),
    GATEWAY_TOKEN_AUTH_FAIL                    (99925, "令牌认证失败"),

    GATEWAY_DECRYPTION_WRONG_FORMAT            (99926, "参数格式不标准"),
    GATEWAY_DECRYPTION_FAILED                  (99927, "参数解密失败"),
    GATEWAY_NO_ACCESS                          (99929, "无访问权限"),
    GATEWAY_UNSUPPORTED_ACCESS_TYPE            (99930, "不支持的访问类型"),

    GATEWAY_SERVICE_FAIL                       (99982,"服务响应失败"),
    GATEWAY_REPLAY_TIMEOUT                     (99983,"服务响应超时"),

    GATEWAY_SERVICE_UNAVAILABLE                (99994,"无可用业务系统提供服务"),
    GATEWAY_SERVICE_MALFUNCTION                (99995,"网络故障"),
    GATEWAY_EXECUTE_ERROR                      (99999,"网关内部未知异常"),
    ;

    private final int code;
    private final String message;

    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }

    GReply(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
