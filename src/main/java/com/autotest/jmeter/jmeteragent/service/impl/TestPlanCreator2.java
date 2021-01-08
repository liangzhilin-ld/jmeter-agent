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
        List<ApiTestcase>  listcase=testData.getTestcaseByIds(trig);
        if(threadNum>listcase.size()) {
        	testPlanTree.add(testPlan, createThreadGroup(listcase));
        	return testPlanTree;
        }
        for (int i = 0; i < listcase.size()/threadNum; i++) {
//        	ThreadGroup threadGroup = ThreadGroups.create(ThreadGroups.apiTestTheadGroup());                
//            ListedHashTree threadGroupHashTree = new ListedHashTree(threadGroup);
//            log.info("添加登陆组件");
//            threadGroupHashTree.add(threadGroup, HTTPSampler.loginControll());
//            log.info("添加接口数据");
            List<ApiTestcase> subList=listcase.subList(i*threadNum, (i+1)*threadNum);
//            subList.forEach(item->jmeterCompant.addSamplers(threadGroupHashTree, threadGroup, item));
//            testPlanTree.add(testPlan, threadGroupHashTree);
            testPlanTree.add(testPlan,  createThreadGroup(subList));
            
		}
        if(listcase.size() % threadNum != 0) {
        	List<ApiTestcase> subList=listcase.subList((listcase.size() / threadNum) * threadNum, listcase.size());
        	testPlanTree.add(testPlan,  createThreadGroup(subList));
        }
        return testPlanTree;
    }

    
    public ListedHashTree createThreadGroup(List<ApiTestcase> subList) {
    	ThreadGroup threadGroup = ThreadGroups.create(ThreadGroups.apiTestTheadGroup());                
        ListedHashTree threadGroupHashTree = new ListedHashTree(threadGroup);
        log.info("添加登陆组件");
        threadGroupHashTree.add(threadGroup, HTTPSampler.loginControll());
        subList.forEach(item->jmeterCompant.addSamplers(threadGroupHashTree, threadGroup, item));
        return threadGroupHashTree;
    }
    
    
    
    
    public HashTree reTryTest(TestScheduled trig,List<ApiTestcase> failedList) {
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
        testPlanTree.add(testPlan,  createThreadGroup(failedList));
        return testPlanTree;
    }
    
    
    /**
     * 创建线程组
     *
     * @return
     */
    public ThreadGroup threadGroupTree() {
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

 
	

}