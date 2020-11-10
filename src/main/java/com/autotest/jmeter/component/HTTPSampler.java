package com.autotest.jmeter.component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.assertions.BeanShellAssertion;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.control.OnceOnlyController;
import org.apache.jmeter.extractor.BeanShellPostProcessor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.modifiers.BeanShellPreProcessor;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.sampler.TechstarHTTPSamplerProxy;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.ListedHashTree;

import com.autotest.data.mode.ApiTestcase;
import com.autotest.jmeter.entity.assertion.ResponseAssert;
import com.autotest.jmeter.entity.processors.JSONExtractor;
//import com.autotest.data.mode.ApiTestcase;

public class HTTPSampler {
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
    public static TechstarHTTPSamplerProxy crtHTTPSampler(ApiTestcase testApi,Map<String, String> header) {
    	TechstarHTTPSamplerProxy httpSampler = new TechstarHTTPSamplerProxy(HTTPSamplerFactory.DEFAULT_CLASSNAME);
        httpSampler.setName(testApi.getApiUri());
        HeaderManager headerManager=ConfigElement.createHeaderManager(header);
//        headerManager.setProperty("Content-Type", "multipart/form-data");
//        httpSampler.setDomain("uttesh.com");
//        httpSampler.setPort(80);
        httpSampler.setProtocol("http");
        httpSampler.setPath(testApi.getApiUri());
        httpSampler.setMethod(testApi.getApiMethod());
        httpSampler.setProperty(TestElement.TEST_CLASS, TechstarHTTPSamplerProxy.class.getName());
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setProperty(TestElement.ENABLED, true);
        httpSampler.setPostBodyRaw(true);    
        httpSampler.setFollowRedirects(true);
        httpSampler.setUseKeepAlive(true);
        httpSampler.setDoMultipart(false);
        httpSampler.addNonEncodedArgument("",testApi.getParameters(),"");
        httpSampler.setHeaderManager(headerManager);
        return httpSampler;
    }
    
    public static ListedHashTree loginControll() {
    	OnceOnlyController onceController=LogicController.onceOnlyController();
    	ListedHashTree onceControllerTree=new ListedHashTree(onceController); 
    	ApiTestcase casess=new ApiTestcase();
    	//g接口添加
        casess.setApiUri("/auth/g");
        casess.setApiMethod("GET");
        Map<String, String> header=new HashMap();
        TechstarHTTPSamplerProxy gSampler=HTTPSampler.crtHTTPSampler(casess,header);
        ListedHashTree gHashTree = new ListedHashTree(gSampler);
    	
      //添加后置处理器与断言
        JSONExtractor jsonpathA=new JSONExtractor();
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
        casess.setParameters("{\"username\":\"${username}\",\"password\":\"${password}\",\"icode\":\"${icode}\",\"deviceId\":\"${deviceId}\",\"id\":\"${id}\"}");
        TechstarHTTPSamplerProxy pwSampler=HTTPSampler.crtHTTPSampler(casess,header);
        ListedHashTree pwHashTree = new ListedHashTree(pwSampler);
        
        jsonpathA=new JSONExtractor();
        jsonpathA.setVariableName("author");
        jsonpathA.setJsonPath("$.data.access_token");
        jsonpathA.setMatchNo("1");
        jsonpathA.setDefaultValue("null");
        jsonPostProcess=PostProcessors.jsonPostProcessor(jsonpathA);
        pwHashTree.add(pwSampler, jsonPostProcess); 
        ResponseAssert ra=new ResponseAssert();
        ra.setTestString(Arrays.asList("认证成功"));
        ResponseAssertion pwResponse=Assertions.responseAssertion(ra);         
        pwHashTree.add(pwSampler, pwResponse); 
        
        
        
        
        
        onceControllerTree.add(onceController,gHashTree);
        onceControllerTree.add(onceController,imageHashTree);
        onceControllerTree.add(onceController,pwHashTree);
        return onceControllerTree;
    }
}
