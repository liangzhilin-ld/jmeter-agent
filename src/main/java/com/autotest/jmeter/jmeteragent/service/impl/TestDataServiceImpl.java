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
	private List<ApiHeader> headers;
	
	@Override
	public List<ApiHeader> getApiHeader() {
		List<ApiHeader> apiHeaders=apiHeader.list();
		headers=apiHeaders;
		return apiHeaders;
	}
	
	public Map<String, String>  getTestPlanHeader(int projectID){
		Map<String, String> headerMap=new HashMap<String, String>();
		for (ApiHeader header : headers) {
			if(header.getProjectId().equals(projectID))
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
	public List<ApiTestcase> getTestcase() {
		return apiTestcase.list();
	}
	public List<UserDefinedVariable> getUserDefinedVar() {
		return userDefinedVar.list();
	}
	
}
