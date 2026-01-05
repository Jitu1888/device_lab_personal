package com.games24x7.actuatordevices.deviceinformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.games24x7.actuatordevices.model.AppiumSession;
import com.games24x7.actuatordevices.model.Device;
import com.games24x7.actuatordevices.repository.DeviceRepository;
import com.games24x7.actuatordevices.utils.ADBUtilities;
import com.games24x7.actuatordevices.utils.CommandLineExecutor;
import com.games24x7.actuatordevices.utils.IOSUtilities;
import com.games24x7.actuatordevices.utils.StringUtils;

/**
 * @author jitu-patel
 *
 */
@Component
public class DeviceInformationDb implements CommandLineRunner{
	private DeviceRepository deviceRepository;
	private Integer PORT = 4723;
	private Integer SYSTEM_PORT = 8280;
	private Integer CHROME_PORT = 8000;
	
		

	public DeviceInformationDb(DeviceRepository deviceRepository) {
		super();
		this.deviceRepository = deviceRepository;
	}

	@Override
	public void run(String... args) throws Exception {
		Map<String, Device> connectedAndroidDevices = ADBUtilities.getConnectedAndroidDevices();
		Map<String, Device> connectediOSDevices = IOSUtilities.getConnectediOSDevices();
		connectedAndroidDevices.putAll(connectediOSDevices);
		connectedAndroidDevices.forEach((k,v)->{
			AppiumSession appiumSession = new AppiumSession();
			int newPort = PORT++;
			int chromePort = CHROME_PORT++;
			appiumSession.setPort(newPort);
			appiumSession.setAppiumUrl(String.format("http://%s:%s",StringUtils.getLocalNetworkIP(),newPort));
			appiumSession.setSystemPort(SYSTEM_PORT++);
			appiumSession.setSession("NOT-SET");
			appiumSession.setAppiumLogs("PATH");
			appiumSession.setChromePort(CHROME_PORT);
			v.setAppiumSession(appiumSession);
			v.setUser("NOT-SET");
			v.setAvailable(true);
			
		});
	
		
		if(!connectedAndroidDevices.isEmpty()) {
			List<Device> devices = new ArrayList<>();
			connectedAndroidDevices.forEach((k,v)->{
				devices.add(v);
			});
			deviceRepository.saveAll(devices);

		}

	}
	
	
	
}
