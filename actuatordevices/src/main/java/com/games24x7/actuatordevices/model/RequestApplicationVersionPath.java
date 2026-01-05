package com.games24x7.actuatordevices.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Data
@ToString
public class RequestApplicationVersionPath {
	
    private String projectName;
    private String  appVersion;
    private String udid;

}
