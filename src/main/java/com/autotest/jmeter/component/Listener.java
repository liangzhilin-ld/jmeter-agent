package com.autotest.jmeter.component;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.jmeter.visualizers.backend.APIBackendListenerClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.visualizers.backend.BackendListener;
import org.apache.jmeter.visualizers.backend.BackendListenerGui;
import org.apache.jmeter.visualizers.backend.influxdb.InfluxdbBackendListenerClient;


public class Listener {
	  /**
     * 创建influxdb Backend Listener监听器，用于性能执行
     *
     * @return
     */
    public static BackendListener influxdbBackendListener() {
    	BackendListener influxDblister=new BackendListener();
    	Arguments arguments = new Arguments();
        Map<String, String> DEFAULT_ARGS = new LinkedHashMap<>();
        DEFAULT_ARGS.put("influxdbMetricsSender", "org.apache.jmeter.visualizers.backend.influxdb.HttpMetricsSender");
        DEFAULT_ARGS.put("influxdbUrl", "http://172.16.206.71:8086/write?db=jmeter");
        DEFAULT_ARGS.put("application", "zhongtai");
        DEFAULT_ARGS.put("measurement", "jmeter");
        DEFAULT_ARGS.put("summaryOnly", "false");
        DEFAULT_ARGS.put("samplersRegex", ".*");
        DEFAULT_ARGS.put("percentiles", "99;95;90");
        DEFAULT_ARGS.put("testTitle", "中台接口");
        DEFAULT_ARGS.put("eventTags", "");        
        DEFAULT_ARGS.forEach(arguments::addArgument);                   
        influxDblister.setProperty(new TestElementProperty("arguments", arguments));
        influxDblister.setProperty(TestElement.GUI_CLASS,BackendListenerGui.class.getName());
        influxDblister.setProperty(TestElement.TEST_CLASS,BackendListener.class.getName());
        influxDblister.setProperty(TestElement.NAME,"Backend Listener");
        influxDblister.setProperty(TestElement.ENABLED,true);
        influxDblister.setProperty("classname",InfluxdbBackendListenerClient.class.getName());
        return influxDblister;
    }
    /**
     * 接口测试结果处理入口
     * @param testId
     * @param debugReportId
     * @param runMode
     * @return
     */
    public static BackendListener backendListener(String jobid, String historyId, String runMode) {
        BackendListener backendListener = new BackendListener();
        backendListener.setName(jobid);
        backendListener.setComment("");
        Arguments arguments = new Arguments();
        arguments.addArgument(APIBackendListenerClient.JOB_ID, jobid);
        if (StringUtils.isNotBlank(runMode)) {
            arguments.addArgument("runMode", runMode);
        }
        if (StringUtils.isNotBlank(historyId)) {
            arguments.addArgument("historyId", historyId);
        }
        backendListener.setArguments(arguments);
        backendListener.setClassname(APIBackendListenerClient.class.getCanonicalName());
      //testPlan.add(testPlan.getArray()[0], backendListener);
        return backendListener;
    }
    
}
