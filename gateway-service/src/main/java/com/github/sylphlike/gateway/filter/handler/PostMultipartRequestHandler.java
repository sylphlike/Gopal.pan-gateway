package com.github.sylphlike.gateway.filter.handler;

import com.github.sylphlike.framework.norm.util.CharsetUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * <p>  time 29/09/2020 13:49  星期二 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */

@Component
public class PostMultipartRequestHandler extends AbstractRequestHandler {
    @Override
    public String requestContentType() {
        return HttpMethod.POST.toString() + CharsetUtil.CHAR_ENGLISH_DASHED + MediaType.MULTIPART_FORM_DATA_VALUE;
    }

    @Override
    public Mono<Void> distributionExecution(ServerWebExchange exchange, GatewayFilterChain chain, String path) {
        LOGGER.info("【unite-gateway】POST 文件上传");
        return chain.filter(exchange);
    }
}
