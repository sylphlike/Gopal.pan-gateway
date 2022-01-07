package com.github.sylphlike.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>  time 21:36 2022/01/07  星期五 </p>
 * <p> email 15923508369@163.com     </P>
 * @author Gopal.pan
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.github.sylphlike")
public class GatewayAdminApplication {
    public static void main(String[] args) {
        try {
            new SpringApplication(GatewayAdminApplication.class).run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
