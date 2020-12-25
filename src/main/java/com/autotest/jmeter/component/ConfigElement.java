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

import com.autotest.data.mode.ApiHeader;
import com.autotest.data.mode.ProjectManage;
import com.autotest.data.mode.SyetemDb;
import com.autotest.data.mode.UserDefinedVariable;

import cn.hutool.core.util.StrUtil;

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
    public static HeaderManager createHeaderManager(List<ApiHeader> headers) {
    	HeaderManager headerManager = new HeaderManager();
        headerManager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
        headerManager.setProperty(TestElement.NAME, "HTTP HeaderManager");
        headerManager.setProperty(TestElement.COMMENTS, "Created from cURL on "+LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        boolean hasAcceptEncoding = false;
        for (ApiHeader apiHeader : headers) {
        	hasAcceptEncoding = hasAcceptEncoding || apiHeader.getKey().equalsIgnoreCase("Accept-Encoding");
        	headerManager.getHeaders().addItem(new Header(apiHeader.getKey(), apiHeader.getValue()));
		}
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
    public static Arguments createArguments2(List<UserDefinedVariable> listUserdv) {
    	List<String[]> definedVars = new ArrayList<>();
    	for (UserDefinedVariable userDefine : listUserdv) {
 		   String[] record= {userDefine.getName(),userDefine.getValue(),userDefine.getDesc()};
 		   definedVars.add(record);
 	   	}
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
     * @param dbInfo   数据库连接信息
     * @return
     */
    public static DataSourceElement JdbcConnection(SyetemDb dbInfo) {    	
    	DataSourceElement jdbcConn=new DataSourceElement();    	
    	jdbcConn.setName("JDBC Connection Configuration");    	
    	jdbcConn.setEnabled(true);
    	jdbcConn.setProperty(TestElement.GUI_CLASS,TestBeanGUI.class.getName());
    	jdbcConn.setProperty(TestElement.TEST_CLASS,DataSourceElement.class.getName()); 
    	jdbcConn.setComment("");
    	jdbcConn.setProperty("dataSource",dbInfo.getCnnName());
    	jdbcConn.setProperty("poolMax","0");
    	jdbcConn.setProperty("timeout","10000");
    	jdbcConn.setProperty("trimInterval","60000");    	
    	jdbcConn.setProperty("autocommit",true); 
    	jdbcConn.setProperty("transactionIsolation","DEFAULT"); 
    	jdbcConn.setProperty("initQuery","");     	
    	jdbcConn.setProperty("keepAlive",true);     	
    	jdbcConn.setProperty("connectionAge",5000);
    	jdbcConn.setProperty("checkQuery","");	
    	jdbcConn.setProperty("dbUrl",StrUtil.format(
    			"jdbc:mysql://{}:{}/{}?useSSL=FALSE&serverTimezone=UTC",
    			dbInfo.getCnnHost(),dbInfo.getCnnPort(),dbInfo.getDbName()));
    	jdbcConn.setProperty("driver",dbInfo.getDriverClass());//"com.mysql.cj.jdbc.Driver"
    	jdbcConn.setProperty("username",dbInfo.getUsername());
    	jdbcConn.setProperty("password",dbInfo.getPassword());
    	jdbcConn.setProperty("preinit",false);
    	jdbcConn.setProperty("connectionProperties","");
    	return jdbcConn;
    }

    /**
     * JDBC Data Set
     * @param dbInfo    数据库连接信息
     * @param selectCmmd   sql查询语句
     * @param variableName  定义后续被引用的变量名称，多个变量用,(逗号)隔开
     * @return
     */
    public static JdbcDataSet jdbcDataSet(SyetemDb dbInfo,String selectCmmd,String variableName) {
    	JdbcDataSet jds=new JdbcDataSet();
    	jds.setName("JDBC Data Set Config");
    	jds.setEnabled(true);
    	jds.setProperty(TestElement.GUI_CLASS,TestBeanGUI.class.getName());
    	jds.setProperty(TestElement.TEST_CLASS,JdbcDataSet.class.getName());
    	
    	jds.setProperty("filename",selectCmmd);//select * from api_swagger
    	jds.setProperty("recycle",false);//是否循环
    	jds.setProperty("shareMode","shareMode.all");
    	jds.setProperty("variableNames",variableName);
    	jds.setProperty("stopThread",true);//当需要取值完成并结束执行时setRecycle设置false,setStopThread设为true
//    	jds.setProperty("dbUrl","jdbc:mysql://172.16.206.82:3306/autoplat?useSSL=FALSE&serverTimezone=UTC");
//    	jds.setProperty("driver","com.mysql.cj.jdbc.Driver");//com.mysql.jdbc.Driver
//    	jds.setProperty("username","vbi");
//    	jds.setProperty("password","123456");   	
    	jds.setProperty("dbUrl",StrUtil.format(
    			"jdbc:mysql://{}:{}/{}?useSSL=FALSE&serverTimezone=UTC",
    			dbInfo.getCnnHost(),dbInfo.getCnnPort(),dbInfo.getDbName()));
    	jds.setProperty("driver",dbInfo.getDriverClass());//"com.mysql.cj.jdbc.Driver"
    	jds.setProperty("username",dbInfo.getUsername());
    	jds.setProperty("password",dbInfo.getPassword());
    	return jds;
    }



}
