package com.autotest.jmeter.jmeteragent.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.TechstarHTTPSamplerProxy;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.ListedHashTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autotest.data.mapper.ApiReportMapper;
import com.autotest.data.mode.*;
import com.autotest.data.service.impl.ApiHeaderServiceImpl;
import com.autotest.data.service.impl.UserDefinedVariableServiceImpl;
import com.autotest.jmeter.component.Assertions;
import com.autotest.jmeter.component.ConfigElement;
import com.autotest.jmeter.component.HTTPSampler;
import com.autotest.jmeter.component.PostProcessors;
import com.autotest.jmeter.component.PreProcessors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import cn.hutool.core.text.StrSpliter;
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
        args.addArgument(new Argument("JobId", trig.getId()));
        args.addArgument(new Argument("HistoryId", trig.getHistoryId()));
	    return args;
	}
	/**
	 * 获取单个测试用例自定义参数
	 * @param trig
	 * @return
	 */
	private void addArguments(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,ApiTestcase api) {
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
	private void addHeader(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,ApiTestcase api) {
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
	private void addMockSampler(ListedHashTree threadGroupHashTree,ThreadGroup threadGroup,ApiTestcase api) {
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
	private void addPreProcessors(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,ApiTestcase api) {
		
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
	private void addPostProcessors(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,ApiTestcase api) {
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
	private void addAssertions(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,ApiTestcase api) {
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
	 * 前置用例
	 * @param threadGroupHashTree
	 * @param threadGroup
	 * @param api
	 */
	private void addParentSampler(ListedHashTree threadGroupHashTree,ThreadGroup threadGroup,ApiTestcase api) {
		if(StrUtil.isNotEmpty(api.getPreCases())) {
			ApiTestcase parent=testDadaService.getTestcaseByID(api.getPreCases());
			addSamplers(threadGroupHashTree,threadGroup,parent);
		}
		
	}
	/**
	 * 用例实例化
	 * @param threadGroupHashTree
	 * @param threadGroup
	 * @param api
	 */
	public void addSamplers(ListedHashTree threadGroupHashTree,ThreadGroup threadGroup,ApiTestcase api) {
		log.info("创建http sampler");
		//header.put("case_id", api.getCaseId().toString());//也可通过comments设置用例ID
		TechstarHTTPSamplerProxy sampler=HTTPSampler.crtHTTPSampler(api,header);
		sampler.setComment(api.getCaseId().toString()+"||"+api.getSuiteName());
    	ListedHashTree testApiTree = new ListedHashTree(sampler);
    	//header,用户自定义变量，前置，后置，断言查询并解析
    	log.info("header添加");
    	this.addHeader(testApiTree, sampler, api); 
    	log.info("用户自定义变量");
    	this.addArguments(testApiTree, sampler, api);
    	//jmeterCompant.addPreProcessors(testApiTree, sampler, api);
    	log.info("前置添加");
    	this.addParentSampler(threadGroupHashTree,threadGroup,api);
    	this.addPreProcessors(testApiTree,sampler,api);
    	this.addMockSampler(threadGroupHashTree, threadGroup, api);
    	log.info("后置添加");
    	this.addPostProcessors(testApiTree, sampler, api);
    	log.info("断言设置");
    	this.addAssertions(testApiTree, sampler, api);
    	threadGroupHashTree.add(threadGroup, testApiTree);
		
	}
	
	public synchronized void writeSamplers(HTTPSampleResult result) {
		
		JMeterContext ctx=JMeterContextService.getContext();
		String commts=ctx.getCurrentSampler().getComment();
		if(commts.length()==0)return;
		ApiReport rport=new ApiReport();
		rport.setHistoryId(ctx.getVariables().get("HistoryId"));
		rport.setJobId(ctx.getVariables().get("JobId"));
		List<String> comments=StrSpliter.splitTrimIgnoreCase(commts,"||",2,true);
		rport.setCaseId(Integer.parseInt(comments.get(0)));
		rport.setTcSuite(comments.get(1));
		rport.setTcName(result.getSampleLabel());
		rport.setTcResult(result.isSuccessful());
		rport.setTcDuration(String.valueOf(result.getLatency()));
		String assertStr="";
		rport.setTcLog("");
		if(!result.isSuccessful()) {
			String tcLog="Response code:"+result.getResponseCode()+"\r\n"+
					  	 "Response message: "+result.getResponseMessage();
			rport.setTcLog(tcLog);
			for(AssertionResult assR:result.getAssertionResults()) {
				if(assR.isError()||assR.isFailure()) {
					assertStr=assertStr+"["+assR.getName()+"]"+assR.getFailureMessage()+"\r\n";
				}	
			}
		}
		rport.setTcHeader(result.getRequestHeaders());
		rport.setTcRequest(result.getSamplerData());
		rport.setTcResponse(result.getResponseDataAsString());
		rport.setTcAssert(assertStr);
		rport.setTcRunsNum(1);
		rport.setCreateTime(LocalDateTime.now());
		Boolean flag=testDadaService.updateApiReport(rport);
		if(!flag)log.error("用例ID:"+rport.getCaseId()+",名称:"+rport.getTcName()+"-----更新失败");
		//result.getQueryString();
		
	}
}
