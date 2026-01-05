package com.games24x7.actuatordevices.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Capabilities;
import org.springframework.stereotype.Service;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public  class AppiumServiceProvider {

    
    public AppiumServiceProvider() {
    }
   
    public  AppiumDriverLocalService getService(String ip,String udid,Integer port,String logFile,Integer timeOut,String deviceRequestId) throws Exception {
    	 AppiumDriverLocalService service = null;
        if (service == null) {
            initAppiumService(ip,udid,port,timeOut,deviceRequestId);
        }
        if (service == null) {
            throw new Exception("Cannot start Appium Driver Local Service.");
        }
        return service;
    }

    public  synchronized String initAppiumService(String ip,String udid,int port,Integer timeOut,String deviceRequestId) {
    	AppiumDriverLocalService service = null;
        Map<String, String> env = new HashMap<>(System.getenv());
        env.put("PATH", "/usr/local/bin:" + env.get("PATH"));
        env.put("ANDROID_HOME", "/Users/jenkins/Library/Android/sdk");
        env.put("JAVA_HOME",System.getProperty("java.home"));
      
       
        
        service = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
                .usingPort(port)
                .withAppiumJS(new File("/usr/local/lib/node_modules/appium/build/lib/main.js"))
                .usingDriverExecutable(new File("/Users/jenkins/.nvm/versions/node/v14.5.0/bin/node"))
                .withArgument(GeneralServerFlag.SESSION_OVERRIDE)
                .withArgument(GeneralServerFlag.LOG_LEVEL, "debug")
                .withArgument(GeneralServerFlag.RELAXED_SECURITY)
                .withEnvironment(env)
                .withLogFile(new File(System.getProperty("user.dir")+"/appium/"+udid+"/"+deviceRequestId+".log"))
                .withStartUpTimeOut(timeOut, TimeUnit.SECONDS));

        log.info("New Appium service: " + service.getUrl());
        service.clearOutPutStreams();
        service.start();
        if(service.isRunning()) {
        	log.info("Service is running in port "+ port + "and "+ udid);
        }else {
        	log.info("Service is not running in port "+ port + "and "+ udid);
        }
        System.out.println(String.format("http://%s:8085/actuator-slave/appium/%s/%s.log", ip, udid, deviceRequestId));
      
        return String.format("http://%s:8085/actuator-slave/appium/%s/%s.log", ip, udid, deviceRequestId);
        
    }
    
    
 
}
