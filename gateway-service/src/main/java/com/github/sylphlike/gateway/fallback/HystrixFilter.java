package com.github.sylphlike.gateway.fallback;


import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;
import rx.Subscription;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.containsEncodedParts;

/**
 * 熔断过滤器 默认5000ms 后熔断 可为单独的请求地址做定制配置
 * <p> default-filters:
 *         - name: GlobalHystrix
 *           args:
 *             name: fallbackcmd
 *             fallbackUri: forward:/fallback
 *             timeout:
 *               # 业务自定义熔断配置参数
 *               # 业务系统请求全路径(每一个层级使用"_"分割)
 *               # example
 *               # 业务系统的请求全路径为 /unite-demo/n/customize6  那么开始位的"/"舍弃 后面的"/"替换成"_" 最后为 unite-demo_n_customize6
 *               unite-demo_n_customize6: 9000
 * <p>  time 20/11/2020 13:49  星期五 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */

@Component
public class HystrixFilter  extends AbstractGatewayFilterFactory<HystrixConfig> {

    @Autowired  private FallbackService fallbackService;


    private static final String NAME = "GlobalHystrix";

    /*默认熔断时间*/
    private static final int TIMEOUT_MS = 4000;


    private final ObjectProvider<DispatcherHandler> dispatcherHandlerProvider;
    private volatile DispatcherHandler dispatcherHandler;


    public HystrixFilter(ObjectProvider<DispatcherHandler> dispatcherHandlerProvider) {
        super(HystrixConfig.class);
        this.dispatcherHandlerProvider = dispatcherHandlerProvider;
    }

    private DispatcherHandler getDispatcherHandler() {
        if (dispatcherHandler == null) {
            dispatcherHandler = dispatcherHandlerProvider.getIfAvailable();
        }

        return dispatcherHandler;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList(NAME_KEY);
    }


    @Override
    public GatewayFilter apply(HystrixConfig config) {
        processConfig(config);
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().pathWithinApplication().value();
            int timeout = TIMEOUT_MS;
            if(ObjectUtils.isNotEmpty(config.getTimeout())){
                timeout = config.getTimeout().getOrDefault(path, TIMEOUT_MS);
            }
            GlobalHystrixCommand command = new GlobalHystrixCommand(config.getFallbackUri(), exchange, chain, timeout, path);
            return Mono.create(s -> {
                Subscription sub = command.toObservable().subscribe(s::success, s::error, s::success);
                s.onCancel(sub::unsubscribe);
            }).onErrorResume((Function<Throwable, Mono<Void>>) throwable -> {
                if (throwable instanceof HystrixRuntimeException) {
                    HystrixRuntimeException e = (HystrixRuntimeException) throwable;
                    HystrixRuntimeException.FailureType failureType = e.getFailureType();
                    switch (failureType) {
                        case TIMEOUT:
                            return Mono.error(new TimeoutException());
                        case COMMAND_EXCEPTION: {
                            Throwable cause = e.getCause();
                            if (cause instanceof ResponseStatusException || AnnotatedElementUtils
                                    .findMergedAnnotation(cause.getClass(), ResponseStatus.class) != null) {
                                return Mono.error(cause);
                            }
                        }
                        default:
                            break;
                    }
                }
                return Mono.error(throwable);
            }).then();
        };
    }


    /**
     * YAML解析的时候MAP的KEY不支持'/'，这里只能用'-'替代
     *
     * @param config config
     */
    private void processConfig(HystrixConfig config) {

        if (null != config.getTimeout()) {
            Map<String, Integer> timeout = new HashMap<>(8);
            config.getTimeout().forEach((k, v) -> {
                String key = k.replace("_", "/");
                if (!key.startsWith("/")) {
                    //解析是需要带入网关的跟路径 追加/op-gateway
                    key = "/" + key;
                }
                timeout.put(key, v);
            });
            config.setTimeout(timeout);

        }
    }


    @Override
    public String name() {
        return NAME;
    }

    private class GlobalHystrixCommand extends HystrixObservableCommand<Void> {

        private final URI fallbackUri;
        private final ServerWebExchange exchange;
        private final GatewayFilterChain chain;

        public GlobalHystrixCommand(URI fallbackUri,
                                    ServerWebExchange exchange,
                                    GatewayFilterChain chain,
                                    int timeout,
                                    String key) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(key))
                    .andCommandKey(HystrixCommandKey.Factory.asKey(key))
                    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(timeout)));
            this.fallbackUri = fallbackUri;
            this.exchange = exchange;
            this.chain = chain;
        }

        @Override
        protected Observable<Void> construct() {
            return RxReactiveStreams.toObservable(this.chain.filter(exchange));
        }

        @Override
        protected Observable<Void> resumeWithFallback() {
            if (null == fallbackUri) {
                return super.resumeWithFallback();
            }
            URI uri = exchange.getRequest().getURI();
            boolean encoded = containsEncodedParts(uri);
            URI requestUrl = UriComponentsBuilder.fromUri(uri)
                    .host(null)
                    .port(null)
                    .uri(this.fallbackUri)
                    .build(encoded)
                    .toUri();
            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
            ServerHttpRequest request = this.exchange.getRequest().mutate().uri(requestUrl).build();
            ServerWebExchange mutated = exchange.mutate().request(request).build();

            fallbackService.statisticalRecord(exchange,super.executionResult);

            return RxReactiveStreams.toObservable(getDispatcherHandler().handle(mutated));
        }
    }

}
