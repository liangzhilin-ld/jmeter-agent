package com.autotest.jmeter.jmeteragent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author pengbin
 * @Date 2020/8/27 15:05
 * @Description
 */
@Component
@ConfigurationProperties(prefix = "jmeter")
@Data
public class JmeterProperties {
	private String logPath = "logs";
	private String home;
	private String propertiesFileName;
	private boolean scheduler = false;
	private int delay = 0;
	private int loops = -1;
	private String resultReceiverUrl;

}
