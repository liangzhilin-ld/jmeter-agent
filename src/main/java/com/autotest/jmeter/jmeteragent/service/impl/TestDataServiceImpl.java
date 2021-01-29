/**
 * 
 */
package com.autotest.jmeter.jmeteragent.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.autotest.data.mapper.ProjectManageMapper;
import com.autotest.data.mode.*;
import com.autotest.data.mode.assertions.AssertEntity;
import com.autotest.data.mode.assertions.JsonAssertion;
import com.autotest.data.mode.assertions.ResponseAssertion;
import com.autotest.data.mode.confelement.ApiHeader;
import com.autotest.data.mode.confelement.UserDefinedVariable;
import com.autotest.data.mode.custom.BeanShell;
import com.autotest.data.mode.processors.JdbcProcessor;
import com.autotest.data.mode.processors.JsonExtractor;
import com.autotest.data.mode.processors.PostProcessors;
import com.autotest.data.mode.processors.PreProcessors;
import com.autotest.data.service.impl.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import net.sf.json.JSONObject;

/**
 * @author Techstar
 *
 */
@Repository
@Transactional(rollbackFor=Exception.class)
public class TestDataServiceImpl {


	private @Autowired TheadGroupConfigServiceImpl theadGroupConfig;
	private @Autowired HttpTestcaseServiceImpl apiTestcase;
	private @Autowired HttpTestcaseServiceImpl httpServer;
//	private @Autowired ApiTestcase2ServiceImpl apiTestcase2;
	private @Autowired TestScheduledServiceImpl testSchedule;
	private @Autowired ApiReportServiceImpl apiReport;
	private @Autowired ApiReportHistoryListServiceImpl historyStat;
	private @Autowired SyetemDictionaryServiceImpl syetemDic;
	private @Autowired ProjectManageServiceImpl projectManage;
	private @Autowired SyetemDbServiceImpl sysDb;
	private @Autowired ApiMockServiceImpl mockData;
	
	private @Autowired SyetemEnvServiceImpl envServer;

//	public ApiTestcase2 saveTestCase2(ApiTestcase2 api) {
//		apiTestcase2.save(api);
//		return api;
//	}

//	public Map<String, String> getTestPlanHeader(int projectID){
//		getApiHeader();
//		Map<String, String> headerMap=new HashMap<String, String>();
//		for (ApiHeader header : headers) {
//			if(header.getProjectId().equals(projectID)&&header.getCaseId().equals(-1))
//				headerMap.put(header.getKey(), header.getValue());
//		}
//		return headerMap;
//	} 
//	public Map<String, String>  getSamplerHeader(int projectID,int caseID){
//		Map<String, String> headerMap=new HashMap<String, String>();
//		for (ApiHeader header : headers) {
//			if(header.getProjectId().equals(projectID)&&header.getCaseId().equals(caseID))
//				headerMap.put(header.getKey(), header.getValue());
//		}
//		return headerMap;
//	}
	public List<ApiHeader> getSamplerHeader(int caseId) {
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId,caseId);
	    return httpServer.getOne(queryWrapper).getHeaders();
	}
	public List<ApiHeader> getPubHeader(int projectId) {
		QueryWrapper<ProjectManage> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ProjectManage::getProjectId, projectId);
		List<ApiHeader> apiHeaders=projectManage.getOne(queryWrapper).getHeaders();
	    return apiHeaders;
	}
	public SyetemEnv getEnv(int envId) {
		QueryWrapper<SyetemEnv> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(SyetemEnv::getId, envId);
		SyetemEnv apiHeaders=envServer.getOne(queryWrapper);
	    return apiHeaders;
	}
	public List<SyetemDictionary> getSyetemDic() {
		return syetemDic.list();
	}
	public List<TestScheduled> getTestSchedule() {
		return testSchedule.list();
	}

	public List<ApiReport> getApiReport() {
		return apiReport.list();
	}
	/**
	 * 更新详细用例执行结果
	 * @param report
	 * @return
	 */
	public Boolean updateApiReport(ApiReport report) {
		UpdateWrapper<ApiReport> updateWrapper = new UpdateWrapper<>();
		updateWrapper.lambda().set(ApiReport::getTcSuite, report.getTcSuite())
							.set(ApiReport::getTcName, report.getTcName())
							.set(ApiReport::getTcResult, report.getTcResult())
							.set(ApiReport::getTcDuration, report.getTcDuration())
							.set(ApiReport::getTcHeader, report.getTcHeader())
							.set(ApiReport::getTcLog, report.getTcLog())
							.set(ApiReport::getTcRequest, report.getTcRequest())
							.set(ApiReport::getTcResponse, report.getTcResponse())
							.set(ApiReport::getTcAssert, report.getTcAssert())
							.setSql("TC_RUNS_NUM=TC_RUNS_NUM+1")
							.set(ApiReport::getCreateTime, report.getCreateTime())
							 .eq(ApiReport::getHistoryId, report.getHistoryId())
							 .eq(ApiReport::getJobId, report.getJobId())
							 .eq(ApiReport::getCaseId, report.getCaseId());
		return apiReport.update(updateWrapper);
	}
	public List<TheadGroupConfig> getTheadGroupConfig() {
		return theadGroupConfig.list();
	}
	
	/**
	 * 更新历史纪录列表数据
	 * @param job
	 * @return
	 */
	public Boolean updateHistoryListTable(TestScheduled job) {
		QueryWrapper<ApiReport> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ApiReport::getHistoryId,job.getHistoryId())
							.eq(ApiReport::getJobId,job.getId())
							.select(ApiReport::getTcResult);
		List<ApiReport> apiList=apiReport.list(queryWrapper);
		List<ApiReport> succList=apiList.stream()
				.filter(s -> s.getTcResult().equals(true))
				.collect(Collectors.toList());
		UpdateWrapper<ApiReportHistoryList> updateWrapper = new UpdateWrapper<>();
		updateWrapper.lambda().set(ApiReportHistoryList::getTcTotal, apiList.size())
		 	.set(ApiReportHistoryList::getTcPassed, succList.size())
		 	.set(ApiReportHistoryList::getTcFailed, apiList.size()-succList.size())
		 	.set(ApiReportHistoryList::getEndTime, LocalDateTime.now())
		 	.eq(ApiReportHistoryList::getId, job.getHistoryId())
		 	.eq(ApiReportHistoryList::getJobId, job.getId());
		return historyStat.update(updateWrapper);
	}
	
	/**
	 * 根据用例ID查询用例
	 * @param id
	 * @return
	 */
	public HttpTestcase getTestcaseByID(String id) {
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("CASE_ID", Integer.valueOf(id));
		return apiTestcase.getOne(queryWrapper);
	}
	public List<HttpTestcase> getTestcaseOfFail(String history) {
		QueryWrapper<ApiReport> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ApiReport::getHistoryId, history)
							.eq(ApiReport::getTcResult,false)
							.select(ApiReport::getCaseId);
		List<ApiReport> ids=apiReport.list(queryWrapper);
		if(ids.size()==0)
			return new ArrayList<HttpTestcase>();
		List<Integer> cids = new ArrayList<Integer>();
		ids.forEach(item->cids.add(item.getCaseId()));
		QueryWrapper<HttpTestcase> wrapper = new QueryWrapper<>();
		wrapper.lambda().in(HttpTestcase::getCaseId, cids);
		return httpServer.list(wrapper);
	}
	public List<HttpTestcase> getTestcase() {
		return httpServer.list();
	}
	
	/**
	 * 根据用例ID集合查询用例
	 * @param trig 任务
	 * @return
	 */
	public List<HttpTestcase> getTestcaseByIds(TestScheduled trig) {
		if(StrUtil.isEmpty(trig.getTcCaseids()))
			return getTestcase();
		List<String> idList=ListUtil.of(trig.getTcCaseids().split(","));
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().in(HttpTestcase::getCaseId, idList);
		return httpServer.list(queryWrapper);
	}
	/**
	 * 查询用户自定义变量
	 * @param projectID 项目编号
	 * @return
	 */
	public List<UserDefinedVariable> getArgumentsByPid(int projectID) {
		QueryWrapper<ProjectManage> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ProjectManage::getProjectId, projectID);
		
		return projectManage.getOne(queryWrapper,false).getArguments();
	}
	/**
	 * 查询自定义变量
	 * @param caseId 用例编号
	 * @return
	 */
	public List<UserDefinedVariable> getArgumentsByCaseId(int caseId) {
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId, caseId);
		httpServer.getOne(queryWrapper).getArguments();
		return httpServer.getOne(queryWrapper).getArguments();
	}
	public ProjectManage getPoject(String projetID) {
		List<ProjectManage> list=projectManage.list();
		for (ProjectManage projectManage : list) {
			if(projectManage.getProjectId().equals(projetID)) {}
				return projectManage;
		}
		return null;
	}

	public SyetemDb getSyetemDb(String cnnName) {
		List<SyetemDb> list=sysDb.list();
		for (SyetemDb dbinfo : list) {
			if(dbinfo.getCnnName().equals(cnnName)) {}
				return dbinfo;
		}
		return null;
	}
	public List<SyetemDb> getSyetemDbAll() {
		List<SyetemDb> list=sysDb.list();
		return list;
	}
	public ApiMock getApiMock(int caseID) {
		List<ApiMock> list=mockData.list();
		for (ApiMock mock : list) {
			if(mock.getCaseId().equals(caseID)) {}
				return mock;
		}
		return null;
	}
	public List<Integer> getPreCases(int caseID) {
		List<Integer> list=new ArrayList<Integer>();
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId, caseID);
		ArrayList<Object> tree=httpServer.getOne(queryWrapper).getHashtree();	
		for(Object item:tree){
			JSONObject json = JSONObject.fromObject(item);
			if(json.getString("type").equals("PreExtract")) {
				PreProcessors pre=(PreProcessors) JSONObject.toBean(json, PreProcessors.class);
				list.addAll(pre.getPreCaseIds());
				break;
			}
        }
		return list;
		
	}
	
	public List<BeanShell> getPreBeanshell(int caseID) {
		List<BeanShell> list=new ArrayList<BeanShell>();
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId, caseID);
		ArrayList<Object> tree=httpServer.getOne(queryWrapper).getHashtree();
	
		for(Object item:tree){
			JSONObject json = JSONObject.fromObject(item);
			if(json.getString("type").equals("PreExtract")) {
				PreProcessors pre=(PreProcessors) JSONObject.toBean(json, PreProcessors.class);
				list.addAll(pre.getBeanShellPreProcessor());
				break;
			}
        }
		return list;
		
	}
	public List<BeanShell> getPostBeanshell(int caseID) {
		List<BeanShell> list=new ArrayList<BeanShell>();
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId, caseID);
		ArrayList<Object> tree=httpServer.getOne(queryWrapper).getHashtree();
		for(Object item:tree){
			JSONObject json = JSONObject.fromObject(item);
			if(json.getString("type").equals("PostExtract")) {
				PostProcessors pre=(PostProcessors) JSONObject.toBean(json, PostProcessors.class);
				list=pre.getBeanShellPostProcessor();
				break;
			}
		}
		return list;
		
	}
	public List<BeanShell> getAssertBeanshell(int caseID) {
		List<BeanShell> list=new ArrayList<BeanShell>();
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId, caseID);
		ArrayList<Object> tree=httpServer.getOne(queryWrapper).getHashtree();
		
		for(Object item:tree){
			JSONObject json = JSONObject.fromObject(item);
			if(json.getString("type").equals("Assertions")) {
				AssertEntity pre=(AssertEntity) JSONObject.toBean(json, AssertEntity.class);
				list=pre.getBeanShellAssertion();
				break;
			}
		}
		return list;
		
	}
	public List<JdbcProcessor> getPreJdbc(int caseID) {
		List<JdbcProcessor> list=new ArrayList<JdbcProcessor>();
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId, caseID);
		ArrayList<Object> tree=httpServer.getOne(queryWrapper).getHashtree();
		
		
		for(Object item:tree){
			JSONObject json = JSONObject.fromObject(item);
			if(json.getString("type").equals("PreExtract")) {
				PreProcessors pre=(PreProcessors) JSONObject.toBean(json, PreProcessors.class);
				list=pre.getJdbcPreProcessor();
				break;
			}
		}
		return list;
		
	}
	public List<JdbcProcessor> getPostJdbc(int caseID) {
		List<JdbcProcessor> list=new ArrayList<JdbcProcessor>();
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId, caseID);
		ArrayList<Object> tree=httpServer.getOne(queryWrapper).getHashtree();
		
		for(Object item:tree){
			JSONObject json = JSONObject.fromObject(item);
			if(json.getString("type").equals("PostExtract")) {
				PostProcessors pre=(PostProcessors) JSONObject.toBean(json, PostProcessors.class);
				list=pre.getJdbcPostProcessor();
				break;
			}
		}
		return list;
		
	}
	public List<ApiMock> getPreMock(int caseID) {
		List<Integer> list=new ArrayList<Integer>();
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId, caseID);
		ArrayList<Object> tree=httpServer.getOne(queryWrapper).getHashtree();
		for(Object item:tree){
			JSONObject json = JSONObject.fromObject(item);
			if(json.getString("type").equals("PostExtract")) {
				PreProcessors pre=(PreProcessors) JSONObject.toBean(json, PreProcessors.class);
				list=pre.getPreMockIds();
				break;
			}
		}
		QueryWrapper<ApiMock> mockqrapper = new QueryWrapper<>();
		mockqrapper.lambda().in(ApiMock::getId, list);
		return mockData.list(mockqrapper);
		
	}
	public List<JsonExtractor> getPostJson(int caseID) {
		List<JsonExtractor> list=new ArrayList<JsonExtractor>();
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId, caseID);
		ArrayList<Object> tree=httpServer.getOne(queryWrapper).getHashtree();
		
		for(Object item:tree){
			JSONObject json = JSONObject.fromObject(item);
			if(json.getString("type").equals("PostExtract")) {
				PostProcessors pre=(PostProcessors) JSONObject.toBean(json, PostProcessors.class);
				list=pre.getJsonExtractor();
				break;
			}
		}
		
		
		return list;
		
	}
	public List<JsonAssertion> getAssertJson(int caseID) {
		
		
		List<JsonAssertion> list=new ArrayList<JsonAssertion>();
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId, caseID);
		ArrayList<Object> tree=httpServer.getOne(queryWrapper).getHashtree();
		
		for(Object item:tree){
			JSONObject json = JSONObject.fromObject(item);
			if(json.getString("type").equals("Assertions")) {
				AssertEntity pre=(AssertEntity) JSONObject.toBean(json, AssertEntity.class);
				list=pre.getJsonAssertion();
				break;
			}
		}
		
		return list;
		
	}
	public List<ResponseAssertion> getResponse(int caseID) {
		List<ResponseAssertion> list=new ArrayList<ResponseAssertion>();
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId, caseID);
		ArrayList<Object> tree=httpServer.getOne(queryWrapper).getHashtree();
		for(Object item:tree){
			JSONObject json = JSONObject.fromObject(item);
			if(json.getString("type").equals("Assertions")) {
				AssertEntity pre=(AssertEntity) JSONObject.toBean(json, AssertEntity.class);
				list=pre.getResponseAssertion();
				break;
			}
		}
		return list;
		
	}
	
}
