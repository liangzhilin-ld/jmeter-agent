package com.autotest.jmeter.component;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.OnceOnlyController;
import org.apache.jmeter.control.gui.OnceOnlyControllerGui;
import org.apache.jmeter.testelement.TestElement;

public class LogicController {
	 /**
     * 创建循环控制器
     *
     * @return
     */
    public static LoopController createLoopController(String loops) {
        // Loop Controller
        LoopController loopController = new LoopController();
        loopController.setLoops(loops);
        loopController.setContinueForever(false);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.initialize();
        return loopController;
    }
    
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
    
}
