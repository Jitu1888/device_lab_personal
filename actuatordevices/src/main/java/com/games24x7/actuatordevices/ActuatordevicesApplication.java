package com.games24x7.actuatordevices;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.MultipartConfigElement;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.unit.DataSize;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author jitu-patel
 *
 */
@SpringBootApplication
@EnableScheduling
public class ActuatordevicesApplication implements AsyncConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(ActuatordevicesApplication.class, args);
    }


    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofBytes(512000000L));
        factory.setMaxRequestSize(DataSize.ofBytes(512000000L));
        return factory.createMultipartConfig();
    }

    @Override
    public Executor getAsyncExecutor() {
        Executor executor = new ThreadPoolExecutor(10, 20, 20, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10),
                new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        // TODO Auto-generated method stub
        return new SimpleAsyncUncaughtExceptionHandler();
    }


    @Bean
    public CorsFilter corsFilter() {

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        //config.setAllowCredentials(true); // you USUALLY want this
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startAppiumAndCreateSymLinkToMp4Path() {
        String serverConfigPath = System.getProperty("user.dir") + "/src/main/resources/serverconfig.json";
        String startAppiumScriptPath = System.getProperty("user.dir") + "/src/main/resources/startAppium.sh";
        System.out.println("serverConfigPath = " + serverConfigPath);
        String startAppiumCommand = "/bin/sh " + startAppiumScriptPath + " " + serverConfigPath;
        System.out.println("startAppiumCommand " + startAppiumCommand);
        try {
            Process process = Runtime.getRuntime().exec(startAppiumCommand);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Process exited abnormally");
                System.exit(-1);
            } else {
                System.out.println("Appium start script triggered successfully, " +
                        "please refer /tmp/appium.log to get the running processID...");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
