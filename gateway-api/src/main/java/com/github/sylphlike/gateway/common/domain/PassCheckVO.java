package com.github.sylphlike.gateway.common.domain;

import lombok.*;

/**
 * 权限检查返回实体
 * <p>  time 14/11/2020 10:28  星期六 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassCheckVO {

    /** 用户身份标识 个人用户为用户ID， 企业用户为企业ID*/
    private String identity;

    /** 业务请求参数*/
    private String params;

}
