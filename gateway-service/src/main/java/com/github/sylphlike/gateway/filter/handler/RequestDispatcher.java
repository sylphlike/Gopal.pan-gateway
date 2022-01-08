package com.github.sylphlike.gateway.filter.handler;

import com.github.sylphlike.framework.norm.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用sprig aware 根据规则实例化  AbstractHandler类 实现根据不同请求方式，不同请求媒体类型走不同的实现类
 * <p>  time 28/09/2020 15:16  星期一 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */
@Component
public class RequestDispatcher implements ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(RequestDispatcher.class);

    private ApplicationContext context;



    /**
     * AbstractHandler 子类集合
     *       key 为子类定义的， 规则为 HttpMethod ,MediaType
     *          {@link HttpMethod}
     *          {@link MediaType}
     *          当没有指定MediaType时，默认为 application/x-www-form-urlencoded
     * <p>  time 9:33 2021/1/6 (HH:mm yyyy/MM/dd)
     * <p> email 15923508369@163.com
     * @param null
     * @return  null
     * @author  Gopal.pan
     */
    private final Map<String, AbstractRequestHandler> chooseMap = new HashMap<>();



    public AbstractRequestHandler choose(ServerHttpRequest httpRequest) {
        HttpMethod method = httpRequest.getMethod();
        MediaType mediaType = httpRequest.getHeaders().getContentType();
        assert method != null;
        String key = "";
        if(null != mediaType){
            String type = mediaType.getType();
            String subType = mediaType.getSubtype();
            logger.info("【unite-gateway】指定的MediaType类型为[{},{}]",type,subType);

            key  = StringUtils.join( method.toString(), CharsetUtil.CHAR_ENGLISH_DASHED,type,CharsetUtil.CHAR_ENGLISH_SLASH,subType);

        }else {
            logger.info("【unite-gateway】没有指定MediaType，使用默认类型转发");
            key = StringUtils.join( method.toString(), CharsetUtil.CHAR_ENGLISH_DASHED,MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        }

        return chooseMap.get(key);
    }



    @PostConstruct
    public void register() {
        Map<String, AbstractRequestHandler> solverMap = context.getBeansOfType(AbstractRequestHandler.class);
        for (AbstractRequestHandler solver : solverMap.values()) {
            chooseMap.put(solver.requestContentType(),solver);
        }
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
