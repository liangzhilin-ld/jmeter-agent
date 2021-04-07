package com.autotest.jmeter.jmeteragent.config;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.CSVDataSetBeanInfo;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.RunTime;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.*;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.timers.ConstantThroughputTimer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import com.autotest.data.enums.ApiRunMode;
import com.autotest.data.mode.ScenarioTestcase;
import com.autotest.data.mode.TestScheduled;
import com.autotest.jmeter.component.ConfigElement;
import com.autotest.jmeter.component.Listener;
import com.autotest.jmeter.jmeteragent.service.impl.JmeterHashTreeServiceImpl;
import com.autotest.jmeter.jmeteragent.service.impl.TestDataServiceImpl;
import com.autotest.util.SpringContextUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 调试创建jmeter jmx脚本
 * https://www.cnblogs.com/liufei2/p/13799669.html
 * @Author 刘翊扬
 * @Date 2020/10/10 9:52 下午
 * @Version 1.0
 */
public class TestPlanCreator {

    public static final String JMETER_ENCODING = "UTF-8";

    // 因为我们就模拟一条请求，所以这个线程数先设置成1
    public static final int NUMBER_THREADS = 1;

    /** 执行结果输出的日志 */
    public static final String replayLogPath = "E:\\java\\replay_result.log";

    /** 生成的jmx的地址 */
    public static final String jmxPath = "E:\\java\\test.jmx";

    public static void main(String[] args) {
        //run();
    }

    public static void run(TestScheduled trig) {
    	JmeterHashTreeServiceImpl jmeterCompant=SpringContextUtil.getBean(JmeterHashTreeServiceImpl.class);
    	TestDataServiceImpl testData=SpringContextUtil.getBean(TestDataServiceImpl.class);
    	CrtHashTreeService hashService=SpringContextUtil.getBean(CrtHashTreeService.class);
    	
        String url = "localhost";
        String port = "8088";
        String api = "/mongo/insert";
        String request = "{\"name\":\"wangwu\",\"password\":\"wnagwu123456\"}";        
        String jemterHome = "E:\\automation\\Appache\\apache-jmeter-5.3";
        JMeterUtils.setJMeterHome(jemterHome);
        JMeterUtils.loadJMeterProperties(JMeterUtils.getJMeterBinDir() + "\\jmeter.properties");
        //JMeterUtils.initLocale();

        // 获取TestPlan
        TestPlan testPlan = getTestPlan();

        // 获取设置循环控制器
        LoopController loopController = getLoopController();

        // 获取线程组
        ThreadGroup threadGroup = getThreadGroup(loopController, NUMBER_THREADS);

        // 获取Http请求信息
        HTTPSamplerProxy httpSamplerProxy = getHttpSamplerProxy(url, port, api, request);

        // 获取结果：如汇总报告、察看结果树
        List<ResultCollector> resultCollector = getResultCollector(replayLogPath);

        // 获取设置吞吐量
        ConstantThroughputTimer constantThroughputTimer = getConstantThroughputTimer(20);

        // 获取请求头信息
        HeaderManager headerManager = getHeaderManager();

        HashTree fourHashTree = new HashTree();
        resultCollector.stream().forEach(item -> fourHashTree.add(item));
        //fourHashTree.add(headerManager);

        HashTree thirdHashTree = new HashTree();
        // 注意：设置吞吐量需要和Http请求同一级，否则无效
        thirdHashTree.add(constantThroughputTimer);
        thirdHashTree.add(httpSamplerProxy, fourHashTree);

        HashTree secondHashTree = new HashTree();
        secondHashTree.add(threadGroup, thirdHashTree);

        
        
        ListedHashTree firstTreeTestPlan = new ListedHashTree();

        
        
        
        //变量添加
        HashTree argHashTree = new HashTree();
        Arguments arg=jmeterCompant.getPubArguments(trig);
        argHashTree.add(arg);
        firstTreeTestPlan.add(testPlan, argHashTree);
        //http默认请求
        HashTree httpDefault = new HashTree();
        httpDefault.add(ConfigElement.httpDefaultsGui());
        firstTreeTestPlan.add(testPlan,httpDefault);
        //cookie
        HashTree cookieDefault = new HashTree();
        cookieDefault.add(ConfigElement.createCookieManager());
        firstTreeTestPlan.add(testPlan,cookieDefault);
        
        //Cache
        HashTree cache = new HashTree();
        cache.add(ConfigElement.createCacheManager());
        firstTreeTestPlan.add(testPlan,cache);
       
        //HeaderManager
        HashTree headerHashTree = new HashTree();
        headerHashTree.add(ConfigElement.createHeaderManager(jmeterCompant.getPubHeader(trig)));
        firstTreeTestPlan.add(testPlan,headerHashTree);
        
        //JdbcConnection
        testData.getSyetemDbAll().forEach(item->{
        	HashTree jdbcconn = new HashTree();
        	jdbcconn.add(ConfigElement.JdbcConnection(item));
        	firstTreeTestPlan.add(testPlan,jdbcconn);
        });
 
        //firstTreeTestPlan.add(testPlan, secondHashTree);
        
        List<Object> collects=new ArrayList<Object>();
        List<ScenarioTestcase>  scens=testData.getScenarios(trig);
        if(scens.size()>0)collects.addAll(scens);
        firstTreeTestPlan.add(testPlan, hashService.createThreadGroup(collects));
        firstTreeTestPlan.add(testPlan, fourHashTree);
        
        try {
            SaveService.saveTree(firstTreeTestPlan, new FileOutputStream(jmxPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 第一种方式：运行
        StandardJMeterEngine jMeterEngine = new StandardJMeterEngine();
        jMeterEngine.configure(firstTreeTestPlan);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jMeterEngine.run();
        System.out.println("运行成功!!!");

        // 使用命令
       /* String command = JMeterUtils.getJMeterBinDir() + "/jmeter -n -t " + jmxPath + " -l /Users/liufei/Downloads/jmter/replay_result.jtl";
        Runtime.getRuntime().exec(command);
        System.out.println(command);*/
    }

    /***
     * 监听结果
     * @param replayLogPath  将结果保存到文件中，这个是文件的路径
     * @return
     */
    private static List<ResultCollector> getResultCollector(String replayLogPath) {
        // 察看结果数
        List<ResultCollector> resultCollectors = new ArrayList<>();
        Summariser summariser = new Summariser("速度");
        ResultCollector resultCollector = new ResultCollector(summariser);
        resultCollector.setProperty(new BooleanProperty("ResultCollector.error_logging", false));
        resultCollector.setProperty(new ObjectProperty("saveConfig", getSampleSaveConfig()));
        resultCollector.setProperty(new StringProperty("TestElement.gui_class", "org.apache.jmeter.visualizers.ViewResultsFullVisualizer"));
        resultCollector.setProperty(new StringProperty("TestElement.name", "察看结果树"));
        resultCollector.setProperty(new StringProperty("TestElement.enabled", "true"));
        resultCollector.setProperty(new StringProperty("filename", replayLogPath));
        resultCollectors.add(resultCollector);

        // 结果汇总
        ResultCollector resultTotalCollector = new ResultCollector();
        resultTotalCollector.setProperty(new BooleanProperty("ResultCollector.error_logging", false));
        resultTotalCollector.setProperty(new ObjectProperty("saveConfig", getSampleSaveConfig()));
        resultTotalCollector.setProperty(new StringProperty("TestElement.gui_class", "org.apache.jmeter.visualizers.SummaryReport"));
        resultTotalCollector.setProperty(new StringProperty("TestElement.name", "汇总报告"));
        resultTotalCollector.setProperty(new StringProperty("TestElement.enabled", "true"));
        resultTotalCollector.setProperty(new StringProperty("filename", ""));
        resultCollectors.add(resultTotalCollector);

        return resultCollectors;
    }

    private static SampleSaveConfiguration getSampleSaveConfig() {
        SampleSaveConfiguration sampleSaveConfiguration = new SampleSaveConfiguration();
        sampleSaveConfiguration.setTime(true);
        sampleSaveConfiguration.setLatency(true);
        sampleSaveConfiguration.setTimestamp(true);
        sampleSaveConfiguration.setSuccess(true);
        sampleSaveConfiguration.setLabel(true);
        sampleSaveConfiguration.setCode(true);
        sampleSaveConfiguration.setMessage(true);
        sampleSaveConfiguration.setThreadName(true);
        sampleSaveConfiguration.setDataType(true);
        sampleSaveConfiguration.setEncoding(false);
        sampleSaveConfiguration.setAssertions(true);
        sampleSaveConfiguration.setSubresults(true);
        sampleSaveConfiguration.setResponseData(false);
        sampleSaveConfiguration.setSamplerData(false);
        sampleSaveConfiguration.setAsXml(false);
        sampleSaveConfiguration.setFieldNames(true);
        sampleSaveConfiguration.setResponseHeaders(false);
        sampleSaveConfiguration.setRequestHeaders(false);
        //sampleSaveConfiguration.setAssertionResultsFailureMessage(true);  responseDataOnError
        sampleSaveConfiguration.setAssertionResultsFailureMessage(true);
        //sampleSaveConfiguration.setsserAtionsResultsToSave(0); assertionsResultsToSave
        sampleSaveConfiguration.setBytes(true);
        sampleSaveConfiguration.setSentBytes(true);
        sampleSaveConfiguration.setUrl(true);
        sampleSaveConfiguration.setThreadCounts(true);
        sampleSaveConfiguration.setIdleTime(true);
        sampleSaveConfiguration.setConnectTime(true);
        return sampleSaveConfiguration;
    }

    /***
     * 创建http请求信息
     * @param url ip地址
     * @param port 端口
     * @param api url
     * @param request 请求参数（请求体）
     * @return
     */
    private static HTTPSamplerProxy getHttpSamplerProxy(String url, String port, String api, String request) {
        HTTPSamplerProxy httpSamplerProxy = new HTTPSamplerProxy();
        Arguments HTTPsamplerArguments = new Arguments();
        HTTPArgument httpArgument = new HTTPArgument();
        httpArgument.setProperty(new BooleanProperty("HTTPArgument.always_encode", false));
        httpArgument.setProperty(new StringProperty("Argument.value", request));
        httpArgument.setProperty(new StringProperty("Argument.metadata", "="));
        ArrayList<TestElementProperty> list1 = new ArrayList<>();
        list1.add(new TestElementProperty("", httpArgument));
        HTTPsamplerArguments.setProperty(new CollectionProperty("Arguments.arguments", list1));
        httpSamplerProxy.setProperty(new TestElementProperty("HTTPsampler.Arguments", HTTPsamplerArguments));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.domain", url));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.port", port));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.protocol", "http"));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.path", api));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.method", "POST"));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.contentEncoding", JMETER_ENCODING));
        httpSamplerProxy.setProperty(new BooleanProperty("HTTPSampler.follow_redirects", true));
        httpSamplerProxy.setProperty(new BooleanProperty("HTTPSampler.postBodyRaw", true));
        httpSamplerProxy.setProperty(new BooleanProperty("HTTPSampler.auto_redirects", false));
        httpSamplerProxy.setProperty(new BooleanProperty("HTTPSampler.use_keepalive", true));
        httpSamplerProxy.setProperty(new BooleanProperty("HTTPSampler.DO_MULTIPART_POST", false));
        httpSamplerProxy.setProperty(new StringProperty("TestElement.gui_class", "org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui"));
        httpSamplerProxy.setProperty(new StringProperty("TestElement.test_class", "org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy"));
        httpSamplerProxy.setProperty(new StringProperty("TestElement.name", "HTTP Request"));
        httpSamplerProxy.setProperty(new StringProperty("TestElement.enabled", "true"));
        httpSamplerProxy.setProperty(new BooleanProperty("HTTPSampler.postBodyRaw", true));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.embedded_url_re", ""));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.connect_timeout", ""));
        httpSamplerProxy.setProperty(new StringProperty("HTTPSampler.response_timeout", ""));
        return httpSamplerProxy;
    }

    /***
     * 创建线程组
     * @param loopController 循环控制器
     * @param numThreads 线程数量
     * @return
     */
    private static ThreadGroup getThreadGroup(LoopController loopController, int numThreads) {
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(numThreads);
        threadGroup.setRampUp(1);
        threadGroup.setDelay(0);
        threadGroup.setDuration(0);
        threadGroup.setProperty(new StringProperty(ThreadGroup.ON_SAMPLE_ERROR, "continue"));
        threadGroup.setScheduler(false);
        threadGroup.setName("回放流量");
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        threadGroup.setProperty(new BooleanProperty(TestElement.ENABLED, true));
        threadGroup.setProperty(new TestElementProperty(ThreadGroup.MAIN_CONTROLLER, loopController));
        return threadGroup;
    }

    private static LoopController getLoopController() {
        LoopController loopController = new LoopController();
        loopController.setContinueForever(false);
        loopController.setProperty(new StringProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName()));
        loopController.setProperty(new StringProperty(TestElement.TEST_CLASS, LoopController.class.getName()));
        loopController.setProperty(new StringProperty(TestElement.NAME, "循环控制器"));
        loopController.setProperty(new StringProperty(TestElement.ENABLED, "true"));
        loopController.setProperty(new StringProperty(LoopController.LOOPS, "1"));
        return loopController;
    }

    private static TestPlan getTestPlan() {
        TestPlan testPlan = new TestPlan("Test Plan");
        testPlan.setFunctionalMode(false);
        testPlan.setSerialized(false);
        testPlan.setTearDownOnShutdown(true);
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.setProperty(new BooleanProperty(TestElement.ENABLED, true));
        testPlan.setProperty(new StringProperty(TestElement.COMMENTS, ""));
        testPlan.setTestPlanClasspath("");
        Arguments arguments = new Arguments();
        testPlan.setUserDefinedVariables(arguments);
        return testPlan;
    }

    /**
     * 设置请求头信息
     * @return
     */
    private static HeaderManager getHeaderManager() {
        ArrayList<TestElementProperty> headerMangerList = new ArrayList<>();
        HeaderManager headerManager = new HeaderManager();
        Header header = new Header("Content-Type", "application/json");
        TestElementProperty HeaderElement = new TestElementProperty("", header);
        headerMangerList.add(HeaderElement);

        headerManager.setEnabled(true);
        headerManager.setName("HTTP Header Manager");
        headerManager.setProperty(new CollectionProperty(HeaderManager.HEADERS, headerMangerList));
        headerManager.setProperty(new StringProperty(TestElement.TEST_CLASS, HeaderManager.class.getName()));
        headerManager.setProperty(new StringProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName()));
        return headerManager;
    }

    private static CSVDataSet getCSVDataSet(String bodyPath) {
        CSVDataSet csvDataSet = new CSVDataSet();
        csvDataSet.setEnabled(true);
        csvDataSet.setName("body请求体");
        csvDataSet.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
        csvDataSet.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());

        csvDataSet.setProperty(new StringProperty("filename", bodyPath));
        csvDataSet.setProperty(new StringProperty("fileEncoding", JMETER_ENCODING));
        csvDataSet.setProperty(new BooleanProperty("ignoreFirstLine", false));
        csvDataSet.setProperty(new BooleanProperty("quotedData", true));
        csvDataSet.setProperty(new BooleanProperty("recycle", false));
        csvDataSet.setProperty(new BooleanProperty("stopThread", false));
        csvDataSet.setProperty(new StringProperty("variableNames", ""));
        csvDataSet.setProperty(new StringProperty("shareMode", CSVDataSetBeanInfo.getShareTags()[0]));
        csvDataSet.setProperty(new StringProperty("delimiter", ","));
        return csvDataSet;
    }

    /***
     * 限制QPS设置
     * @param throughputTimer
     * @return
     */
    private static ConstantThroughputTimer getConstantThroughputTimer(int throughputTimer) {
        ConstantThroughputTimer constantThroughputTimer = new ConstantThroughputTimer();
        constantThroughputTimer.setEnabled(true);
        constantThroughputTimer.setName("常数吞吐量定时器");
        constantThroughputTimer.setProperty(TestElement.TEST_CLASS, ConstantThroughputTimer.class.getName());
        constantThroughputTimer.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());
        constantThroughputTimer.setCalcMode(ConstantThroughputTimer.Mode.AllActiveThreads.ordinal());

        constantThroughputTimer.setProperty(new IntegerProperty("calcMode", ConstantThroughputTimer.Mode.AllActiveThreads.ordinal()));
        DoubleProperty doubleProperty = new DoubleProperty();
        doubleProperty.setName("throughput");
        doubleProperty.setValue(throughputTimer * 60f);
        constantThroughputTimer.setProperty(doubleProperty);
        return constantThroughputTimer;
    }
}
