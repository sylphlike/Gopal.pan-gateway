package com.github.sylphlike.gateway.filter.handler;

import com.github.sylphlike.framework.adapt.Constants;
import com.github.sylphlike.framework.norm.Response;
import com.github.sylphlike.framework.norm.util.CharsetUtil;
import com.github.sylphlike.gateway.common.domain.PassCheckVO;
import com.github.sylphlike.gateway.common.enums.ApproveType;
import com.github.sylphlike.gateway.common.enums.GReply;
import com.github.sylphlike.gateway.common.enums.ParamsType;
import com.github.sylphlike.gateway.common.utils.ResponseUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * <p>  time 28/09/2020 15:27  星期一 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */
@Component
public class GetFormRequestHandler extends AbstractRequestHandler {


    @Override
    public String requestContentType() {
        return HttpMethod.GET.toString() + CharsetUtil.CHAR_ENGLISH_DASHED + MediaType.APPLICATION_FORM_URLENCODED_VALUE;
    }

    @Override
    public Mono<Void> distributionExecution(ServerWebExchange exchange, GatewayFilterChain chain, String path) {

        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        URI uri = exchange.getRequest().getURI();
        String originParams = uri.getQuery();
        LOGGER.info("【unite-gateway】GET form请求,原始请求参数[{}]",originParams);

        ApproveType approveType = PermissionHandler.requestType(path);
        if(approveType.equals(ApproveType.ENCRYPT)){
            /*GET请求后需要重新替换参数值，均不能处理特殊符号
              方式1：
                UriComponentsBuilder.fromUri(uri).replaceQuery(UrlUtils.encode(params)).build(true).toUri()
              方式2：
                UriComponentsBuilder.fromUri(uri).replaceQuery("").build().toString();
                String join = StringUtils.join(baseUri, CharsetUtil.STRING_DOUBT, param);
                uri =  UriComponentsBuilder.fromUriString(join).build().toUri(); */
            return ResponseUtils.writeMessage(serverHttpResponse, GReply.GATEWAY_UNSUPPORTED_ACCESS_TYPE);
        }

        //根据接口地址前缀分发不同的权限校验
        Response<PassCheckVO> response = PermissionHandler.implement(approveType, path, null, ParamsType.FORM_DATA,
                exchange.getRequest().getHeaders().getFirst(Constants.TOKEN));
        if(response.isFail()){
            return ResponseUtils.writeMessage(serverHttpResponse, response);
        }

        //重载请求
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .uri(uri)
                .header(Constants.USER_CONTEXT, response.getData().getIdentity())
                .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

}
