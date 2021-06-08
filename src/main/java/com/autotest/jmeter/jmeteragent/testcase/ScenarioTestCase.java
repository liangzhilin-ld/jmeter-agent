package com.autotest.jmeter.jmeteragent.testcase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.protocol.http.sampler.TechstarHTTPSamplerProxy;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.ListedHashTree;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.autotest.data.mode.HttpTestcase;
import com.autotest.data.mode.ScenarioTestcase;
import com.autotest.data.mode.TestScheduled;
import com.autotest.jmeter.component.HTTPSampler;
import com.autotest.jmeter.component.LogicController;
import com.autotest.jmeter.component.ThreadGroups;
import com.autotest.jmeter.jmeteragent.common.TestDataServiceImpl;
import com.autotest.jmeter.jmeteragent.testplan.JmeterHashTreeServiceImpl;

public class ScenarioTestCase {
	private @Autowired TestDataServiceImpl testData;
	private @Autowired JmeterHashTreeServiceImpl jmeterCompant;
	
//	public ListedHashTree addScenario(TestScheduled trig) {
//		List<Object> collects=new ArrayList<Object>();
//		List<ScenarioTestcase>  scens=testData.getScenarios(trig);
//		List<HttpTestcase>  listcase=testData.getTestcaseByIds(trig);
//		if(scens.size()>0)collects.addAll(scens);
//		if(listcase.size()>0)collects.addAll(listcase);
//		
//    	ThreadGroup threadGroup = ThreadGroups.create(ThreadGroups.apiTestTheadGroup());                
//        ListedHashTree threadGroupHashTree = new ListedHashTree(threadGroup);
//		scens.forEach(stc->{
//			ListedHashTree tcControllerTree=creatTransactionControllerTree(stc);
//			threadGroupHashTree.add(threadGroup, tcControllerTree);
//		});
//		return threadGroupHashTree;
//	}

	
	
//    public ListedHashTree createThreadGroup(List<Object> subList) {
//    	ThreadGroup threadGroup = ThreadGroups.create(ThreadGroups.apiTestTheadGroup());                
//        ListedHashTree threadGroupHashTree = new ListedHashTree(threadGroup);
//        Boolean flag=subList.get(0) instanceof ScenarioTestcase;
//        int pid=flag?((ScenarioTestcase)subList.get(0)).getProjectId()
//        				:((HttpTestcase)subList.get(0)).getProjectId();
//        ScenarioTestcase loginsc=testData.getLoginScenarios(pid);
//        threadGroupHashTree.add(threadGroup, creatTransactionControllerTree(loginsc));
//        
//        for (Object object : subList) {
//        	if(object instanceof ScenarioTestcase) {
//        		ScenarioTestcase stc=(ScenarioTestcase) object;
//        		stc.getArguments();
//        		ListedHashTree tcControllerTree=creatTransactionControllerTree(stc);
//    			threadGroupHashTree.add(threadGroup, tcControllerTree);
//	        }
//	        if(object instanceof HttpTestcase) {
//	        	HttpTestcase tc=(HttpTestcase) object;
//	        	jmeterCompant.addSamplers(threadGroupHashTree, threadGroup, tc);
//	        	
//	        }
//		}
//        return threadGroupHashTree;
//    }
//    
//    public ListedHashTree creatTransactionControllerTree(ScenarioTestcase tc) {
//    	TransactionController tsController=LogicController.transactionController(tc.getScenarioName());
//		ListedHashTree tcControllerTree=new ListedHashTree(tsController); 
//		ArrayList<JSONObject> listObj=tc.getHashtree();
//		if(listObj.size()==0)return tcControllerTree;
//		for (JSONObject json : listObj) {
//			String type=json.getString("type");
//			if(type.contains("HTTPSampler")) {
//				HttpTestcase htc=testData.getTestcaseByID(json.getInteger("id"));
//				jmeterCompant.addSamplers(tcControllerTree, tsController, htc);
//			}
//			if(type.contains("LoginController")) {
//				
//			}
//			if(type.contains("Scenario")) {
//				ScenarioTestcase stc=testData.getScenariosByid(json.getInteger("id"));
//				tcControllerTree.add(tsController,creatTransactionControllerTree(stc));
//			}
//		}
//		return tcControllerTree;
//    }
//    
	
}
