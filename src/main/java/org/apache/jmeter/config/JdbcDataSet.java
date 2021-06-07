/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.config;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.ResourceBundle;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoConfigMerge;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.services.JdbcServer;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.GenericTestBeanCustomizer;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read lines from a file and split int variables.
 *
 * The iterationStart() method is used to set up each set of values.
 *
 * By default, the same file is shared between all threads
 * (and other thread groups, if they use the same file name).
 *
 * The shareMode can be set to:
 * <ul>
 * <li>All threads - default, as described above</li>
 * <li>Current thread group</li>
 * <li>Current thread</li>
 * <li>Identifier - all threads sharing the same identifier</li>
 * </ul>
 *
 * The class uses the FileServer alias mechanism to provide the different share modes.
 * For all threads, the file alias is set to the file name.
 * Otherwise, a suffix is appended to the filename to make it unique within the required context.
 * For current thread group, the thread group identityHashcode is used;
 * for individual threads, the thread hashcode is used as the suffix.
 * Or the user can provide their own suffix, in which case the file is shared between all
 * threads with the same suffix.
 *
 */
@GUIMenuSortOrder(1)
public class JdbcDataSet extends ConfigTestElement 
    implements TestBean, LoopIterationListener, NoConfigMerge {
    private static final Logger log = LoggerFactory.getLogger(JdbcDataSet.class);
    private static final long serialVersionUID = 233L;
    private static final String EOFVALUE = // value to return at EOF
        JMeterUtils.getPropDefault("csvdataset.eofstring", "<EOF>"); //$NON-NLS-1$ //$NON-NLS-2$

    private transient String filename;
    private transient String variableNames;
    private transient boolean recycle = true;
    private transient boolean stopThread;
    private transient String[] vars;
    private transient String alias;
    private transient String shareMode;
	private transient String driver;
    private transient String dbUrl;
    private transient String username;
    private transient String password;
    private transient BasicDataSource dataSource;
    private Object readResolve(){
        recycle = true;
        return this;
    }

    /**
     * Override the setProperty method in order to convert
     * the original String shareMode property.
     * This used the locale-dependent display value, so caused
     * problems when the language was changed. 
     * If the "shareMode" value matches a resource value then it is converted
     * into the resource key.
     * To reduce the need to look up resources, we only attempt to
     * convert values with spaces in them, as these are almost certainly
     * not variables (and they are definitely not resource keys).
     */
    @Override
    public void setProperty(JMeterProperty property) {
        if (property instanceof StringProperty) {
            final String propName = property.getName();
            if ("shareMode".equals(propName)) { // The original name of the property
                final String propValue = property.getStringValue();
                if (propValue.contains(" ")){ // variables are unlikely to contain spaces, so most likely a translation
                    try {
                        final BeanInfo beanInfo = Introspector.getBeanInfo(this.getClass());
                        final ResourceBundle rb = (ResourceBundle) beanInfo.getBeanDescriptor().getValue(GenericTestBeanCustomizer.RESOURCE_BUNDLE);
                        for(String resKey : JdbcDataSetBeanInfo.getShareTags()) {
                            if (propValue.equals(rb.getString(resKey))) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Converted {}={} to {} using Locale: {}", propName, propValue, resKey, rb.getLocale());
                                }
                                ((StringProperty) property).setValue(resKey); // reset the value
                                super.setProperty(property);
                                return;                                        
                            }
                        }
                        // This could perhaps be a variable name
                        log.warn("Could not translate {}={} using Locale: {}", propName, propValue, rb.getLocale());
                    } catch (IntrospectionException e) {
                        log.error("Could not find BeanInfo; cannot translate shareMode entries", e);
                    }
                }
            }
        }
        super.setProperty(property);        
    }
    JdbcServer server = JdbcServer.getFileServer();
    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
    	//JdbcServer server = JdbcServer.getFileServer();
        final JMeterContext context = getThreadContext();
        if (vars == null) {
            String fileName = getFilename();
            String mode = getShareMode();
            int modeInt = JdbcDataSetBeanInfo.getShareModeAsInt(mode);
            switch(modeInt){
                case JdbcDataSetBeanInfo.SHARE_ALL:
                    alias = fileName;
                    break;
                case JdbcDataSetBeanInfo.SHARE_GROUP:
                    alias = fileName+"@"+System.identityHashCode(context.getThreadGroup());
                    break;
                case JdbcDataSetBeanInfo.SHARE_THREAD:
                    alias = fileName+"@"+System.identityHashCode(context.getThread());
                    break;
                default:
                    alias = fileName+"@"+mode; // user-specified key
                    break;
            }
            final String names = getVariableNames();
            if (StringUtils.isEmpty(names)) {       
            	BasicDataSource ds=getDataSource();
                vars=server.getVars(ds,fileName);
                if(vars.length==0)throw new IllegalArgumentException("Could not split DB filed from sqlcmd:" + fileName);
                //firstLineIsNames = true;
            } else {
                vars = JOrphanUtils.split(names, ","); // $NON-NLS-1$
            }
            trimVarNames(vars);
        }
           
        // TODO: fetch this once as per vars above?
        JMeterVariables threadVars = context.getVariables();
        String[] lineValues = {};     
        try {
            lineValues=server.readLineSql(getDataSource(),alias,recycle);
            for (int a = 0; a < vars.length && a < lineValues.length; a++) {
                threadVars.put(vars[a], lineValues[a]);
            }
        } catch (Exception e) { // treat the same as EOF
            log.error(e.toString());
        }
        if (lineValues.length == 0) {// i.e. EOF
            if (getStopThread()) {
                throw new JMeterStopThreadException("End of DB:"+ getFilename()+" detected for JDBC DataSet:"
                        +getName()+" configured with stopThread:"+ getStopThread()+", recycle:" + getRecycle());
            }
            for (String var :vars) {
                threadVars.put(var, EOFVALUE);
            }
        }
    }

    /**
     * trim content of array varNames
     * @param varsNames
     */
    private void trimVarNames(String[] varsNames) {
        for (int i = 0; i < varsNames.length; i++) {
            varsNames[i] = varsNames[i].trim();
        }
    }

    /**
     * @return Returns the filename.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename
     *            The filename to set.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }


    /**
     * @return Returns the variableNames.
     */
    public String getVariableNames() {
        return variableNames;
    }

    /**
     * @param variableNames
     *            The variableNames to set.
     */
    public void setVariableNames(String variableNames) {
        this.variableNames = variableNames;
    }

    public boolean getRecycle() {
        return recycle;
    }

    public void setRecycle(boolean recycle) {
        this.recycle = recycle;
    }

    public boolean getStopThread() {
        return stopThread;
    }

    public void setStopThread(boolean value) {
        this.stopThread = value;
    }

    public String getShareMode() {
        return shareMode;
    }

    public void setShareMode(String value) {
        this.shareMode = value;
    }

    public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	public BasicDataSource getDataSource() {
		if (this.dataSource!=null)
			return this.dataSource;
	    BasicDataSource dtSource = new BasicDataSource();
	    dtSource.setDriverClassName(getDriver());
	    dtSource.setUrl(getDbUrl());
	    if (getUsername().length() > 0){
	    	dtSource.setUsername(getUsername());
	    	dtSource.setPassword(getPassword());
	    }
	    this.dataSource=dtSource;
		return dtSource;
	}

}
