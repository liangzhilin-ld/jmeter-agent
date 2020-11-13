package com.autotest.jmeter.jmeteragent.service.impl;


import com.autotest.data.mode.ApiTestcase;
import com.autotest.data.mode.ProjectManage;
import com.autotest.data.mode.UserDefinedVariable;
import com.autotest.jmeter.component.ConfigElement;
import com.autotest.jmeter.component.HTTPSampler;
import com.autotest.jmeter.component.ThreadGroups;
import com.autotest.jmeter.entity.theadgroup.TheadGroupEntity;
//import com.techstar.dmp.jmeteragent.bean.*;
import com.autotest.jmeter.jmeteragent.config.JmeterProperties;

import cn.hutool.core.util.StrUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.activation.ActivationGroup_Stub;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.assertions.JSONPathAssertion;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.TechstarHTTPSamplerProxy;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.extractor.BeanShellPostProcessor;
import org.apache.jmeter.modifiers.BeanShellPreProcessor;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.util.JMeterUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSON;


/**
 * @Date 2020/6/17 9:16
 * @Description 创建jmeter测试计划
 */
@Service
public class TestPlanCreator {

    private static final Logger log = LoggerFactory.getLogger(TestPlanCreator.class);
    private @Autowired TestDataServiceImpl testData;
    private @Autowired JmeterProperties jmeterProperties;	
	private int engineCount;
 
    private int getEngineCount() {
	    JMeterUtils.loadJMeterProperties(this.jmeterProperties.getHome() + "\\" + this.jmeterProperties.getPropertiesFileName());
	    this.engineCount = JMeterUtils.getProperty("remote_hosts").split(",").length;
	    return this.engineCount;
    }

    public HashTree create() {
        log.info("创建测试计划");
        TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
        ListedHashTree testPlanTree = new ListedHashTree(testPlan);
        
        //-------------------------------------------------------------------------------------------
        log.info("创建公共配置");
        testPlanTree.add(testPlan, getArguments("1"));
        testPlanTree.add(testPlan,ConfigElement.httpDefaultsGui());
        testPlanTree.add(testPlan,ConfigElement.createCookieManager());
        testPlanTree.add(testPlan,ConfigElement.createCacheManager());
        testPlanTree.add(testPlan,ConfigElement.createHeaderManager(testData.getTestPlanHeader(1)));
        //testPlanTree.add(testPlan,ConfigElement.jdbcDataSet());
        //testPlanTree.add(testPlan,ConfigElement.JdbcConnection());
        log.info("创建线程组");
        ThreadGroup threadGroup = ThreadGroups.create(ThreadGroups.apiTestTheadGroup());                
        ListedHashTree threadGroupHashTree = new ListedHashTree(threadGroup);
        log.info("添加登陆组件");
        threadGroupHashTree.add(threadGroup, HTTPSampler.loginControll());
        log.info("添加接口数据");
        List<ApiTestcase>  listcase=testData.getTestcase();
        Map<String, String> header=new HashMap();
        for (ApiTestcase api : listcase) {
        	TechstarHTTPSamplerProxy sampler=HTTPSampler.crtHTTPSampler(api,header);
        	ListedHashTree testApiTree = new ListedHashTree(sampler);
//        	if(api.getApiPre().equals("1")) {}
//        	if(api.getTcPost().equals("1")) {}
//        	if(api.getCheckPoint().equals("1")) {}
//        	if(!api.getTcVar().isEmpty()) {}
//        	if(api.getConfigElement().equals("1")) {}
//        	if(api.getPreCases().equals("1")) {}
//        	if(!api.getAttachment().isEmpty()) {}
//        	if(api.getApiIn().equals("query")) {}
        	threadGroupHashTree.add(threadGroup, testApiTree);
		}
        //----------------------------------------------------------------------------

       

        log.info("添加http请求头管理器");
 
        log.info("添加用户自定义变量");
        log.info("创建循环控制器");
        log.info("创建http请求收集器");
        log.info("创建http请求");
        log.info("创建预处理条件");
        log.info("设置断言"); 
        
        testPlanTree.add(testPlan, threadGroupHashTree);
        return testPlanTree;
    }

    /**
     * 创建用户自定义变量
     */
    public static Arguments createArguments() {
        Arguments arguments = new Arguments();
        Argument argument = new Argument("param", "");
//		Argument username = new Argument("username", Constants.VBI_NAME);
//		Argument password = new Argument("password", Constants.VBI_PWD);
        Argument token = new Argument("token", "");
        Argument deviceId = new Argument("deviceId", "");
        Argument vercode = new Argument("vercode", "");
        Argument id = new Argument("id", "");
        Argument smKey = new Argument("smKey", "e9664b2bebccb6fe80da086044608115");
        Argument publicKey = new Argument("publicKey", "");
        arguments.addArgument(argument);
//		arguments.addArgument(username);
//		arguments.addArgument(password);
        arguments.addArgument(token);
        arguments.addArgument(deviceId);
        arguments.addArgument(vercode);
        arguments.addArgument(id);
        arguments.addArgument(smKey);
        arguments.addArgument(publicKey);
        return arguments;
    }

    /**
     * 创建线程组
     *
     * @return
     */
    public ThreadGroup createThreadGroup() {
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Example Thread Group");
        threadGroup.setNumThreads(2);
        //计算所有线程启动完成时间，设置为线程持续时间的10%
        BigDecimal bd = new BigDecimal(10);
        bd = bd.multiply(new BigDecimal("0.1"));
        bd = bd.setScale(0, BigDecimal.ROUND_UP);
        //threadGroup.setRampUp(this.requestParam.getThreadNum()/this.requestParam.getThreadNumPS());
        threadGroup.setRampUp(bd.intValue());
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setScheduler(jmeterProperties.isScheduler());
        threadGroup.setDuration(20);
        threadGroup.setDelay(jmeterProperties.getDelay());
        return threadGroup;
    }

    /**
     * 创建循环控制器
     *
     * @return
     */
    public LoopController createLoopController() {
        // Loop Controller
        LoopController loopController = new LoopController();
        loopController.setLoops(jmeterProperties.getLoops());
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.initialize();
        return loopController;
    }

    /**
     * 创建http采样器
     *
     * @return
     */
    public TechstarHTTPSamplerProxy createHTTPSamplerProxy(HeaderManager headerManager) {
        TechstarHTTPSamplerProxy httpSamplerProxy = new TechstarHTTPSamplerProxy();
        httpSamplerProxy.setHeaderManager(headerManager);
        httpSamplerProxy.setName("a6a3a43de25b400f88c89b19d38a072a");
        //httpSamplerProxy.setDomain("172.16.206.128");
        //httpSamplerProxy.setPort(8888);
        httpSamplerProxy.setPath("/dcp-ec-ecJobsDicType-service/queryecJobsDicType");
        httpSamplerProxy.setMethod("POST");
        httpSamplerProxy.setConnectTimeout("3000");
        httpSamplerProxy.setUseKeepAlive(true);
        httpSamplerProxy.setProperty(TestElement.TEST_CLASS, TechstarHTTPSamplerProxy.class.getName());
        httpSamplerProxy.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSamplerProxy.setProtocol("http");
//        CacheManager cacheManager=new CacheManager();
//        httpSamplerProxy.setCacheManager(cacheManager);
        httpSamplerProxy.setEnabled(true);
        httpSamplerProxy.addNonEncodedArgument("", "${param}", "=");
        return httpSamplerProxy;
    }

    /**
     * 创建beanshell前置处理器
     */
    public static BeanShellPreProcessor createBeanShellPreProcessor() {
        BeanShellPreProcessor beanShellPreProcessor = new BeanShellPreProcessor();
        String script = String.format("import java.util.UUID;\r\n" +
                "\r\n" +
                "public String mock_uuid(){\r\n" +
                "	return UUID.randomUUID().toString().replaceAll(\"-\",\"\");\r\n" +
                "}\r\n" +
                "vars.put(\"param\", \"%s\");", "test");
        beanShellPreProcessor.setScript(script);
        beanShellPreProcessor.setProperty("script", script);
        return beanShellPreProcessor;
    }

    /**
     * 创建beanshell后置处理器
     */
    public static BeanShellPostProcessor createBeanShellPostProcessor() {
        BeanShellPostProcessor beanShellPostProcessor = new BeanShellPostProcessor();
        String s = "import java.io.*;\r\n" +
                "import redis.clients.jedis.Jedis;\r\n" +
                "\r\n" +
                "Jedis jedis = new Jedis(\"localhost\",6379);\r\n" +
                "String response=\"\";\r\n" +
                "String Str = \"{\\\"status\\\":200\";\r\n" +
                "response = prev.getResponseDataAsString();\r\n" +
                "if(response==\"\"){\r\n" +
                "	Failure = true;\r\n" +
                "	FailureMessage=\"系统无响应，获取不到响应数据！\";\r\n" +
                "	log.info(FailureMessage);\r\n" +
                "}else if(response.contains(Str)==false){\r\n" +
                "	Failure=true;\r\n" +
                "	String Msg =\"响应结果与期望不一致，请排查性能问题，还是程序代码问题\";\r\n" +
                "	FailureMessage = Msg + \"期望结果:\" + Str+\",\" + \"响应内容:\"+ response;\r\n" +
                "	jedis.zadd(\"errorLog\", 0, FailureMessage);\r\n" +
                "}";
        beanShellPostProcessor.setScript(s);
        beanShellPostProcessor.setProperty("script", s);
        return beanShellPostProcessor;
    }

    
	/**
	 * 用户自定义变量
	 * @return
	 */
	public Arguments getArguments(String projectID) {
   	 //自定义变量
	   ProjectManage pro=testData.getPoject(projectID);
	   List<String[]> definedVars = new ArrayList<>();
	   List<UserDefinedVariable> listDefine=testData.getUserDefinedVar();
	   for (UserDefinedVariable userDefine : listDefine) {
		   String[] record= {userDefine.getName(),userDefine.getValue()};//
		   definedVars.add(record);
	   }
	   String[] host= {"host",pro.getTestIp()};
	   String[] port= {"port",pro.getTestPort()};
	   definedVars.add(host);
       definedVars.add(port);
       //List<String[]> definedVars = new ArrayList<>();
//       String[] host= {"host","172.16.206.127"};//
//       String[] port= {"port","31100"};//8888
//       String[] userName= {"userName","xtgly"};//zhengyanlin
//       String[] pwd= {"pwd","a1234567"};//admin@123   a1234567
//       String[] sleep= {"sleep","5000"};
//       String[] sysid= {"sysid","266946423468851203"};
//       String[] smDecryptKey= {"smDecryptKey","e9664b2bebccb6fe80da086044608115"};
//       String[] users= {"users","${__P(users,1)}"};
//       definedVars.add(host);
//       definedVars.add(port);
//       definedVars.add(userName);
//       definedVars.add(pwd);
//       definedVars.add(sleep);
//       definedVars.add(sysid);
//       definedVars.add(smDecryptKey);
//       definedVars.add(users);
       Arguments value = ConfigElement.createArguments(definedVars);
       return value;
   }

}
