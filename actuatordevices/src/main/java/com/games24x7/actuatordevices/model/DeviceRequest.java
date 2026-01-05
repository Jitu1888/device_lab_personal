package com.games24x7.actuatordevices.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeviceRequest {

	private boolean isAndroid;
	private String deviceID;
	private String user;
	private String deviceRequestId;
	
	
}
