package com.github.sylphlike.gateway.filter;

import com.github.sylphlike.framework.adapt.Constants;
import com.github.sylphlike.framework.adapt.cache.UUIDCache;
import com.github.sylphlike.gateway.common.enums.GReply;
import com.github.sylphlike.gateway.common.utils.DeviceResolver;
import com.github.sylphlike.gateway.common.utils.ReactiveIPUtils;
import com.github.sylphlike.gateway.common.utils.ResponseUtils;
import com.github.sylphlike.gateway.filter.handler.AbstractRequestHandler;
import com.github.sylphlike.gateway.filter.handler.RequestDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * <p>  time 14:02 2022/01/08  星期六 </p>
 * <p> email 15923508369@163.com     </P>
 * @author Gopal.pan
 * @version 1.0.0
 */
@Component
public class EntranceFilter implements GlobalFilter, Ordered {

    Logger logger = LoggerFactory.getLogger(EntranceFilter.class);

    private final RequestDispatcher dispatcher;
    public EntranceFilter(RequestDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String trace = UUIDCache.UUID();
        MDC.put(Constants.TRACE, trace );

        ServerHttpRequest serverHttpRequest = exchange.getRequest().mutate().header(Constants.TRACE, trace).build();
        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        ServerWebExchange serverWebExchange = exchange.mutate().request(serverHttpRequest).build();

        String realIP = ReactiveIPUtils.getIpAddress(serverHttpRequest);
        String path = serverHttpRequest.getURI().getPath();

        logger.info("【unite-gateway】客户端IP地址[{}]", realIP);
        logger.info("【unite-gateway】客户端地理位置[{}]", ReactiveIPUtils.ipRegion(realIP));
        logger.info("【unite-gateway】客户端信息[{}]", DeviceResolver.resolveDevice(serverWebExchange));
        logger.info("【unite-gateway】请求服务路径[{}]", path);
        logger.info("【unite-gateway】请求方法[{}]",serverHttpRequest.getMethodValue());

        //黑白IP,限流
        //接口可用状态


        AbstractRequestHandler requestHandler = dispatcher.choose(serverHttpRequest);
        if(null == requestHandler){
            return ResponseUtils.writeMessage(serverHttpResponse, GReply.GATEWAY_NOT_SUPPORT_REQUEST);
        }

        return requestHandler.distributionExecution(serverWebExchange,chain,path);
    }



}
