package com.github.sylphlike.gateway.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证授权类型
 * <p>  time 11/11/2020 11:55  星期三 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */

@AllArgsConstructor
@Getter
public enum ApproveType {
    NONE    ("n","无需任何权限"),
    LOGIN   ("l","登录授权"),
    ROLE    ("r","角色资源权限"),
    ENCRYPT ("e","加密授权"),
    STATE   ("s","开放平台登录接口回调认证");


    private final String code;
    private final String desc;

    public static ApproveType getEnum(String code){
        ApproveType[] values = ApproveType.values();
        for (ApproveType value : values) {
            if(value.code.equals(code)){
                return value;
            }
        }
        return null;

    }

}
