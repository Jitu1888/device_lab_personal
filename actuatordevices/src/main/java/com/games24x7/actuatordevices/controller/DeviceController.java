package com.games24x7.actuatordevices.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.games24x7.actuatordevices.model.AdbRequest;
import com.games24x7.actuatordevices.model.Device;
import com.games24x7.actuatordevices.repository.DeviceRepository;
import com.games24x7.actuatordevices.utils.ADBUtilities;

import lombok.extern.slf4j.Slf4j;

/**
 * @author jitu-patel
 *
 */
@Slf4j
@RestController
@CrossOrigin(origins = "http://devicelab.games24x7.com")
public class DeviceController {

	@Autowired
	DeviceRepository deviceRepository;

	@PostMapping("/addDevice")
	private String addDevice(@RequestBody Device device) {
		deviceRepository.save(device);
		return "";
	}

	@GetMapping("/findAllDevices")
	private List<Device> getDevices() {
		log.info("" + new Date() + ": ThreadId " + Thread.currentThread().getId());		
		return deviceRepository.findAll();
	}

	@GetMapping("/findDevice")
	private Optional<Device> getDevice(@PathVariable final String udid) {
		return deviceRepository.findById(udid);
	}

	

	@GetMapping("/findDeviceResoution")
	private Optional<Device> findDeviceResoution(@PathVariable final String udid) {
		return deviceRepository.findById(udid);
	}
	
	@GetMapping("/deleteDevice")
	private void deleteDevice(@PathVariable final String udid) {
		deviceRepository.deleteById(udid);
		
	}
	
}
