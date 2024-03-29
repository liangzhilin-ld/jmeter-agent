/**
 * 
 */
package com.autotest.jmeter.jmeteragent.common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSONArray;
import com.autotest.data.mode.*;
import com.autotest.data.mode.confelement.ApiHeader;
import com.autotest.data.mode.confelement.UserDefinedVariable;
import com.autotest.data.mode.custom.SamplerReport;
import com.autotest.data.service.impl.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
//import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;

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
	private @Autowired TestScheduledServiceImpl testSchedule;
	private @Autowired ScenarioReportServiceImpl apiReport;
	private @Autowired ApiReportHistoryListServiceImpl historyStat;
	private @Autowired SyetemDictionaryServiceImpl syetemDic;
	private @Autowired ProjectManageServiceImpl projectManage;
	private @Autowired SyetemDbServiceImpl sysDb;
	private @Autowired ApiMockServiceImpl mockData;
	private @Autowired SyetemEnvServiceImpl envServer;
	private @Autowired  ScenarioTestcaseServiceImpl scenarioServer;
	private @Autowired  ScenarioReportServiceImpl reportServer;
	
	public Boolean addReport(ScenarioReport report) {
		return reportServer.save(report);
	}
	public Boolean updateReport(ScenarioReport report) {
		UpdateWrapper<ScenarioReport> updateWrapper = new UpdateWrapper<>();
		updateWrapper.lambda().set(ScenarioReport::getTcSuite, report.getTcSuite())
							.set(ScenarioReport::getTcName, report.getTcName())
							.set(ScenarioReport::getTcResult, report.getTcResult())
							.set(ScenarioReport::getTcDuration, report.getTcDuration())
							.set(ScenarioReport::getTcName, report.getTcName())
							.set(ScenarioReport::getHashtree, JSONArray.toJSONString(report.getHashtree()))
							.set(ScenarioReport::getTcSuite, report.getTcSuite())
							.set(ScenarioReport::getTcResult, report.getTcResult())
							.set(ScenarioReport::getTcType, report.getTcType())
							.setSql("TC_RUNS_NUM=TC_RUNS_NUM+1")
							 .eq(ScenarioReport::getHistoryId, report.getHistoryId())
							 .eq(ScenarioReport::getJobId, report.getJobId())
							 .eq(ScenarioReport::getTcId, report.getTcId());
		return reportServer.update(report, updateWrapper);
	}
	public List<ApiHeader> getSamplerHeader(int caseId) {
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(HttpTestcase::getCaseId,caseId);
		
	    return httpServer.getOne(queryWrapper).getHeaders();
	}
	public List<ApiHeader> getPubHeader(int projectId) {
		QueryWrapper<ProjectManage> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ProjectManage::getProjectId, projectId);
		List<ApiHeader> apiHeaders=projectManage.getOne(queryWrapper).getHeaders();
		if(apiHeaders.size()>0) {
			apiHeaders=(List<ApiHeader>)JSONArray.parseArray(apiHeaders.toString(),ApiHeader.class);
		}
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

	public List<ScenarioReport> getApiReport() {
		return apiReport.list();
	}
	/**
	 * 更新详细用例执行结果
	 * @param report
	 * @return
	 */
//	public Boolean updateApiReport(ApiReport report) {
//		UpdateWrapper<ApiReport> updateWrapper = new UpdateWrapper<>();
//		updateWrapper.lambda().set(ApiReport::getTcSuite, report.getTcSuite())
//							.set(ApiReport::getTcName, report.getTcName())
//							.set(ApiReport::getTcResult, report.getTcResult())
//							.set(ApiReport::getTcDuration, report.getTcDuration())
//							.set(ApiReport::getTcHeader, report.getTcHeader())
//							.set(ApiReport::getTcLog, report.getTcLog())
//							.set(ApiReport::getTcRequest, report.getTcRequest())
//							.set(ApiReport::getTcResponse, report.getTcResponse())
//							.set(ApiReport::getTcAssert, report.getTcAssert())
//							.setSql("TC_RUNS_NUM=TC_RUNS_NUM+1")
//							.set(ApiReport::getCreateTime, report.getCreateTime())
//							 .eq(ApiReport::getHistoryId, report.getHistoryId())
//							 .eq(ApiReport::getJobId, report.getJobId())
//							 .eq(ApiReport::getCaseId, report.getCaseId());
//		return apiReport.update(report,updateWrapper);
//	}
//	public List<TheadGroupConfig> getTheadGroupConfig() {
//		return theadGroupConfig.list();
//	}
	
	/**
	 * 更新历史纪录列表数据
	 * @param job
	 * @return
	 */
	public Boolean updateHistoryListTable(TestScheduled job) {
		QueryWrapper<ScenarioReport> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ScenarioReport::getHistoryId,job.getHistoryId())
							.eq(ScenarioReport::getJobId,job.getId())
							.select(ScenarioReport::getTcId,ScenarioReport::getTcResult);
		List<ScenarioReport> apiList=apiReport.list(queryWrapper);
		List<ScenarioReport> succList=apiList.stream()
				.filter(s -> s.getTcResult()==null||s.getTcResult().equals(true))
				.collect(Collectors.toList());
		UpdateWrapper<ApiReportHistoryList> updateWrapper = new UpdateWrapper<>();
		updateWrapper.lambda().set(ApiReportHistoryList::getTcPassed, succList.size())
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
	public HttpTestcase getTestcaseByID(Integer id) {
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("CASE_ID", id);
		return apiTestcase.getOne(queryWrapper);
	}
	public List<Object> getTestcaseOfFail(String history) {
		List<Object> faileList=new ArrayList<Object>();
		QueryWrapper<ScenarioReport> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ScenarioReport::getHistoryId, history)
							 .and(wrapper -> wrapper.isNull(ScenarioReport::getTcResult)
									 .or().eq(ScenarioReport::getTcResult,false))
							 .select(ScenarioReport::getTcId,ScenarioReport::getTcType);
		List<ScenarioReport> ids=apiReport.list(queryWrapper);
		if(ids.size()==0)
			return faileList;
//		List<Integer> cids = new ArrayList<Integer>();
//		ids.forEach(item->cids.add(Integer.parseInt(item.getTcId())));
//		
		ids.forEach(item->{
			if(item.getTcType().equals(ScenarioTestcase.TYPE_SCENARIO)) {
				QueryWrapper<ScenarioTestcase> wrapper = new QueryWrapper<>();
				wrapper.lambda().eq(ScenarioTestcase::getId, item.getTcId());
				faileList.add(scenarioServer.getOne(wrapper));
			}else {
				QueryWrapper<HttpTestcase> wrapper = new QueryWrapper<>();
				wrapper.lambda().eq(HttpTestcase::getCaseId, item.getTcId());
				faileList.add(httpServer.getOne(wrapper));
			}
		});
		return faileList;
//		QueryWrapper<HttpTestcase> wrapper = new QueryWrapper<>();
//		wrapper.lambda().in(HttpTestcase::getCaseId, cids);
//		return httpServer.list(wrapper);
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
		List<Integer> idList=trig.getTcCaseids().get(TestScheduled.TYPE_SAMPLER);
		if(idList.size()==0||idList==null)
			return new ArrayList<HttpTestcase>();//getTestcase();
		QueryWrapper<HttpTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().in(HttpTestcase::getCaseId, idList);
		return httpServer.list(queryWrapper);
	}
	
	public ScenarioTestcase getScenariosByid(int id) {		

		return scenarioServer.getById(id);
	}
	
	public List<ScenarioTestcase> getScenarios(TestScheduled trig) {		
		List<Integer> idList=trig.getTcCaseids().get(TestScheduled.TYPE_SCENARIO);
		if(idList.size()==0||idList==null)
			return new ArrayList<ScenarioTestcase>();
		QueryWrapper<ScenarioTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().in(ScenarioTestcase::getId, idList);
		return scenarioServer.list(queryWrapper);
	}
	
	/**
	 * 获取登陆场景
	 * @param trig
	 * @return
	 */
	public ScenarioTestcase getLoginScenarios(Integer pid) {		
//		List<Integer> idList=trig.getTcCaseids().get("scenarioIds");
//		if(idList.size()==0||idList==null)
//			return null;
//		QueryWrapper<ScenarioTestcase> queryWrapper1 = new QueryWrapper<>();
//		queryWrapper1.lambda().eq(ScenarioTestcase::getId, idList.get(0));
//		String pid=scenarioServer.getOne(queryWrapper1).getProjectId();
		QueryWrapper<ScenarioTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ScenarioTestcase::getTag, "登陆")
							 .eq(ScenarioTestcase::getProjectId, pid);
		return scenarioServer.getOne(queryWrapper, false);
	}
	
	/**
	 * 查询用户自定义变量
	 * @param projectID 项目编号
	 * @return
	 */
	public List<UserDefinedVariable> getArgumentsByPid(int projectID) {
		QueryWrapper<ProjectManage> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ProjectManage::getProjectId, projectID);
		List<UserDefinedVariable> listarg=projectManage.getOne(queryWrapper,false).getArguments();
		listarg=(List<UserDefinedVariable>)JSONArray.parseArray(listarg.toString(),UserDefinedVariable.class);
		return listarg;
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
			if(projectManage.getProjectId().equals(Integer.parseInt(projetID))) {}
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
	public List<ApiMock> getPreMock(List<Integer> ids) {
	QueryWrapper<ApiMock> mockqrapper = new QueryWrapper<>();
	mockqrapper.lambda().in(ApiMock::getId, ids);
	return mockData.list(mockqrapper);
	
	}
	
}
