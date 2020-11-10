package com.autotest.jmeter.component;

import org.apache.jmeter.extractor.BeanShellPostProcessor;
import org.apache.jmeter.modifiers.BeanShellPreProcessor;
import org.apache.jmeter.protocol.jdbc.processor.JDBCPreProcessor;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;

public class PreProcessors {

	   public static JDBCPreProcessor jdbcPostProcessor() {
		   JDBCPreProcessor jdbcPre=new JDBCPreProcessor();
		   jdbcPre.setProperty(TestElement.GUI_CLASS,TestBeanGUI.class.getName());
		   jdbcPre.setProperty(TestElement.TEST_CLASS,JDBCPreProcessor.class.getName());
		   jdbcPre.setEnabled(true);
		   jdbcPre.setName("JDBC 预处理程序");//名称
		   jdbcPre.setDataSource("");//数据源连接名称
		   jdbcPre.setQueryType("Callable Statement");
		   jdbcPre.setQuery("");//sql语句，结尾不能带分号
	    	
		   jdbcPre.setQueryArguments("");
		   jdbcPre.setQueryArgumentsTypes("");
		   jdbcPre.setVariableNames("");//变量名称，按列
		   jdbcPre.setResultVariable("");//结果存储变量
		   jdbcPre.setQueryTimeout("");
		   jdbcPre.setResultSetHandler("Store as String");
		   return jdbcPre;
	    }
		 
		 public static BeanShellPreProcessor beanShellPreProcessor(String script) {
		     BeanShellPreProcessor beanShellPreProcessor = new BeanShellPreProcessor();
		     beanShellPreProcessor.setName("BeanShell PreProcessor");
		     beanShellPreProcessor.setProperty("resetInterpreter", false);
		     beanShellPreProcessor.setProperty("parameters", "");		 
		     beanShellPreProcessor.setProperty("filename", "");
		     beanShellPreProcessor.setProperty("script", script);
		     return beanShellPreProcessor;
		 }
}
