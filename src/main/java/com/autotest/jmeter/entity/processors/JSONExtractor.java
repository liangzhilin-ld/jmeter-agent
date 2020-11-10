package com.autotest.jmeter.entity.processors;

import java.util.HashMap;

import lombok.Data;

@Data
public class JSONExtractor {
	private String name="JSON Extractor";
	private HashMap<String,String> applyTo;
	private String variableName;
	private String jsonPath;	
	private String matchNo;
	private boolean suffix_ALL=false;
	private String defaultValue;
	public JSONExtractor() {
		this.setApplyTo("Main_Sample_Only");
	}	
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
}
