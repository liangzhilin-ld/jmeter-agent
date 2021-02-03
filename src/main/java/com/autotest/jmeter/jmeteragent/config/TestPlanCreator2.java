package com.autotest.jmeter.jmeteragent.config;

import com.autotest.jmeter.component.ConfigElement;
import com.autotest.jmeter.component.HTTPSampler;
import com.autotest.jmeter.component.ThreadGroups;
//import com.techstar.dmp.jmeteragent.bean.*;
import com.autotest.jmeter.jmeteragent.config.JmeterProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.activation.ActivationGroup_Stub;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.assertions.JSONPathAssertion;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.TechstarHTTPSamplerProxy;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.extractor.BeanShellPostProcessor;
import org.apache.jmeter.modifiers.BeanShellPreProcessor;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.util.JMeterUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSON;


/**
 * @Date 2020/6/17 9:16
 * @Description 创建jmeter测试计划
 */
public class TestPlanCreator2 {

    private static final Logger log = LoggerFactory.getLogger(TestPlanCreator2.class);

    private String jsonString;
    private JmeterProperties jmeterProperties;	
	private int engineCount;

    public TestPlanCreator2(JmeterProperties jmeterProperties, String jsonString) throws URISyntaxException {
        this.jmeterProperties = jmeterProperties;
        this.jsonString = jsonString;
//        this.initParam();
    }
    public TestPlanCreator2(String jsonString) throws URISyntaxException {
        this.jsonString = jsonString;
       // this.initParam();
    } 
    private int getEngineCount() {
	    JMeterUtils.loadJMeterProperties(this.jmeterProperties.getHome() + "\\" + this.jmeterProperties.getPropertiesFileName());
	    this.engineCount = JMeterUtils.getProperty("remote_hosts").split(",").length;
	    return this.engineCount;
    }

    public HashTree create() {
        log.info("创建测试计划");
        TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
        ListedHashTree testPlanTree = new ListedHashTree(testPlan);
        
        //-------------------------------------------------------------------------------------------
        Map<String, String> headerMap=new HashMap<String, String>() {
			private static final long serialVersionUID = 3823206455638368097L;
			{
                put("Referer", "http://${host}:${port}/");
                put("Accept-Language", "zh-CN,zh;q=0.9");
                put("Origin", "http://${host}:${port}");
                put("Content-Type", "application/json");
                put("Accept-Encoding", "gzip, deflate");
                put("Accept", "application/json, text/plain, */*");
                put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
                put("Authorization", "Bearer ${author}");
                put("Connection", "keep-alive");
            }
        };
        testPlanTree.add(testPlan, getArguments());
        testPlanTree.add(testPlan,ConfigElement.httpDefaultsGui());
        testPlanTree.add(testPlan,ConfigElement.createCookieManager());
        testPlanTree.add(testPlan,ConfigElement.createCacheManager());
        testPlanTree.add(testPlan,ConfigElement.createHeaderManager(headerMap));
        //testPlanTree.add(testPlan,ConfigElement.jdbcDataSet());
        //testPlanTree.add(testPlan,ConfigElement.JdbcConnection());
//        ThreadGroup threadGroup = ThreadGroups.create();                
//        ListedHashTree threadGroupHashTree = new ListedHashTree(threadGroup);
        
        
//        ApiTestcase casess=new ApiTestcase();
//        casess.setApiUri("/auth/g");
//        casess.setApiMethod("GET");
//        Map<String, String> header=new HashMap();
//        TechstarHTTPSamplerProxy examplecomSampler=HTTPSampler.crtHTTPSampler(casess,header);
        //----------------------------------------------------------------------------

        log.info("创建线程组");
        ThreadGroup threadGroup = createThreadGroup();
        ListedHashTree threadGroupHashTree = new ListedHashTree(threadGroup);

        log.info("添加http请求头管理器");
        HeaderManager headerManager = createHeaderManager();

        log.info("添加用户自定义变量");
        Arguments arguments = createArguments();
        threadGroupHashTree.add(threadGroup, arguments);

        log.info("创建循环控制器");
        //LoopController loopController = createLoopController();
        //threadGroup.setSamplerController(loopController);

        log.info("创建http请求收集器");
        log.info("创建http请求");
        TechstarHTTPSamplerProxy examplecomSampler = createHTTPSamplerProxy(headerManager);
        ListedHashTree httpSamplerHashTree = new ListedHashTree(examplecomSampler);
        log.info("创建预处理条件");
        BeanShellPreProcessor beanShellPreProcessor = createBeanShellPreProcessor();
        httpSamplerHashTree.add(examplecomSampler, beanShellPreProcessor);
        log.info("设置断言");
        threadGroupHashTree.add(threadGroup, httpSamplerHashTree);

        testPlanTree.add(testPlan, threadGroupHashTree);
        return testPlanTree;

    }

    /**
     * http请求头管理器
     */
    public static HeaderManager createHeaderManager() {
        HeaderManager headerManager = new HeaderManager();
        Header header = new Header("Content-Type", "application/json;charset=UTF-8");
        headerManager.add(header);
        return headerManager;
    }

    /**
     * 创建用户自定义变量
     */
    public static Arguments createArguments() {
        Arguments arguments = new Arguments();
        Argument argument = new Argument("param", "");
//		Argument username = new Argument("username", Constants.VBI_NAME);
//		Argument password = new Argument("password", Constants.VBI_PWD);
        Argument token = new Argument("token", "");
        Argument deviceId = new Argument("deviceId", "");
        Argument vercode = new Argument("vercode", "");
        Argument id = new Argument("id", "");
        Argument smKey = new Argument("smKey", "e9664b2bebccb6fe80da086044608115");
        Argument publicKey = new Argument("publicKey", "");
        arguments.addArgument(argument);
//		arguments.addArgument(username);
//		arguments.addArgument(password);
        arguments.addArgument(token);
        arguments.addArgument(deviceId);
        arguments.addArgument(vercode);
        arguments.addArgument(id);
        arguments.addArgument(smKey);
        arguments.addArgument(publicKey);
        return arguments;
    }

    /**
     * 创建线程组
     *
     * @return
     */
    public ThreadGroup createThreadGroup() {
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Example Thread Group");
        threadGroup.setNumThreads(2);
        //计算所有线程启动完成时间，设置为线程持续时间的10%
        BigDecimal bd = new BigDecimal(10);
        bd = bd.multiply(new BigDecimal("0.1"));
        bd = bd.setScale(0, BigDecimal.ROUND_UP);
        //threadGroup.setRampUp(this.requestParam.getThreadNum()/this.requestParam.getThreadNumPS());
        threadGroup.setRampUp(bd.intValue());
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setScheduler(jmeterProperties.isScheduler());
        threadGroup.setDuration(20);
        threadGroup.setDelay(jmeterProperties.getDelay());
        return threadGroup;
    }

    /**
     * 创建循环控制器
     *
     * @return
     */
    public LoopController createLoopController() {
        // Loop Controller
        LoopController loopController = new LoopController();
        loopController.setLoops(jmeterProperties.getLoops());
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.initialize();
        return loopController;
    }

    /**
     * 创建http采样器
     *
     * @return
     */
    public TechstarHTTPSamplerProxy createHTTPSamplerProxy(HeaderManager headerManager) {
        TechstarHTTPSamplerProxy httpSamplerProxy = new TechstarHTTPSamplerProxy();
        httpSamplerProxy.setHeaderManager(headerManager);
        httpSamplerProxy.setName("a6a3a43de25b400f88c89b19d38a072a");
        //httpSamplerProxy.setDomain("172.16.206.128");
        //httpSamplerProxy.setPort(8888);
        httpSamplerProxy.setPath("/dcp-ec-ecJobsDicType-service/queryecJobsDicType");
        httpSamplerProxy.setMethod("POST");
        httpSamplerProxy.setConnectTimeout("3000");
        httpSamplerProxy.setUseKeepAlive(true);
        httpSamplerProxy.setProperty(TestElement.TEST_CLASS, TechstarHTTPSamplerProxy.class.getName());
        httpSamplerProxy.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSamplerProxy.setProtocol("http");
//        CacheManager cacheManager=new CacheManager();
//        httpSamplerProxy.setCacheManager(cacheManager);
        httpSamplerProxy.setEnabled(true);
        httpSamplerProxy.addNonEncodedArgument("", "${param}", "=");
        return httpSamplerProxy;
    }

    /**
     * 创建beanshell前置处理器
     */
    public static BeanShellPreProcessor createBeanShellPreProcessor() {
        BeanShellPreProcessor beanShellPreProcessor = new BeanShellPreProcessor();
        String script = String.format("import java.util.UUID;\r\n" +
                "\r\n" +
                "public String mock_uuid(){\r\n" +
                "	return UUID.randomUUID().toString().replaceAll(\"-\",\"\");\r\n" +
                "}\r\n" +
                "vars.put(\"param\", \"%s\");", "test");
        beanShellPreProcessor.setScript(script);
        beanShellPreProcessor.setProperty("script", script);
        return beanShellPreProcessor;
    }

    /**
     * 创建beanshell后置处理器
     */
    public static BeanShellPostProcessor createBeanShellPostProcessor() {
        BeanShellPostProcessor beanShellPostProcessor = new BeanShellPostProcessor();
        String s = "import java.io.*;\r\n" +
                "import redis.clients.jedis.Jedis;\r\n" +
                "\r\n" +
                "Jedis jedis = new Jedis(\"localhost\",6379);\r\n" +
                "String response=\"\";\r\n" +
                "String Str = \"{\\\"status\\\":200\";\r\n" +
                "response = prev.getResponseDataAsString();\r\n" +
                "if(response==\"\"){\r\n" +
                "	Failure = true;\r\n" +
                "	FailureMessage=\"系统无响应，获取不到响应数据！\";\r\n" +
                "	log.info(FailureMessage);\r\n" +
                "}else if(response.contains(Str)==false){\r\n" +
                "	Failure=true;\r\n" +
                "	String Msg =\"响应结果与期望不一致，请排查性能问题，还是程序代码问题\";\r\n" +
                "	FailureMessage = Msg + \"期望结果:\" + Str+\",\" + \"响应内容:\"+ response;\r\n" +
                "	jedis.zadd(\"errorLog\", 0, FailureMessage);\r\n" +
                "}";
        beanShellPostProcessor.setScript(s);
        beanShellPostProcessor.setProperty("script", s);
        return beanShellPostProcessor;
    }

    /* *//*
     * 创建beanshell前置处理器
     *//*
	public static BeanShellPreProcessor createBeanShellPreProcessor(TestCase tc) {
		BeanShellPreProcessor beanShellPreProcessor = new BeanShellPreProcessor();
		String script=String.format("import java.util.UUID;\r\n" + 
				"\r\n" + 
				"public String mock_uuid(){\r\n" + 
				"	return UUID.randomUUID().toString().replaceAll(\"-\",\"\");\r\n" + 
				"}\r\n" + 
				"vars.put(\"param\", \"%s\");",tc.getRequestBody().replace("\"", "\\\""));
		beanShellPreProcessor.setScript(script);
		beanShellPreProcessor.setProperty("script", script);
		return beanShellPreProcessor;
	}
	
	*//**
     * 创建beanshell后置处理器
     *//*
	public static BeanShellPostProcessor createBeanShellPostProcessor() {
		BeanShellPostProcessor beanShellPostProcessor = new BeanShellPostProcessor();
		String s = "import java.io.*;\r\n" + 
				"import redis.clients.jedis.Jedis;\r\n" + 
				"\r\n" + 
				"Jedis jedis = new Jedis(\"localhost\",6379);\r\n" + 
				"String response=\"\";\r\n" + 
				"String Str = \"{\\\"status\\\":200\";\r\n" + 
				"response = prev.getResponseDataAsString();\r\n" + 
				"if(response==\"\"){\r\n" + 
				"	Failure = true;\r\n" + 
				"	FailureMessage=\"系统无响应，获取不到响应数据！\";\r\n" + 
				"	log.info(FailureMessage);\r\n" + 
				"}else if(response.contains(Str)==false){\r\n" + 
				"	Failure=true;\r\n" + 
				"	String Msg =\"响应结果与期望不一致，请排查性能问题，还是程序代码问题\";\r\n" + 
				"	FailureMessage = Msg + \"期望结果:\" + Str+\",\" + \"响应内容:\"+ response;\r\n" + 
				"	jedis.zadd(\"errorLog\", 0, FailureMessage);\r\n" + 
				"}";
		beanShellPostProcessor.setScript(s);
		beanShellPostProcessor.setProperty("script", s);
		return beanShellPostProcessor;
	}*/

    /**
     * 创建response断言
     *
     * @return
     */
//    public static ResponseAssertion createResponseAssertion() {
//        ResponseAssertion responseAssertion = new ResponseAssertion();
//        responseAssertion.setTestFieldResponseDataAsDocument();
//        String assertionType = tc.getAssertion().getValidateType();
//        if (assertionType == null) {
//            return null;
//        }
//        if (assertionType.equals("text")) {
//            responseAssertion.setToEqualsType();
//        } else if (assertionType.equals("regex")) {
//            responseAssertion.setToMatchType();
//        }
//        responseAssertion.addTestString(tc.getAssertion().getPattern());
//        return responseAssertion;
//    }

    /**
     * 创建json断言
     *
     * @return
     */
//    public static List<JSONPathAssertion> createJSONPathAssertion(TestCase tc) {
//        List<JSONPathAssertion> jsonPathAssertions = new ArrayList<JSONPathAssertion>();
//        List<JsonPathAssert> paths = tc.getAssertion().getPaths();
//        for (JsonPathAssert path : paths) {
//            JSONPathAssertion jsonPathAssertion = new JSONPathAssertion();
//            jsonPathAssertion.setJsonPath(path.getPath());
//            jsonPathAssertion.setExpectedValue(path.getExpectedValue());
//            jsonPathAssertions.add(jsonPathAssertion);
//        }
//        return jsonPathAssertions;
//    }
    
    
	public Arguments getArguments() {
   	 //自定义变量
       List<String[]> definedVars = new ArrayList<>();
       String[] host= {"host","172.16.206.69"};//128
       String[] port= {"port","6600"};//8888
       String[] userName= {"userName","xtgly"};//zhengyanlin
       String[] pwd= {"pwd","a1234567"};//admin@123
       String[] sleep= {"sleep","5000"};
       String[] sysid= {"sysid","266946423468851203"};
       String[] smDecryptKey= {"smDecryptKey","e9664b2bebccb6fe80da086044608115"};
       String[] users= {"users","${__P(users,1)}"};
       definedVars.add(host);
       definedVars.add(port);
       definedVars.add(userName);
       definedVars.add(pwd);
       definedVars.add(sleep);
       definedVars.add(sysid);
       definedVars.add(smDecryptKey);
       definedVars.add(users);
       Arguments value = ConfigElement.createArguments(definedVars);
       return value;
   }
}
