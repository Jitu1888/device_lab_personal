package com.games24x7.actuatordevices.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DeviceInformation {
	private String manufacture;
	private String model;
	private String deviceName;
	private String os;
	private String resolution;
	private boolean isAndroid;
	private boolean isRealDevice;
	
}
