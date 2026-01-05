package com.games24x7.actuatordevices.utils;

import lombok.Data;

@Data
public class CommandLineResponse {
	
	
	private int exitCode;
	private String stdOut;
	private String errOut;

}
