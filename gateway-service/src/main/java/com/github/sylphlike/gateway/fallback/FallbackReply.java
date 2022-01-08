package com.github.sylphlike.gateway.fallback;

import com.github.sylphlike.framework.adapt.Constants;
import com.github.sylphlike.framework.norm.Response;
import com.github.sylphlike.gateway.common.enums.GReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

/**
 * <p>  time 20/11/2020 14:13  星期五 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */

@RestController
public class FallbackReply {

    private static final Logger LOGGER = LoggerFactory.getLogger(FallbackReply.class);

    /**
     * 回退统一返回前端参数
     * <p>  time 11:11 2020/11/21 【HH:mm yyyy/MM/dd】  </p>
     * <p> email 15923508369@163.com </p>
     * @param exchange exchange
     * @return   com.horse.framework.norm.Response<java.lang.Void>
     * @author   Gopal.pan
     */
    @RequestMapping("/fallback")
    public Response<Void> fallback(ServerWebExchange exchange) {
        MDC.put(Constants.TRACE, exchange.getRequest().getHeaders().getFirst(Constants.TRACE));
        LOGGER.error("【unite-gateway】服务响应失败");
        return new Response<>(GReply.GATEWAY_REPLAY_TIMEOUT);
    }
}
