package com.games24x7.actuatordevices.services;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.games24x7.actuatordevices.model.Device;
import com.games24x7.actuatordevices.utils.ADBUtilities;
import com.games24x7.actuatordevices.utils.CommandLineExecutor;
import com.games24x7.actuatordevices.utils.STFServiceBuilder;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class MaintenanceService {

	@Autowired
	private DeviceService deviceService;

//	@Scheduled(fixedDelay = 10000)
//	public void releasePhone() {
//		RestTemplate restTemplate = new RestTemplate();
//		log.info("Starting Device cycle");
//		List<Device> devices = deviceService.getDevices();
//		devices.forEach((e)->{
//			Optional<String> session = Optional.of(e.getAppiumSession().getSession());
//			if(session.isPresent() && !session.get().equals("NOT-SET")){
//				String baseUrl = String.format("%s/wd/hub/session/%s",e.getAppiumSession().getAppiumUrl()
//						,e.getAppiumSession().getSession());
//				log.info(baseUrl);
//				ResponseEntity<String> response;
//				try {
//					response = restTemplate.getForEntity(baseUrl, String.class);
//					if (response.getStatusCode().equals(HttpStatus.OK)) {
//						e.setIsFree("busy");
//						deviceService.updateDevice(e);
//
//					}
//				} catch (RestClientException e1) {
//					e1.printStackTrace();
//					e.setIsFree("Available");
//					e.setUser("NOT-SET");
//					e.setUniqueNumber("NOT-SET");
//					e.getAppiumSession().setSession("NOT-SET");
//					deviceService.updateDevice(e);
//				}
//			}else {
//				e.setUser("NOT-SET");
//				deviceService.updateDevice(e);
//				
//			}
//		});
//	}

	@Scheduled(cron = "${cron.check_stf_service}")
	public void checkingSTFService() {
		if (!STFServiceBuilder.builder().isSTFRunning()) {
			STFServiceBuilder.builder().restart();
		}
	}
	
	
	@Scheduled(cron = "${cron.restart.device}")
	public void restartDevice() {
		log.info("********************** restarting devices **********************");
//		List<Device> devices = deviceService.getDevices();
//		
//		for(Device device : devices) {
//			if(device.getIsFree().equals("Available")) {
//				ADBUtilities.runAndroidDeviceCommand(device.getUdid(), "reboot");
//			}
//			
//		}
//		log.info("********************** restarting devices finished **********************");
//		try {
//			Thread.sleep(300000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		STFServiceBuilder.builder().restart();
	}
	
	@Scheduled(cron = "${cron.restart.device}")
	public void restartSTF() {
		log.info("********************** restarting devices **********************");
		STFServiceBuilder.builder().restart();
	}
	
	
	@Scheduled(fixedDelay = 7200000)
	public void restartLogs() {
		log.info("********************** cleaning appium **********************");
		File f = new File(System.getProperty("user.dir")+"/Logs");
		removeDirectory(f);
	}

	public static void removeDirectory(File dir) {
        long purgeTime = System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000);

        if(dir.isDirectory() == false && dir.lastModified() < purgeTime) dir.delete();
        else if (dir.isDirectory() == true && dir.lastModified() < purgeTime) recursiveDelete(dir);
        else if(dir.isDirectory()){
             File[] files = dir.listFiles();
             for (File aFile : files)
                 removeDirectory(aFile);
        }
        
	}
	
	  public static void recursiveDelete(File file) {
          if (!file.exists())
              return;

          if (file.isDirectory()) {
              for (File f : file.listFiles()) {
                  recursiveDelete(f);
              }
          }
          file.delete();
      }
	
}
