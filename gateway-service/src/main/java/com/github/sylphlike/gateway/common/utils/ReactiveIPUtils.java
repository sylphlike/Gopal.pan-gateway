package com.github.sylphlike.gateway.common.utils;

import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.URL;

/**
 * <p>  time 28/09/2020 09:31  星期一 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 *
 * @author Gopal.pan
 * @version 1.0.0
 */
public class ReactiveIPUtils {
    private  static final Logger LOGGER = LoggerFactory.getLogger(ReactiveIPUtils.class);

    static DbSearcher searcher;
    static {
        try {
            DbConfig config = new DbConfig();
            URL resource = ReactiveIPUtils.class.getClassLoader().getResource("ip2region.db");
            if(resource == null){
                throw new IllegalArgumentException("ip2region.db file not find");
            }
            String dbPath = resource.getPath();
            LOGGER.info("【DbSearcher】 resourcePath 值为[{}]",dbPath);

            searcher = new DbSearcher(config, dbPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据IP地址获取地理位置
     * <p>  time 9:20 2021/1/6 (HH:mm yyyy/MM/dd)
     * <p> email 15923508369@163.com
     * @param ipAddress IP地址
     * @return  java.lang.String
     * @author  Gopal.pan
     */
    public static String ipRegion(String ipAddress){
        try {
            DataBlock btreeSearch = searcher.btreeSearch(ipAddress);
            return btreeSearch.getRegion();
        } catch (Exception e) {
            LOGGER.error("【DbSearcher】 解析IP地址异常",e);
        }
        return null;
    }


    /**
     * 获取用户真实IP地址
     * <p> 不使用request.getRemoteAddr()的原因是有可能用户使用了代理软件方式避免真实IP地址, 如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值
     * <p>  time 9:20 2021/1/6 (HH:mm yyyy/MM/dd)
     * <p> email 15923508369@163.com
     * @param request
     * @return  java.lang.String
     * @author  Gopal.pan
     */
    public static String getIpAddress(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.contains(",")) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress().getAddress().getHostAddress();
        }
        return ip;
    }
}
