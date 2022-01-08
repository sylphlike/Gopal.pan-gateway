package com.github.sylphlike.gateway.filter;

import com.github.sylphlike.framework.adapt.Constants;
import com.github.sylphlike.framework.adapt.JsonConfig;
import com.github.sylphlike.framework.norm.Response;
import com.github.sylphlike.gateway.common.enums.GReply;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR;

/**
 * 包装业务系统返回
 * 处理请求业务系统返回404，返回结果为空串 的情况做结果处理，返回统一
 * 的数据格式,springGateway 对返回数据流使用了分段传输，当数据大小过大时 会出现返回数据不完整情况,使用fluxboy.buffer(),方式获取缓冲中所有的数据后再处理业务逻辑
 * <p>  time 14:04 2022/01/08  星期六 </p>
 * <p> email 15923508369@163.com     </P>
 * @author Gopal.pan
 * @version 1.0.0
 */
@Component
public class ExportFilter implements GlobalFilter, Ordered {

    private final Logger logger = LoggerFactory.getLogger(ExportFilter.class);
    private static final Joiner joiner = Joiner.on("");

    @Override
    public int getOrder() {
        return -2; // order需要小于-1
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @SneakyThrows
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                MDC.put(Constants.TRACE,  exchange.getRequest().getHeaders().getFirst(Constants.TRACE));
                if (body instanceof Flux) {
                    int statusCode = Objects.requireNonNull(getStatusCode()).value();
                    if (statusCode == HttpStatus.OK.value()) {

                        //当类型为 application/json 转换处理图片类型，文件类型不处理
                        String contentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
                        if (!StringUtils.isEmpty(contentType) && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {

                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                                List<String> list = Lists.newArrayList();
                                dataBuffers.forEach(dataBuffer -> {
                                    byte[] content = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(content);
                                    // 释放掉内存
                                    DataBufferUtils.release(dataBuffer);
                                    list.add(new String(content, StandardCharsets.UTF_8));
                                });

                                String responseData = joiner.join(list);
                                logger.info("【unite-gateway】业务系统响应,响应值[{}]", responseData);

                                byte[] responseByte = responseData.getBytes(StandardCharsets.UTF_8);
                                this.getDelegate().getHeaders().setContentLength(responseByte.length);
                                return bufferFactory.wrap(responseByte);

                            }));
                        }else {
                            logger.info("【unite-gateway】业务系统响应,业务系统返回数据Content-Type为[{}]",contentType);
                            return super.writeWith(body);
                        }
                    }else {
                        logger.info("【unite-gateway】业务系统响应，响应失败，返回状态码[{}]",statusCode);

                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                        Response<Void> response = new Response<>(GReply.GATEWAY_SERVICE_FAIL, "HTTP状态码["+statusCode+"]");
                        String value = JsonConfig.mapper().writeValueAsString(response);
                        logger.info("【unite-gateway】转换失败返回,响应值[{}]", value);
                        byte[] responseByte = value.getBytes(StandardCharsets.UTF_8);
                        return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                            this.getDelegate().getHeaders().setContentLength(responseByte.length);
                            return bufferFactory.wrap(responseByte);
                        }));
                    }


                }
                return super.writeWith(body);
            }

        };
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }


}
