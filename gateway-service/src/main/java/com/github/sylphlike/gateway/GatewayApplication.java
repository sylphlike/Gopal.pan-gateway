package com.github.sylphlike.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>  time 05/01/2021 17:16  ζζδΊ (dd/MM/YYYY HH:mm)
 * <p> email 15923508369@163.com
 *
 * @author Gopal.pan
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.github.sylphlike")
public class GatewayApplication {

    public static void main(String[] args) {
        try {
            new SpringApplication(GatewayApplication.class).run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
