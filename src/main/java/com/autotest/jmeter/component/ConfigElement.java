package com.autotest.jmeter.component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.JdbcDataSet;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.config.gui.HttpDefaultsGui;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.gui.CacheManagerGui;
import org.apache.jmeter.protocol.http.gui.CookiePanel;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.jdbc.config.DataSourceElement;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.collections.SearchByClass;

public class ConfigElement {

    /**
     * http默认请求
     *
     * @return
     */
    public static ConfigTestElement httpDefaultsGui() {
    	
    	 ConfigTestElement cong=new ConfigTestElement();
    	 cong.setProperty(TestElement.TEST_CLASS,  ConfigTestElement.class.getName());
         cong.setProperty(TestElement.GUI_CLASS,  HttpDefaultsGui.class.getName());//"org.apache.jmeter.protocol.http.config.gui.HttpDefaultsGui"
         cong.setProperty(TestElement.NAME, "http默认请求");
         cong.setProperty(TestElement.ENABLED, true);
         cong.setProperty(HTTPSampler.DOMAIN, "${host}");//69  172.16.206.128
         cong.setProperty(HTTPSampler.PORT, "${port}");//6600   8888
         cong.setProperty(HTTPSampler.PROTOCOL, "http");
         cong.setProperty(HTTPSampler.CONTENT_ENCODING, "UTF-8");
         cong.setProperty(HTTPSampler.CONCURRENT_POOL, "6");
         cong.setProperty(HTTPSampler.PATH, "");
         cong.setProperty(HTTPSampler.CONNECT_TIMEOUT, "");
         cong.setProperty(HTTPSampler.RESPONSE_TIMEOUT, "");
         return cong;
    }
	  /**
     * http heder manager
     *
     * @return
     */
    public static HeaderManager createHeaderManager(Map<String, String> map) {
    	HeaderManager headerManager = new HeaderManager();
        headerManager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
        headerManager.setProperty(TestElement.NAME, "HTTP HeaderManager");
        headerManager.setProperty(TestElement.COMMENTS, "Created from cURL on "+LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        boolean hasAcceptEncoding = false;
        for (Map.Entry<String, String> header : map.entrySet()) {
            String key = header.getKey();
            hasAcceptEncoding = hasAcceptEncoding || key.equalsIgnoreCase("Accept-Encoding");
            headerManager.getHeaders().addItem(new Header(key, header.getValue()));
        }
//        if(!hasAcceptEncoding && request.isCompressed()) {
//            headerManager.getHeaders().addItem(new Header("Accept-Encoding", "gzip, deflate"));
//        }
        return headerManager;
    }
    
	  /**
     * User Defined Variables
     *
     * @return
     */
    public static Arguments createArguments(List<String[]> definedVars) {
    	Arguments args = new Arguments();
    	args.setEnabled(true);
    	args.setName("用户定义的变量");
    	args.setProperty(TestElement.GUI_CLASS,ArgumentsPanel.class.getName());
    	args.setProperty(TestElement.TEST_CLASS,Arguments.class.getName());
    	for (String[] clipboardCols : definedVars) {
    		Argument arg2=new Argument();
    		arg2.setMetaData("=");
        	arg2.setName(clipboardCols[0]);
        	   if (clipboardCols.length > 1) {
        		   arg2.setValue(clipboardCols[1]);
                   if (clipboardCols.length > 2) {
                	   arg2.setDescription(clipboardCols[2]);
                   }
               }        	      
        	args.addArgument(arg2);
		}  
//    	args.addArgument("arg1", "val1");
//    	args.addArgument("arg2", "val1");    	
//        TestElementProperty prop = new TestElementProperty("args", args);
//         ConfigTestElement te = new ConfigTestElement();
//         te.addProperty(prop);
//         te.setRunningVersion(true);
       
        return args;
    }
    
	  /**
     * HTTP缓存管理器
     *
     * @return
     */
    public static CacheManager createCacheManager() {
    	CacheManager cacheManager=new CacheManager();
    	cacheManager.setName("HTTP缓存管理器");
    	cacheManager.setClearEachIteration(false);
     	cacheManager.setControlledByThread(false);
    	cacheManager.setUseExpires(true);     
    	cacheManager.setMaxSize(5000);	
    	cacheManager.setEnabled(true);
    	cacheManager.setProperty(TestElement.GUI_CLASS,CacheManagerGui.class.getName());
    	cacheManager.setProperty(TestElement.TEST_CLASS,CacheManager.class.getName());
    	return cacheManager;
    }
	  /**
     * HTTP Cookie管理器
     * @return
     * 
     */
    public static CookieManager createCookieManager() {
    	CookieManager coolieManager=new CookieManager();
    	coolieManager.setName("HTTP Cookie管理器");
    	coolieManager.setClearEachIteration(false);
    	coolieManager.setControlledByThread(false);
    	coolieManager.setCookiePolicy("standard");
    	coolieManager.setEnabled(true);
    	coolieManager.setProperty(new CollectionProperty("CookieManager.cookies", new ArrayList<>()));
    	coolieManager.setProperty(TestElement.GUI_CLASS,CookiePanel.class.getName());
    	coolieManager.setProperty(TestElement.TEST_CLASS,CookieManager.class.getName());   	
    	return coolieManager;
    }
	  /**
     * JDBC Connection管理器
     * @return
     * 
     */
    public static DataSourceElement JdbcConnection() {
    	DataSourceElement jdbcConn=new DataSourceElement();    	
    	jdbcConn.setName("JDBC Connection Configuration");
    	jdbcConn.setEnabled(true);
    	jdbcConn.setProperty(TestElement.GUI_CLASS,TestBeanGUI.class.getName());
    	jdbcConn.setProperty(TestElement.TEST_CLASS,DataSourceElement.class.getName()); 
    	jdbcConn.setDataSource("mysql");//连接池变量名称
    	jdbcConn.setPoolMax("0");
    	jdbcConn.setTimeout("10000");
    	jdbcConn.setTrimInterval("60000");
    	jdbcConn.setAutocommit(true);
    	jdbcConn.setTransactionIsolation("DEFAULT");
    	jdbcConn.setInitQuery("");
    	jdbcConn.setKeepAlive(true);
    	jdbcConn.setConnectionAge("5000");
    	jdbcConn.setCheckQuery("");
    	jdbcConn.setDbUrl("jdbc:mysql://172.16.206.82:3306/autoplat?useSSL=FALSE&serverTimezone=UTC"); //数据库地址
    	jdbcConn.setDriver("com.mysql.jdbc.Driver");//驱动类路径
    	jdbcConn.setUsername("vbi");  // 用户名 	
    	jdbcConn.setPassword("123456");//密码
    	JMeterContextService.startTest();
    	SearchByClass<TestStateListener> testListeners = new SearchByClass<>(TestStateListener.class);
    	
    	//notifyTestListenersOfStart(testListeners);
    	TestBeanHelper.prepare(jdbcConn);
    	return jdbcConn;
    }



	  /**
     * JDBC Data Set
     * @return
     * 
     */
    public static JdbcDataSet jdbcDataSet() {
    	JdbcDataSet jds=new JdbcDataSet();
    	jds.setName("JDBC Data Set Config");
    	jds.setEnabled(true);
    	jds.setProperty(TestElement.GUI_CLASS,TestBeanGUI.class.getName());
    	jds.setProperty(TestElement.TEST_CLASS,JdbcDataSet.class.getName());
    	jds.setFilename("select * from api_swagger");//select sid,sname,sage,ssex from student
    	jds.setRecycle(true);//是否循环
    	jds.setShareMode("shareMode.all");
    	jds.setVariableNames("");
    	jds.setStopThread(false);//当需要取值完成并结束执行时setRecycle设置false,setStopThread设为true
    	jds.setDbUrl("jdbc:mysql://172.16.206.82:3306/autoplat?useSSL=FALSE&serverTimezone=UTC");//vbi_api
    	jds.setDriver("com.mysql.jdbc.Driver");
    	jds.setUsername("vbi");
    	jds.setPassword("123456");
    	return jds;
    }



}
