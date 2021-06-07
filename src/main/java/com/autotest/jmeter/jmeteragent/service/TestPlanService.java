package com.autotest.jmeter.jmeteragent.service;

import java.net.URISyntaxException;

import com.autotest.data.mode.HttpTestcase;
import com.autotest.data.mode.TestScheduled;

public interface TestPlanService {
	//启动测试计划
	public void startTestPlan(TestScheduled trig) throws URISyntaxException;
	
	public Boolean reTryTestPlan(TestScheduled trig) throws URISyntaxException;
	public <T> void debugTestCase(T api,String envId,String testId);
	//停止测试计划
	public void stopTestPlan();
	
//	//通知开始执行
//	public void noticeStart();
//	
//	//通知执行完成并返回测试结果
//	public void noticeFinish();
}
