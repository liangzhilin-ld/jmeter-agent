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

import com.autotest.data.mode.*;
import com.autotest.data.service.impl.*;
import com.autotest.jmeter.jmeteragent.service.TestDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author Techstar
 *
 */
@Repository
@Transactional(rollbackFor=Exception.class)
public class TestDataServiceImpl implements TestDataService {

	private @Autowired ApiHeaderServiceImpl apiHeader;
	private @Autowired TheadGroupConfigServiceImpl theadGroupConfig;
	private @Autowired ApiTestcaseServiceImpl apiTestcase;
	private @Autowired UserDefinedVariableServiceImpl userDefinedVar;
	private @Autowired TestScheduledServiceImpl testSchedule;
	private @Autowired ApiReportServiceImpl apiReport;
	private @Autowired ApiReportHistoryListServiceImpl historyStat;
	private @Autowired SyetemDictionaryServiceImpl syetemDic;
	private @Autowired ProjectManageServiceImpl projectManage;
	private @Autowired ProcessorJdbcServiceImpl jdbcProcess;
	private @Autowired SyetemDbServiceImpl sysDb;
	private @Autowired ApiMockServiceImpl mockData;
	private @Autowired BeanshellServiceImpl beanshell;
	private @Autowired ProcessorJsonServiceImpl jsonServer;
	private @Autowired AssertJsonServiceImpl assertJson;
	private @Autowired AssertResponseServiceImpl assertRes;
	private @Autowired SyetemEnvServiceImpl envServer;
	private List<ApiHeader> headers;

	@Override
	public List<ApiHeader> getApiHeader() {
		List<ApiHeader> apiHeaders=apiHeader.list();
		headers=apiHeaders;
		return apiHeaders;
	}

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
		QueryWrapper<ApiHeader> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ApiHeader::getCaseId,caseId)
							 .ne(ApiHeader::getCaseId, -1);
		List<ApiHeader> apiHeaders=apiHeader.list(queryWrapper);
	    return apiHeaders;
	}
	public List<ApiHeader> getPubHeader(int projectId) {
		QueryWrapper<ApiHeader> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ApiHeader::getProjectId, projectId)
							 .eq(ApiHeader::getCaseId,-1);
		List<ApiHeader> apiHeaders=apiHeader.list(queryWrapper);
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
	public ApiTestcase getTestcaseByID(String id) {
		QueryWrapper<ApiTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("CASE_ID", Integer.valueOf(id));
		return apiTestcase.getOne(queryWrapper);
	}
	public List<ApiTestcase> getTestcaseOfFail(String history) {
		QueryWrapper<ApiReport> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ApiReport::getHistoryId, history)
							.eq(ApiReport::getTcResult,false)
							.select(ApiReport::getCaseId);
		List<ApiReport> ids=apiReport.list(queryWrapper);
		if(ids.size()==0)
			return new ArrayList<ApiTestcase>();
		List<Integer> cids = new ArrayList<Integer>();
		ids.forEach(item->cids.add(item.getCaseId()));
		QueryWrapper<ApiTestcase> wrapper = new QueryWrapper<>();
		wrapper.lambda().in(ApiTestcase::getCaseId, cids);
		return apiTestcase.list(wrapper);
	}
	public List<ApiTestcase> getTestcase() {
		return apiTestcase.list();
	}
	
	/**
	 * 根据用例ID集合查询用例
	 * @param trig 任务
	 * @return
	 */
	public List<ApiTestcase> getTestcaseByIds(TestScheduled trig) {
		if(StrUtil.isEmpty(trig.getTcCaseids()))
			return getTestcase();
		List<String> idList=ListUtil.of(trig.getTcCaseids().split(","));
		QueryWrapper<ApiTestcase> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().in(ApiTestcase::getCaseId, idList);
		return apiTestcase.list(queryWrapper);
	}
	/**
	 * 查询用户自定义变量
	 * @param projectID 项目编号
	 * @return
	 */
	public List<UserDefinedVariable> getArgumentsByPid(int projectID) {
		QueryWrapper<UserDefinedVariable> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(UserDefinedVariable::getProjectId, projectID)
							 .eq(UserDefinedVariable::getCaseId, -1);
		return userDefinedVar.list(queryWrapper);
	}
	/**
	 * 查询自定义变量
	 * @param caseId 用例编号
	 * @return
	 */
	public List<UserDefinedVariable> getArgumentsByCaseId(int caseId) {
		QueryWrapper<UserDefinedVariable> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(UserDefinedVariable::getCaseId, caseId)
							 .ne(UserDefinedVariable::getCaseId, -1);
		return userDefinedVar.list(queryWrapper);
	}
	public ProjectManage getPoject(String projetID) {
		List<ProjectManage> list=projectManage.list();
		for (ProjectManage projectManage : list) {
			if(projectManage.getProjectId().equals(projetID)) {}
				return projectManage;
		}
		return null;
	}

	public ProcessorJdbc getProcessorJdbc(int id,String type) {
		List<ProcessorJdbc> list=jdbcProcess.list();
		for (ProcessorJdbc processor : list) {
			if(processor.getCaseId().equals(id)&&
					processor.getProcessorType().equals(type)) {}
				return processor;
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

	public List<Beanshell> getPreBeanshell(int caseID) {
		QueryWrapper<Beanshell> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(Beanshell::getCaseId, caseID)
		.eq(Beanshell::getBeanshellType,"2");
		return beanshell.list(queryWrapper);
		
	}
	public List<Beanshell> getPostBeanshell(int caseID) {
		QueryWrapper<Beanshell> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(Beanshell::getCaseId, caseID)
		.eq(Beanshell::getBeanshellType,"8");
		return beanshell.list(queryWrapper);
		
	}
	public List<Beanshell> getAssertBeanshell(int caseID) {
		QueryWrapper<Beanshell> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(Beanshell::getCaseId, caseID)
		.eq(Beanshell::getBeanshellType,"11");
		return beanshell.list(queryWrapper);
		
	}
	public List<ProcessorJdbc> getPreJdbc(int caseID) {
		QueryWrapper<ProcessorJdbc> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ProcessorJdbc::getCaseId, caseID)
		.eq(ProcessorJdbc::getProcessorType,"3");
		return jdbcProcess.list(queryWrapper);
		
	}
	public List<ProcessorJdbc> getPostJdbc(int caseID) {
		QueryWrapper<ProcessorJdbc> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ProcessorJdbc::getCaseId, caseID)
		.eq(ProcessorJdbc::getProcessorType,"9");
		return jdbcProcess.list(queryWrapper);
		
	}
	public List<ApiMock> getPreMock(int caseID) {
		QueryWrapper<ApiMock> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ApiMock::getCaseId, caseID);
		return mockData.list(queryWrapper);
		
	}
	public List<ProcessorJson> getPostJson(int caseID) {
		QueryWrapper<ProcessorJson> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ProcessorJson::getCaseId, caseID);
		return jsonServer.list(queryWrapper);
		
	}
	public List<AssertJson> getAssertJson(int caseID) {
		QueryWrapper<AssertJson> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(AssertJson::getCaseId, caseID);
		return assertJson.list(queryWrapper);
		
	}
	public List<AssertResponse> getResponse(int caseID) {
		QueryWrapper<AssertResponse> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(AssertResponse::getCaseId, caseID);
		return assertRes.list(queryWrapper);
		
	}
	
}
