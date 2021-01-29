package com.autotest.jmeter.jmeteragent.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
import org.springframework.transaction.annotation.Transactional;

import com.autotest.data.mapper.ApiReportMapper;
import com.autotest.data.mode.*;
import com.autotest.data.mode.assertions.JsonAssertion;
import com.autotest.data.mode.assertions.ResponseAssertion;
import com.autotest.data.mode.confelement.ApiHeader;
import com.autotest.data.mode.confelement.UserDefinedVariable;
import com.autotest.data.mode.custom.BeanShell;
import com.autotest.data.mode.processors.JdbcProcessor;
import com.autotest.data.mode.processors.JsonExtractor;
import com.autotest.data.service.impl.HttpTestcaseServiceImpl;
import com.autotest.data.service.impl.ProjectManageServiceImpl;
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
@Transactional(rollbackFor=Exception.class)
public class JmeterHashTreeServiceImpl {
	private @Autowired TestDataServiceImpl testDadaService;
	private @Autowired HttpTestcaseServiceImpl httpServer;
	private @Autowired  ProjectManageServiceImpl projectManage;
//	private @Autowired ApiHeaderServiceImpl apiHeader;
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
		HttpTestcase testcase = testDadaService.getTestcaseByID(cid);
		List<UserDefinedVariable> udvs=testDadaService.getArgumentsByPid(testcase.getProjectId());
//		List<UserDefinedVariable> definedVars=udvs.stream()
//				.filter(u -> u.getCaseId().equals(-1))
//				.collect(Collectors.toList());
		Arguments args=ConfigElement.createArguments2(udvs);
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
	 * 调试获取公共变量
	 * @param projectId 项目编号
	 * @param envId  环境
	 * @return
	 */
	public Arguments getPubArgumentsOfDebug(int projectId,int envId) {	
		List<UserDefinedVariable> udvs=testDadaService.getArgumentsByPid(projectId);
		SyetemEnv env=testDadaService.getEnv(envId);
		Arguments args=ConfigElement.createArguments2(udvs);
		Argument host = new Argument("host", env.getIp());
        Argument port = new Argument("port", env.getPort());
        args.addArgument(host);
        args.addArgument(port);
		return args;
	}
	/**
	 * 获取单个测试用例自定义参数
	 * @param trig
	 * @return
	 */
	private void addArguments(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,HttpTestcase api) {
//		QueryWrapper<UserDefinedVariable> queryWrapper = new QueryWrapper<>();
//		queryWrapper.lambda().eq(UserDefinedVariable::getCaseId, api.getCaseId())
//							 .eq(UserDefinedVariable::getProjectId, api.getProjectId());
		
		
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId, api.getCaseId())
		 .eq(HttpTestcase::getProjectId, api.getProjectId());
		List<UserDefinedVariable> definedVars=httpServer.getOne(queryWrapper).getArguments();
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
		HttpTestcase testcase = testDadaService.getTestcaseByID(cid);
		
		QueryWrapper<ProjectManage> queryWrapper=new QueryWrapper<ProjectManage>();
		queryWrapper.lambda().eq(ProjectManage::getProjectId, testcase.getProjectId())
							 .select(ProjectManage::getHeaders);
		List<ApiHeader> headers=projectManage.getOne(queryWrapper).getHeaders();
	    return headers;
	}
	
	/**
	 * 获取测试用例对应的header
	 * @param testApiTree   http请求tree
	 * @param sampler   TechstarHTTPSamplerProxy对象
	 * @param api 用例
	 */
	private void addHeader(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,HttpTestcase api) {
//		ApiTestcase testcase = testDadaService.getTestcaseByID(String.valueOf(caseId));
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getProjectId, api.getProjectId())
							 .eq(HttpTestcase::getCaseId,api.getCaseId());
		List<ApiHeader> apiHeaders=httpServer.getOne(queryWrapper).getHeaders();
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
	private void addMockSampler(ListedHashTree threadGroupHashTree,ThreadGroup threadGroup,HttpTestcase api) {
		
		
		
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
	private void addPreProcessors(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,HttpTestcase api) {
		
		List<BeanShell> shell=testDadaService.getPreBeanshell(api.getCaseId());
		
		if (shell.size()>0)
			shell.forEach(item->testApiTree.add(sampler,PreProcessors.beanShellPreProcessor(item.getScript())));
		
		
		List<JdbcProcessor> preJdbc=testDadaService.getPreJdbc(api.getCaseId());
		if (preJdbc.size()>0)
			preJdbc.forEach(item->testApiTree.add(sampler,PreProcessors.jdbcPreProcessor(item)));	
	}
	/**
	 * 后置处理器添加
	 * @param testApiTree
	 * @param sampler
	 * @param api
	 */
	private void addPostProcessors(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,HttpTestcase api) {
		List<BeanShell> shell=testDadaService.getPostBeanshell(api.getCaseId());
		if (shell.size()>0)
			shell.forEach(item->testApiTree.add(sampler,PostProcessors.beanShellPostProcessor(item.getScript())));
		List<JdbcProcessor> postJdbc=testDadaService.getPostJdbc(api.getCaseId());
		if (postJdbc.size()>0)
			postJdbc.forEach(item->testApiTree.add(sampler,PostProcessors.jdbcPostProcessor(item)));	
		List<JsonExtractor> jsonList=testDadaService.getPostJson(api.getCaseId());
		if (jsonList.size()>0)
			jsonList.forEach(item->testApiTree.add(sampler,PostProcessors.jsonPostProcessor(item)));
	}
	
	/**
	 * 断言添加
	 * @param testApiTree
	 * @param sampler
	 * @param api
	 */
	private void addAssertions(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,HttpTestcase api) {
		List<JsonAssertion> assertJsonList=testDadaService.getAssertJson(api.getCaseId());
		if (assertJsonList.size()>0)
			assertJsonList.forEach(item->testApiTree.add(sampler,Assertions.jsonPathAssertion(item)));
		
		List<BeanShell> shellList=testDadaService.getAssertBeanshell(api.getCaseId());
		if (shellList.size()>0)
			shellList.forEach(item->testApiTree.add(sampler,Assertions.beanShellAssertion(item.getScript())));
		
		List<ResponseAssertion> assertResList=testDadaService.getResponse(api.getCaseId());
		if (assertResList.size()>0)
			assertResList.forEach(item->testApiTree.add(sampler,Assertions.responseAssertion(item)));
		
	}
	
	/**
	 * 前置用例
	 * @param threadGroupHashTree
	 * @param threadGroup
	 * @param api
	 */
	private void addParentSampler(ListedHashTree threadGroupHashTree,ThreadGroup threadGroup,HttpTestcase api) {
	
		List<Integer> ids=testDadaService.getPreCases(api.getCaseId());
		if(ids.size()>0) {
			HttpTestcase parent=testDadaService.getTestcaseByID(ids.get(0).toString());
			addSamplers(threadGroupHashTree,threadGroup,parent);
		}
		
	}
	/**
	 * 用例实例化
	 * @param threadGroupHashTree
	 * @param threadGroup
	 * @param api
	 */
	public void addSamplers(ListedHashTree threadGroupHashTree,ThreadGroup threadGroup,HttpTestcase api) {
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
	
	private Queue<ApiReport> queue = new LinkedList<ApiReport>();
	
	public synchronized ApiReport getQueue() {
		ApiReport response=queue.poll();
		return response;
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
		if(rport.getJobId()==null) {
//			if(queue.size()>0)
//				queue.forEach(item->queue.poll());
			queue.offer(rport);
			return;
		}
		Boolean flag=testDadaService.updateApiReport(rport);
		if(!flag)log.error("用例ID:"+rport.getCaseId()+",名称:"+rport.getTcName()+"-----更新失败");
		//result.getQueryString();
		
	}
}
