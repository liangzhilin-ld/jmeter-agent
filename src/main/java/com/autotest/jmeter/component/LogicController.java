package com.autotest.jmeter.component;

import org.apache.jmeter.control.IfController;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.OnceOnlyController;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.control.gui.IfControllerPanel;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.OnceOnlyControllerGui;
import org.apache.jmeter.control.gui.TransactionControllerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;

public class LogicController {
	 /**
     * 创建循环控制器
     *
     * @return
     */
    public static LoopController createLoopController(String loops) {
        // Loop Controller
        LoopController loopController = new LoopController();
        //loopController.setLoops(loops);
        loopController.setContinueForever(false);
        loopController.setProperty(new StringProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName()));
        loopController.setProperty(new StringProperty(TestElement.TEST_CLASS, LoopController.class.getName()));
        loopController.setProperty(new StringProperty(TestElement.NAME, "循环控制器"));
        loopController.setProperty(new StringProperty(TestElement.ENABLED, "true"));
        loopController.setProperty(new StringProperty(LoopController.LOOPS, loops));
        //loopController.initialize();
        return loopController;
    }
    
    /**
     * 仅一次控制器
     * @return
     */
    public static OnceOnlyController onceOnlyController() {
        // onceController    	
    	OnceOnlyController onceController = new OnceOnlyController();    	
    	onceController.setName("仅一次控制器");
    	onceController.setEnabled(true);
    	onceController.setProperty(TestElement.TEST_CLASS, OnceOnlyController.class.getName());
    	onceController.setProperty(TestElement.GUI_CLASS, OnceOnlyControllerGui.class.getName());    	 	
    	onceController.initialize();
    	return onceController;
    }
    
    /**
     * 事务控制器
     * @param transName 场景名称
     * @return
     */
    public static TransactionController transactionController(String transName) {   	
    	TransactionController transController = new TransactionController();    	
    	transController.setName(transName);
    	transController.setComment("");
    	transController.setGenerateParentSample(true);
    	transController.setIncludeTimers(false);
    	
    	transController.setEnabled(true);
    	transController.setProperty(TestElement.TEST_CLASS, TransactionController.class.getName());
    	transController.setProperty(TestElement.GUI_CLASS, TransactionControllerGui.class.getName());    	 	
    	transController.initialize();
    	return transController;
    }
    /**
     * if控制器
     * @param condition
     * @return
     */
    public static IfController ifController(String condition) {   	
    	IfController ifController = new IfController();    	
    	ifController.setEnabled(true);
    	ifController.setProperty(new StringProperty(TestElement.TEST_CLASS, IfController.class.getName()));
    	ifController.setProperty(new StringProperty(TestElement.GUI_CLASS, IfControllerPanel.class.getName())); 
    	ifController.setName("如果（If）控制器");
    	ifController.setComment("");
    	ifController.setCondition(condition);
    	ifController.setUseExpression(true);
    	ifController.setEvaluateAll(false);
    	ifController.initialize();
    	return ifController;
    }
    
}
