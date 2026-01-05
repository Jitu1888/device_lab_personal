package com.games24x7.actuatordevices.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.games24x7.actuatordevices.model.Device;
import com.games24x7.actuatordevices.model.DeviceInformation;
import com.games24x7.actuatordevices.model.Platform;

import lombok.extern.slf4j.Slf4j;

import static com.games24x7.actuatordevices.utils.CommandLineExecutor.exec;
import static com.games24x7.actuatordevices.utils.StringUtils.extractNumbers;
import static java.util.function.Function.identity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
@Slf4j
public class IOSUtilities {

	private IOSUtilities() {
	}

	private static Integer PORT = 4733;
	private static final Platform PLATFORM = Platform.CURRENT_PLATFORM;

	private static final String REGEX = "([a-zA-Z0-9\\s]+)(?!\\().+?((?<=\\()([A-Z0-9\\-]+)(?=\\) \\(Booted\\)))";

	private static String localSDKVersion;

	static {
		CommandLineResponse response = exec("ios-deploy --version");
		if (response.getExitCode() == 0) {
			int majorVersion = Integer
					.parseInt(response.getStdOut().substring(0, response.getStdOut().indexOf('.')).trim());
			if (majorVersion < 1) {
				throw new RuntimeException("please install `ios-deploy` version 1.X or higher");
			}
		}
	}

	public static String getSDKVersion() {
		CommandLineResponse response = exec("xcodebuild -showsdks | grep -Eoi iphonesimulator[0-9\\.]+");
		if (response.getExitCode() == 0) {
			Double version = extractNumbers(response.getStdOut());
			if (version != 0D) {
				log.debug("local iOS sdk version => {}", version);
				return version.toString();
			}
		}
		log.error("unable to fetch iOS simulator SDK version");
		return null;
	}

	public static Map<String, Device> getConnectediOSDevices() {
		if (PLATFORM != Platform.MACINTOSH) {
			return Collections.emptyMap();
		}
		String cmd = "ios-deploy --detect --json --timeout 1";
		CommandLineResponse response = exec(cmd);
		if (response != null && response.getExitCode() == 0) {
			String regex = "(?<=\"Device\" : \\{).*?(?=\\})";
			String text = response.getStdOut().replaceAll("\\s+", " ");

			List<String> list = StringUtils.getMatches(text, regex).stream().map(e -> String.format("{ %s }", e))
					.collect(Collectors.toList());
		
			TypeReference<Map<String, String>> type = new TypeReference<Map<String, String>>() {
			};
			return list.stream().map(str -> {
				Map<String, String> map = ParserUtilities.jsonToPojo(str, type);
				Device property = new Device();
				
				property.setUdid(map.get("DeviceIdentifier"));
				DeviceInformation deviceInformation = new DeviceInformation();
				deviceInformation.setManufacture("Apple");
				deviceInformation.setAndroid(false);
				deviceInformation.setModel(map.get("modelName"));
				deviceInformation.setOs(map.get("ProductVersion"));
				deviceInformation.setRealDevice(true);
				property.setIsFree("Available");
				property.setDeviceInformation(deviceInformation);
				property.setSlaveIp(StringUtils.getLocalNetworkIP());
				return property;
			}).distinct().filter(Objects::nonNull).collect(Collectors.toMap(Device::getUdid, identity()));

		}
		return Collections.emptyMap();
	}
}
