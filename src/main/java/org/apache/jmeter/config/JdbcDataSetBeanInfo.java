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

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;

public class JdbcDataSetBeanInfo extends BeanInfoSupport {

    // These names must agree case-wise with the variable and property names
    private static final String FILENAME = "filename";               //$NON-NLS-1$
    private static final String VARIABLE_NAMES = "variableNames";    //$NON-NLS-1$
    private static final String RECYCLE = "recycle";                 //$NON-NLS-1$
    private static final String STOPTHREAD = "stopThread";           //$NON-NLS-1$
    private static final String SHAREMODE = "shareMode";             //$NON-NLS-1$

    // Access needed from CSVDataSet
    private static final String[] SHARE_TAGS = new String[3];
    static final int SHARE_ALL    = 0;
    static final int SHARE_GROUP  = 1;
    static final int SHARE_THREAD = 2;

    // Store the resource keys
    static {
        SHARE_TAGS[SHARE_ALL]    = "shareMode.all"; //$NON-NLS-1$
        SHARE_TAGS[SHARE_GROUP]  = "shareMode.group"; //$NON-NLS-1$
        SHARE_TAGS[SHARE_THREAD] = "shareMode.thread"; //$NON-NLS-1$        
    }

    public JdbcDataSetBeanInfo() {
        super(JdbcDataSet.class);
        createPropertyGroup("database", 
        		new String[] { "dbUrl", "driver", "username", "password" });
        createPropertyGroup("csv_data",             //$NON-NLS-1$
                new String[] { FILENAME, VARIABLE_NAMES,
                        RECYCLE, STOPTHREAD, SHAREMODE });

        
        PropertyDescriptor p = property(FILENAME,TypeEditor.TextAreaEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");        //$NON-NLS-1$
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);

        p = property(VARIABLE_NAMES);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");        //$NON-NLS-1$
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);

        p = property(RECYCLE);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.TRUE);

        p = property(STOPTHREAD);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);

        p = property(SHAREMODE, TypeEditor.ComboStringEditor);
        p.setValue(RESOURCE_BUNDLE, getBeanDescriptor().getValue(RESOURCE_BUNDLE));
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, SHARE_TAGS[SHARE_ALL]);
        p.setValue(NOT_OTHER, Boolean.FALSE);
        p.setValue(NOT_EXPRESSION, Boolean.FALSE);
        p.setValue(TAGS, SHARE_TAGS);
        
        p = property("dbUrl");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("driver", TypeEditor.ComboStringEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setValue(TAGS, getListJDBCDriverClass());
        p = property("username");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("password", TypeEditor.PasswordEditor);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
    }

    public static int getShareModeAsInt(String mode) {
        if (mode == null || mode.length() == 0){
            return SHARE_ALL; // default (e.g. if test plan does not have definition)
        }
        for (int i = 0; i < SHARE_TAGS.length; i++) {
            if (SHARE_TAGS[i].equals(mode)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return array of String for possible sharing modes
     */
    public static String[] getShareTags() {
        String[] copy = new String[SHARE_TAGS.length];
        System.arraycopy(SHARE_TAGS, 0, copy, 0, SHARE_TAGS.length);
        return copy;
    }

    /**
     * Get the list of JDBC driver classname for the main databases
     * @return a String[] with the list of JDBC driver classname
     */
    private String[] getListJDBCDriverClass() {
        return JOrphanUtils.split(JMeterUtils.getPropDefault("jdbc.config.jdbc.driver.class", ""), "|"); //$NON-NLS-1$
    }
}
