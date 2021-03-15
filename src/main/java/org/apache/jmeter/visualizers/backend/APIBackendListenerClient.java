package org.apache.jmeter.visualizers.backend;

//import io.metersphere.api.dto.scenario.request.RequestType;
//import io.metersphere.api.service.*;
//import io.metersphere.base.domain.ApiScenarioReport;
//import io.metersphere.base.domain.ApiTestReport;
//import io.metersphere.commons.constants.APITestStatus;
//import io.metersphere.commons.constants.ApiRunMode;
//import io.metersphere.commons.constants.NoticeConstants;
//import io.metersphere.commons.constants.TestPlanTestCaseStatus;
//import io.metersphere.commons.utils.CommonBeanFactory;
//import io.metersphere.commons.utils.LogUtil;
//import io.metersphere.dto.BaseSystemConfigDTO;
//import io.metersphere.i18n.Translator;
//import io.metersphere.notice.sender.NoticeModel;
//import io.metersphere.notice.service.NoticeSendService;
//import io.metersphere.service.SystemParameterService;
//import io.metersphere.track.service.TestPlanTestCaseService;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;

import com.alibaba.fastjson.JSON;
import com.autotest.data.enums.ApiRunMode;
import com.autotest.data.mode.ScenarioReport;
import com.autotest.data.mode.ScenarioTestcase;
import com.autotest.data.mode.custom.SamplerLable;
import com.autotest.data.mode.custom.SamplerReport;
import com.autotest.jmeter.jmeteragent.service.impl.JmeterHashTreeServiceImpl;
import com.autotest.jmeter.jmeteragent.service.impl.TestDataServiceImpl;
import com.autotest.util.SpringContextUtil;

import cn.hutool.core.util.ReUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.*;

/**
 * JMeter BackendListener扩展, jmx脚本中使用
 */
public class APIBackendListenerClient extends AbstractBackendListenerClient implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(APIBackendListenerClient.class);
    public final static String JOB_ID = "ms.test.id";

    private final static String THREAD_SPLIT = " ";

    private final static String ID_SPLIT = "-";

    private final List<SampleResult> queue = new ArrayList<>();

//    private APITestService apiTestService;
//
//    private APIReportService apiReportService;
//
//    private ApiDefinitionService apiDefinitionService;
//
//    private ApiDefinitionExecResultService apiDefinitionExecResultService;
//
//    private ApiScenarioReportService apiScenarioReportService;


//    public String runMode = ApiRunMode.RUN.name();

    // 测试ID
    private String jobId;
    public String runMode;
    private String historyId;

    //获得控制台内容
    private PrintStream oldPrintStream = System.out;
    private ByteArrayOutputStream bos = new ByteArrayOutputStream();

    private void setConsole() {
        System.setOut(new PrintStream(bos)); //设置新的out
    }

    private String getConsole() {
        System.setOut(oldPrintStream);
        return bos.toString();
    }

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        setConsole();
        setParam(context);
//        apiTestService = CommonBeanFactory.getBean(APITestService.class);
//        if (apiTestService == null) {
//            LogUtil.error("apiTestService is required");
//        }
//
//        apiReportService = CommonBeanFactory.getBean(APIReportService.class);
//        if (apiReportService == null) {
//            LogUtil.error("apiReportService is required");
//        }
//        apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
//        if (apiDefinitionService == null) {
//            LogUtil.error("apiDefinitionService is required");
//        }
//        apiDefinitionExecResultService = CommonBeanFactory.getBean(ApiDefinitionExecResultService.class);
//        if (apiDefinitionExecResultService == null) {
//            LogUtil.error("apiDefinitionExecResultService is required");
//        }
//
//        apiScenarioReportService = CommonBeanFactory.getBean(ApiScenarioReportService.class);
//        if (apiScenarioReportService == null) {
//            LogUtil.error("apiScenarioReportService is required");
//        }

        super.setupTest(context);
    }


    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        queue.addAll(sampleResults);
        writeSamplers(sampleResults);
    }
    @Async
	private void writeSamplers(List<SampleResult> sampleResults) {
    	sampleResults.forEach(res->writeSampler(res));
    }
    private void writeSampler(SampleResult sampleResults) {
    	
    	ScenarioReport report=new ScenarioReport();
    	SamplerReport single=new SamplerReport();
    	List<SamplerReport> listsam=new ArrayList<SamplerReport>();
    	String caseName=sampleResults.getSampleLabel();
    	Boolean isScenaro=sampleResults.getResponseMessage().startsWith("Number of samples in transaction");
    	SampleResult[] sub=sampleResults.getSubResults();
    	if(isScenaro) {
    		SamplerLable data = JSON.parseObject(caseName, SamplerLable.class);
        	if(data.getIsLogin()&&sampleResults.isSuccessful())
        		return;
        	if(sub.length>0){
        		for (int i = 0; i < sub.length; i++) {
        			single.setCaseId(JSON.parseObject(sub[i].getSampleLabel(), SamplerLable.class).getCaseId());
        			single.setIsSuccess(sub[i].isSuccessful());
        			single.setTcName(sub[i].getSampleLabel());
        			single.setTcDuration(String.valueOf(sub[i].getLatency()));
        			single.setTcHeader(sub[i].getRequestHeaders());
        			String tcLog="Response code:"+sub[i].getResponseCode()+"\r\n"+
    					  	 "Response message: "+sub[i].getResponseMessage();
        			single.setTcLog(tcLog);
        			single.setTcRequest(sub[i].getSamplerData());
        			single.setTcResponse(sub[i].getResponseDataAsString());
        			List<String> listAssert=new ArrayList<String>();
        			for(AssertionResult assR:sub[i].getAssertionResults()){
        				if(!assR.isFailure())
        					listAssert.add(assR.getName()+"|"+assR.getFailureMessage());
        			} 
        			single.setTcAssert(listAssert);
        			listsam.add(single);
        			report.setTcName(data.getCaseName());
        			report.setTcId(data.getCaseId());
        			report.setTcSuite(data.getSuiteId().toString());
        			report.setTcType(ScenarioTestcase.TYPE_SCENARIO);
    			}
        	}
    	}else {
    		single.setTcHeader(sampleResults.getRequestHeaders());
			String tcLog="Response code:"+sampleResults.getResponseCode()+"\r\n"+
				  	 "Response message: "+sampleResults.getResponseMessage();
			single.setTcLog(tcLog);
			single.setTcRequest(sampleResults.getSamplerData());
			single.setTcResponse(sampleResults.getResponseDataAsString());
			List<String> listAssert=new ArrayList<String>();
			for(AssertionResult assR:sampleResults.getAssertionResults()){
				if(!assR.isFailure())
					listAssert.add(assR.getName()+"|"+assR.getFailureMessage());
			}
			single.setTcAssert(listAssert);
			
			single.setCaseId(getHeaderKey("CASE_ID:(.+)",single.getTcHeader()));
			
			single.setTcSuite(getHeaderKey("SUITE_Id:(.+)",single.getTcHeader()));
			single.setIsSuccess(sampleResults.isSuccessful());
			single.setJobId(this.jobId);
			single.setTcDuration(String.valueOf(sampleResults.getLatency()));
			listsam.add(single);
			report.setTcName(caseName);
			report.setTcId(single.getCaseId());
			report.setTcSuite(single.getTcSuite());
    	}
		report.setHashtree(listsam);
//		report.setTcName(data.getCaseName());
//		report.setTcId(data.getCaseId());
//		report.setTcSuite(data.getSuiteId().toString());
		
		report.setTcResult(sampleResults.isSuccessful());
		report.setTcDuration(String.valueOf(sampleResults.getLatency()));
		report.setHistoryId(this.historyId);
		report.setJobId(this.jobId);
		report.setProjectId(0);
		
		if(isScenaro)report.setTcType(ScenarioTestcase.TYPE_SCENARIO);
//		if(report.getId()==null)report.setId(1);
		SpringContextUtil.getBean(TestDataServiceImpl.class).updateReport(report);
    }
    
    public String getHeaderKey(String pattern,String content) {
    	
    	String value=ReUtil.get(pattern, content, 1);
    	if(value==null||!(content.length()>0))
    		return "";
    	BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(value.getBytes())));
    	try {
    		value=br.readLine().toString().trim();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			value="";
		}
    	return value;
    }
    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
    	int ll=queue.size();
    	queue.forEach(result->{
    		            // 线程名称: <场景名> <场景Index>-<请求Index>, 例如：Scenario 2-1
          String scenarioName = StringUtils.substringBeforeLast(result.getThreadName(), THREAD_SPLIT);
          String index = StringUtils.substringAfterLast(result.getThreadName(), THREAD_SPLIT);
          String scenarioId = StringUtils.substringBefore(index, ID_SPLIT);
                  	});

    	System.out.println(getConsole());

    	Thread.currentThread().interrupt();
//        TestResult testResult = new TestResult();
//        testResult.setTestId(testId);
//        testResult.setTotal(queue.size()); 
//
//        // 一个脚本里可能包含多个场景(ThreadGroup)，所以要区分开，key: 场景Id
//        final Map<String, ScenarioResult> scenarios = new LinkedHashMap<>();
//        queue.forEach(result -> {
//            // 线程名称: <场景名> <场景Index>-<请求Index>, 例如：Scenario 2-1
//            String scenarioName = StringUtils.substringBeforeLast(result.getThreadName(), THREAD_SPLIT);
//            String index = StringUtils.substringAfterLast(result.getThreadName(), THREAD_SPLIT);
//            String scenarioId = StringUtils.substringBefore(index, ID_SPLIT);
//            ScenarioResult scenarioResult;
//            if (!scenarios.containsKey(scenarioId)) {
//                scenarioResult = new ScenarioResult();
//                try {
//                    scenarioResult.setId(Integer.parseInt(scenarioId));
//                } catch (Exception e) {
//                    scenarioResult.setId(0);
//                    LogUtil.error("场景ID转换异常: " + e.getMessage());
//                }
//                scenarioResult.setName(scenarioName);
//                scenarios.put(scenarioId, scenarioResult);
//            } else {
//                scenarioResult = scenarios.get(scenarioId);
//            }
//
//            if (result.isSuccessful()) {
//                scenarioResult.addSuccess();
//                testResult.addSuccess();
//            } else {
//                scenarioResult.addError(result.getErrorCount());
//                testResult.addError(result.getErrorCount());
//            }
//
//            RequestResult requestResult = getRequestResult(result);
//            scenarioResult.getRequestResults().add(requestResult);
//            scenarioResult.addResponseTime(result.getTime());
//
//            testResult.addPassAssertions(requestResult.getPassAssertions());
//            testResult.addTotalAssertions(requestResult.getTotalAssertions());
//
//            scenarioResult.addPassAssertions(requestResult.getPassAssertions());
//            scenarioResult.addTotalAssertions(requestResult.getTotalAssertions());
//        });
//        testResult.getScenarios().addAll(scenarios.values());
//        testResult.getScenarios().sort(Comparator.comparing(ScenarioResult::getId));
//        ApiTestReport report = null;
//        String reportUrl = null;
//        // 这部分后续优化只留 DELIMIT 和 SCENARIO 两部分
//        if (StringUtils.equals(this.runMode, ApiRunMode.DEBUG.name())) {
//            report = apiReportService.get(debugReportId);
//            apiReportService.complete(testResult, report);
//        } else if (StringUtils.equals(this.runMode, ApiRunMode.DELIMIT.name())) {
//            // 调试操作，不需要存储结果
//            if (StringUtils.isBlank(debugReportId)) {
//                apiDefinitionService.addResult(testResult);
//            } else {
//                apiDefinitionService.addResult(testResult);
//                apiDefinitionExecResultService.saveApiResult(testResult, ApiRunMode.DELIMIT.name());
//            }
//        } else if (StringUtils.equals(this.runMode, ApiRunMode.API_PLAN.name())) {
//            apiDefinitionService.addResult(testResult);
//            apiDefinitionExecResultService.saveApiResult(testResult, ApiRunMode.API_PLAN.name());
//        } else if (StringUtils.equalsAny(this.runMode, ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name())) {
//            // 执行报告不需要存储，由用户确认后在存储
//            testResult.setTestId(testId);
//            ApiScenarioReport scenarioReport = apiScenarioReportService.complete(testResult, this.runMode);
//
//            report = new ApiTestReport();
//            report.setStatus(scenarioReport.getStatus());
//            report.setId(scenarioReport.getId());
//            report.setTriggerMode(scenarioReport.getTriggerMode());
//            report.setName(scenarioReport.getName());
//
//            SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
//            assert systemParameterService != null;
//            BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
//            reportUrl = baseSystemConfigDTO.getUrl() + "/#/api/automation/report";
//
//            testResult.setTestId(scenarioReport.getScenarioId());
//        } else {
//            apiTestService.changeStatus(testId, APITestStatus.Completed);
//            report = apiReportService.getRunningReport(testResult.getTestId());
//            apiReportService.complete(testResult, report);
//        }
        queue.clear();
        super.teardownTest(context);

//        TestPlanTestCaseService testPlanTestCaseService = CommonBeanFactory.getBean(TestPlanTestCaseService.class);
//        List<String> ids = testPlanTestCaseService.getTestPlanTestCaseIds(testResult.getTestId());
//        if (ids.size() > 0) {
//            try {
//                if (StringUtils.equals(APITestStatus.Success.name(), report.getStatus())) {
//                    testPlanTestCaseService.updateTestCaseStates(ids, TestPlanTestCaseStatus.Pass.name());
//                } else {
//                    testPlanTestCaseService.updateTestCaseStates(ids, TestPlanTestCaseStatus.Failure.name());
//                }
//            } catch (Exception e) {
//                LogUtil.error(e.getMessage(), e);
//            }
//        }
//        try {
//            sendTask(report, reportUrl, testResult);
//        } catch (Exception e) {
//            LogUtil.error(e.getMessage(), e);
//        }

    }

//    private static void sendTask(ApiTestReport report, String reportUrl, TestResult testResult) {
//        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
//        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
//        assert systemParameterService != null;
//        assert noticeSendService != null;
//
//        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
//        String url = reportUrl;
//        if (StringUtils.isEmpty(url)) {
//            url = baseSystemConfigDTO.getUrl() + "/#/api/report/view/" + report.getId();
//        }
//        String successContext = "";
//        String failedContext = "";
//        String subject = "";
//        String event = "";
//        if (StringUtils.equals(NoticeConstants.Mode.API, report.getTriggerMode())) {
//            successContext = "接口测试 API任务通知:'" + report.getName() + "'执行成功" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
//            failedContext = "接口测试 API任务通知:'" + report.getName() + "'执行失败" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
//            subject = Translator.get("task_notification_jenkins");
//        }
//        if (StringUtils.equals(NoticeConstants.Mode.SCHEDULE, report.getTriggerMode())) {
//            successContext = "接口测试定时任务通知:'" + report.getName() + "'执行成功" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
//            failedContext = "接口测试定时任务通知:'" + report.getName() + "'执行失败" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
//            subject = Translator.get("task_notification");
//        }
//        if (StringUtils.equals("Success", report.getStatus())) {
//            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
//        }
//        if (StringUtils.equals("Error", report.getStatus())) {
//            event = NoticeConstants.Event.EXECUTE_FAILED;
//        }
//        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("testName", report.getName());
//        paramMap.put("id", report.getId());
//        paramMap.put("type", "api");
//        paramMap.put("url", baseSystemConfigDTO.getUrl());
//        paramMap.put("status", report.getStatus());
//        NoticeModel noticeModel = NoticeModel.builder()
//                .successContext(successContext)
//                .successMailTemplate("ApiSuccessfulNotification")
//                .failedContext(failedContext)
//                .failedMailTemplate("ApiFailedNotification")
//                .testId(testResult.getTestId())
//                .status(report.getStatus())
//                .event(event)
//                .subject(subject)
//                .paramMap(paramMap)
//                .build();
//        noticeSendService.send(report.getTriggerMode(), noticeModel);
//    }
                                                                                                                                                                                                                                                                                                                                                                                                             
//    private RequestResult getRequestResult(SampleResult result) {
//        RequestResult requestResult = new RequestResult();
//        requestResult.setName(result.getSampleLabel());
//        requestResult.setUrl(result.getUrlAsString());
//        requestResult.setMethod(getMethod(result));
//        requestResult.setBody(result.getSamplerData());
//        requestResult.setHeaders(result.getRequestHeaders());
//        requestResult.setRequestSize(result.getSentBytes());
//        requestResult.setStartTime(result.getStartTime());
//        requestResult.setTotalAssertions(result.getAssertionResults().length);
//        requestResult.setSuccess(result.isSuccessful());
//        requestResult.setError(result.getErrorCount());
//        if (result instanceof HTTPSampleResult) {
//            HTTPSampleResult res = (HTTPSampleResult) result;
//            requestResult.setCookies(res.getCookies());
//        }
//
//        for (SampleResult subResult : result.getSubResults()) {
//            requestResult.getSubRequestResults().add(getRequestResult(subResult));
//        }
//        for (SampleResult subResult : result.getSubResults()) {
//            requestResult.getSubRequestResults().add(getRequestResult(subResult));
//        }
//
//        ResponseResult responseResult = requestResult.getResponseResult();
//        responseResult.setBody(result.getResponseDataAsString());
//        responseResult.setHeaders(result.getResponseHeaders());
//        responseResult.setLatency(result.getLatency());
//        responseResult.setResponseCode(result.getResponseCode());
//        responseResult.setResponseSize(result.getResponseData().length);
//        responseResult.setResponseTime(result.getTime());
//        responseResult.setResponseMessage(result.getResponseMessage());
//
//        if (JMeterVars.get(result.hashCode()) != null) {
//            List<String> vars = new LinkedList<>();
//            JMeterVars.get(result.hashCode()).entrySet().parallelStream().reduce(vars, (first, second) -> {
//                first.add(second.getKey() + "：" + second.getValue());
//                return first;
//            }, (first, second) -> {
//                if (first == second) {
//                    return first;
//                }
//                first.addAll(second);
//                return first;
//            });
//            responseResult.setVars(StringUtils.join(vars, "\n"));
//            JMeterVars.remove(result.hashCode());
//        }
//        for (AssertionResult assertionResult : result.getAssertionResults()) {
//            ResponseAssertionResult responseAssertionResult = getResponseAssertionResult(assertionResult);
//            if (responseAssertionResult.isPass()) {
//                requestResult.addPassAssertions();
//            }
//            //xpath 提取错误会添加断言错误
//            if (StringUtils.isBlank(responseAssertionResult.getMessage()) || !responseAssertionResult.getMessage().contains("The required item type of the first operand of")) {
//                responseResult.getAssertions().add(responseAssertionResult);
//            }
//        }
//        responseResult.setConsole(getConsole());
//
//        return requestResult;
//    }

//    private String getMethod(SampleResult result) {
//        String body = result.getSamplerData();
//        // Dubbo Protocol
//        String start = "RPC Protocol: ";
//        String end = "://";
//        if (StringUtils.contains(body, start)) {
//            String protocol = StringUtils.substringBetween(body, start, end);
//            if (StringUtils.isNotEmpty(protocol)) {
//                return protocol.toUpperCase();
//            }
//            return RequestType.DUBBO;
//        } else {
//            // Http Method
//            String method = StringUtils.substringBefore(body, " ");
//            for (HttpMethod value : HttpMethod.values()) {
//                if (StringUtils.equals(method, value.name())) {
//                    return method;
//                }
//            }
//            return "Request";
//        }
//    }
//
    private void setParam(BackendListenerContext context) {
        this.jobId = context.getParameter(JOB_ID);
        this.runMode = context.getParameter("runMode");
        this.historyId = context.getParameter("historyId");
        if (StringUtils.isBlank(this.runMode)) {
            this.runMode = ApiRunMode.RUN.name();
        }
    }
//
//    private ResponseAssertionResult getResponseAssertionResult(AssertionResult assertionResult) {
//        ResponseAssertionResult responseAssertionResult = new ResponseAssertionResult();
//        responseAssertionResult.setName(assertionResult.getName());
//        responseAssertionResult.setPass(!assertionResult.isFailure() && !assertionResult.isError());
//        if (!responseAssertionResult.isPass()) {
//            responseAssertionResult.setMessage(assertionResult.getFailureMessage());
//        }
//        return responseAssertionResult;
//    }

}
