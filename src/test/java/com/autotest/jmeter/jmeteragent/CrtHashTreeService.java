package com.autotest.jmeter.jmeteragent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.ListedHashTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.autotest.data.enums.ApiParam;
import com.autotest.data.mode.*;
import com.autotest.data.mode.assertions.JsonAssertion;
import com.autotest.data.mode.assertions.ResponseAssertion;
import com.autotest.data.mode.confelement.ApiHeader;
import com.autotest.data.mode.confelement.UserDefinedVariable;
import com.autotest.data.mode.custom.BeanShell;
import com.autotest.data.mode.custom.SamplerLable;
import com.autotest.data.mode.processors.JdbcProcessor;
import com.autotest.data.mode.processors.JsonExtractor;
import com.autotest.data.service.impl.ProjectManageServiceImpl;
import com.autotest.jmeter.component.Assertions;
import com.autotest.jmeter.component.ConfigElement;
import com.autotest.jmeter.component.HTTPSampler;
import com.autotest.jmeter.component.LogicController;
import com.autotest.jmeter.component.PostProcessors;
import com.autotest.jmeter.component.PreProcessors;
import com.autotest.jmeter.component.ThreadGroups;
import com.autotest.jmeter.jmeteragent.common.TestDataServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.apachecommons.CommonsLog;

/**
 * 创建jmx脚本调试使用该类
 * @author Techstar
 *
 */
@Service
@CommonsLog
@Transactional(rollbackFor=Exception.class)
public class CrtHashTreeService {
	private @Autowired TestDataServiceImpl testDadaService;
	private @Autowired  ProjectManageServiceImpl projectManage;
	
	private Map<String, String> header=new HashMap<String, String>();

	private Integer getProjectId(TestScheduled trig) {
		Integer project_id=1;
		try {
			Integer cid=trig.getTcCaseids().get(TestScheduled.TYPE_SAMPLER).get(0);
			HttpTestcase testcase = testDadaService.getTestcaseByID(cid);
			project_id=testcase.getProjectId();
		} catch (Exception e) {
			Integer sid=trig.getTcCaseids().get(TestScheduled.TYPE_SCENARIO).get(0);
			ScenarioTestcase stc=testDadaService.getScenariosByid(sid);
			project_id=stc.getProjectId();
		}
		return project_id;
	}
	/**
	 * 获取测试套件配置的公共参数
	 * @param trig
	 * @return
	 */
	public Arguments getPubArguments(TestScheduled trig) {		
	   	 //自定义变量
		Integer project_id=getProjectId(trig);
		List<UserDefinedVariable> udvs=testDadaService.getArgumentsByPid(project_id);
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
	private void addArguments(ListedHashTree testApiTree,HTTPSamplerProxy sampler,HttpTestcase api) {
		
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
		Integer project_id=getProjectId(trig);
		QueryWrapper<ProjectManage> queryWrapper=new QueryWrapper<ProjectManage>();
		queryWrapper.lambda().eq(ProjectManage::getProjectId, project_id);
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
	private void addHeader(ListedHashTree testApiTree,HTTPSamplerProxy sampler,HttpTestcase api) {

		List<ApiHeader> apiHeaders=api.getHeaders();
		if(apiHeaders.size()>0) {
			if(!(apiHeaders.get(0) instanceof ApiHeader)) {
				apiHeaders=(List<ApiHeader>)JSONArray.parseArray(apiHeaders.toString(),ApiHeader.class);
			}
			HeaderManager apiHeader=ConfigElement.createHeaderManager(apiHeaders);
			testApiTree.add(sampler,apiHeader);
		}	
//		HeaderManager apiHeader=ConfigElement.createHeaderManager(apiHeaders);
//		apiHeader.getHeaders().addItem(new Header(ApiParam.CASE_ID.name(), api.getCaseId().toString()));
//		apiHeader.getHeaders().addItem(new Header(ApiParam.SUITE_Id.name(), api.getSuiteId().toString()));
//		testApiTree.add(sampler,apiHeader);
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
	private void addPreProcessors(ListedHashTree testApiTree,HTTPSamplerProxy sampler,HttpTestcase api) {

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
	private void addPostProcessors(ListedHashTree testApiTree,HTTPSamplerProxy sampler,HttpTestcase api) {

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
	private void addAssertions(ListedHashTree testApiTree,HTTPSamplerProxy sampler,HttpTestcase api) {

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
		HTTPSamplerProxy sampler=HTTPSampler.getHttpSamplerProxy(api,header);
    	ListedHashTree testApiTree = new ListedHashTree();
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

    public ListedHashTree creatTransactionControllerTree(ScenarioTestcase tc) {
    	SamplerLable transLable=new SamplerLable();
    	transLable.setCaseId(tc.getId().toString());
    	transLable.setSuiteId(tc.getSuiteId().toString());
    	transLable.setCaseName(tc.getScenarioName());
    	if(tc.getTag().equals("登陆"))transLable.setIsLogin(true);
    	
    	TransactionController tsController=LogicController.transactionController(JSON.toJSONString(transLable));
		ListedHashTree tcControllerTree=new ListedHashTree(); 
		ArrayList<JSONObject> listObj=tc.getHashtree();
		if(listObj.size()==0)return tcControllerTree;
		for (JSONObject json : listObj) {
			String type=json.getString("type");
			if(type.contains(ScenarioTestcase.TYPE_HTTP_SAMPLER)) {
				HttpTestcase htc=testDadaService.getTestcaseByID(json.getInteger("id"));
				
				this.addSamplers(tcControllerTree, tsController, htc);
				continue;
			}
			if(type.contains(ScenarioTestcase.TYPE_LOGIN_CONTROLLER)) {
				continue;
			}
			if(type.contains(ScenarioTestcase.TYPE_SCENARIO)) {
				ScenarioTestcase stc=testDadaService.getScenariosByid(json.getInteger("id"));
				tcControllerTree.add(tsController,creatTransactionControllerTree(stc));
			}
		}
		return tcControllerTree;
    }
    
    public ListedHashTree createThreadGroup(List<Object> subList) {
    	ThreadGroup threadGroup = ThreadGroups.create(ThreadGroups.apiTestTheadGroup());                
        ListedHashTree threadGroupHashTree = new ListedHashTree();
        Boolean flag=subList.get(0) instanceof ScenarioTestcase;
        Integer pid=flag?((ScenarioTestcase)subList.get(0)).getProjectId()
        				:((HttpTestcase)subList.get(0)).getProjectId();
        ScenarioTestcase loginsc=testDadaService.getLoginScenarios(pid);
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
	        	this.addSamplers(threadGroupHashTree, threadGroup, tc);
	        	
	        }
		}
        return threadGroupHashTree;
    }
}
