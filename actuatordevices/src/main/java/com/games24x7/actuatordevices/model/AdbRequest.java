package com.games24x7.actuatordevices.model;

import lombok.Data;
import lombok.Getter;

import lombok.Setter;

@Setter
@Getter
@Data

public class AdbRequest {
	
	private String deviceId;
	private String command;

}
