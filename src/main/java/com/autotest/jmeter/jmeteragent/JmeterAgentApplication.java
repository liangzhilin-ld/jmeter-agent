package com.autotest.jmeter.jmeteragent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@MapperScan(value = "com.autotest.data.mapper")
@ComponentScan(basePackages = "com.autotest")
@SpringBootApplication
public class JmeterAgentApplication {
	public static void main(String[] args) {
		SpringApplication.run(JmeterAgentApplication.class, args);
	}
}
