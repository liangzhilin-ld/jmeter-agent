package com.autotest.jmeter.entity.assertion;


import lombok.Data;

@Data
public class JsonAssert {
	private String jsonPath;
	private String expectedValue;
	private boolean addAssertValue=true;
	private boolean expectNull=false;
	private boolean invertAssert=false;
}
