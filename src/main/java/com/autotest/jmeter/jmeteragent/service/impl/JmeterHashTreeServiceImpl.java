package com.autotest.jmeter.jmeteragent.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.TechstarHTTPSamplerProxy;
import org.apache.jorphan.collections.ListedHashTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSONArray;
import com.autotest.data.mode.*;
import com.autotest.data.mode.assertions.JsonAssertion;
import com.autotest.data.mode.assertions.ResponseAssertion;
import com.autotest.data.mode.confelement.ApiHeader;
import com.autotest.data.mode.confelement.UserDefinedVariable;
import com.autotest.data.mode.custom.BeanShell;
import com.autotest.data.mode.processors.JdbcProcessor;
import com.autotest.data.mode.processors.JsonExtractor;
import com.autotest.data.service.impl.ProjectManageServiceImpl;
import com.autotest.jmeter.component.Assertions;
import com.autotest.jmeter.component.ConfigElement;
import com.autotest.jmeter.component.HTTPSampler;
import com.autotest.jmeter.component.PostProcessors;
import com.autotest.jmeter.component.PreProcessors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
	private @Autowired  ProjectManageServiceImpl projectManage;
	private Map<String, String> header=new HashMap<String, String>();

	/**
	 * 获取测试套件配置的公共参数
	 * @param trig
	 * @return
	 */
	public Arguments getPubArguments(TestScheduled trig) {		
	   	 //自定义变量
		Integer cid=trig.getTcCaseids().get("samplerIds").get(0);
		HttpTestcase testcase = testDadaService.getTestcaseByID(cid);
		List<UserDefinedVariable> udvs=testDadaService.getArgumentsByPid(testcase.getProjectId());
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
	public Arguments getPubArgumentsOfDebug(int projectId,String envStr) {	
		List<UserDefinedVariable> udvs=testDadaService.getArgumentsByPid(projectId);
//		SyetemEnv env=testDadaService.getEnv(envId);
		Arguments args=ConfigElement.createArguments2(udvs);
		
		String[] env=envStr.split("//");
		if(env.length==2) {
			String[] addr=env[1].split(":");
			if(addr.length==2) {
				Argument host = new Argument("host", addr[0]);
		        Argument port = new Argument("port", addr[1]);
		        args.addArgument(host);
		        args.addArgument(port);
			}
		}		
		return args;
	}
	/**
	 * 获取单个测试用例自定义参数
	 * @param trig
	 * @return
	 */
	private void addArguments(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,HttpTestcase api) {
		
		List<UserDefinedVariable> definedVars=api.getArguments();
		if(definedVars.size()>0) {
			if(!(definedVars.get(0) instanceof UserDefinedVariable)) {
				definedVars=(List<UserDefinedVariable>)JSONArray.parseArray(definedVars.toString(),UserDefinedVariable.class);
			}
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
		Integer cid=trig.getTcCaseids().get("samplerIds").get(0);
		HttpTestcase testcase = testDadaService.getTestcaseByID(cid);
		
		QueryWrapper<ProjectManage> queryWrapper=new QueryWrapper<ProjectManage>();
		queryWrapper.lambda().eq(ProjectManage::getProjectId, testcase.getProjectId());
		List<ApiHeader> headers=projectManage.getOne(queryWrapper,false).getHeaders();
		if(headers.size()>0&&!(headers.get(0) instanceof ApiHeader)) {
			headers=(List<ApiHeader>)JSONArray.parseArray(headers.toString(),ApiHeader.class);
		}
		
	    return headers;
	}
	
	/**
	 * 获取测试用例对应的header
	 * @param testApiTree   http请求tree
	 * @param sampler   TechstarHTTPSamplerProxy对象
	 * @param api 用例
	 */
	private void addHeader(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,HttpTestcase api) {

		List<ApiHeader> apiHeaders=api.getHeaders();
		if(apiHeaders.size()>0) {
			if(!(apiHeaders.get(0) instanceof ApiHeader)) {
				apiHeaders=(List<ApiHeader>)JSONArray.parseArray(apiHeaders.toString(),ApiHeader.class);
			}
			HeaderManager apiHeader=ConfigElement.createHeaderManager(apiHeaders);
			testApiTree.add(sampler,apiHeader);
		}		
	}
		
	/**
	 * 添加mock数据
	 * @param <T>
	 * @param threadGroupHashTree
	 * @param threadGroup
	 * @param api
	 */
	private <T> void addMockSampler(ListedHashTree threadGroupHashTree,T threadGroup,HttpTestcase api) {
 		List<Integer>  mockids=api.getIdOfPreMock();
		if(mockids==null||mockids.size()==0)return;
		List<ApiMock> preMock=testDadaService.getPreMock(mockids);
		if (preMock!=null&&preMock.size()>0) {
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

		List<BeanShell> shell=api.getPreBeanshell();
		if (shell!=null&&shell.size()>0)			
			shell.forEach(item->testApiTree.add(sampler,PreProcessors.beanShellPreProcessor(item.getScript())));
		
		List<JdbcProcessor> preJdbc=api.getPreJdbc();
		if (preJdbc!=null&&preJdbc.size()>0)
			preJdbc.forEach(item->testApiTree.add(sampler,PreProcessors.jdbcPreProcessor(item)));	
	}
	/**
	 * 后置处理器添加
	 * @param testApiTree
	 * @param sampler
	 * @param api
	 */
	private void addPostProcessors(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,HttpTestcase api) {

		List<BeanShell> shell=api.getPostBeanshell();
		if (shell!=null&&shell.size()>0)
			shell.forEach(item->testApiTree.add(sampler,PostProcessors.beanShellPostProcessor(item.getScript())));
		List<JdbcProcessor> postJdbc=api.getPostJdbc();
		if (postJdbc!=null&&postJdbc.size()>0)
			postJdbc.forEach(item->testApiTree.add(sampler,PostProcessors.jdbcPostProcessor(item)));			
		List<JsonExtractor> jsonList=api.getPostJson();
		if (jsonList!=null&&jsonList.size()>0)
			jsonList.forEach(item->testApiTree.add(sampler,PostProcessors.jsonPostProcessor(item)));
	}
	
	/**
	 * 断言添加
	 * @param testApiTree
	 * @param sampler
	 * @param api
	 */
	private void addAssertions(ListedHashTree testApiTree,TechstarHTTPSamplerProxy sampler,HttpTestcase api) {

		List<JsonAssertion> assertJsonList=api.getAssertJson();
		if (assertJsonList!=null&&assertJsonList.size()>0)
			assertJsonList.forEach(item->testApiTree.add(sampler,Assertions.jsonPathAssertion(item)));
		List<BeanShell> shellList=api.getAssertBeanshell();
		if (shellList!=null&&shellList.size()>0)
			shellList.forEach(item->testApiTree.add(sampler,Assertions.beanShellAssertion(item.getScript())));
		List<ResponseAssertion> assertResList=api.getResponse();
		if (assertResList!=null&&assertResList.size()>0)
			assertResList.forEach(item->testApiTree.add(sampler,Assertions.responseAssertion(item)));
		
	}
	
//	/**
//	 * 前置用例
//	 * @param threadGroupHashTree
//	 * @param threadGroup
//	 * @param api
//	 */
//	private void addParentSampler(ListedHashTree threadGroupHashTree,ThreadGroup threadGroup,HttpTestcase api) {
//	
//		//List<Integer> ids=testDadaService.getPreCases(api.getCaseId());
//		List<Integer> ids=api.getPreCases();
//		if(ids.size()>0) {
//			HttpTestcase parent=testDadaService.getTestcaseByID(ids.get(0));
//			addSamplers(threadGroupHashTree,threadGroup,parent);
//		}
//		
//	}
	/**
	 * 用例实例化
	 * @param <T>
	 * @param threadGroupHashTree
	 * @param threadGroup
	 * @param api
	 */
	public <T> void addSamplers(ListedHashTree threadGroupHashTree,T threadGroup,HttpTestcase api) {
		log.info("创建http sampler");
		//header.put("case_id", api.getCaseId().toString());//也可通过comments设置用例ID
		TechstarHTTPSamplerProxy sampler=HTTPSampler.crtHTTPSampler(api,header);
//		if(!api.getTag().equals(ScenarioTestcase.LOGIN_SIGN))
//			sampler.setComment(api.getCaseId().toString()+"||"+api.getSuiteName());
    	ListedHashTree testApiTree = new ListedHashTree(sampler);
    	//header,用户自定义变量，前置，后置，断言查询并解析
    	log.info("header添加");
    	this.addHeader(testApiTree, sampler, api); 
    	log.info("用户自定义变量");
    	this.addArguments(testApiTree, sampler, api);
    	//jmeterCompant.addPreProcessors(testApiTree, sampler, api);
    	log.info("前置添加");
//    	this.addParentSampler(threadGroupHashTree,threadGroup,api);
    	this.addPreProcessors(testApiTree,sampler,api);
    	this.addMockSampler(threadGroupHashTree, threadGroup, api);
    	log.info("后置添加");
    	this.addPostProcessors(testApiTree, sampler, api);
    	log.info("断言设置");
    	this.addAssertions(testApiTree, sampler, api);
    	threadGroupHashTree.add(threadGroup, testApiTree);
		
	}

//	private Queue<ApiReport> queue = new LinkedList<ApiReport>();	
//	public ApiReport getQueue() {
//		ApiReport response=queue.poll();
//		return response;
//	}
//
//	public synchronized void writeSamplers(HTTPSampleResult result) {
//	
//		JMeterContext ctx=JMeterContextService.getContext();
//		String commts=ctx.getCurrentSampler().getComment();
//		if(commts.length()==0)return;
//		ApiReport rport=new ApiReport();
//		rport.setHistoryId(ctx.getVariables().get("HistoryId"));
//		rport.setJobId(ctx.getVariables().get("JobId"));
//		List<String> comments=StrSpliter.splitTrimIgnoreCase(commts,"||",2,true);
//		rport.setCaseId(Integer.parseInt(comments.get(0)));
//		rport.setTcSuite(comments.get(1));
//		rport.setTcName(result.getSampleLabel());
//		rport.setTcResult(result.isSuccessful());
//		rport.setTcDuration(String.valueOf(result.getLatency()));
//		String assertStr="";
//		rport.setTcLog("");
//		if(!result.isSuccessful()) {
//			String tcLog="Response code:"+result.getResponseCode()+"\r\n"+
//					  	 "Response message: "+result.getResponseMessage();
//			rport.setTcLog(tcLog);
//			for(AssertionResult assR:result.getAssertionResults()) {
//				if(assR.isError()||assR.isFailure()) {
//					assertStr=assertStr+"["+assR.getName()+"]"+assR.getFailureMessage()+"\r\n";
//				}	
//			}
//		}
//		rport.setTcHeader(result.getRequestHeaders());
//		rport.setTcRequest(result.getSamplerData());
//		rport.setTcResponse(result.getResponseDataAsString());
//		rport.setTcAssert(assertStr);
//		rport.setTcRunsNum(1);
//		rport.setCreateTime(LocalDateTime.now());
//		if(rport.getJobId()==null) {
//			queue.offer(rport);
//			return;
//		}
//		Boolean flag=testDadaService.updateApiReport(rport);
//		if(!flag)log.error("用例ID:"+rport.getCaseId()+",名称:"+rport.getTcName()+"-----更新失败");
//		//result.getQueryString();		
//	}
}
