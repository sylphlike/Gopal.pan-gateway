package com.github.sylphlike.gateway.filter.handler;

import com.github.sylphlike.framework.norm.util.CharsetUtil;
import com.github.sylphlike.gateway.common.enums.ParamsType;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * <p>  time 28/09/2020 16:29  星期一 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */
@Component
public class PostJsonRequestHandler extends AbstractRequestHandler {
    @Override
    public String requestContentType() {
        return HttpMethod.POST.toString() + CharsetUtil.CHAR_ENGLISH_DASHED + MediaType.APPLICATION_JSON_VALUE;
    }

    @Override
    public Mono<Void> distributionExecution(ServerWebExchange exchange, GatewayFilterChain chain, String path) {

        return universal(exchange, chain, path, ParamsType.JSON, "POST JSON 请求");




    }


}
