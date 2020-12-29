package com.autotest.jmeter.jmeteragent.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.TechstarHTTPSamplerProxy;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.ListedHashTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.autotest.data.mode.*;
import com.autotest.data.service.impl.ApiHeaderServiceImpl;
import com.autotest.data.service.impl.UserDefinedVariableServiceImpl;
import com.autotest.jmeter.component.Assertions;
import com.autotest.jmeter.component.ConfigElement;
import com.autotest.jmeter.component.HTTPSampler;
import com.autotest.jmeter.component.PostProcessors;
import com.autotest.jmeter.component.PreProcessors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.hutool.core.util.StrUtil;
import lombok.extern.apachecommons.CommonsLog;

/**
 * 测试用例数据转换，组装
 * @author Techstar
 *
 */
@Service
@CommonsLog
public class JmeterHashTreeServiceImpl {
	private @Autowired TestDataServiceImpl testDadaService;
	private @Autowired UserDefinedVariableServiceImpl userDefinedVar;
	private @Autowired ApiHeaderServiceImpl apiHeader;
	private @Autowired JmeterHashTreeServiceImpl jmeterCompant;
	private Map<String, String> header=new HashMap();
	
//	private void createConfig(TestScheduled trig) {
//		trig.getNumOfConcurrent();
//		String[] ids=trig.getTcCaseids().split(",");
//		for (String caseId : ids) {
//			ApiTestcase testcase = testDadaService.getTestcaseByID(caseId);
//			List<UserDefinedVariable> udvs=testDadaService.getUserDefinedVar(testcase.getProjectId());
//			List<UserDefinedVariable> pubUserdvs=udvs.stream()
//					.filter(u -> u.getCaseId() == null)
//					.collect(Collectors.toList());
//			List<UserDefinedVariable> caseUserdvs=udvs.stream()
//					.filter(u -> u.getCaseId() == testcase.getCaseId())
//					.collect(Collectors.toList());
//		}
//	}
	
	
	/**
	 * 获取测试套件配置的公共参数
	 * @param trig
	 * @return
	 */
	public Arguments getPubArguments(TestScheduled trig) {		
	   	 //自定义变量
		String cid=trig.getTcCaseids().split(",")[0];
		ApiTestcase testcase = testDadaService.getTestcaseByID(cid);
		List<UserDefinedVariable> udvs=testDadaService.getUserDefinedVar(testcase.getProjectId());
		List<UserDefinedVariable> definedVars=udvs.stream()
				.filter(u -> u.getType().equals("0"))
				.collect(Collectors.toList());
		Arguments args=ConfigElement.createArguments2(definedVars);
		Argument host = new Argument("host", trig.getTestIp());
        Argument port = new Argument("port", trig.getTestPort());
        host.setMetaData("=");
        port.setMetaData("=");
        args.addArgument(host);
        args.addArgument(port);
	    return args;
	}
	/**
	 * 获取单个测试用例自定义参数
	 * @param trig
	 * @return
	 */
	public void addArguments(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,ApiTestcase api) {
		QueryWrapper<UserDefinedVariable> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(UserDefinedVariable::getCaseId, api.getCaseId())
							 .eq(UserDefinedVariable::getType, "1");
		List<UserDefinedVariable> definedVars=userDefinedVar.list(queryWrapper);
		if(definedVars.size()>0) {
			Arguments args=ConfigElement.createArguments2(definedVars);
			testApiTree.add(sampler,args);
		}
	}
	
	/**
	 * 获取测试计划公共header
	 * @param trig
	 * @return
	 */
	public List<ApiHeader> getPubHeader(TestScheduled trig) {
		String cid=trig.getTcCaseids().split(",")[0];
		ApiTestcase testcase = testDadaService.getTestcaseByID(cid);
		QueryWrapper<ApiHeader> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ApiHeader::getProjectId, testcase.getProjectId())
							 .eq(ApiHeader::getType,"0");
		List<ApiHeader> apiHeaders=apiHeader.list(queryWrapper);
	    return apiHeaders;
	}
	
	/**
	 * 获取测试用例对应的header
	 * @param testApiTree   http请求tree
	 * @param sampler   TechstarHTTPSamplerProxy对象
	 * @param api 用例
	 */
	public void addHeader(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,ApiTestcase api) {
//		ApiTestcase testcase = testDadaService.getTestcaseByID(String.valueOf(caseId));
		QueryWrapper<ApiHeader> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ApiHeader::getProjectId, api.getProjectId())
							 .eq(ApiHeader::getCaseId,api.getCaseId())
							 .eq(ApiHeader::getType,"1");
		List<ApiHeader> apiHeaders=apiHeader.list(queryWrapper);
		if(apiHeaders.size()>0) {
			HeaderManager apiHeader=ConfigElement.createHeaderManager(apiHeaders);
			testApiTree.add(sampler,apiHeader);
		}		
	}
		
	/**
	 * 添加mock数据
	 * @param threadGroupHashTree
	 * @param threadGroup
	 * @param api
	 */
	public void addMockSampler(ListedHashTree threadGroupHashTree,ThreadGroup threadGroup,ApiTestcase api) {
		List<ApiMock> preMock=testDadaService.getPreMock(api.getCaseId());
		if (preMock.size()>0) {
			for (ApiMock apiMock : preMock) {
				ListedHashTree mockTree=HTTPSampler.mockSampler(apiMock);
				threadGroupHashTree.add(threadGroup, mockTree);
			}			
		}
	}
	
	
	/**
	 * 前置数据添加
	 * @param testApiTree
	 * @param sampler
	 * @param api
	 */
	public void addPreProcessors(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,ApiTestcase api) {
		if(StrUtil.isNotEmpty(api.getPreCases())) {
			ApiTestcase parent=testDadaService.getTestcaseByID(api.getPreCases());
		}
		List<Beanshell> shell=testDadaService.getPreBeanshell(api.getCaseId());
		if (shell.size()>0)
			shell.forEach(item->testApiTree.add(sampler,PreProcessors.beanShellPreProcessor(item.getScript())));
		
		List<ProcessorJdbc> preJdbc=testDadaService.getPreJdbc(api.getCaseId());
		if (preJdbc.size()>0)
			preJdbc.forEach(item->testApiTree.add(sampler,PreProcessors.jdbcPreProcessor(item)));	
	}
	/**
	 * 后置处理器添加
	 * @param testApiTree
	 * @param sampler
	 * @param api
	 */
	public void addPostProcessors(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,ApiTestcase api) {
		List<Beanshell> shell=testDadaService.getPostBeanshell(api.getCaseId());
		if (shell.size()>0)
			shell.forEach(item->testApiTree.add(sampler,PostProcessors.beanShellPostProcessor(item.getScript())));
		List<ProcessorJdbc> postJdbc=testDadaService.getPostJdbc(api.getCaseId());
		if (postJdbc.size()>0)
			postJdbc.forEach(item->testApiTree.add(sampler,PostProcessors.jdbcPostProcessor(item)));	
		List<ProcessorJson> jsonList=testDadaService.getPostJson(api.getCaseId());
		if (jsonList.size()>0)
			jsonList.forEach(item->testApiTree.add(sampler,PostProcessors.jsonPostProcessor(item)));
	}
	
	/**
	 * 断言添加
	 * @param testApiTree
	 * @param sampler
	 * @param api
	 */
	public void addAssertions(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,ApiTestcase api) {
		List<AssertJson> assertJsonList=testDadaService.getAssertJson(api.getCaseId());
		if (assertJsonList.size()>0)
			assertJsonList.forEach(item->testApiTree.add(sampler,Assertions.jsonPathAssertion(item)));
		
		List<Beanshell> shellList=testDadaService.getAssertBeanshell(api.getCaseId());
		if (shellList.size()>0)
			shellList.forEach(item->testApiTree.add(sampler,Assertions.beanShellAssertion(item.getScript())));
		
		List<AssertResponse> assertResList=testDadaService.getResponse(api.getCaseId());
		if (assertResList.size()>0)
			assertResList.forEach(item->testApiTree.add(sampler,Assertions.responseAssertion(item)));
		
	}

	/**
	 * 用例实例化
	 * @param threadGroupHashTree
	 * @param threadGroup
	 * @param api
	 */
	public void addSamplers(ListedHashTree threadGroupHashTree,ThreadGroup threadGroup,ApiTestcase api) {
		log.info("创建http sampler");
		TechstarHTTPSamplerProxy sampler=HTTPSampler.crtHTTPSampler(api,header);
    	ListedHashTree testApiTree = new ListedHashTree(sampler);
    	//header,用户自定义变量，前置，后置，断言查询并解析
    	log.info("header添加");
    	jmeterCompant.addHeader(testApiTree, sampler, api); 
    	log.info("用户自定义变量");
    	jmeterCompant.addArguments(testApiTree, sampler, api);
    	//jmeterCompant.addPreProcessors(testApiTree, sampler, api);
    	log.info("前置添加");
		if(StrUtil.isNotEmpty(api.getPreCases())) {
			ApiTestcase parent=testDadaService.getTestcaseByID(api.getPreCases());
			addSamplers(threadGroupHashTree,threadGroup,parent);
		}
		List<Beanshell> shell=testDadaService.getPreBeanshell(api.getCaseId());
		if (shell.size()>0)
			shell.forEach(item->testApiTree.add(sampler,PreProcessors.beanShellPreProcessor(item.getScript())));
		
		List<ProcessorJdbc> preJdbc=testDadaService.getPreJdbc(api.getCaseId());
		if (preJdbc.size()>0)
			preJdbc.forEach(item->testApiTree.add(sampler,PreProcessors.jdbcPreProcessor(item)));
		
    	jmeterCompant.addMockSampler(threadGroupHashTree, threadGroup, api);
    	log.info("后置添加");
    	jmeterCompant.addPostProcessors(testApiTree, sampler, api);
    	log.info("断言设置");
    	jmeterCompant.addAssertions(testApiTree, sampler, api);
    	threadGroupHashTree.add(threadGroup, testApiTree);
		
	}
	
}
