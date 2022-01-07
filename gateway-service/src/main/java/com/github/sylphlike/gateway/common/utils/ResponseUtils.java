package com.github.sylphlike.gateway.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.sylphlike.framework.adapt.JsonConfig;
import com.github.sylphlike.framework.norm.RCode;
import com.github.sylphlike.framework.norm.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

/**
 * <p>  time 29/09/2020 15:58  星期二 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 *
 * @author Gopal.pan
 * @version 1.0.0
 */
public class ResponseUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseUtils.class);


    public static Mono<Void> writeMessage(ServerHttpResponse serverHttpResponse , RCode gReply){
        HttpHeaders httpHeaders = serverHttpResponse.getHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
        httpHeaders.add(HttpHeaders.CACHE_CONTROL,"no-store, no-cache, must-revalidate, max-age=0");

        String message = null;
        try {
            message = JsonConfig.mapper().writeValueAsString(Response.error(gReply));
        } catch (JsonProcessingException e) {
            LOGGER.error("【unite-gateway】错误响应转换异常",e);
        }

        LOGGER.info("【unite-gateway】网关拦截返回[{}]",message);
        DataBuffer bodyDataBuffer = serverHttpResponse.bufferFactory().wrap(message.getBytes());
        return serverHttpResponse.writeWith(Mono.just(bodyDataBuffer));
    }


    public static Mono<Void> writeMessage(ServerHttpResponse serverHttpResponse , Response<?> response){
        HttpHeaders httpHeaders = serverHttpResponse.getHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
        httpHeaders.add(HttpHeaders.CACHE_CONTROL,"no-store, no-cache, must-revalidate, max-age=0");

        String message = null;
        try {
            message = JsonConfig.mapper().writeValueAsString(response);
        } catch (JsonProcessingException e) {
            LOGGER.error("【unite-gateway】错误响应转换异常",e);
        }

        LOGGER.info("【unite-gateway】网关拦截返回[{}]",message);
        DataBuffer bodyDataBuffer = serverHttpResponse.bufferFactory().wrap(message.getBytes());
        return serverHttpResponse.writeWith(Mono.just(bodyDataBuffer));
    }
}
