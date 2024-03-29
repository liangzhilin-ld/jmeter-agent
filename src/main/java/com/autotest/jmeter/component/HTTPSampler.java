package com.autotest.jmeter.component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.jmeter.assertions.BeanShellAssertion;
import org.apache.jmeter.control.OnceOnlyController;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.sampler.TechstarHTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.collections.ListedHashTree;
import com.autotest.data.mode.assertions.ResponseAssertion;
import com.autotest.data.enums.ApiParam;
import com.autotest.data.mode.ApiMock;
import com.autotest.data.mode.HttpTestcase;
import com.autotest.data.mode.processors.JsonExtractor;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import kg.apc.jmeter.samplers.DummySampler;
import kg.apc.jmeter.samplers.DummySamplerGui;

public class HTTPSampler {
	
	public static HTTPSamplerProxy getHttpSamplerProxy(HttpTestcase testApi,Map<String, String> header) {
//        HTTPSamplerProxy httpSamplerProxy = new HTTPSamplerProxy();
//		  参考org.apache.jmeter.protocol.http.gui.action.ParseCurlCommandAction
        HTTPSamplerProxy httpSamplerProxy = (HTTPSamplerProxy) HTTPSamplerFactory
                .newInstance(HTTPSamplerFactory.DEFAULT_CLASSNAME);
        header=new HashMap<String, String>();
        header.put(ApiParam.CASE_ID.name(), testApi.getCaseId().toString());
        header.put(ApiParam.SUITE_Id.name(), testApi.getSuiteId().toString()); 
        HeaderManager headerManager=ConfigElement.createHeaderManager(header);
        httpSamplerProxy.addTestElement(headerManager);
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.domain", ""));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.port", ""));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.protocol", testApi.getApiProtocol()));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.path", testApi.getApiUri()));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.method", testApi.getApiMethod()));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.contentEncoding", "UTF-8"));
        httpSamplerProxy.setProperty(new BooleanProperty("HTTPSampler.follow_redirects", true));
        httpSamplerProxy.setProperty(new BooleanProperty("HTTPSampler.auto_redirects", false));
        httpSamplerProxy.setProperty(new BooleanProperty("HTTPSampler.use_keepalive", true));
        httpSamplerProxy.setProperty("HTTPSampler.postBodyRaw", false, false);
        httpSamplerProxy.setProperty(new BooleanProperty("HTTPSampler.DO_MULTIPART_POST", false));
        httpSamplerProxy.setProperty(new StringProperty("TestElement.gui_class", "org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui"));
        httpSamplerProxy.setProperty(new StringProperty("TestElement.test_class", "org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy"));
        httpSamplerProxy.setProperty(new StringProperty("TestElement.name", testApi.getApiName()));
        httpSamplerProxy.setProperty(new StringProperty("TestElement.enabled", "true"));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.embedded_url_re", ""));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.connect_timeout", ""));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.response_timeout", ""));
        if(testApi.getApiIn().equals("body")) {
        	//!"GET".equals(request.getMethod()) && request.getPostData() != null
        	httpSamplerProxy.setPostBodyRaw(true);    
        	httpSamplerProxy.addNonEncodedArgument("",testApi.getParameters(),"=");            
        }else {
        	//表单数据参数提交
        	AddArgumentFromClipboard(httpSamplerProxy,testApi.getParameters()); 
        	httpSamplerProxy.setDoMultipart(true);
        }
        return httpSamplerProxy;
    }
	
	
    /**
     * 创建http Sampler请求.性能测试调用
     * @param <T>
     *
     * @return
     */
//    @SuppressWarnings("unchecked")
//	public static <T extends HTTPSamplerBase> T createHTTPSampler(TestCase testApi,Map<String, String> header) {
//        T httpSampler = (T) HTTPSamplerFactory.newInstance(HTTPSamplerFactory.DEFAULT_CLASSNAME);
//        httpSampler.setName(testApi.getAPI_URI());
//        HeaderManager headerManager=ConfigElement.createHeaderManager(header);
////        headerManager.setProperty("Content-Type", "multipart/form-data");
////        httpSampler.setDomain("uttesh.com");
////        httpSampler.setPort(80);
//        httpSampler.setProtocol("http");
//        httpSampler.setPath("/auth/g");
//        httpSampler.setMethod("GET");
//        httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
//        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
//        httpSampler.setProperty(TestElement.ENABLED, true);
//        httpSampler.setPostBodyRaw(true);    
//        httpSampler.setFollowRedirects(true);
//        httpSampler.setUseKeepAlive(true);
//        httpSampler.setDoMultipart(false);
//        httpSampler.addNonEncodedArgument("",testApi.getPARAMETERS(),"");
//        httpSampler.setHeaderManager(headerManager);
//        return httpSampler;
//    }
    /**
     * 创建接口测试http请求HTTPSamplerProxy
     *
     * @return
     */
    public static TechstarHTTPSamplerProxy crtHTTPSampler(HttpTestcase testApi,Map<String, String> header) {
    	header=new HashMap<String, String>();
        header.put(ApiParam.CASE_ID.name(), testApi.getCaseId().toString());
        header.put(ApiParam.SUITE_Id.name(), testApi.getSuiteId().toString());        
    	TechstarHTTPSamplerProxy httpSampler = new TechstarHTTPSamplerProxy(HTTPSamplerFactory.DEFAULT_CLASSNAME);
    	
        httpSampler.setProperty(new StringProperty("HTTPSampler.domain", ""));
        httpSampler.setProperty(new StringProperty("HTTPSampler.port", ""));
        httpSampler.setContentEncoding("UTF-8");
        httpSampler.setEmbeddedUrlRE("");
        httpSampler.setConnectTimeout("");
        httpSampler.setResponseTimeout("");
        
        
    	httpSampler.setName(testApi.getApiName());//JSON.toJSONString(lable)
    	httpSampler.setEnabled(true);
        HeaderManager headerManager=ConfigElement.createHeaderManager(header);
//        httpSampler.setDomain("uttesh.com");
//        httpSampler.setPort(80);
        httpSampler.setProtocol(testApi.getApiProtocol());
        httpSampler.setPath(testApi.getApiUri());
        httpSampler.setMethod(testApi.getApiMethod());
        httpSampler.setProperty(TestElement.TEST_CLASS, TechstarHTTPSamplerProxy.class.getName());
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setProperty(TestElement.ENABLED, true);
        httpSampler.setAutoRedirects(false);
        httpSampler.setFollowRedirects(true);
        httpSampler.setUseKeepAlive(true);
        httpSampler.setDoMultipart(false);
        httpSampler.setHeaderManager(headerManager);
        if(testApi.getApiIn().equals("body")) {
        	httpSampler.setPostBodyRaw(true);    
            httpSampler.addNonEncodedArgument("",testApi.getParameters(),"=");            
        }else {
        	//表单数据参数提交
        	AddArgumentFromClipboard(httpSampler,testApi.getParameters());  
        	httpSampler.setDoMultipart(true);
        }
        return httpSampler;
    }
    /**
     * 向httpSampler提交表单参数，3种调用方式，区别主要是可修改的参数个数不一样
     * 方法1:AddArgumentFromClipboard(httpSampler,testApi.getParameters());
     * 方法2:httpSampler.addArgument("name1", "test11");
     * 方法3:httpSampler.addNonEncodedArgument("name1", "value111", "=", "text/plain");
     * @param httpSampler  TechstarHTTPSamplerProxy实例对象
     * @param args 从api_testcase表中读取的json请求字符串内容
     */
    public static void AddArgumentFromClipboard(TechstarHTTPSamplerProxy httpSampler,String args) {    	
    	if(StrUtil.isNotEmpty(args)) {
    		JSONObject jsonOb=JSONUtil.parseObj(args);
    		if(!jsonOb.isEmpty()) {
        		for (Entry<String, Object> para : jsonOb.entrySet()) {
        			String getValue=StrUtil.isEmptyIfStr(para.getValue())?"":para.getValue().toString();
        	    	HTTPArgument argument = new HTTPArgument("", "");	    	
        	        argument.setName(para.getKey());//参数名称
        	        argument.setValue(getValue); //参数值
        	        argument.setAlwaysEncoded(false);//URL_Encode?
        	        argument.setContentType("text/plain");//默认
        	        argument.setUseEquals(true);//Include_Equals?
        	        argument.setMetaData("=");//参数与值之间的分隔符，默认=号        	        
        			httpSampler.getArguments().addArgument(argument);           			
				}
    		}		
    	}
    }
    public static void AddArgumentFromClipboard(HTTPSamplerProxy httpSampler,String args) {    	
    	if(StrUtil.isNotEmpty(args)) {
    		JSONObject jsonOb=JSONUtil.parseObj(args);
    		if(!jsonOb.isEmpty()) {
        		for (Entry<String, Object> para : jsonOb.entrySet()) {
        			String getValue=StrUtil.isEmptyIfStr(para.getValue())?"":para.getValue().toString();
        	    	HTTPArgument argument = new HTTPArgument("", "");	    	
        	        argument.setName(para.getKey());//参数名称
        	        argument.setValue(getValue); //参数值
        	        argument.setAlwaysEncoded(false);//URL_Encode?
        	        argument.setContentType("text/plain");//默认
        	        argument.setUseEquals(true);//Include_Equals?
        	        argument.setMetaData("=");//参数与值之间的分隔符，默认=号        	        
        			httpSampler.getArguments().addArgument(argument);           			
				}
    		}		
    	}
    }
    /**
     * 接口测试登陆方法
     * @return
     */
    public static ListedHashTree loginControll() {
    	OnceOnlyController onceController=LogicController.onceOnlyController();
    	ListedHashTree onceControllerTree=new ListedHashTree(onceController); 
    	HttpTestcase casess=new HttpTestcase();
    	//g接口添加
        casess.setApiUri("/auth/g");
        casess.setApiMethod("GET");
        casess.setApiIn("query");
        Map<String, String> header=new HashMap<String, String>();
        TechstarHTTPSamplerProxy gSampler=HTTPSampler.crtHTTPSampler(casess,header);
        ListedHashTree gHashTree = new ListedHashTree(gSampler);
    	
      //添加后置处理器与断言
        JsonExtractor jsonpathA=new JsonExtractor();
        jsonpathA.setName("JSON Extractor");
        jsonpathA.setVariableName("gdata");
        jsonpathA.setJsonPath("$.data");
        jsonpathA.setMatchNo("1");
        jsonpathA.setDefaultValue("empty");
        JSONPostProcessor jsonPostProcess=PostProcessors.jsonPostProcessor(jsonpathA);
        gHashTree.add(gSampler, jsonPostProcess);        
        String script="if(\"${gdata}\"==\"empty\"){\r\n" + 
        		"	AssertionResult.setFailure(true);\r\n" + 
        		"    AssertionResult.setFailureMessage(\"g接口异常,响应内容:\"+prev.getResponseDataAsString());\r\n" + 
        		"}";
        BeanShellAssertion shellaser=Assertions.beanShellAssertion(script);
        gHashTree.add(gSampler, shellaser); 
                
        casess.setApiUri("/auth/code/image");
        casess.setApiMethod("GET");
        TechstarHTTPSamplerProxy imageSampler=HTTPSampler.crtHTTPSampler(casess,header);
        ListedHashTree imageHashTree = new ListedHashTree(imageSampler);
        jsonpathA.setVariableName("veryCode");
        jsonPostProcess=PostProcessors.jsonPostProcessor(jsonpathA);
        imageHashTree.add(imageSampler, jsonPostProcess); 
   
        script="import com.autotest.util.sm.SMUtils;\r\n" + 
        		"import org.json.*;\r\n" + 
        		"if(\"${veryCode}\"==\"empty\"||\"${gdata}\"==\"empty\"){\r\n" + 
        		"	AssertionResult.setFailure(true);\r\n" + 
        		"     AssertionResult.setFailureMessage(\"验证码接口异常:\"+prev.getResponseDataAsString());\r\n" + 
        		"}\r\n" + 
        		"else{\r\n" + 
        		" 	//获取g接口公钥\r\n" + 
        		" 	SMUtils sm=new SMUtils();\r\n" + 
        		"	JSONObject data_obj=new JSONObject(sm.sm4_ecb_decrypt(\"${smDecryptKey}\",\"${gdata}\"));\r\n" + 
        		"	String publicKey=data_obj.getString(\"publicKey\");\r\n" + 
        		"	//验证码sm4解析\r\n" + 
        		"	String imagData=sm.sm4_ecb_decrypt(\"${smDecryptKey}\",\"${veryCode}\");\r\n" + 
        		"	//账号sm2加密处理\r\n" + 
        		"	vars.put(\"username\",sm.sm2Encrypt(publicKey,\"${userName}\"));\r\n" + 
        		"	vars.put(\"password\",sm.sm2Encrypt(publicKey,\"${pwd}\"));\r\n" + 
        		"	vars.put(\"icode\",imagData.split(\",\")[1]);\r\n" + 
        		"	vars.put(\"deviceId\",imagData.split(\",\")[0]);\r\n" + 
        		"	vars.put(\"id\",data_obj.getString(\"id\"));\r\n" + 
        		"}";
        BeanShellAssertion shellimage=Assertions.beanShellAssertion(script);
        imageHashTree.add(imageSampler, shellimage);

        casess.setApiUri("/auth/login/pw");
        casess.setApiMethod("POST");
        casess.setApiIn("body");
        casess.setParameters("{\"username\":\"${username}\",\"password\":\"${password}\",\"icode\":\"${icode}\",\"deviceId\":\"${deviceId}\",\"id\":\"${id}\"}");
        TechstarHTTPSamplerProxy pwSampler=HTTPSampler.crtHTTPSampler(casess,header);
        ListedHashTree pwHashTree = new ListedHashTree(pwSampler);
        
        jsonpathA=new JsonExtractor();
        jsonpathA.setVariableName("author");
        jsonpathA.setJsonPath("$.data.access_token");
        jsonpathA.setMatchNo("1");
        jsonpathA.setDefaultValue("null");
        jsonPostProcess=PostProcessors.jsonPostProcessor(jsonpathA);
        pwHashTree.add(pwSampler, jsonPostProcess);
        ResponseAssertion ra=new ResponseAssertion();
        ra.setTestString(Arrays.asList("认证成功"));
        org.apache.jmeter.assertions.ResponseAssertion pwResponse=Assertions.responseAssertion(ra);         
        pwHashTree.add(pwSampler, pwResponse); 
        onceControllerTree.add(onceController,gHashTree);
        onceControllerTree.add(onceController,imageHashTree);
        onceControllerTree.add(onceController,pwHashTree);
        return onceControllerTree;
    }

    /**
     * Mock接口数据，直接从数据库获取信息，根据URL地址与备注信息进行查找，
     * @return
     */
    public static ListedHashTree mockSampler(ApiMock mockData) {
    	DummySampler mock=new DummySampler();
    	mock.setProperty(TestElement.GUI_CLASS,DummySamplerGui.class.getName());
    	mock.setProperty(TestElement.TEST_CLASS,DummySampler.class.getName());
    	mock.setEnabled(true);
    	mock.setName(mockData.getName());//sampler lable标签,一般URI路径
    	mock.setComment("备注信息");//添加备注
    	mock.setProperty("URL",mockData.getUrl());//匹配接口,一般URI路径
    	mock.setProperty("SUCCESFULL",true);
    	mock.setProperty("RESPONSE_CODE","200");
    	mock.setProperty("RESPONSE_MESSAGE","OK");
    	mock.setProperty("CONNECT","${__Random(1,5)}");
    	mock.setProperty("LATENCY","${__Random(1,50)}");
    	mock.setProperty("RESPONSE_TIME","${__Random(50,500)}");
    	mock.setProperty("WAITING",true);
    	mock.setProperty("REQUEST_DATA",mockData.getRequestData());
    	mock.setProperty("RESPONSE_DATA",mockData.getResponseData());   	
    	mock.setProperty("RESULT_CLASS","org.apache.jmeter.protocol.http.sampler.HTTPSampleResult");
        /*
         * RESULT_CLASS类有有以下三种方式
         * org.apache.jmeter.samplers.SampleResult
         * org.apache.jmeter.protocol.http.sampler.HTTPSampleResult
         * org.apache.jmeter.samplers.StatisticalSampleResult
         * 
         */
    	ListedHashTree mockSamplerTree = new ListedHashTree(mock);
    	mockSamplerTree.add(mock, PostProcessors.beanShellPostProcessor("prev.setIgnore();"));
    	return mockSamplerTree;
    }
    
}
