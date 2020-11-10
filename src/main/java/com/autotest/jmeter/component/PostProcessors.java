package com.autotest.jmeter.component;

import org.apache.jmeter.assertions.BeanShellAssertion;
import org.apache.jmeter.assertions.gui.BeanShellAssertionGui;
import org.apache.jmeter.extractor.BeanShellPostProcessor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.extractor.json.jsonpath.gui.JSONPostProcessorGui;
import org.apache.jmeter.protocol.jdbc.processor.JDBCPostProcessor;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import com.autotest.jmeter.entity.processors.JSONExtractor;

public class PostProcessors {
    public static JSONPostProcessor jsonPostProcessor(JSONExtractor jsonExtrator) {
    	//JMeterContext context = JMeterContextService.getContext();
    	JSONPostProcessor jsonPost=new JSONPostProcessor();
    	//jsonPost.setThreadContext(context);
    	jsonPost.setProperty(TestElement.GUI_CLASS,JSONPostProcessorGui.class.getName());
    	jsonPost.setProperty(TestElement.TEST_CLASS,JSONPostProcessor.class.getName());
    	jsonPost.setEnabled(true);
    	jsonPost.setName(jsonExtrator.getName());//名称
    	jsonPost.setRefNames(jsonExtrator.getVariableName());//引用名称
    	jsonPost.setJsonPathExpressions(jsonExtrator.getJsonPath());//表达式
    	jsonPost.setMatchNumbers(jsonExtrator.getMatchNo());//匹配
    	jsonPost.setDefaultValues(jsonExtrator.getDefaultValue());//默认值    
		switch (jsonExtrator.getApplyTo().get("scope")) {
		 	case "MAIN_AND_SUB":
		 		jsonPost.setScopeAll();
		 		break;
		 	case "SUB":
		 		jsonPost.setScopeChildren();
		 		break;
		 	case "variableName":
		 		jsonPost.setScopeVariable(jsonExtrator.getApplyTo().get("variableValue"));
		 		break;
		 	default:
		 		jsonPost.setScopeParent(); //Main sample only
		 		break;
		 }
    	//jsonPost.setScopeAll();// 对应提取器的applyTo设置
    	if(jsonExtrator.isSuffix_ALL())
    		jsonPost.setComputeConcatenation(true);    	
    	return jsonPost;
    }
    public static JDBCPostProcessor jdbcPostProcessor() {
    	JDBCPostProcessor jdbcPost=new JDBCPostProcessor();
    	jdbcPost.setProperty(TestElement.GUI_CLASS,TestBeanGUI.class.getName());
    	jdbcPost.setProperty(TestElement.TEST_CLASS,JDBCPostProcessor.class.getName());
    	jdbcPost.setEnabled(true);
    	jdbcPost.setName("JDBC 后置处理程序");//名称
    	jdbcPost.setDataSource("");//数据源连接名称
    	jdbcPost.setQueryType("Callable Statement");
    	jdbcPost.setQuery("");//sql语句，结尾不能带分号
    	
    	jdbcPost.setQueryArguments("");
    	jdbcPost.setQueryArgumentsTypes("");
     	jdbcPost.setVariableNames("");//变量名称，按列
    	jdbcPost.setResultVariable("");//结果存储变量
    	jdbcPost.setQueryTimeout("");
    	jdbcPost.setResultSetHandler("Store as String");
    	return jdbcPost;
    }
	 public static BeanShellPostProcessor beanShellPostProcessor(String script) {
		 
		 BeanShellPostProcessor beanshellPost=new BeanShellPostProcessor();
		 beanshellPost.setProperty(TestElement.GUI_CLASS,TestBeanGUI.class.getName());
		 beanshellPost.setProperty(TestElement.TEST_CLASS,BeanShellPostProcessor.class.getName());
		 beanshellPost.setEnabled(true);
		 beanshellPost.setName("BeanShell PostProcessor");//名称			 
		 beanshellPost.setProperty("resetInterpreter", false);
		 beanshellPost.setProperty("parameters", "");		 
		 beanshellPost.setProperty("filename", "");
		 beanshellPost.setProperty("script", script);
		 return beanshellPost;
	 }
    
}
