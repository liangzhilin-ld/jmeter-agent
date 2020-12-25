/**
 * 
 */
package com.autotest.jmeter.jmeteragent.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.autotest.data.mode.*;
import com.autotest.data.service.impl.*;
import com.autotest.jmeter.jmeteragent.service.TestDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author Techstar
 *
 */
@Component
public class TestDataServiceImpl implements TestDataService {

	private @Autowired ApiHeaderServiceImpl apiHeader;
	private @Autowired TheadGroupConfigServiceImpl theadGroupConfig;
	private @Autowired ApiTestcaseServiceImpl apiTestcase;
	private @Autowired UserDefinedVariableServiceImpl userDefinedVar;
	private @Autowired TestScheduledServiceImpl testSchedule;
	private @Autowired ApiReportServiceImpl apiReport;
	private @Autowired SyetemDictionaryServiceImpl syetemDic;
	private @Autowired ProjectManageServiceImpl projectManage;
	private @Autowired ProcessorJdbcServiceImpl jdbcProcess;
	private @Autowired SyetemDbServiceImpl sysDb;
	private @Autowired ApiMockServiceImpl mockData;
	private @Autowired BeanshellServiceImpl beanshell;
	private @Autowired TestScheduledServiceImpl jsobService;
	private List<ApiHeader> headers;

	@Override
	public List<ApiHeader> getApiHeader() {
		List<ApiHeader> apiHeaders=apiHeader.list();
		headers=apiHeaders;
		return apiHeaders;
	}
	
	public Map<String, String> getTestPlanHeader(int projectID){
		getApiHeader();
		Map<String, String> headerMap=new HashMap<String, String>();
		for (ApiHeader header : headers) {
			if(header.getProjectId().equals(projectID)&&header.getType().equals("0"))
				headerMap.put(header.getKey(), header.getValue());
		}
		return headerMap;
	} 
	public Map<String, String>  getSamplerHeader(int projectID,int caseID){
		Map<String, String> headerMap=new HashMap<String, String>();
		for (ApiHeader header : headers) {
			if(header.getProjectId().equals(projectID)&&header.getCaseId().equals(caseID))
				headerMap.put(header.getKey(), header.getValue());
		}
		return headerMap;
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
	public List<TheadGroupConfig> getTheadGroupConfig() {
		return theadGroupConfig.list();
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
	public List<UserDefinedVariable> getUserDefinedVar(int projectID) {
		QueryWrapper<UserDefinedVariable> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(UserDefinedVariable::getProjectId, projectID);
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
	public Beanshell getBeanshell(int caseID) {
		QueryWrapper<Beanshell> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("CASE_ID", caseID);
		return beanshell.getOne(queryWrapper);
		
	}
}
