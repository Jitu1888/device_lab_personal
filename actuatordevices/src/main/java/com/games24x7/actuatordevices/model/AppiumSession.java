package com.games24x7.actuatordevices.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter

public class AppiumSession {
	private String appiumUrl;
	private Integer port;
	private String appiumLogs;
	private long systemPort;
	private String session;
	private Integer chromePort;

}
