package com.github.sylphlike.gateway.common.enums;

/**
 * <p>  time 21/11/2020 10:50  星期六 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */
public enum FuseEnum {


    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE","503 SERVICE_UNAVAILABLE","未找到实例/内部异常"),
    HYSTRIX_TIMEOUT("HYSTRIX_TIMEOUT","","业务系统返回超时"),
    CONNECTION_REFUSED("CONNECTION_REFUSED","Connection refused","业务系统拒接连接/请求失败"),
    SHORT_CIRCUITED("SHORT_CIRCUITED","Hystrix circuit short-circuited and is OPEN","熔断开启拦截调用"),
    NOT_ACQUIRE_SEMAPHORE("NOT_ACQUIRE_SEMAPHORE","","线程池/队列/信号量溢出"),


    /**
     * 其它类型异常
     * 已知类型异常： Connection prematurely closed BEFORE response
     */
    UNKNOWN_ERROR("UNKNOWN_ERROR","","未知异常"),
    ;




    private final String code;
    private final String aberrantPrefix;
    private final String desc;


    FuseEnum(String code, String aberrantPrefix, String desc) {
        this.code = code;
        this.aberrantPrefix = aberrantPrefix;
        this.desc = desc;
    }


    public String getCode() {
        return code;
    }
    public String getAberrantPrefix() {
        return aberrantPrefix;
    }
    public String getDesc() {
        return desc;
    }

}
