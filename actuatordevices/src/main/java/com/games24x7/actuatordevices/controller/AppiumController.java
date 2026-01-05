package com.games24x7.actuatordevices.controller;

import com.games24x7.actuatordevices.exception.FileNotAvailableException;
import com.games24x7.actuatordevices.model.*;
import com.games24x7.actuatordevices.services.DeviceService;
import com.games24x7.actuatordevices.utils.*;
import io.github.martinschneider.justtestlah.mobile.tools.ApplicationInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Slf4j
@RestController
@RequestMapping("/appium")
public class AppiumController {


	@Autowired
	private DeviceService deviceService;

	@Autowired
	private AppiumServiceProvider appiumServiceProvider;

	@Autowired
	private ADBUtilities adbUtilities;

	@PostMapping("/allocate")
	private URL allocateDevice(@RequestParam(defaultValue = "200", value = "timeout") int timeoutInSeconds,
			@RequestBody DeviceRequest deviceRequest, HttpServletRequest httpServletRequest) throws Exception {

		log.info("Allocation Request for Device ID"+deviceRequest.getDeviceID()+"and request id "+deviceRequest.getDeviceRequestId());
		String deviceID = deviceRequest.getDeviceID();
		Optional<Device> optional = deviceService.findById(deviceID);

		//CommandLineExecutor.killProcessListeningAtPort(optional.get().getAppiumSession().getSystemPort());
		optional.get().setUser(deviceRequest.getUser());
		optional.get().setUniqueNumber(deviceRequest.getDeviceRequestId());


		if (optional.isPresent() && optional.get().getIsFree().equals("Available")) {
			int port = optional.get().getAppiumSession().getPort();
			//CommandLineExecutor.killProcessListeningAtPort(port);

			optional.get().setIsFree("Busy");
			deviceService.updateDevice(optional.get());
			//			
			//			try {
			//				ADBUtilities.runAndroidDeviceCommand(deviceID,"uninstall io.appium.uiautomator2.server");
			//				ADBUtilities.runAndroidDeviceCommand(deviceID,"uninstall io.appium.uiautomator2.server.test");
			//				ADBUtilities.runAndroidDeviceCommand(deviceID,"uninstall io.appium.unlock");
			//				ADBUtilities.runAndroidDeviceCommand(deviceID,"uninstall io.appium.settings");
			//			} catch (Exception e1) {
			//				throw new Exception("Not able to allocate the device "+deviceID);
			//			}

			//String command = "forward --remove-all";
			//ADBUtilities.runAndroidDeviceCommand(deviceID, command);
			//			return new URL(appiumServiceProvider.initAppiumService(optional.get().getSlaveIp(), deviceID, port,
			//					timeoutInSeconds, deviceRequest.getDeviceRequestId()));

			//new code
			return new URL(String.format("https://%s:8083/actuator-slave/appium/%s/%s",optional.get().getSlaveIp(),deviceID,
					deviceRequest.getDeviceRequestId()));


		} else {
			optional.get().setIsFree("Available");
			throw new Exception(String.format("Device ID %s is not free or not available", deviceID));
		}
	}

	@PostMapping("/deallocate/{udid}")
	private boolean deallocateDevice(@PathVariable final String udid) {
		System.out.println("Deallocate "+ udid);
		Optional<Device> optional = deviceService.findById(udid);
		optional.get().setUser("NOT-SET");
		optional.get().setIsFree("Available");
		optional.get().setUniqueNumber("NOT-SET");
		optional.get().getAppiumSession().setSession("NOT-SET");
		
		
		deviceService.updateDevice(optional.get());

		//return CommandLineExecutor.killAppiumProcessesByDeviceId(optional.get().getUdid());
		//return CommandLineExecutor.killProcessListeningAtPort(optional.get().getAppiumSession().getPort());
		return CommandLineExecutor.killAppiumProcessesByDeviceId(udid);

	}


	@PostMapping("/resolution/{udid}")
	private String resolutionDevice(@PathVariable final String udid) {
		Optional<Device> optional = deviceService.findById(udid);
		return optional.get().getDeviceInformation().getResolution();


	}
	@PostMapping("/command")
	private String command(@RequestBody AdbRequest deviceRequest) {
		String deviceID = deviceRequest.getDeviceId();
		String command = deviceRequest.getCommand();
		Optional<Device> optional = deviceService.findById(deviceID);
		if (!optional.isEmpty()) {
			if (optional.get().getDeviceInformation().isAndroid()) {
				return ADBUtilities.runAndroidDeviceCommand(deviceID, command);
			}
		} else {
			throw new RuntimeException(String.format("DeviceID  %s is not found", deviceID));
		}
		return "unsuccessfull";

	}

	@PostMapping("/uploadActuatorFarm")
	private String uploadActuatorFarm(@RequestParam("file") MultipartFile file) throws Exception {
		String status = "unsuccessfull";
		File file2 = null;
		boolean result = file.getOriginalFilename().contains(".apk");
		if (!file.isEmpty() && result) {
			String path = System.getProperty("user.dir") + "/upload";
			File fi = new File(path);
			if (!fi.exists()) {
				fi.mkdir();
			}
			String cmd = ADBUtilities.getAndroidPath() + adbUtilities.getBuildToolsPath() +"aapt dump badging " + fi.getAbsolutePath()
			+ "/" + file.getOriginalFilename() + " | grep package:\\ name";
			FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(new File(fi, file.getOriginalFilename())));
			CommandLineResponse response = CommandLineExecutor.exec(cmd);
			String[] name1 = response.getStdOut().split(" ");

			HashMap<String, String> hm = new HashMap<>();
			for (int i = 1; i < name1.length; i++) {
				String[] value = name1[i].split("=");
				hm.put(value[0], value[1].substring(1, value[1].length() - 1));
			}
			FileUtils.deleteDirectory(fi);
			String appPackage = hm.get("name");
			String appVersion = hm.get("versionName");
			File file1 = new File(appPackage);
			if (!file1.exists()) {
				file1.mkdir();
			}
			file2 = new File(file1, appVersion);
			if (!file2.exists()) {
				file2.mkdir();
			}
			if (file2.list() != null)
				FileCopyUtils.copy(file.getInputStream(),
						new FileOutputStream(new File(file2, file.getOriginalFilename())));
			status = "success";

		} else {
			throw new Exception("file .apk not found");
		}

		return status;
	}


	@PostMapping("/uploadActuatorFarmPS")
	private String uploadActuatorFarmPS(@RequestParam("file") MultipartFile file) throws Exception {
		String status = "unsuccessfull";
		File file2 = null;
		boolean result = file.getOriginalFilename().contains(".apks");
		if (!file.isEmpty() && result) {
			String path = System.getProperty("user.dir") + "/upload";
			File fi = new File(path);
			if (!fi.exists()) {
				fi.mkdir();
			}
			String cmd = ADBUtilities.getAndroidPath() + adbUtilities.getBuildToolsPath() +"aapt dump badging " + fi.getAbsolutePath()
					+ "/" + file.getOriginalFilename() + " | grep package:\\ name";
			FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(new File(fi, file.getOriginalFilename())));
			File directoryPath = new File(fi.getAbsolutePath());
			File filesList[] = directoryPath.listFiles();
			String apksName = filesList[0].getName();
			Pattern pattern = Pattern.compile("\\d+\\.\\d+");

			// Match the pattern against the input string
			Matcher matcher = pattern.matcher(apksName);
			String versionName = null;
			// Find and print the matched number
			while (matcher.find()) {
				versionName = matcher.group();
				System.out.println("Extracted number: " + versionName);
			}
			File file1 = new File("com.games24x7.rummycircle.rummy.stage");
			if (!file1.exists()) {
				file1.mkdir();
			}

			file2 = new File(file1, versionName);
			if (!file2.exists()) {
				file2.mkdir();
			}
			if (file2.list() != null)
				FileCopyUtils.copy(file.getInputStream(),
						new FileOutputStream(new File(file2, file.getOriginalFilename())));

			status = "success";

		} else {
			throw new Exception("file .apk not found");
		}

		return status;
	}



	@PostMapping("/installApk")
	public synchronized GetApkVersionPathResponse getApkPathandinstallInGivenUdid(
			@RequestBody RequestApplicationVersionPath requestApplicationVersionPath) throws FileNotFoundException {
		String appVersion = requestApplicationVersionPath.getAppVersion();
		String projectName = requestApplicationVersionPath.getProjectName();
		String udid = requestApplicationVersionPath.getUdid();
		log.info("appVersion is "+appVersion +" projectName "+projectName+" udid is "+ udid);
		GetApkVersionPathResponse fileapkpathresponse = new GetApkVersionPathResponse();
		if (projectName == "" || projectName == null) {
			throw new FileNotAvailableException("ProjectName should be mentioned in the request body");
		}
		if (appVersion == "" || appVersion == null) {
			throw new FileNotAvailableException("ApkVersion should be mentioned in the request body");
		}
		if (udid == "" || udid == null) {
			throw new FileNotAvailableException("Udid should be mentioned in the request body");
		}

		File file = new File(projectName);
		String status;
		if (file.exists()) {
			File file1 = new File(file, appVersion);
			if (file1.exists()) {
				String[] filename = file1.list();
				int num = 0;
				if(filename[0].contains(".DS")){
					 num = 1;
				}
				if (filename.length != 0 && filename[num].contains(".apk")) {

					if (new ApplicationInfoService().getAppInfo(file1 + "/" + filename[0]).getVersionName()
							.equals(appVersion)) {
						fileapkpathresponse.setFilePath(file1.getAbsolutePath() + "/" + filename[0]);
						Optional<Device> optional = deviceService.findById(udid);
						if (!optional.isEmpty()) {
							System.out.println(	ADBUtilities.getAdbExecutable() + " -s " + udid + " install "
									+ fileapkpathresponse.getFilePath());
							String command = ADBUtilities.getAdbExecutable() + " -s " + udid + " install "
									+ fileapkpathresponse.getFilePath();
							CommandLineResponse response = CommandLineExecutor.exec(command);
							String res = response.getStdOut();
							log.info(res);
							if (res.contains("Success")) {
								fileapkpathresponse.setAppinstalledStatus("Success");
							} else if (res.contains("ALREADY_EXISTS")) {
								fileapkpathresponse.setAppinstalledStatus(res);
								throw new FileNotAvailableException("Failed to install - app is already present");
							} else {
								fileapkpathresponse.setAppinstalledStatus(res);
								throw new FileNotAvailableException(
										"Failed to install - filename doesn't end .apk or .apex");
							}
						} else
							throw new FileNotAvailableException(
									"Device is not available to install given version apk " + "appVersion is "+appVersion +" projectName "+projectName+" udid is "+ udid);
					} else
						throw new FileNotAvailableException(appVersion + "appVersion is "+appVersion +" projectName "+projectName+" udid is "+ udid);
				} else
					throw new FileNotAvailableException("ApK file is not available in " + appVersion + "folder" + "appVersion is "+appVersion +" projectName "+projectName+" udid is "+ udid);
			} else {
				fileapkpathresponse.setAppinstalledStatus("ApK file is not available in " + appVersion + "folder" + "appVersion is "+appVersion +" projectName "+projectName+" udid is "+ udid);
				throw new FileNotAvailableException("appVersion given is not available in " + projectName + "folder" +"appVersion is "+appVersion +" projectName "+projectName+" udid is "+ udid);
			}
			return fileapkpathresponse;
		} else
			throw new FileNotAvailableException("Project Name is not found" +"appVersion is "+appVersion +" projectName "+projectName+" udid is "+ udid);
	}


	@PostMapping("/installApkPS")
	public synchronized GetApkVersionPathResponse getApkPathandinstallInGivenUdidPS(
			@RequestBody RequestApplicationVersionPath requestApplicationVersionPath) throws FileNotFoundException {
		String appVersion = requestApplicationVersionPath.getAppVersion();
		String projectName = requestApplicationVersionPath.getProjectName();
		String udid = requestApplicationVersionPath.getUdid();
		log.info("appVersion is "+appVersion +" projectName "+projectName+" udid is "+ udid);
		GetApkVersionPathResponse fileapkpathresponse = new GetApkVersionPathResponse();
		if (projectName == "" || projectName == null) {
			throw new FileNotAvailableException("ProjectName should be mentioned in the request body");
		}
		if (appVersion == "" || appVersion == null) {
			throw new FileNotAvailableException("ApkVersion should be mentioned in the request body");
		}
		if (udid == "" || udid == null) {
			throw new FileNotAvailableException("Udid should be mentioned in the request body");
		}

		File file = new File(projectName);
		String status;
		if (file.exists()) {
			File file1 = new File(file, appVersion);
			if (file1.exists()) {
				String[] filename = file1.list();
				int num = 0;
				if(filename[0].contains(".DS")){
					num = 1;
				}
				if (filename.length != 0 && filename[num].contains(".apk")) {
						fileapkpathresponse.setFilePath(file1.getAbsolutePath() + "/" + filename[0]);
						Optional<Device> optional = deviceService.findById(udid);
						if (!optional.isEmpty()) {
							System.out.println(	"bundletool install-apks --device-id="+ udid + " --apks="
									+ fileapkpathresponse.getFilePath());
							String command = "bundletool install-apks --device-id="+ udid + " --apks="
									+ fileapkpathresponse.getFilePath();
							CommandLineResponse response = CommandLineExecutor.exec(command);
							String res = response.getStdOut();
							log.info(res);
							if (res.contains("")) {
								fileapkpathresponse.setAppinstalledStatus("Success");
							} else if (res.contains("ALREADY_EXISTS")) {
								fileapkpathresponse.setAppinstalledStatus(res);
								throw new FileNotAvailableException("Failed to install - app is already present");
							} else {
								fileapkpathresponse.setAppinstalledStatus(res);
								throw new FileNotAvailableException(
										"Failed to install - filename doesn't end .apk or .apex");
							}
						} else
							throw new FileNotAvailableException(
									"Device is not available to install given version apk " + "appVersion is "+appVersion +" projectName "+projectName+" udid is "+ udid);
					} else
						throw new FileNotAvailableException(appVersion + "appVersion is "+appVersion +" projectName "+projectName+" udid is "+ udid);
				} else
					throw new FileNotAvailableException("ApK file is not available in " + appVersion + "folder" + "appVersion is "+appVersion +" projectName "+projectName+" udid is "+ udid);
			} else {
				throw new FileNotAvailableException("appVersion given is not available in " + projectName + "folder" +"appVersion is "+appVersion +" projectName "+projectName+" udid is "+ udid);
			}
			return fileapkpathresponse;

//			throw new FileNotAvailableException("Project Name is not found" +"appVersion is "+appVersion +" projectName "+projectName+" udid is "+ udid);
	}

	@PostMapping("/getRemoteUrl/{udid}")
	private String getRemoteUrl(@PathVariable final String udid) {
		System.out.println("Deallocate "+ udid);
		Optional<Device> optional = deviceService.findById(udid);
		String url = optional.get().getAppiumSession().getAppiumUrl();
		return url;

	}



	@PostMapping("/startRecording/{udid}")
	private URL startRecording(@PathVariable final String udid) throws IOException {
		log.info("request '{}' ::: video recording", udid);
		String os = "ios";
		Optional<Device> optional = deviceService.findById(udid);
		//String path = System.getProperty("user.dir") +System.getProperty("file.separator")+ "logs"+System.getProperty("file.separator")+udid;


		String folder = optional.get().getUniqueNumber();
		String testCase = optional.get().getTestCase();
		String retry = String.valueOf(optional.get().getTestRetry());


		File file = Paths.get("Logs",folder,"video",udid,retry,testCase+".mp4").toFile();
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if(optional.get().getDeviceInformation().isAndroid()) {
			os = "android";
		}
		ADBUtilities.runAndroidDeviceCommand(udid, "rm -rf /sdcard/flick/*");

		log.info(file.getParentFile().toString());


		String command = "flick video -a start -p "+os+
				" -u "+udid+" -e "+"true"+" -c "+24000 +" -o "+System.getProperty("user.dir")+System.getProperty("file.separator")+file.getParentFile().toString()+" -n "+testCase; 

		log.info(command);

		new Thread(() -> {

			CommandLineResponse response = CommandLineExecutor.exec(command);
			if (response.getExitCode() != 0) {
				log.info("video recording failed with exit code '{}' and message '{}'",
						response.getExitCode(), response.getStdOut());
			}
		}).start();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new URL(String.format("http://%s:8088/actuator-slave/"+file.getParentFile().toString()
				+"/"+testCase+".mp4",StringUtils.getLocalNetworkIP().toString()));

	}

	@PostMapping("/stopRecording/{udid}")
	private URL stopRecording(@PathVariable final String udid) throws IOException {
		log.info("request '{}' :::  stop video recording", udid);
		String os = "ios";
		Optional<Device> optional = deviceService.findById(udid);

		String floder = optional.get().getUniqueNumber();
		String testCase = optional.get().getTestCase();
		String retry = String.valueOf(optional.get().getTestRetry());


		File file = Paths.get("Logs",floder,"video",udid,retry,testCase+".mp4").toFile();
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if(optional.get().getDeviceInformation().isAndroid()) {
			os = "android";
		}
		log.info(file.getParentFile().toString());
		String command = "flick video -a stop -p "+os+
				" -u "+udid+" -o "+System.getProperty("user.dir") +System.getProperty("file.separator")+file.getParentFile().toString()+" -n "+testCase+" -c "+24000;


		//		String command = "flick video -a stop -p "+os+
		//			     " -u "+udid; 
		log.info(command);

		new Thread(() -> {

			CommandLineResponse response = CommandLineExecutor.exec(command);
			log.info(response.getStdOut());
			if (response.getExitCode() != 0) {
				log.info("video recording failed with exit code '{}' and message '{}'",
						response.getExitCode(), response.getStdOut());
			}
		}).start();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new URL(String.format("http://%s:8088/actuator-slave/"+file.getParentFile().toString()
				+"/"+testCase+".mp4",StringUtils.getLocalNetworkIP().toString()));

	}


	@PostMapping("/startAbdLogs/{udid}")
	private URL startAbdLogs(@PathVariable final String udid) throws IOException {
		log.info("request '{}' ::: logs recording", udid);
		String os = "ios";
		Optional<Device> optional = deviceService.findById(udid);
		//String path = System.getProperty("user.dir") +System.getProperty("file.separator")+ "logs"+System.getProperty("file.separator")+udid;

		String folder = optional.get().getUniqueNumber();
		String testCase = optional.get().getTestCase();

		String retry = String.valueOf(optional.get().getTestRetry());


		File file = Paths.get("Logs",folder,"log",udid,retry,testCase+".txt").toFile();

		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if(optional.get().getDeviceInformation().isAndroid()) {
			os = "android";
		}

		log.info(file.getParentFile().toString());

		

		String command = "flick log -a start -p "+os+
				" -u "+udid+ " -o "+System.getProperty("user.dir") +System.getProperty("file.separator")+file.getParentFile().toString()+" -u "+udid+" -n "+testCase; 

		log.info(command);

		//new Thread(() -> {
		CommandLineResponse response = CommandLineExecutor.exec(command);
		log.info(response.getStdOut());
		if (response.getExitCode() != 0) {
			log.info("adb  startAbdLogs failed with exit code '{}' and message '{}'",
					response.getExitCode(), response.getStdOut());
		}
		//}).start();
		return new URL(String.format("http://%s:8088/actuator-slave/"+file.getParentFile().toString()
				+"/"+testCase+".log",StringUtils.getLocalNetworkIP().toString()));

	}



	@PostMapping("/stopAbdLogs/{udid}")
	private URL stopAbdLogs(@PathVariable final String udid) throws IOException {
		log.info("request '{}' :::  stop log recording", udid);
		String os = "ios";
		Optional<Device> optional = deviceService.findById(udid);

		String folder = optional.get().getUniqueNumber();
		String testCase = optional.get().getTestCase();
		String retry = String.valueOf(optional.get().getTestRetry());


		File file = Paths.get("Logs",folder,"log",udid,retry,testCase+".txt").toFile();


		log.info("folder name is {} udid is {} testcase name is {}",folder,udid,testCase);


		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if(optional.get().getDeviceInformation().isAndroid()) {
			os = "android";
		}
		log.info(file.getParentFile().toString());
		String command = "flick log -a stop -p "+os+
				" -u "+udid; 
		log.info(command);

		new Thread(() -> {
			CommandLineResponse response = CommandLineExecutor.exec(command);
			if (response.getExitCode() != 0) {
				log.info("video stopAbdLogs failed with exit code '{}' and message '{}'",
						response.getExitCode(), response.getStdOut());
			}
		}).start();


		return new URL(String.format("http://%s:8088/actuator-slave/"+file.getParentFile().toString()
				+"/"+testCase+".log",StringUtils.getLocalNetworkIP().toString()));

	}

	@PostMapping("/symlink/{udid}")
	private URL createSymLink(@PathVariable final String udid, @RequestBody(required = true) final SymLink symLinkObject ) throws IOException {
		log.info("request '{}' :::  symlink", udid);
		String os = "ios";
		Optional<Device> optional = deviceService.findById(udid);

		String folder = symLinkObject.getUniqueId();
		String testCase = optional.get().getTestCase();
		log.info("folder name is {} udid is {} testcase name is {}",folder,udid,testCase);
		log.info("SessionId is {}", symLinkObject.getSessionId());
		int retry =optional.get().getTestRetry();
		File videoDir =
				new File(System.getProperty("user.dir")+"/Logs/"+folder+"/video/"+udid+"/"+retry);
		if(!videoDir.exists()){
			videoDir.mkdirs();
		}
		CommandLineExecutor.exec("rm -rf "+ System.getProperty("user.dir")+"/Logs/"+folder+"/video/"+udid+"/"+retry+"/"+testCase+"_symlink.mp4");
		CommandLineExecutor.exec("ln -s /Users/jenkins/.cache/appium-dashboard-plugin/videos/"+symLinkObject.getSessionId()+".mp4 "+ System.getProperty("user.dir")+"/Logs/"+folder+"/video/"+udid+"/"+retry+"/"+testCase+"_symlink.mp4");

		File file = Paths.get("Logs",folder,"video",udid,String.valueOf(retry),testCase+".mp4").toFile();
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		return new URL(String.format("http://%s:8088/actuator-slave/"+file.getParentFile().toString()
				+"/"+testCase+"_symlink.mp4",StringUtils.getLocalNetworkIP().toString()));

	}

	@PostMapping("/takeScreenShot/{udid}")
	private URL takeScreenShot(@PathVariable final String udid) throws IOException {
		log.info("request '{}' ::: Screenshot", udid);
		String os = "ios";
		Optional<Device> optional = deviceService.findById(udid);
		//String path = System.getProperty("user.dir") +System.getProperty("file.separator")+ "logs"+System.getProperty("file.separator")+udid;

		String floder = optional.get().getUniqueNumber();
		String testCase = optional.get().getTestCase();


		File file = Paths.get("Logs",floder,"screenshot",udid,testCase+".png").toFile();
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if(optional.get().getDeviceInformation().isAndroid()) {
			os = "android";
		}

		log.info(file.getParentFile().toString());

		String command = "flick screenshot -p "+os+
				" -u "+udid+ " -o "+System.getProperty("user.dir") +System.getProperty("file.separator")+file.getParentFile().toString()+" -u "+udid+" -n "+testCase; ; 

				log.info(command);

				new Thread(() -> {
					CommandLineResponse response = CommandLineExecutor.exec(command);
					if (response.getExitCode() != 0) {
						log.info("adb  screenshot failed with exit code '{}' and message '{}'",
								response.getExitCode(), response.getStdOut());
					}
				}).start();
				return new URL(String.format("http://%s:8088/actuator-slave/"+file.getParentFile().toString()
						+"/"+testCase+".png",StringUtils.getLocalNetworkIP().toString()));

	}







	@PostMapping("/postTestCase/{udid}")
	private void postTestCase(@PathVariable final String udid,@RequestBody String testCase) {
		System.out.println("Device is "+ udid + "Test case name is "+testCase);
		JSONObject json = new JSONObject(testCase);
		String tc = json.getString("testCase");
		Optional<Device> optional = deviceService.findById(udid);
		optional.get().setTestCase(tc);
		deviceService.updateDevice(optional.get());


	}





	//	
	//	@PostMapping("/restart/{udid}")
	//	private void restart(@PathVariable final String udid ) {
	//		log.info("********************** restarting devices **********************");
	//		ADBUtilities.runAndroidDeviceCommand(udid, "reboot");
	//		log.info("********************** restarting devices finished **********************");
	//	
	//	}
	//	


	@PostMapping("/startCodeCoverage/{udid}")
	private URL startCodeCoverage(@PathVariable final String udid) throws IOException {
		log.info("request '{}' :::  stop log recording", udid);
		String os = "ios";
		Optional<Device> optional = deviceService.findById(udid);

		String floder = optional.get().getUniqueNumber();
		String testCase = optional.get().getTestCase();


		File file = Paths.get("Logs",floder,"log",udid,testCase+".txt").toFile();
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if(optional.get().getDeviceInformation().isAndroid()) {
			os = "android";
		}
		log.info(file.getParentFile().toString());
		String command = "flick log -a stop -p "+os+
				" -u "+udid; 
		log.info(command);

		new Thread(() -> {
			CommandLineResponse response = CommandLineExecutor.exec(command);
			if (response.getExitCode() != 0) {
				log.info("video stopAbdLogs failed with exit code '{}' and message '{}'",
						response.getExitCode(), response.getStdOut());
			}
		}).start();
	
		


		return new URL(String.format("http://%s:8088/actuator-slave/"+file.getParentFile().toString()
				+"/"+testCase+".log",StringUtils.getLocalNetworkIP().toString()));

	}
	@PostMapping("/uploadAndroidLogs/{folderName}")
	public void uploadAndroidLogs(@PathVariable final String folderName) {

		new s3Buckket().upload(folderName);


	}
	
	@PostMapping("/switchOffPhones")
	public void switchOffPhones() {
		List<Device> devices = deviceService.getDevices();
		for(Device device : devices) {
			ADBUtilities.runAndroidDeviceCommand(device.getUdid(), "reboot -p");
			
		}
	}

	
	@PostMapping("/test")
	public Callable<String> getFoobar() throws InterruptedException {
	 return new Callable<String>() {
        @Override
        public String call() throws Exception {
            Thread.sleep(120000); //this will cause a timeout
            return "foobar";
        }
    };
	
		
		
		
	}

	
	
	@PostMapping("/getFatalExceptionOccurs/{udid}")
	private String getFatalExceptionOccurs(@PathVariable final String udid) {
		log.info("request '{}' :::  stop log recording", udid);
		
		Optional<Device> optional = deviceService.findById(udid);
		String floder = optional.get().getUniqueNumber();
		String testCase = optional.get().getTestCase();
		File file = Paths.get("Logs",floder,"log",udid,testCase+".txt").toFile();
		
		log.info("file path is {}",file.toString());
		

		String command = "grep -rn FATAL "+file;
		CommandLineResponse response = CommandLineExecutor.exec(command);
		if (response.getExitCode() != 0) {
			log.info("getFatalExceptionOccursfailed with exit code '{}' and message '{}'",
					response.getExitCode(), response.getStdOut());
		}
		
		return response.getStdOut();
	}

	@PostMapping("/updateUniqueId/{udid}")
	private void updateSessionId(@PathVariable final String udid,@RequestBody String uniqueId) {
		System.out.println("Device is " + udid + "and uniqueId ID is " + uniqueId);
		JSONObject json = new JSONObject(uniqueId);
		String uniqueIdValue = json.getString("uniqueId");
		Optional<Device> optional = deviceService.findById(udid);
		optional.get().setUniqueNumber(uniqueIdValue);
		deviceService.updateDevice(optional.get());
	}
}


