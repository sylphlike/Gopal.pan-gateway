package com.github.sylphlike.gateway.common.utils;

import com.github.sylphlike.framework.norm.util.CharsetUtil;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

/**
 * <p>  time 03/11/2020 15:17  星期二 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 *
 * @author Gopal.pan
 * @version 1.0.0
 */
public class DeviceResolver {

    public static String resolveDevice(ServerWebExchange serverWebExchange) {
        //获取ua，用来判断是否为移动端访问
        HttpHeaders headers = serverWebExchange.getRequest().getHeaders();
        String userAgent =  headers.get("user-agent").get(0);

        UserAgent agent = UserAgent.parseUserAgentString(userAgent);
        OperatingSystem operatingSystem = agent.getOperatingSystem();
        Browser browser = agent.getBrowser();
        return StringUtils.join("设备类型[", operatingSystem.getDeviceType(), "];操作系统[", operatingSystem.getName(),
                "];浏览器[", browser.getName(), CharsetUtil.CHAR_ENGLISH_COLON, browser.getVersion(userAgent), "]");
    }
}
