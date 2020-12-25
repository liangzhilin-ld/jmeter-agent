package com.autotest.jmeter.jmeteragent.service.impl;

import com.autotest.data.mode.TestScheduled;
import com.autotest.jmeter.jmeteragent.config.JmeterProperties;
//import com.autotest.jmeter.jmeteragent.config.TestPlanCreator;
import com.autotest.jmeter.jmeteragent.service.TestPlanService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.apache.jorphan.collections.HashTree;

import java.net.URISyntaxException;

@Service
public class TestPlanServiceImpl implements TestPlanService{
	@Autowired
	private JmeterProperties jmeterProperties;
	private LoadDispatcher loadDispatcher;
	private @Autowired TestPlanCreator2 testPlan;
	private static final Logger log = LoggerFactory.getLogger(TestPlanServiceImpl.class);
	@Override
	public void startTestPlan(TestScheduled trig) throws URISyntaxException {
		log.info("创建测试计划树");
		
//		TestPlanCreator testPlanCreator = new TestPlanCreator(jmeterProperties,jsonString);
//		HashTree testPlanTree = testPlanCreator.create();
		HashTree testPlanTree=testPlan.create(trig);
		log.info("执行测试");
		//loadDispatcher=new LoadDispatcher(jmeterProperties,testPlanCreator.getRequestParam().getTestRecordId());
		loadDispatcher=new LoadDispatcher(jmeterProperties);
		loadDispatcher.startTestPlan(testPlanTree);
	}
	
	@Override
	public void stopTestPlan() {
		log.info("停止测试");
		loadDispatcher.stopTestPlan();
	}
}
