package com.games24x7.actuatordevices.utils;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.games24x7.actuatordevices.model.Device;
import com.games24x7.actuatordevices.model.DeviceInformation;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.games24x7.actuatordevices.utils.CommandLineExecutor.exec;
import static com.games24x7.actuatordevices.utils.StringUtils.splitLines;

/**
 * @author jitu-patel
 *
 */
@Slf4j
@Service
public final class ADBUtilities {
	
	private static final String MODEL = "ro.product.model";
	private static final String MANUFACTURE = "ro.product.brand";
	private static final String OS = "ro.build.version.release";
	private static final String DEVICENAME = "ro.product.model";
	private static final String RESOLUTION = "Physical size";
	private static final String BUILD_TOOLS_VERSION = "build.tools.version";
	private static final String ANDROID_HOME_PATH = System.getenv("HOME")+"/Library/Android/sdk";
	private static final String PROPERTY_REGEX = "(?<=\\[).+?(?=\\])";

	@Autowired
	ConfigProperties configProp;

	public static String getAndroidPath() {
		 return StringUtils.isBlank(ANDROID_HOME_PATH.isEmpty()) ? System.getProperty("android.home", "/opt/android-sdk")
				: ANDROID_HOME_PATH.trim();
	}


	public static String getAdbExecutable() {
		return Paths.get(getAndroidPath(), "platform-tools", "adb").toString();
	}


	public String getBuildToolsPath() {
		String version = configProp.getConfigValue(BUILD_TOOLS_VERSION);
		return "/build-tools/" + version + "/";
	}


	
	public static List<String> connectedDevices() {
		CommandLineResponse response = exec(getAdbExecutable() + " devices | tail -n +2");
		if (response.getExitCode() == 0) {
			List<String> split = splitLines(response.getStdOut());
			return split.stream()
					.filter(e -> e.contains("device") && !e.trim().isEmpty())
					.map(e -> e.split("\\s+")[0].trim())
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
	
	
	public static Map<String, Device> getConnectedAndroidDevices(){
		List<String> deviceId = connectedDevices();
		Map<String,Device> devices = new HashMap<String, Device>();
		deviceId.stream().forEach(e->{
			Map<String,String> mapWithPropertiesFormat = StringUtils.
					getMapWithPropertiesFormat(getAndroidProperties(e));
			DeviceInformation d  =	new DeviceInformation();
			d.setDeviceName(mapWithPropertiesFormat.get(DEVICENAME));
			d.setAndroid(true);
			d.setRealDevice(true);
			
			d.setManufacture(mapWithPropertiesFormat.get(MANUFACTURE));
			d.setModel(mapWithPropertiesFormat.get(MODEL));
			d.setOs(mapWithPropertiesFormat.get(OS));
			d.setResolution(StringUtils.
					getMapWithPropertiesFormat(getAndroidDeviceResolution(e)).get(RESOLUTION));
			Device device = new Device();
			device.setDeviceInformation(d);
			device.setStfUrl("http://"+StringUtils.getLocalNetworkIP()+":7100/#!/control/"+e);
			device.setSlaveIp(StringUtils.getLocalNetworkIP());
			device.setUdid(e);
			device.setIsFree("Available");
			devices.put(e, device);
				
		});
		
		return devices;
		
	}
	
	
	

	public static String getDeviceName(@NonNull String deviceId) {
		String cmd = String.format(
				"%s -s %s shell cat /proc/meminfo | grep -i MemTotal | cut -d ' ' -f 9", getAdbExecutable(), deviceId);
		CommandLineResponse response = exec(cmd);
		if (response.getExitCode() == 0) {
			return response.getStdOut();
		}
		return null;
	}
	
	public static String getAndroidProperties(@NonNull String deviceId) {
		String cmd = String.format(
				"%s -s %s shell getprop", getAdbExecutable(), deviceId);
		CommandLineResponse response = exec(cmd);
		if (response.getExitCode() == 0) {
			return response.getStdOut();
		}
		return null;
	}
	
	public static String getAndroidDeviceResolution(@NonNull String deviceId) {
		String cmd = String.format(
				"%s -s %s shell wm size", getAdbExecutable(), deviceId);
		CommandLineResponse response = exec(cmd);
		if (response.getExitCode() == 0) {
			return response.getStdOut();
		}
		return null;
	}
	
	
	public static String runAndroidDeviceCommand(@NonNull String deviceId,@NonNull String command) {
		String cmd = String.format(
				"%s -s %s "+command, getAdbExecutable(), deviceId);
		log.info(cmd);
		CommandLineResponse response = exec(cmd);
		if (response.getExitCode() == 0) {
			return response.getStdOut();
		}
		return null;
	}

}
