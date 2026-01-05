package com.games24x7.actuatordevices.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.games24x7.actuatordevices.model.Device;
import com.games24x7.actuatordevices.repository.DeviceRepository;

@Service
public class DeviceService {
	
	@Autowired
	private DeviceRepository deviceRepository;
	
	
     public Optional<Device> findById(String id) {
		return deviceRepository.findAll().stream().filter(e -> e.getUdid().equals(id)).findFirst();
	}
	
     
     public Optional<Device> findDeviceBySessionId(String sessionId) {
 		return deviceRepository.findAll().stream().filter(e -> e.getAppiumSession().getSession().equals(sessionId)).findFirst();
 	}

     public List getDevices() {
    	 return deviceRepository.findAll();
    	 
     }
     
     public void updateDevice(Device device) {
    	  deviceRepository.save(device);
    	 
     }
     
     
   
}
