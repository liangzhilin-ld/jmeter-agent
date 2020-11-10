package com.autotest.jmeter.entity.assertion;


import java.util.HashMap;
import java.util.List;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

public class ResponseAssert {	
	private String name;
	private HashMap<String,String> applyTo;
	private String filedToTest;
	private String patternMatchRules;
	private List<String> testString;
	private String customFailureMessage;
	public ResponseAssert(){ 		
		this.setName("响应断言");
		this.setApplyTo("Main_Sample_Only");
		this.setFiledToTest("Text_Response");
		this.setPatternMatchRules("SUBSTRING");
    }
	public HashMap<String, String> getApplyTo() {
		return applyTo;
	}
	//设置ApplyTo为Jmeter variable Name to Use
	public HashMap<String,String> setApplyTo(String scope,String variableValue){
		HashMap<String,String> applyTo=new HashMap<String, String>(); 
		applyTo.put("scope", scope);
		applyTo.put("variableValue", variableValue);
		this.applyTo=applyTo;
		return applyTo;
	}
	//设置ApplyTo不为Jmeter variable Name to Use方法
	public HashMap<String,String> setApplyTo(String scope){
		HashMap<String,String> applyTo=new HashMap<String, String>(); 
		applyTo.put("scope", scope);
		this.applyTo=applyTo;
		return applyTo;
	}
	public String getFiledToTest() {
		return filedToTest;
	}
	public void setFiledToTest(String filedToTest) {
		this.filedToTest = filedToTest;
	}
	public String getPatternMatchRules() {
		return patternMatchRules;
	}
	public void setPatternMatchRules(String patternMatchRules) {
		this.patternMatchRules = patternMatchRules;
	}
	public List<String> getTestString() {
		return testString;
	}
	public void setTestString(List<String> testString) {
		this.testString = testString;
	}
	public String getCustomFailureMessage() {
		return customFailureMessage;
	}
	public void setCustomFailureMessage(String customFailureMessage) {
		this.customFailureMessage = customFailureMessage;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
