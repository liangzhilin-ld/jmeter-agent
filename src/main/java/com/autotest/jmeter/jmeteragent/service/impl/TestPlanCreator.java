package com.autotest.jmeter.jmeteragent.service.impl;


import com.autotest.data.mode.HttpTestcase;
import com.autotest.data.mode.ScenarioTestcase;
import com.autotest.data.mode.TestScheduled;
import com.autotest.data.mode.custom.SamplerLable;
import com.autotest.jmeter.component.ConfigElement;
import com.autotest.jmeter.component.HTTPSampler;
import com.autotest.jmeter.component.Listener;
import com.autotest.jmeter.component.LogicController;
import com.autotest.jmeter.component.ThreadGroups;
//import com.autotest.jmeter.entity.theadgroup.TheadGroupEntity;
//import com.techstar.dmp.jmeteragent.bean.*;
import com.autotest.jmeter.jmeteragent.config.JmeterProperties;

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
import org.apache.jmeter.control.TransactionController;
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
import org.apache.jmeter.testelement.property.TestElementProperty;
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
        testPlanTree.add(testPlan,Listener.backendListener(trig.getId(),trig.getHistoryId(), "debug"));
        log.info("创建线程组");
        int threadNum=trig.getNumOfConcurrent();//并发数判断
   
        List<Object> collects=new ArrayList<Object>();
		List<ScenarioTestcase>  scens=testData.getScenarios(trig);
		List<HttpTestcase>  listcase=testData.getTestcaseByIds(trig);
		if(scens.size()>0)collects.addAll(scens);
		if(listcase.size()>0)collects.addAll(listcase);
        //将用例按并发数threadNum平均分配到各线程组
        if(threadNum>collects.size()) {
        	testPlanTree.add(testPlan, createThreadGroup(collects));
        	return testPlanTree;
        }
        for (int i = 0; i < collects.size()/threadNum; i++) {
        	List<Object> subList=collects.subList(i*threadNum, (i+1)*threadNum);
            testPlanTree.add(testPlan,  createThreadGroup(subList));            
		}
        if(collects.size() % threadNum != 0) {
        	List<Object> subList=collects.subList((collects.size() / threadNum) * threadNum, collects.size());
        	testPlanTree.add(testPlan,  createThreadGroup(subList));
        }
        return testPlanTree;
    }
    /**
     * 测试计划创建
     * @param subList
     * @return
     */
    
//    public ListedHashTree createThreadGroup(List<HttpTestcase> subList) {
//    	ThreadGroup threadGroup = ThreadGroups.create(ThreadGroups.apiTestTheadGroup());                
//        ListedHashTree threadGroupHashTree = new ListedHashTree(threadGroup);
//        log.info("添加登陆组件");
//        threadGroupHashTree.add(threadGroup, HTTPSampler.loginControll());
//        subList.forEach(item->jmeterCompant.addSamplers(threadGroupHashTree, threadGroup, item));
//        return threadGroupHashTree;
//    }
    
    
    
    
    public ListedHashTree createThreadGroup(List<Object> subList) {
    	ThreadGroup threadGroup = ThreadGroups.create(ThreadGroups.apiTestTheadGroup());                
        ListedHashTree threadGroupHashTree = new ListedHashTree(threadGroup);
        Boolean flag=subList.get(0) instanceof ScenarioTestcase;
        Integer pid=flag?((ScenarioTestcase)subList.get(0)).getProjectId()
        				:((HttpTestcase)subList.get(0)).getProjectId();
        ScenarioTestcase loginsc=testData.getLoginScenarios(pid);
        threadGroupHashTree.add(threadGroup, creatTransactionControllerTree(loginsc));
//        threadGroupHashTree.add(threadGroup, HTTPSampler.loginControll());
        for (Object object : subList) {
        	if(object instanceof ScenarioTestcase) {
        		ScenarioTestcase stc=(ScenarioTestcase) object;
        		ListedHashTree tcControllerTree=creatTransactionControllerTree(stc);
    			threadGroupHashTree.add(threadGroup, tcControllerTree);
    			continue;
	        }
	        if(object instanceof HttpTestcase) {
	        	HttpTestcase tc=(HttpTestcase) object;
	        	jmeterCompant.addSamplers(threadGroupHashTree, threadGroup, tc);
	        	
	        }
		}
        return threadGroupHashTree;
    }
    public ListedHashTree creatTransactionControllerTree(ScenarioTestcase tc) {
    	SamplerLable transLable=new SamplerLable();
    	transLable.setCaseId(tc.getId().toString());
    	transLable.setSuiteId(tc.getSuiteId().toString());
    	transLable.setCaseName(tc.getScenarioName());
    	if(tc.getTag().equals("登陆"))transLable.setIsLogin(true);
    	
    	TransactionController tsController=LogicController.transactionController(JSON.toJSONString(transLable));
		ListedHashTree tcControllerTree=new ListedHashTree(tsController); 
		ArrayList<JSONObject> listObj=tc.getHashtree();
		if(listObj.size()==0)return tcControllerTree;
		for (JSONObject json : listObj) {
			String type=json.getString("type");
			if(type.contains(ScenarioTestcase.TYPE_HTTP_SAMPLER)) {
				HttpTestcase htc=testData.getTestcaseByID(json.getInteger("id"));
				
				jmeterCompant.addSamplers(tcControllerTree, tsController, htc);
				continue;
			}
			if(type.contains(ScenarioTestcase.TYPE_LOGIN_CONTROLLER)) {
				continue;
			}
			if(type.contains(ScenarioTestcase.TYPE_SCENARIO)) {
				ScenarioTestcase stc=testData.getScenariosByid(json.getInteger("id"));
				tcControllerTree.add(tsController,creatTransactionControllerTree(stc));
			}
		}
		return tcControllerTree;
    }
    
    
    /**
     * 失败重试
     * @param trig
     * @param failedList
     * @return
     */
    public HashTree reTryTest(TestScheduled trig,List<HttpTestcase> failedList) {
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
        List<Object> cases =new ArrayList<Object>();
        cases.addAll(failedList);
        testPlanTree.add(testPlan,  createThreadGroup(cases));
        return testPlanTree;
    }
     
  
	/**
	 * 用例调试HttpTestcase
	 * @param <T>
	 * @param api
	 * @param envId
	 * @return
	 */
	public <T> HashTree createDebug(T api,int envId) {
    	TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
        ListedHashTree testPlanTree = new ListedHashTree(testPlan);
        log.info("创建公共配置");
        int projectId=(api instanceof HttpTestcase)
        				?((HttpTestcase)api).getProjectId()
        					:((ScenarioTestcase)api).getProjectId();
        testPlanTree.add(testPlan, jmeterCompant.getPubArgumentsOfDebug(projectId,envId));
        testPlanTree.add(testPlan,ConfigElement.httpDefaultsGui());
        testPlanTree.add(testPlan,ConfigElement.createCookieManager());
        testPlanTree.add(testPlan,ConfigElement.createCacheManager());
        testPlanTree.add(testPlan,ConfigElement.createHeaderManager(testData.getPubHeader(projectId)));
        testData.getSyetemDbAll().forEach(item->testPlanTree.add(testPlan,ConfigElement.JdbcConnection(item)));
        List<Object> cases =new ArrayList<Object>();
        cases.add(api);
        testPlanTree.add(testPlan,  createThreadGroup(cases));
        return testPlanTree;
    }
    /**
     * 创建线程组
     *
     * @return
     */
//    public ThreadGroup threadGroupTree() {
//        ThreadGroup threadGroup = new ThreadGroup();
//        threadGroup.setName("Example Thread Group");
//        threadGroup.setNumThreads(2);
//        //计算所有线程启动完成时间，设置为线程持续时间的10%
//        BigDecimal bd = new BigDecimal(10);
//        bd = bd.multiply(new BigDecimal("0.1"));
//        bd = bd.setScale(0, BigDecimal.ROUND_UP);
//        //threadGroup.setRampUp(this.requestParam.getThreadNum()/this.requestParam.getThreadNumPS());
//        threadGroup.setRampUp(bd.intValue());
//        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
//        threadGroup.setScheduler(jmeterProperties.isScheduler());
//        threadGroup.setDuration(20);
//        threadGroup.setDelay(jmeterProperties.getDelay());
//        return threadGroup;
//    }

 
	

}
