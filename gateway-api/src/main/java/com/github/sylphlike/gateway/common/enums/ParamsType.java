package com.github.sylphlike.gateway.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>  time 13/11/2020 09:08  星期五 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 *
 * @author Gopal.pan
 * @version 1.0.0
 */

@AllArgsConstructor
@Getter
public enum ParamsType {

    JSON("json","json格式"),
    FORM_DATA("form_data","form表单提交"),
    ;


    private final String code;
    private final String desc;
}
