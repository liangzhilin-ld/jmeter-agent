//package com.autotest.jmeter.component;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import org.apache.jmeter.assertions.BeanShellAssertion;
//import org.apache.jmeter.assertions.ResponseAssertion;
//import org.apache.jmeter.config.Arguments;
//import org.apache.jmeter.config.gui.ArgumentsPanel;
//import org.apache.jmeter.control.OnceOnlyController;
//import org.apache.jmeter.control.gui.TestPlanGui;
//import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
//import org.apache.jmeter.protocol.http.sampler.TechstarHTTPSamplerProxy;
//import org.apache.jmeter.testelement.TestElement;
//import org.apache.jmeter.testelement.TestPlan;
//import org.apache.jmeter.threads.ThreadGroup;
//import org.apache.jmeter.util.JMeterUtils;
//import org.apache.jorphan.collections.HashTree;
//import org.apache.jorphan.collections.ListedHashTree;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import com.autotest.data.mode.ApiTestcase;
//import com.autotest.data.service.impl.ApiTestcaseServiceImpl;
//import com.autotest.jmeter.assertion.*;
//import com.autotest.jmeter.processors.JSONExtractor;
//@Service
//public class TestPlanTree {
//	private static final Logger log = LoggerFactory.getLogger(TestPlanTree.class);
//	@Autowired
//	private ApiTestcaseServiceImpl testcase;
//	public void mainMethod(){
//		String jmeterHome="E:\\automation\\Appache\\apache-jmeter-5.3";
//		String logPath=jmeterHome.concat("\\jtl\\");
//		String jmeterLogConf=jmeterHome.concat("\\bin\\log4j2.xml");		
//		String jmeterPropertiesFilePath=jmeterHome.concat("\\bin\\jmeter.properties");
//		
//		String random = String.valueOf(System.currentTimeMillis() / 1000);
//        String jtlName = logPath.concat(random).concat(".jtl");
//        String htlmPath = logPath.concat(random).concat("html");
//        String[] args = new String[]{"-n","-e", "-o".concat(htlmPath), "-l".concat(jtlName),"-i".concat(jmeterLogConf),"-L".concat("jmeter.report=DEBUG"),"-L".concat("jmeter.util=DEBUG"), "-L".concat("jorphan=DEBUG")};
//		//
//		HashTree tree=create();//new TestPlanTree2().
//		File jmeterPropertiesFile = new File(jmeterPropertiesFilePath);
//        JMeterUtils.loadJMeterProperties(jmeterPropertiesFile.getPath());
//        JMeterUtils.loadProperties(jmeterLogConf);
//        JMeterUtils.setJMeterHome(jmeterHome);        
//        
//        LoadDispatcher load = new LoadDispatcher();
//        //执行测试
//        load.start(args, tree);
//	}
//    public HashTree create() {
//    	
//        // 创建测试计划
//        TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
//        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
//        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
//        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());
////        testPlan.setUserDefinedVariables(getArguments());
//        testPlan.setProperty(TestElement.NAME,"测试计划");
//        testPlan.setProperty(TestElement.COMMENTS,"");
//        testPlan.setProperty(TestElement.ENABLED,true);
//        testPlan.setFunctionalMode(false);
//        testPlan.setTearDownOnShutdown(true);
//        testPlan.setSerialized(false);
////        testPlan.setTestPlanClasspath("E:\\java\\eclipse-workspace\\myjmeter\\lib\\smutil.jar");
//        ListedHashTree testPlanTree = new ListedHashTree(testPlan);      
//      
//        //添加配置元件
//		Map<String, String> headerMap=new HashMap<String, String>() {
//			private static final long serialVersionUID = 3823206455638368097L;
//			{
//                put("Referer", "http://${host}:${port}/");
//                put("Accept-Language", "zh-CN,zh;q=0.9");
//                put("Origin", "http://${host}:${port}");
//                put("Content-Type", "application/json");
//                put("Accept-Encoding", "gzip, deflate");
//                put("Accept", "application/json, text/plain, */*");
//                put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
//                put("Authorization", "Bearer ${author}");
//                put("Connection", "keep-alive");
//            }
//        };
//        testPlanTree.add(testPlan, getArguments());
//        testPlanTree.add(testPlan,ConfigElement.httpDefaultsGui());
//        testPlanTree.add(testPlan,ConfigElement.createCookieManager());
//        testPlanTree.add(testPlan,ConfigElement.createCacheManager());
//        testPlanTree.add(testPlan,ConfigElement.createHeaderManager(headerMap));
//        //testPlanTree.add(testPlan,new ConfigElement().jdbcDataSet());
//        //testPlanTree.add(testPlan,ConfigElement.JdbcConnection());
// 
//        // 创建登陆http请求
//        List<ApiTestcase> list=testcase.list();
//        //添加G接口
//        ApiTestcase casess=new ApiTestcase();
//        casess.setApiUri("/auth/g");
//        casess.setApiMethod("GET");
//        Map<String, String> header=new HashMap();
//        TechstarHTTPSamplerProxy examplecomSampler = new HTTPSampler().crtHTTPSampler(casess,header);        
//        ListedHashTree httpSamplerHashTree = new ListedHashTree(examplecomSampler);
//               
//        //添加后置处理器与断言
//        JSONExtractor jsonpathA=new JSONExtractor();
//        jsonpathA.setVariableName("gdata");
//        jsonpathA.setJsonPath("$.data");
//        jsonpathA.setMatchNo("1");
//        jsonpathA.setDefaultValue("empty");
//        JSONPostProcessor jsonPostProcess=PostProcessors.jsonPostProcessor(jsonpathA);
//        httpSamplerHashTree.add(examplecomSampler, jsonPostProcess);
//        String script="if(\"${gdata}\"==\"empty\"){\r\n" + 
//        		"	AssertionResult.setFailure(true);\r\n" + 
//        		"    AssertionResult.setFailureMessage(\"g接口异常,响应内容:\"+prev.getResponseDataAsString());\r\n" + 
//        		"}";
//        BeanShellAssertion shellaser=Assertions.beanShellAssertion(script);
//        httpSamplerHashTree.add(examplecomSampler, shellaser);
////        ResponseAssertion ra=Assertions.responseAssertion(responAssert());
////        httpSamplerHashTree.add(examplecomSampler, ra);
//        
//        //添加image接口
//        casess.setApiUri("/auth/code/image");
//        header=new HashMap();
//        header.put("Authorization", "Bearer undefined");
//        TechstarHTTPSamplerProxy imageSampler = new HTTPSampler().crtHTTPSampler(casess,header);        
//        ListedHashTree imageHashTree = new ListedHashTree(imageSampler);        
//        jsonpathA=new JSONExtractor();
//        jsonpathA.setVariableName("veryCode");
//        jsonpathA.setJsonPath("$.data");
//        jsonpathA.setMatchNo("1");
//        jsonpathA.setDefaultValue("empty");
//        jsonPostProcess=PostProcessors.jsonPostProcessor(jsonpathA);
//        imageHashTree.add(imageSampler, jsonPostProcess);        
//        script="import com.techstar.utils.sm.SMUtils;\r\n" + 
//        		"import org.json.*;\r\n" + 
//        		"if(\"${veryCode}\"==\"empty\"||\"${gdata}\"==\"empty\"){\r\n" + 
//        		"	AssertionResult.setFailure(true);\r\n" + 
//        		"     AssertionResult.setFailureMessage(\"验证码接口异常:\"+prev.getResponseDataAsString());\r\n" + 
//        		"}\r\n" + 
//        		"else{\r\n" + 
//        		" 	//获取g接口公钥\r\n" + 
//        		" 	SMUtils sm=new SMUtils();\r\n" + 
//        		"	JSONObject data_obj=new JSONObject(sm.sm4_ecb_decrypt(\"${smDecryptKey}\",\"${gdata}\"));\r\n" + 
//        		"	String publicKey=data_obj.getString(\"publicKey\");\r\n" + 
//        		"	//验证码sm4解析\r\n" + 
//        		"	String imagData=sm.sm4_ecb_decrypt(\"${smDecryptKey}\",\"${veryCode}\");\r\n" + 
//        		"	//账号sm2加密处理\r\n" + 
//        		"	vars.put(\"username\",sm.sm2Encrypt(publicKey,\"${userName}\"));\r\n" + 
//        		"	vars.put(\"password\",sm.sm2Encrypt(publicKey,\"${pwd}\"));\r\n" + 
//        		"	vars.put(\"icode\",imagData.split(\",\")[1]);\r\n" + 
//        		"	vars.put(\"deviceId\",imagData.split(\",\")[0]);\r\n" + 
//        		"	vars.put(\"id\",data_obj.getString(\"id\"));\r\n" + 
//        		"}";
//        shellaser=Assertions.beanShellAssertion(script);
//        imageHashTree.add(imageSampler, shellaser);
//        
//        
//        
//        //添加pw接口
//        casess.setApiUri("/auth/login/pw");
//        casess.setApiMethod("POST");
//        casess.setParameters("{\"username\":\"${username}\",\"password\":\"${password}\",\"icode\":\"${icode}\",\"deviceId\":\"${deviceId}\",\"id\":\"${id}\"}");
//        header=new HashMap();
//        TechstarHTTPSamplerProxy pwSampler = new HTTPSampler().crtHTTPSampler(casess,header);        
//        ListedHashTree pwHashTree = new ListedHashTree(pwSampler);  
//        
//        ResponseAssertion ra=Assertions.responseAssertion(responAssert());
//        pwHashTree.add(pwSampler, ra);
//        
//        
//        
//        
//        
//        //添加控制器
//        //LogicController onceController=new LogicController();
//        OnceOnlyController onceController=LogicController.onceOnlyController();
//        ListedHashTree onceControllerTree=new ListedHashTree(onceController); 
//        //将HTTP请求添加到一次性控制器
//        onceControllerTree.add(onceController,httpSamplerHashTree);
////        onceControllerTree.add(onceController,imageHashTree);
////        onceControllerTree.add(onceController,pwHashTree);
//        //添加线程组
//        ThreadGroup threadGroup = ThreadGroups.create();                
//        ListedHashTree threadGroupHashTree = new ListedHashTree(threadGroup);
//        //将一次性控制添加到线程组，将线程组添加到测试计划
//        //threadGroupHashTree.add(threadGroup,onceControllerTree);
//        threadGroupHashTree.add(threadGroup,httpSamplerHashTree);
//        //不需要登陆时的采样器添加       
////        for (ApiTestcase tcd : list) {
////        	log.info("创建http请求");
////        	TechstarHTTPSamplerProxy sampler = HTTPSampler.createHTTPSampler(tcd,header);
////            ListedHashTree samplerTree = new ListedHashTree(sampler);           
////            log.info("设置后置处理器");
////            JSONPostProcessor jsonPost=PostProcessors.jsonPostProcessor();
////            samplerTree.add(sampler, jsonPost); 
////            log.info("设置断言");
////            ResponseAssertion resa=Assertions.responseAssertion(responAssert());
////            samplerTree.add(sampler, resa);
////            threadGroupHashTree.add(threadGroup, samplerTree);
////		}
//      
//        testPlanTree.add(testPlan, threadGroupHashTree);
//        
//        
//        //添加监听器
//        //testPlanTree.add(testPlan,Listener.influxdbBackendListener());
//        return testPlanTree;
//    }
//
//	public Arguments getArguments() {
//    	 //自定义变量
//        List<String[]> definedVars = new ArrayList<>();
//        String[] host= {"host","172.16.206.69"};//128
//        String[] port= {"port","6600"};//8888
//        String[] userName= {"userName","xtgly"};//zhengyanlin
//        String[] pwd= {"pwd","a1234567"};//admin@123
//        String[] sleep= {"sleep","5000"};
//        String[] sysid= {"sysid","266946423468851203"};
//        String[] smDecryptKey= {"smDecryptKey","e9664b2bebccb6fe80da086044608115"};
//        String[] users= {"users","${__P(users,1)}"};
//        definedVars.add(host);
//        definedVars.add(port);
//        definedVars.add(userName);
//        definedVars.add(pwd);
//        definedVars.add(sleep);
//        definedVars.add(sysid);
//        definedVars.add(smDecryptKey);
//        definedVars.add(users);
//        Arguments value = ConfigElement.createArguments(definedVars);
//        return value;
//    }
//	public ResponseAssert responAssert() {
//		ResponseAssert fg=new ResponseAssert();
//        fg.setApplyTo("main_only");
//        fg.setPatternMatchRules("text_response");
//        fg.setFiledToTest("substring");
//        //List<String> testStar=new ArrayList<>(Arrays.asList("xxx","yyy","zzz"));
//        List<String> testStar=new ArrayList<>(Arrays.asList("认证成功"));
//        fg.setTestString(testStar);
//        fg.setCustomFailureMessage("断言失败");
//        return fg;
//	}
//    
//}
