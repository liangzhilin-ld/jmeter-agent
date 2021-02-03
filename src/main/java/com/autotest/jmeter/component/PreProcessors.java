package com.autotest.jmeter.component;

import org.apache.jmeter.modifiers.BeanShellPreProcessor;
import org.apache.jmeter.protocol.jdbc.processor.JDBCPreProcessor;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import com.autotest.data.mode.processors.JdbcProcessor;

public class PreProcessors {

	   public static JDBCPreProcessor jdbcPreProcessor(JdbcProcessor preJdbc) {
		   JDBCPreProcessor jdbcPre=new JDBCPreProcessor();
		   jdbcPre.setProperty(TestElement.GUI_CLASS,TestBeanGUI.class.getName());
		   jdbcPre.setProperty(TestElement.TEST_CLASS,JDBCPreProcessor.class.getName());
		   jdbcPre.setEnabled(true);
		   jdbcPre.setName("JDBC 预处理程序");//名称		   
		   jdbcPre.setProperty("dataSource",preJdbc.getVariableNamePool());//数据源连接名称
		   jdbcPre.setProperty("queryType","Callable Statement");
		   jdbcPre.setProperty("query",preJdbc.getQuery());//sql语句，结尾不能带分号    	
		   jdbcPre.setProperty("queryArguments","");
		   jdbcPre.setProperty("queryArgumentsTypes","");
		   jdbcPre.setProperty("variableNames","");//变量名称，按列
		   jdbcPre.setProperty("resultVariable","");//结果存储变量
		   jdbcPre.setProperty("queryTimeout","");
		   jdbcPre.setProperty("resultSetHandler","Store as String");
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
