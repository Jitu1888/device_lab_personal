package com.games24x7.actuatordevices.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString

@Document(collection = "Device")
public class Device {
	@Id
	private String udid;
	private String testCase;
	private String slaveIp;
	private DeviceInformation deviceInformation;
	private String isFree;
	private String user;
	private String appiumUrl;
	private String uniqueNumber;
	private AppiumSession appiumSession;
	private boolean available;
	private String stfUrl;
	private int testRetry;

}
