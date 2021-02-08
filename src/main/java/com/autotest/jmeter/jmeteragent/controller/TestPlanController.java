package com.autotest.jmeter.jmeteragent.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autotest.data.mode.ApiReport;
import com.autotest.data.mode.HttpTestcase;
import com.autotest.data.mode.ScenarioTestcase;
//import com.autotest.data.mode.ApiTestcase2;
import com.autotest.data.mode.TestScheduled;
//import com.techstar.dmp.jmeteragent.bean.Response;
import com.autotest.jmeter.jmeteragent.service.TestPlanService;
import com.autotest.jmeter.jmeteragent.service.impl.JmeterHashTreeServiceImpl;
import com.autotest.jmeter.jmeteragent.service.impl.TestDataServiceImpl;
import com.autotest.jmeter.jmeteragent.service.impl.TestPlanServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "TestPlan操作接口")
@RestController
public class TestPlanController {
	private static final Logger log = LoggerFactory.getLogger(TestPlanServiceImpl.class);

	
	private @Autowired TestPlanService testPlanService;
	private @Autowired JmeterHashTreeServiceImpl debugrePonse;

	/**
	 * 启动测试计划
	 */
//    @RequestMapping(value = "/startTestPlan", method = RequestMethod.POST)
//    public Response<Map<String, Object>> startTestPlan(@RequestBody String jsonString) {
//    	String jsonstr="{\"duration\":1,\"host\":\"http://172.16.206.128:8888/service\",\"httpTimeout\":3000,\"testCaseQuantity\":2,\"testCases\":[{\"assertion\":{\"paths\":[],\"validationType\":\"jsonpath\"},\"method\":\"POST\",\"requestBody\":\"{\\\"query\\\":{\\\"id\\\":\\\"${mock_uuid()}\\\"},\\\"pageSize\\\":\\\"10\\\",\\\"pageNum\\\":\\\"1\\\",\\\"order\\\":[{\\\"field\\\":\\\"id\\\",\\\"table\\\":\\\"ecJobsDicType\\\",\\\"order\\\":\\\"asc\\\"}]}\",\"testCaseId\":\"a6a3a43de25b400f88c89b19d38a072a\",\"url\":\"/dcp-ec-ecJobsDicType-service/queryecJobsDicType\"},{\"assertion\":{\"paths\":[],\"validationType\":\"jsonpath\"},\"method\":\"POST\",\"requestBody\":\"{\\\"query\\\":{\\\"id\\\":\\\"${mock_uuid()}\\\"},\\\"pageSize\\\":\\\"10\\\",\\\"pageNum\\\":\\\"1\\\",\\\"order\\\":[{\\\"field\\\":\\\"id\\\",\\\"table\\\":\\\"ecOrganvarianceHistory\\\",\\\"order\\\":\\\"asc\\\"}]}\",\"testCaseId\":\"96fa020bba3c449eb6574b803e3d8b35\",\"url\":\"/dcp-ec-ecOrganvarianceHistory-service/queryecOrganvarianceHistory\"}],\"testRecordId\":\"c85d838c-33e9-4ec1-8428-e0c9fe456618\",\"threadNum\":100}";
//        System.out.println("接收到测试请求：");
//        System.out.println(jsonString);
//        log.info("启动测试");
//        try {
//            testPlanService.startTestPlan(jsonString);
//            System.out.println(jsonString);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return Response.error(101, "启动测试失败");
//        }
//        Response<Map<String, Object>> response = new Response<>();
//        Map<String, Object> data = new HashMap<>();
//        data.put("taskId", "demo");
//        data.put("detail", "启动执行！");
//        response.setData(data);
//        return response;
//    }
//    
//    /**
//     * 启动测试计划
//     */
//    @RequestMapping(value = "/startTest", method = RequestMethod.GET)
//    public Response<Map<String, Object>> start() {
//    	String jsonstr="{\"duration\":1,\"host\":\"http://172.16.206.128:8888/service\",\"httpTimeout\":3000,\"testCaseQuantity\":2,\"testCases\":[{\"assertion\":{\"paths\":[],\"validationType\":\"jsonpath\"},\"method\":\"POST\",\"requestBody\":\"{\\\"query\\\":{\\\"id\\\":\\\"${mock_uuid()}\\\"},\\\"pageSize\\\":\\\"10\\\",\\\"pageNum\\\":\\\"1\\\",\\\"order\\\":[{\\\"field\\\":\\\"id\\\",\\\"table\\\":\\\"ecJobsDicType\\\",\\\"order\\\":\\\"asc\\\"}]}\",\"testCaseId\":\"a6a3a43de25b400f88c89b19d38a072a\",\"url\":\"/dcp-ec-ecJobsDicType-service/queryecJobsDicType\"},{\"assertion\":{\"paths\":[],\"validationType\":\"jsonpath\"},\"method\":\"POST\",\"requestBody\":\"{\\\"query\\\":{\\\"id\\\":\\\"${mock_uuid()}\\\"},\\\"pageSize\\\":\\\"10\\\",\\\"pageNum\\\":\\\"1\\\",\\\"order\\\":[{\\\"field\\\":\\\"id\\\",\\\"table\\\":\\\"ecOrganvarianceHistory\\\",\\\"order\\\":\\\"asc\\\"}]}\",\"testCaseId\":\"96fa020bba3c449eb6574b803e3d8b35\",\"url\":\"/dcp-ec-ecOrganvarianceHistory-service/queryecOrganvarianceHistory\"}],\"testRecordId\":\"c85d838c-33e9-4ec1-8428-e0c9fe456618\",\"threadNum\":100}";
//        System.out.println("接收到测试请求：");
//        System.out.println(jsonstr);
//        log.info("启动测试");
//        try {
//            testPlanService.startTestPlan(jsonstr);
//            System.out.println(jsonstr);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return Response.error(101, "启动测试失败");
//        }
//        Response<Map<String, Object>> response = new Response<>();
//        Map<String, Object> data = new HashMap<>();
//        data.put("taskId", "demo");
//        data.put("detail", "启动执行！");
//        response.setData(data);
//        return response;
//    }
//
    /**
     * 停止测试
     */
	@ApiOperation(value = "停止测试")
    @RequestMapping(value = "/stopTestPlan", method = RequestMethod.GET)
    public Map<String, Object> stopTestPlan() {
        testPlanService.stopTestPlan();
        //Response<Map<String, Object>> response = new Response<>();
        //Map<String, Object> response=new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", "demo");
        data.put("detail", "停止任务执行！");
//        response.setData(data);
        return data;
    }

	/**
	 * 启动测试计划
	 */
	@ApiOperation(value = "开始测试")
	@RequestMapping(value = "/startTest", method = RequestMethod.POST)
	public String startPlan(@RequestBody TestScheduled job) {
		//String jsonstr = "{\"duration\":1,\"host\":\"http://172.16.206.128:8888/service\",\"httpTimeout\":3000,\"testCaseQuantity\":2,\"testCases\":[{\"assertion\":{\"paths\":[],\"validationType\":\"jsonpath\"},\"method\":\"POST\",\"requestBody\":\"{\\\"query\\\":{\\\"id\\\":\\\"${mock_uuid()}\\\"},\\\"pageSize\\\":\\\"10\\\",\\\"pageNum\\\":\\\"1\\\",\\\"order\\\":[{\\\"field\\\":\\\"id\\\",\\\"table\\\":\\\"ecJobsDicType\\\",\\\"order\\\":\\\"asc\\\"}]}\",\"testCaseId\":\"a6a3a43de25b400f88c89b19d38a072a\",\"url\":\"/dcp-ec-ecJobsDicType-service/queryecJobsDicType\"},{\"assertion\":{\"paths\":[],\"validationType\":\"jsonpath\"},\"method\":\"POST\",\"requestBody\":\"{\\\"query\\\":{\\\"id\\\":\\\"${mock_uuid()}\\\"},\\\"pageSize\\\":\\\"10\\\",\\\"pageNum\\\":\\\"1\\\",\\\"order\\\":[{\\\"field\\\":\\\"id\\\",\\\"table\\\":\\\"ecOrganvarianceHistory\\\",\\\"order\\\":\\\"asc\\\"}]}\",\"testCaseId\":\"96fa020bba3c449eb6574b803e3d8b35\",\"url\":\"/dcp-ec-ecOrganvarianceHistory-service/queryecOrganvarianceHistory\"}],\"testRecordId\":\"c85d838c-33e9-4ec1-8428-e0c9fe456618\",\"threadNum\":100}";
		System.out.println("接收到测试请求：");
		System.out.println(job);
		log.info("启动测试");
		try {
			testPlanService.startTestPlan(job);
			System.out.println(job);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new Date().toString() + "  测试完成！";
	}

	@ApiOperation(value = "接口调试")
	@RequestMapping(value = "/startDebug", method = RequestMethod.POST)
	public ApiReport debug(@RequestParam(value = "envId", required = true) int envId,@RequestBody HttpTestcase api) {
		
		try {
			long startTime = System.currentTimeMillis();
			testPlanService.debugTestCase(api,envId);
			ApiReport response=debugrePonse.getQueue();
			while(response==null) {
				long dur=System.currentTimeMillis()-startTime;
				response=debugrePonse.getQueue();
				if(dur>20000)break;
			}
			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		//debugrePonse.getQueue()
	}
	@ApiOperation(value = "场景调试")
	@RequestMapping(value = "/debugScenario", method = RequestMethod.POST)
	public ApiReport debugScenario(@RequestParam(value = "envId", required = true) int envId,@RequestBody ScenarioTestcase api) {
		
		try {
			long startTime = System.currentTimeMillis();
			testPlanService.debugTestCase(api,envId);
			ApiReport response=debugrePonse.getQueue();
			while(response==null) {
				long dur=System.currentTimeMillis()-startTime;
				response=debugrePonse.getQueue();
				if(dur>20000)break;
			}
			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		//debugrePonse.getQueue()
	}
	private @Autowired TestDataServiceImpl testData;
//	
//	@ApiOperation(value = "数据库调试")
//	@PostMapping("addDtest")
	//@RequestMapping(value = "/getDtest", method = RequestMethod.POST)
//	public ApiTestcase2 addDtest(@RequestBody ApiTestcase2 api) {
//		return testData.saveTestCase2(api);
		
//		Wrapper<ApiHeader> queryWrapper = new QueryWrapper<>();
		// theadGroup.selectList(queryWrapper);

//		System.out.println(systemDbMapper.);
		//testcaseMapper.selectList(queryWrapper);
		
//		Map<String, String> headerMap=new HashMap<String, String>();
//		for (ApiHeader apiHeader : headers) {
//			headerMap.put(apiHeader.getKey(), apiHeader.getValue());
//		}
//		Map<String, String> headerMap=testData.getTestPlanHeader(1);
//        return testcaseMapper.selectList(queryWrapper);
//		return headers;
//	}
}
