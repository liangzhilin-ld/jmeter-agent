package com.autotest.jmeter.jmeteragent.service.impl;


import com.autotest.data.mode.ApiMock;
import com.autotest.data.mode.ApiTestcase;
import com.autotest.data.mode.Beanshell;
import com.autotest.data.mode.ProcessorJdbc;
import com.autotest.data.mode.ProjectManage;
import com.autotest.data.mode.SyetemDb;
import com.autotest.data.mode.TestScheduled;
import com.autotest.data.mode.UserDefinedVariable;
import com.autotest.jmeter.component.ConfigElement;
import com.autotest.jmeter.component.HTTPSampler;
import com.autotest.jmeter.component.PostProcessors;
import com.autotest.jmeter.component.PreProcessors;
import com.autotest.jmeter.component.ThreadGroups;
//import com.autotest.jmeter.entity.theadgroup.TheadGroupEntity;
//import com.techstar.dmp.jmeteragent.bean.*;
import com.autotest.jmeter.jmeteragent.config.JmeterProperties;

import cn.hutool.core.collection.ListUtil;
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
import org.apache.jmeter.protocol.jdbc.processor.JDBCPreProcessor;
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
public class TestPlanCreator2 {

    private static final Logger log = LoggerFactory.getLogger(TestPlanCreator2.class);
    private @Autowired TestDataServiceImpl testData;
    private @Autowired JmeterProperties jmeterProperties;	
    private @Autowired JmeterHashTreeServiceImpl jmeterCompant;
    
	private int engineCount;
 
    private int getEngineCount() {
	    JMeterUtils.loadJMeterProperties(this.jmeterProperties.getHome() + "\\" + this.jmeterProperties.getPropertiesFileName());
	    this.engineCount = JMeterUtils.getProperty("remote_hosts").split(",").length;
	    return this.engineCount;
    }

    public HashTree create(TestScheduled trig) {
        log.info("创建测试计划");
        TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
        ListedHashTree testPlanTree = new ListedHashTree(testPlan);
        
        //-------------------------------------------------------------------------------------------
        log.info("创建公共配置");
        testPlanTree.add(testPlan, jmeterCompant.getPubArguments(trig));
        testPlanTree.add(testPlan,ConfigElement.httpDefaultsGui());
        testPlanTree.add(testPlan,ConfigElement.createCookieManager());
        testPlanTree.add(testPlan,ConfigElement.createCacheManager());
        testPlanTree.add(testPlan,ConfigElement.createHeaderManager(jmeterCompant.getPubHeader(trig)));
        testData.getSyetemDbAll().forEach(item->testPlanTree.add(testPlan,ConfigElement.JdbcConnection(item)));
        log.info("创建线程组");
        int threadNum=trig.getNumOfConcurrent();//并发数判断
        ThreadGroup threadGroup = ThreadGroups.create(ThreadGroups.apiTestTheadGroup());                
        ListedHashTree threadGroupHashTree = new ListedHashTree(threadGroup);
        log.info("添加登陆组件");
        threadGroupHashTree.add(threadGroup, HTTPSampler.loginControll());
        log.info("添加接口数据");
        List<ApiTestcase>  listcase=testData.getTestcaseByIds(trig);
        //listcase.forEach(item->jmeterCompant.addSamplers(threadGroupHashTree, threadGroup, item));
        for (ApiTestcase api : listcase) {
        	jmeterCompant.addSamplers(threadGroupHashTree, threadGroup, api);
        }
//        Map<String, String> header=new HashMap();
//        for (ApiTestcase api : listcase) {
//        	log.info("创建http sampler");
//        	TechstarHTTPSamplerProxy sampler=HTTPSampler.crtHTTPSampler(api,header);
//        	ListedHashTree testApiTree = new ListedHashTree(sampler);
//        	//header,用户自定义变量，前置，后置，断言查询并解析
//        	log.info("添加http请求头管理器");
//        	jmeterCompant.addHeader(testApiTree, sampler, api);  
//        	log.info("添加用户自定义变量");
//        	jmeterCompant.addArguments(testApiTree, sampler, api);
//        	log.info("创建前置处理");
//        	
//        	
//        	jmeterCompant.addPreProcessors(testApiTree, sampler, api);
//        	
//        	jmeterCompant.addMockSampler(threadGroupHashTree, threadGroup, api);
//        	log.info("创建后置处理");
//        	jmeterCompant.addPostProcessors(testApiTree, sampler, api);
//        	log.info("设置断言");
//        	jmeterCompant.addAssertions(testApiTree, sampler, api);
//        	
////        	if(!api.getAttachment().isEmpty()) {}
//        	threadGroupHashTree.add(threadGroup, testApiTree);
//		}
//        threadGroupHashTree.add(threadGroup,HTTPSampler.mockSampler(testData.getApiMock(1)));
        //----------------------------------------------------------------------------
        testPlanTree.add(testPlan, threadGroupHashTree);
        return testPlanTree;
    }
 
    /**
     * 前置处理器添加
     * @param threadGroupHashTree  线程组树
     * @param threadGroup  线程组对象
     * @param api  被测接口
     */
//    public void preProcessAdd(ListedHashTree threadGroupHashTree,ThreadGroup threadGroup,ApiTestcase api) {
//    	Map<String, String> header=new HashMap();
//    	TechstarHTTPSamplerProxy sampler=HTTPSampler.crtHTTPSampler(api,header);
//		ListedHashTree testApiTree = new ListedHashTree(sampler);
//		
//    	if(api.getApiPre().contains("1")) {
//    		ApiTestcase parent=testData.getTestcaseByID(api.getPreCases());
//    		if(parent.getApiPre().length()>0)
//    			preProcessAdd(threadGroupHashTree,threadGroup,parent); 
//    		Map<String, String> headers=new HashMap();
//    		TechstarHTTPSamplerProxy preSampler=HTTPSampler.crtHTTPSampler(parent,headers);
//    		ListedHashTree parentTree = new ListedHashTree(preSampler);
//    		threadGroupHashTree.add(threadGroup, parentTree);
//    	}
//    	if(api.getApiPre().contains("2")) {
//    		Beanshell shell=testData.getPreBeanshell(api.getCaseId());
//    		BeanShellPreProcessor prebeanshell=PreProcessors.beanShellPreProcessor(shell.getScript());    		
//    		testApiTree.add(sampler,prebeanshell);
//    	}
//    	if(api.getApiPre().contains("3")) {
//    		ProcessorJdbc prejdbc=testData.getProcessorJdbc(api.getCaseId(),"1");
//    		JDBCPreProcessor prejdbcpro=PreProcessors.jdbcPreProcessor(prejdbc);
//    		testApiTree.add(sampler,prejdbcpro);  
//    	}
//    	if(api.getApiPre().contains("4")) {
//    		ApiMock dummy=testData.getApiMock(api.getCaseId());
//    		ListedHashTree mockTree=HTTPSampler.mockSampler(dummy);
//    		threadGroupHashTree.add(threadGroup, mockTree);
//    	}
//		threadGroupHashTree.add(threadGroup, testApiTree);
//    }
  

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
	   List<UserDefinedVariable> listDefine=testData.getUserDefinedVar(1);
	   for (UserDefinedVariable userDefine : listDefine) {
		   String[] record= {userDefine.getName(),userDefine.getValue()};//
		   definedVars.add(record);
	   }
       Arguments value = ConfigElement.createArguments(definedVars);
       return value;
   }

}
