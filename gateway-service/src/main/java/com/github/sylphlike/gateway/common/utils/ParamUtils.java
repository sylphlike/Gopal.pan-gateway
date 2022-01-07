package com.github.sylphlike.gateway.common.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.sylphlike.framework.adapt.Constants;
import com.github.sylphlike.framework.adapt.JsonConfig;
import com.github.sylphlike.framework.norm.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>  time 28/09/2020 15:29  星期一 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 *
 * @author Gopal.pan
 * @version 1.0.0
 */
public class ParamUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParamUtils.class);

    /**
     *  获取原始请求参数长度
     * <p>  time 14:55 2020/10/7 【HH:mm yyyy/MM/dd】  </p>
     * <p> email 15923508369@163.com </p>
     * @param serverWebExchange  交换
     * @return long
     * @author Gopal.pan
     */
    public static long checkContentLength(ServerWebExchange serverWebExchange) {
        return serverWebExchange.getRequest().getHeaders().getContentLength();
    }



    /**
     * 请求参数转json字符串
     * <p> GET text/plain, POST  application/x-www-form-urlencoded 请求类型实用
     * <p>  time 9:17 2021/1/6 (HH:mm yyyy/MM/dd)
     * <p> email 15923508369@163.com
     * @param originParams  原始参数
     * @return  java.lang.String
     * @author  Gopal.pan
     */
    public static String paramFormatJson(String originParams) {
        if(StringUtils.isEmpty(originParams)){
            return CharsetUtil.CHAR_ENGLISH_EMPTY;
        }
        String[] params = originParams.split(CharsetUtil.STRING_AND);

        ObjectNode objectNode = JsonConfig.mapper().createObjectNode();
            for (String s : params) {
                String[] param = s.split(CharsetUtil.STRING_EQUAL);
                if (param.length >= 2) {
                    String paramName = param[0];
                    String[] split = param[1].split(CharsetUtil.STRING_ENGLISH_COMMA);
                    if (split.length >= 2) {
                        List<String> valList = new ArrayList<>(8);
                        Collections.addAll(valList, split);
                        objectNode.putPOJO(paramName, valList);
                    } else {
                        objectNode.put(paramName, split[0]);
                    }
                }else {
                    objectNode.put(param[0],CharsetUtil.CHAR_ENGLISH_EMPTY);
                }
            }

        return objectNode.toString();
    }


    /**
     * GET请求方式参数去空格
     * <p>  time 9:17 2021/1/6 (HH:mm yyyy/MM/dd)
     * <p> email 15923508369@163.com
     * @param originParams   解密后的请求参数
     * @return  java.lang.String
     * @author  Gopal.pan
     */
    public static String paramTrim(String originParams) {

        if(StringUtils.isEmpty(originParams)){
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        String[] params = originParams.split(CharsetUtil.STRING_AND);

        for (String s : params) {
            String[] param = s.split(CharsetUtil.STRING_EQUAL);
            if (param.length >= 2) {
                String paramName = param[0];
                String[] split = param[1].split(CharsetUtil.STRING_ENGLISH_COMMA);
                if (split.length >= 2) {
                    List<String> valList = new ArrayList<>(8);
                    for (String s1 : split) {
                        valList.add(s1.trim());
                    }
                    stringBuilder.append(paramName).append(CharsetUtil.STRING_EQUAL).append(valList).append(CharsetUtil.STRING_AND);
                } else {
                    stringBuilder.append(paramName).append(CharsetUtil.STRING_EQUAL).append(split[0].trim()).append(CharsetUtil.STRING_AND);
                }
            }else {
                stringBuilder.append(param[0]).append(CharsetUtil.STRING_EQUAL).append(CharsetUtil.STRING_AND);
            }
        }
        return stringBuilder.deleteCharAt(stringBuilder.length()-1).toString();
    }








    /**
     * 重写请求头和请求数据调用业务系统
     * <p>  time 9:18 2021/1/6 (HH:mm yyyy/MM/dd)
     * <p> email 15923508369@163.com
     * @param exchange            交换
     * @param chain               链
     * @param identity            身份标识
     * @param businessParameters  业务参数
     * @return  reactor.core.publisher.Mono<java.lang.Void>
     * @author  Gopal.pan
     */
    public static Mono<Void> resetRequest(ServerWebExchange exchange, GatewayFilterChain chain, String identity, byte[] businessParameters) {

        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            //重写请求数据
            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.defer(() -> {
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(businessParameters);
                    return Mono.just(buffer);
                });
            }
            //重写请求头
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(super.getHeaders());
                httpHeaders.setContentLength(businessParameters.length);
                httpHeaders.add(Constants.USER_CONTEXT,identity);
                return httpHeaders;
            }
        };
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }


    /**
     * 请求头增加用户ID
     * <p>  time 9:19 2021/1/6 (HH:mm yyyy/MM/dd)
     * <p> email 15923508369@163.com
     * @param exchange 交换
     * @param identity 身份标识
     * @return  org.springframework.web.server.ServerWebExchange
     * @author  Gopal.pan
     */
    public static ServerWebExchange overloadHeader(ServerWebExchange exchange, String identity) {
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(Constants.USER_CONTEXT,identity)
                .build();
        return exchange.mutate().request(mutatedRequest).build();
    }


}