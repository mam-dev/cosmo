/*
 * Copyright 2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.servletcontext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

/**
 * Initialize Bean.
 * TODO is this class used anywhere?
 */
public class SystemPropertySettingBean implements InitializingBean{

    private Map properties = new HashMap();
        
    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public void afterPropertiesSet() throws Exception {
        Iterator i = properties.keySet().iterator();
        while (i.hasNext()){
            String key = (String)i.next();
            System.setProperty(key, (String)properties.get(key));
        }
    }

}
