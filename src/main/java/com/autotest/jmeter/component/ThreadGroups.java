package com.autotest.jmeter.component;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.gui.ThreadGroupGui;

import com.autotest.data.mode.TheadGroupConfig;
//import com.autotest.jmeter.entity.theadgroup.TheadGroupEntity;

import org.apache.jmeter.threads.ThreadGroup;
public class ThreadGroups {
	   /**
     * 创建线程组
     *
     * @return
     */
    public static ThreadGroup create(TheadGroupConfig tgentity) {
    	ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroups.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
    	threadGroup.setEnabled(true);
        threadGroup.setName(tgentity.getName());  
        threadGroup.setProperty(ThreadGroup.ON_SAMPLE_ERROR, tgentity.getOnSampleError()); 
//        threadGroup.setNumThreads(5);
//        threadGroup.setRampUp(5);        
        threadGroup.setProperty(ThreadGroup.NUM_THREADS,tgentity.getNumThreads());//
        threadGroup.setProperty(ThreadGroup.RAMP_TIME,tgentity.getRampUp());//${__P(rup,0)}
        threadGroup.setScheduler(tgentity.isScheduler());//调度器
        threadGroup.setProperty(ThreadGroup.DURATION, tgentity.getDuration());//持续时间${__P(duration,20)}

        threadGroup.setProperty(ThreadGroup.DELAYED_START, tgentity.isDelayThredCreation(), false);//延时开启
        threadGroup.setProperty(ThreadGroup.DELAY, tgentity.getDelay());//启动延时间设置   
        threadGroup.setSamplerController(LogicController.createLoopController(tgentity.getLoopCount()));//${__P(lc,-1)}循环次数
        threadGroup.setProperty(ThreadGroup.IS_SAME_USER_ON_NEXT_ITERATION,true);
//        Arguments value = new Arguments();
//        threadGroup.setProperty(new TestElementProperty("Arguments", value)) ;    
        return threadGroup;
    }
    
    /**
     * 自动化测试线程组对象
     * @return
     */
    public static TheadGroupConfig apiTestTheadGroup() {
    	TheadGroupConfig config=new TheadGroupConfig();
    	config.setName("线程组");
    	config.setOnSampleError("continue");
    	config.setNumThreads("1");
    	config.setRampUp("1");
    	config.setLoopCount("1");
    	config.setDelayThredCreation(false);
    	config.setScheduler(false);
    	config.setDuration("");
    	config.setDelay("");
    	return config;
    }
}
