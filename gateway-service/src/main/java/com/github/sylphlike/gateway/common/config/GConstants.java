package com.github.sylphlike.gateway.common.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>  time 26/10/2020 11:25  星期一 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */
public interface GConstants {

    /** 无需验证认证URL地址,不在业务系统中定义，且不符合权限校验规范的地址 */
    List<String> NO_VERIFICATION_API_URL =  new ArrayList<>(Arrays.asList("/v2/api-docs","/v2/api-docs-ext"));

}
