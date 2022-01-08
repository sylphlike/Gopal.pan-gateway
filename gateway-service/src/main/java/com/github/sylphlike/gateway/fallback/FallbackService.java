package com.github.sylphlike.gateway.fallback;

import com.github.sylphlike.framework.adapt.Constants;
import com.github.sylphlike.gateway.common.enums.FuseEnum;
import com.netflix.hystrix.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *  网关熔断处理逻辑
 * <p>  time 20/11/2020 13:53  星期五 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */

@Component
public class FallbackService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FallbackService.class);


    @Async
    public void statisticalRecord(ServerWebExchange exchange, ExecutionResult executionResult) {
        MDC.put(Constants.TRACE, exchange.getRequest().getHeaders().getFirst(Constants.TRACE));
        LOGGER.info("【unite-gateway】超时熔断异步处理,请求地址[{}]",exchange.getRequest().getURI().getPath());

        String fuseType = FuseEnum.UNKNOWN_ERROR.getCode();
        String schema = "";
        String details = "";
        Exception exception = executionResult.getException();
        if(exception != null){
            schema = exception.getMessage();
            if(schema.startsWith(FuseEnum.SERVICE_UNAVAILABLE.getAberrantPrefix())){
                fuseType = FuseEnum.SERVICE_UNAVAILABLE.getCode();
            }else if(schema.startsWith(FuseEnum.CONNECTION_REFUSED.getAberrantPrefix())){
                fuseType = FuseEnum.CONNECTION_REFUSED.getCode();
            }
            details = traceTransform(exception);
        }else {
            Exception executionException = executionResult.getExecutionException();
            if(executionException != null){
                Throwable cause = executionException.getCause();
                if(cause != null){
                    schema = cause.getMessage();
                    if(schema.startsWith(FuseEnum.SHORT_CIRCUITED.getAberrantPrefix())){
                        fuseType = FuseEnum.SHORT_CIRCUITED.getCode();
                    }else if(schema.startsWith(FuseEnum.NOT_ACQUIRE_SEMAPHORE.getAberrantPrefix())){
                        fuseType = FuseEnum.NOT_ACQUIRE_SEMAPHORE.getCode();
                    }

                }else{
                    schema = "HystrixTimeoutException";
                    fuseType = FuseEnum.HYSTRIX_TIMEOUT.getCode();
                }
                details = traceTransform(executionException);
            }
        }
        LOGGER.info("【unite-gateway】超时熔断异步处理,熔断类型[{}],摘要[{}],详细信息[{}]",fuseType,schema,details);


    }

    private static String traceTransform(Exception exception){

        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            return  sw.toString();
        } catch (Exception e) {
            LOGGER.error("【unite-gateway】超时熔断异步处理,堆栈信息转化异常",e);
        }
        return null;
    }
}
