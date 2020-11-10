package com.autotest.jmeter.jmeteragent.service;

import java.net.URISyntaxException;

public interface TestPlanService {
	//启动测试计划
	public void startTestPlan(String jsonString) throws URISyntaxException;
	
	//停止测试计划
	public void stopTestPlan();
	
//	//通知开始执行
//	public void noticeStart();
//	
//	//通知执行完成并返回测试结果
//	public void noticeFinish();
}
