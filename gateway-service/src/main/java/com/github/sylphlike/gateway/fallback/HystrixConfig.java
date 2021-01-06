package com.github.sylphlike.gateway.fallback;

import java.net.URI;
import java.util.Map;

/**
 * 全局hystrix配置
 * <p>  time 20/11/2020 13:53  星期五 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */
public class HystrixConfig {

    private static final String FORWARD_KEY = "forward";

    private URI fallbackUri;

    private Map<String, Integer> timeout;

    public URI getFallbackUri() {
        return fallbackUri;
    }

    public HystrixConfig setFallbackUri(URI fallbackUri) {
        if (fallbackUri != null && ! FORWARD_KEY.equals(fallbackUri.getScheme())) {
            throw new IllegalArgumentException("Hystrix Filter currently only supports 'forward' URIs, found " + fallbackUri);
        }
        this.fallbackUri = fallbackUri;
        return this;
    }

    public Map<String, Integer> getTimeout() {
        return timeout;
    }

    public HystrixConfig setTimeout(Map<String, Integer> timeout) {
        this.timeout = timeout;
        return this;
    }
}
