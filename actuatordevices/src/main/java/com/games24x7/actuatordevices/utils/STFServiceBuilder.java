package com.games24x7.actuatordevices.utils;

import static java.lang.Runtime.getRuntime;
import java.io.File;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

public final class STFServiceBuilder {

	private static final String DEFAULT_STF_SCREENSHOT_QUALITY = "25";

	private STFServiceBuilder() {
	}

	public static Builder builder() {
		return new Builder();
	}

	@Slf4j
	public static final class Builder {

		public boolean isSTFRunning() {
			CommandLineResponse response = CommandLineExecutor.exec("ps -ef | grep -i 'start_stf.bash' | wc -l");
			boolean isRunning = Integer.parseInt(response.getStdOut().trim()) > 2;
			if (isRunning) {
				log.info("STF service is running");
			} else {
				log.error("STF service is not running");
			}
			return isRunning;
		}

		public void restart() {
			stop();
			start();
		}

		public void stop() {
			log.info("stopping STF service");
			CommandLineExecutor.killProcess("start_stf.bash", true);
		}

		public void start() {
			String nodeVersion = "16.15.1";
			String script = StringUtils
					.streamToString(STFServiceBuilder.class.getResourceAsStream("/scripts/stf.bash"));
			File scriptFile = new File("start_stf.bash");
			log.info("starting STF service");
			new Thread(() -> {
				try {
					java.nio.file.Files.write(scriptFile.toPath(), script.getBytes());
					String cmd = String.format("/bin/bash %s %s %s %s", scriptFile.getAbsolutePath(),
							nodeVersion,
							StringUtils.getLocalNetworkIP(),
							StringUtils.getProperty("STF_SCREENSHOT_QUALITY", DEFAULT_STF_SCREENSHOT_QUALITY));
					log.info("Command to start stf  -->	"+cmd);
					getRuntime().exec(cmd).waitFor(5, TimeUnit.SECONDS);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					scriptFile.delete();
				}
			}).start();
		}

	}
}
