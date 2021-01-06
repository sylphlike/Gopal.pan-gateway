package com.github.sylphlike.gateway.common.domain;

import lombok.Data;

/**
 * 认证类型为 加密授权 通用请求参数
 * <p>  time 13/11/2020 10:46  星期五 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */
@Data
public class EncryptApproveBO {

    /** 商户Id */
    private String merchantId;

    /** 接口版本号 */
    private String version;

    /** 加密后的业务参数 */
    private String data;

    /** 请求时间戳 */
    private String timestamp;
}
