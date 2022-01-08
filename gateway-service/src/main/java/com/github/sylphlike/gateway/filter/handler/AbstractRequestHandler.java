package com.github.sylphlike.gateway.filter.handler;


import com.github.sylphlike.framework.adapt.Constants;
import com.github.sylphlike.framework.norm.Response;
import com.github.sylphlike.gateway.common.domain.PassCheckVO;
import com.github.sylphlike.gateway.common.enums.ApproveType;
import com.github.sylphlike.gateway.common.enums.GReply;
import com.github.sylphlike.gateway.common.enums.ParamsType;
import com.github.sylphlike.gateway.common.utils.ParamUtils;
import com.github.sylphlike.gateway.common.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 请求类型映射亲情媒体类型抽象类
 * 实例类根据重新requestContextType，将本类注入到 RequestSolverChooser 类中, 调用方根据实例化子类时注入到 RequestSolverChooser类中的key值找到具体的实现内，实现调用
 * <p>  time 28/09/2020 11:38  星期一 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 *
 * @author Gopal.pan
 * @version 1.0.0
 */
public abstract class AbstractRequestHandler {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());


    /**
     * 实现类重写该方法实现
     * <p> return HttpMethod.GET.toString() + "-" + MediaType.TEXT_PLAIN_VALUE;
     * <p>  time 9:23 2021/1/6 (HH:mm yyyy/MM/dd)
     * <p> email 15923508369@163.com
     * @return  java.lang.String
     * @author  Gopal.pan
     */
    public abstract String requestContentType();




    /**
     * 业务处理
     * <p>  time 9:23 2021/1/6 (HH:mm yyyy/MM/dd)
     * <p> email 15923508369@163.com
     * @param exchange  exchange
     * @param chain     chain
     * @param path      请求地址路径
     * @return  reactor.core.publisher.Mono<java.lang.Void>
     * @author  Gopal.pan
     */
    public abstract Mono<Void> distributionExecution(ServerWebExchange exchange, GatewayFilterChain chain, String path);





    /**
     * 非GET、multipart 类型请求通用请求逻辑
     * <p> 使用body携带参数时，当参数长度为0时 DataBufferUtils会中断请求，请求不会转发到业务系统  No sendHeaders() called before complete, sending zero-length header
     * <p>  time 9:24 2021/1/6 (HH:mm yyyy/MM/dd)
     * <p> email 15923508369@163.com
     * @param exchange              exchange
     * @param chain                 chain
     * @param path                  资源地址路径（/应用上下文地址/权限类型/业务模块/业务功能）
     * @param paramsType            原始参数类型
     * @param logPrefix             日志前缀
     * @return  reactor.core.publisher.Mono<java.lang.Void>
     * @author  Gopal.pan
     */
    Mono<Void> universal(ServerWebExchange exchange, GatewayFilterChain chain, String path, ParamsType paramsType, String logPrefix){
        ApproveType approveType = PermissionHandler.requestType(path);
        String token = exchange.getRequest().getHeaders().getFirst(Constants.TOKEN);
        if(ParamUtils.checkContentLength(exchange) <= Constants.DIGITAL_ZERO){
            LOGGER.info("【unite-gateway】{},携带参数长度为0",logPrefix);
            if(approveType.equals(ApproveType.ENCRYPT)){
                LOGGER.info("【unite-gateway】{},携带参数长度为0,认证授权类型为加密授权,必须携带参数",logPrefix);
                return ResponseUtils.writeMessage(exchange.getResponse(), GReply.GATEWAY_NO_PARAM);
            }
            Response<PassCheckVO> response = PermissionHandler.implement(approveType, path, null, null, token);
            if(response.isFail()){
                return ResponseUtils.writeMessage(exchange.getResponse(), response);
            }
            LOGGER.info("【unite-gateway】{},携带参数长度为0,认证授权通过,请求业务系统",logPrefix);
            return chain.filter(ParamUtils.overloadHeader(exchange,response.getData().getIdentity()));
        }
        return paramProcess(exchange,chain,approveType,path,token,paramsType,logPrefix);

    }


    /**
     * <p>  time 16:40 2020/11/16 【HH:mm yyyy/MM/dd】  </p>
     * <p> email 15923508369@163.com </p>
     * @param exchange              exchange
     * @param chain                 chain
     * @param approveType           认证类型
     * @param path                  资源地址路径（/应用上下文地址/权限类型/业务模块/业务功能）
     * @param token                 令牌
     * @param paramsType            原始参数类型
     * @param logPrefix             日志前缀
     * @return   reactor.core.publisher.Mono<java.lang.Void>
     * @author   Gopal.pan
     */
    Mono<Void> paramProcess(ServerWebExchange exchange, GatewayFilterChain chain, ApproveType approveType, String path, String token,
                            ParamsType paramsType, String logPrefix){
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    byte[] originParamsByte = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(originParamsByte);
                    DataBufferUtils.release(dataBuffer);

                    String originParam = new String(originParamsByte, StandardCharsets.UTF_8);
                    LOGGER.info("【unite-gateway】{},原始参数[{}]",logPrefix,originParam);
                    Response<PassCheckVO> response = PermissionHandler.implement(approveType, path, originParam, paramsType, token);
                    if(response.isFail()){
                        return ResponseUtils.writeMessage(exchange.getResponse(), response);
                    }
                    PassCheckVO passCheckVO = response.getData();
                    String identity = passCheckVO.getIdentity();
                    String params = passCheckVO.getParams();

                    if(approveType.equals(ApproveType.ENCRYPT))
                        LOGGER.info("【unite-gateway】{},转发业务系统参数[{}]",logPrefix,params);

                    return ParamUtils.resetRequest(exchange, chain,identity, params.getBytes(StandardCharsets.UTF_8));
                });
    }

}
