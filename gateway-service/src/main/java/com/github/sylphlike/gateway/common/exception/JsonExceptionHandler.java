package com.github.sylphlike.gateway.common.exception;

import com.github.sylphlike.framework.norm.RCode;
import com.github.sylphlike.gateway.common.enums.GReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.all;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * <p>  time 07/10/2020 08:55  星期三 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 *
 * @author Gopal.pan
 * @version 1.0.0
 */
public class JsonExceptionHandler  extends DefaultErrorWebExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonExceptionHandler.class);

    public JsonExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties, ErrorProperties errorProperties,
                                ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }


    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error =  super.getError(request);
        LOGGER.error("【unite-gateway】异常处理类，原始异常信息[{}]",error.getMessage());
        Map<String,Object> errorAttributes = new HashMap<>(8);
        if(error instanceof ResponseStatusException){
            ResponseStatusException responseStatusException = (ResponseStatusException)error;
            int httpStatus = responseStatusException.getStatus().value();
            errorAttributes.put(RCode.HTTP_CODE,httpStatus);

            if(httpStatus == HttpStatus.NOT_FOUND.value() || httpStatus == HttpStatus.SERVICE_UNAVAILABLE.value()){
                errorAttributes.put(RCode.CODE, GReply.GATEWAY_SERVICE_UNAVAILABLE.getCode());
                errorAttributes.put(RCode.MSG, GReply.GATEWAY_SERVICE_UNAVAILABLE.getMsg());
                errorAttributes.put(RCode.SUB_MSG, GReply.GATEWAY_SERVICE_UNAVAILABLE.getSubMsg());
            }else {
                errorAttributes.put(RCode.CODE, GReply.GATEWAY_EXECUTE_ERROR.getCode());
                errorAttributes.put(RCode.MSG, GReply.GATEWAY_EXECUTE_ERROR.getMsg());
                errorAttributes.put(RCode.SUB_MSG, GReply.GATEWAY_EXECUTE_ERROR.getSubMsg());
            }
        }else if (error instanceof ConnectException){
            errorAttributes.put(RCode.HTTP_CODE,HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorAttributes.put(RCode.CODE, GReply.GATEWAY_SERVICE_MALFUNCTION.getCode());
            errorAttributes.put(RCode.MSG, GReply.GATEWAY_SERVICE_MALFUNCTION.getMsg());
            errorAttributes.put(RCode.SUB_MSG, GReply.GATEWAY_SERVICE_MALFUNCTION.getSubMsg());
        }else if (error instanceof GatewayException){
            GatewayException gatewayException = (GatewayException) error;
            errorAttributes.put(RCode.HTTP_CODE,HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorAttributes.put(RCode.CODE, gatewayException.getCode());
            errorAttributes.put(RCode.MSG, gatewayException.getMsg());
            errorAttributes.put(RCode.SUB_MSG, gatewayException.getSubMsg());
        } else{
            errorAttributes.put(RCode.HTTP_CODE,HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorAttributes.put(RCode.CODE, GReply.GATEWAY_EXECUTE_ERROR.getCode());
            errorAttributes.put(RCode.MSG, GReply.GATEWAY_EXECUTE_ERROR.getMsg());
            errorAttributes.put(RCode.SUB_MSG, GReply.GATEWAY_EXECUTE_ERROR.getSubMsg());
        }
        errorAttributes.put("data",null);
        errorAttributes.put("timestamp", LocalDateTime.now().toString());
        return errorAttributes;
    }


    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction( ErrorAttributes errorAttributes) {
        return route(acceptsTextHtml(), this::renderErrorView).andRoute(all(),this::renderErrorResponse);
    }



    /**
     * Get the HTTP error status information from the error map.
     * @param errorAttributes the current error information
     * @return the error HTTP status
     */
    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        LOGGER.info("【unite-gateway】获取HTTP状态码，转换后的错误信息为[{}]",errorAttributes);
        int statusCode = (int) errorAttributes.get("httpCode");
        if(statusCode == 0){
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        errorAttributes.remove("httpCode");
        return statusCode;
    }
}
