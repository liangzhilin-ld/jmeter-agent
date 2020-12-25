package com.autotest.jmeter.jmeteragent.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autotest.data.mode.ApiHeader;
import com.autotest.data.mode.ApiTestcase;
import com.autotest.data.mode.ProjectManage;
import com.autotest.data.mode.TestScheduled;
import com.autotest.data.mode.UserDefinedVariable;
import com.autotest.data.service.impl.ApiHeaderServiceImpl;
import com.autotest.data.service.impl.UserDefinedVariableServiceImpl;
import com.autotest.jmeter.component.ConfigElement;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Service
public class JmeterHashTreeServiceImpl {
	private @Autowired TestDataServiceImpl testDadaService;
	private @Autowired TestDataServiceImpl testData;
	private @Autowired UserDefinedVariableServiceImpl userDefinedVar;
	private void createConfig(TestScheduled trig) {
		trig.getNumOfConcurrent();
		String[] ids=trig.getTcCaseids().split(",");
		for (String caseId : ids) {
			ApiTestcase testcase = testDadaService.getTestcaseByID(caseId);
			List<UserDefinedVariable> udvs=testDadaService.getUserDefinedVar(testcase.getProjectId());
			List<UserDefinedVariable> pubUserdvs=udvs.stream()
					.filter(u -> u.getCaseId() == null)
					.collect(Collectors.toList());
			List<UserDefinedVariable> caseUserdvs=udvs.stream()
					.filter(u -> u.getCaseId() == testcase.getCaseId())
					.collect(Collectors.toList());
		}
	}
	
	
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
	private @Autowired ApiHeaderServiceImpl apiHeader;
	
	public List<ApiHeader> getPubHeader(TestScheduled trig) {
		String cid=trig.getTcCaseids().split(",")[0];
		ApiTestcase testcase = testDadaService.getTestcaseByID(cid);
		QueryWrapper<ApiHeader> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ApiHeader::getProjectId, testcase.getProjectId())
							 .eq(ApiHeader::getType,"0");
		List<ApiHeader> apiHeaders=apiHeader.list(queryWrapper);
	    return apiHeaders;
	}
	public List<ApiHeader> getHeader(int caseId) {
		ApiTestcase testcase = testDadaService.getTestcaseByID(String.valueOf(caseId));
		QueryWrapper<ApiHeader> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ApiHeader::getProjectId, testcase.getProjectId())
							 .eq(ApiHeader::getCaseId,caseId)
							 .eq(ApiHeader::getType,"1");
		List<ApiHeader> apiHeaders=apiHeader.list(queryWrapper);
	    return apiHeaders;
	}
	/**
	 * 获取单个测试用例自定义参数
	 * @param trig
	 * @return
	 */
	public Arguments getArguments(String caseid) {
		QueryWrapper<UserDefinedVariable> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(UserDefinedVariable::getCaseId, Integer.parseInt(caseid))
							 .eq(UserDefinedVariable::getType, "1");
		List<UserDefinedVariable> definedVars=userDefinedVar.list(queryWrapper);
		Arguments args=ConfigElement.createArguments2(definedVars);
		return args;
	}
	
}
