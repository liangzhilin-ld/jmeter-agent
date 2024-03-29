package com.autotest.jmeter.jmeteragent.testplan;

import com.autotest.data.mode.TestScheduled;
import com.autotest.jmeter.jmeteragent.common.TestDataServiceImpl;
import com.autotest.jmeter.jmeteragent.config.JmeterProperties;
import com.autotest.jmeter.jmeteragent.service.TestPlanService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.jorphan.collections.HashTree;
import java.net.URISyntaxException;
import java.util.List;

@Service
public class TestPlanServiceImpl implements TestPlanService{
	
	private @Autowired JmeterProperties jmeterProperties;
	private LoadDispatcher loadDispatcher;
	private @Autowired TestDataServiceImpl testData;
	private @Autowired TestPlanCreator testPlan;
	private static final Logger log = LoggerFactory.getLogger(TestPlanServiceImpl.class);
	@Override
	public void startTestPlan(TestScheduled trig) throws URISyntaxException {
		log.info("创建测试计划树");
		
//		TestPlanCreator testPlanCreator = new TestPlanCreator(jmeterProperties,jsonString);
		HashTree testPlanTree=testPlan.create(trig);
		log.info("执行测试");
		//loadDispatcher=new LoadDispatcher(jmeterProperties,testPlanCreator.getRequestParam().getTestRecordId());
		loadDispatcher=new LoadDispatcher(jmeterProperties,trig);
		loadDispatcher.startTestPlan(testPlanTree);
		
	}
	
	@Override
	
	public Boolean reTryTestPlan(TestScheduled trig) throws URISyntaxException{
		List<Object> failedList=testData.getTestcaseOfFail(trig.getHistoryId());
		if(failedList.size()==0)
			return false;
		HashTree testPlanTree=testPlan.reTryTest(trig,failedList);
		loadDispatcher=new LoadDispatcher(jmeterProperties,trig);
		loadDispatcher.startTestPlan(testPlanTree);
		return true;
	}
	
	@Override
	public <T> void debugTestCase(T api,String envStr,String testId) {
		log.info("创建测试计划树");
		
//		TestPlanCreator testPlanCreator = new TestPlanCreator(jmeterProperties,jsonString);
//		HashTree testPlanTree = testPlanCreator.create();
		HashTree testPlanTree=testPlan.createDebug(api,envStr,testId);
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
