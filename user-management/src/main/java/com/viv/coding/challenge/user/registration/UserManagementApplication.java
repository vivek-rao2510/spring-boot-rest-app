package com.viv.coding.challenge.user.registration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class acts as the entry point to the Spring boot application.
 * <p>
 * {@link SpringBootApplication} annotation is used to mark a configuration class that declares one or more @Bean methods and also triggers
 * auto-configuration and component scanning.
 * 
 * @Author Vivek Rao
 */
@SpringBootApplication
public class UserManagementApplication {

    public static void main(final String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
    }

}
