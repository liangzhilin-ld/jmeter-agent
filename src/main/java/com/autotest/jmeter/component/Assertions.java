package com.autotest.jmeter.component;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.apache.jmeter.assertions.BeanShellAssertion;
import org.apache.jmeter.assertions.JSONPathAssertion;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.assertions.gui.BeanShellAssertionGui;
import org.apache.jmeter.assertions.gui.JSONPathAssertionGui;
import org.apache.jmeter.testelement.TestElement;

import com.autotest.data.mode.AssertJson;
import com.autotest.data.mode.AssertResponse;
import com.autotest.jmeter.entity.assertion.*;

import cn.hutool.json.JSONUtil;
import net.sf.json.JSONArray;
//import com.autotest.domain.jmeter.assertion.JsonPathAssert;
//import com.autotest.domain.jmeter.assertion.ResponseAssert;

public class Assertions {
	
	 public static ResponseAssertion responseAssertion(ResponseAssert ra) {
		 
		 ResponseAssertion resAssert=new ResponseAssertion();
		 resAssert.setProperty(TestElement.GUI_CLASS,AssertionGui.class.getName());
		 resAssert.setProperty(TestElement.TEST_CLASS,ResponseAssertion.class.getName());
		 resAssert.setEnabled(true);
		 resAssert.setName(ra.getName());//名称
		 resAssert.setAssumeSuccess(false);	
		 switch (ra.getApplyTo().get("scope")) {
		 	case "MAIN_AND_SUB":
		 		resAssert.setScopeAll();
		 		break;
		 	case "SUB":
		 		resAssert.setScopeChildren();
		 		break;
		 	case "variableName":
		 		resAssert.setScopeVariable(ra.getApplyTo().get("variableValue"));
		 		break;
		 	default:
		 		resAssert.setScopeParent(); //Main sample only
		 		break;
		 }
		 switch (ra.getFiledToTest()) {		 	
		 	case "RESPONSE_CODE":	
		 		resAssert.setTestFieldResponseCode();
		 		break;
		 	case "RESPONSE_MESSAGE":	
		 		resAssert.setTestFieldResponseMessage();
		 		break;
		 	case "RESPONSE_HEADERS":	
		 		resAssert.setTestFieldResponseHeaders();
		 		break;
		 	case "REQUEST_HEADERS":	
		 		resAssert.setTestFieldRequestHeaders();
		 		break;
		 	case "SAMPLE_URL":	
		 		resAssert.setTestFieldURL();
		 		break;
		 	case "RESPONSE_DATA_AS_DOCUMENT":	
		 		resAssert.setTestFieldResponseDataAsDocument();
		 		break;
		 	case "REQUEST_DATA":	
		 		resAssert.setTestFieldRequestData();
		 		break;
		 	default:
		 		resAssert.setTestFieldResponseData();
		 		break;
		}
		 switch (ra.getPatternMatchRules()) {
		 	case "CONTAINS":
		 		resAssert.setToContainsType();
		 		break;
		 	case "MATCH":
		 		resAssert.setToMatchType();
		 		break;
		 	case "EQUALS":
		 		resAssert.setToEqualsType();
		 		break;
		 	case "OR":
		 		resAssert.setToOrType();
		 		break;
		 	case "NOT":
		 		resAssert.setToNotType();
		 		break;
		 	default:
		 		resAssert.setToSubstringType();
		 		break;
		 }
		 //resAssert.setProperty(new IntegerProperty("Assertion.test_type", 2));//见schematic.xsl
		 for (String testString : ra.getTestString()) {
			 resAssert.addTestString(testString);	
		 }		 
		 resAssert.setCustomFailureMessage(ra.getCustomFailureMessage());
		 return resAssert;
	 }
	 
	 public static ResponseAssertion responseAssertion(AssertResponse ra) {
		 
		 ResponseAssertion resAssert=new ResponseAssertion();
		 resAssert.setProperty(TestElement.GUI_CLASS,AssertionGui.class.getName());
		 resAssert.setProperty(TestElement.TEST_CLASS,ResponseAssertion.class.getName());
		 resAssert.setEnabled(true);
		 resAssert.setName(ra.getName());//名称
		 resAssert.setAssumeSuccess(false);	
		 switch (ra.getApplyTo()) {
		 	case "MAIN_AND_SUB":
		 		resAssert.setScopeAll();
		 		break;
		 	case "SUB":
		 		resAssert.setScopeChildren();
		 		break;
		 	case "variableName":
		 		resAssert.setScopeVariable(ra.getApplyTo());
		 		break;
		 	default:
		 		resAssert.setScopeParent(); //Main sample only
		 		break;
		 }
		 switch (ra.getFieldToText()) {		 	
		 	case "RESPONSE_CODE":	
		 		resAssert.setTestFieldResponseCode();
		 		break;
		 	case "RESPONSE_MESSAGE":	
		 		resAssert.setTestFieldResponseMessage();
		 		break;
		 	case "RESPONSE_HEADERS":	
		 		resAssert.setTestFieldResponseHeaders();
		 		break;
		 	case "REQUEST_HEADERS":	
		 		resAssert.setTestFieldRequestHeaders();
		 		break;
		 	case "SAMPLE_URL":	
		 		resAssert.setTestFieldURL();
		 		break;
		 	case "RESPONSE_DATA_AS_DOCUMENT":	
		 		resAssert.setTestFieldResponseDataAsDocument();
		 		break;
		 	case "REQUEST_DATA":	
		 		resAssert.setTestFieldRequestData();
		 		break;
		 	default:
		 		resAssert.setTestFieldResponseData();
		 		break;
		}
		 switch (ra.getPatternRules()) {
		 	case "CONTAINS":
		 		resAssert.setToContainsType();
		 		break;
		 	case "MATCH":
		 		resAssert.setToMatchType();
		 		break;
		 	case "EQUALS":
		 		resAssert.setToEqualsType();
		 		break;
		 	case "OR":
		 		resAssert.setToOrType();
		 		break;
		 	case "NOT":
		 		resAssert.setToNotType();
		 		break;
		 	default:
		 		resAssert.setToSubstringType();
		 		break;
		 }
		 //resAssert.setProperty(new IntegerProperty("Assertion.test_type", 2));//见schematic.xsl
		 if(!ra.getTestString().isEmpty()&&!JSONUtil.parseObj(ra.getTestString()).isEmpty()) {
			 JSONUtil.parseObj(ra.getTestString())
			 .entrySet()
			 .forEach(item->resAssert
					 .addTestString(item.getValue().toString()
					 )
			 ); 
		 }
		 resAssert.setCustomFailureMessage(ra.getCustomFailureMsg());
		 return resAssert;
	 }

	 public static JSONPathAssertion jsonPathAssertion(AssertJson jsAssert) {
		 
		 JSONPathAssertion jsonAssert=new JSONPathAssertion();
		 jsonAssert.setProperty(TestElement.GUI_CLASS,JSONPathAssertionGui.class.getName());
		 jsonAssert.setProperty(TestElement.TEST_CLASS,JSONPathAssertion.class.getName());
		 jsonAssert.setEnabled(true);
		 jsonAssert.setName("JSON断言");//名称
		 jsonAssert.setJsonPath(jsAssert.getJsonPath());		 
		 jsonAssert.setJsonValidationBool(jsAssert.isAddAssertValue());//默认true为false只校验json路径是否存在
		 jsonAssert.setIsRegex(true);		 
		 jsonAssert.setExpectedValue(jsAssert.getExpectedValue()); 
		 jsonAssert.setExpectNull(jsAssert.isExpectedNull());//默认false,期望值为null
		 jsonAssert.setInvert(jsAssert.isInvertAssert());	//默认false,校验结果反转
		 return jsonAssert;
	 }
	 public static BeanShellAssertion beanShellAssertion(String script) {
		 
		 BeanShellAssertion beanshellAssert=new BeanShellAssertion();
		 beanshellAssert.setProperty(TestElement.GUI_CLASS,BeanShellAssertionGui.class.getName());
		 beanshellAssert.setProperty(TestElement.TEST_CLASS,BeanShellAssertion.class.getName());
		 beanshellAssert.setEnabled(true);
		 beanshellAssert.setName("BeanShell Assertion");//名称
		 beanshellAssert.setProperty("BeanShellAssertion.resetInterpreter", false);
		 beanshellAssert.setProperty("BeanShellAssertion.parameters", "");
		 beanshellAssert.setProperty("BeanShellAssertion.filename", "");
		 beanshellAssert.setProperty("BeanShellAssertion.query", script);
		 return beanshellAssert;
	 }
}
