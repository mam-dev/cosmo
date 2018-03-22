/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.util;

import java.util.Map;
import java.util.Map.Entry;

public class URLQuery {
    private Map<String, String[]> parameterMap;
    public URLQuery(Map<String, String[]> parameterMap){
        this.parameterMap = parameterMap;
    }
    
    public String toString(Map<String, String[]> overrideMap){
        StringBuilder s = new StringBuilder();
        s.append("?");
        for (Entry<String, String[]> entry: parameterMap.entrySet()){
            
            String key = entry.getKey();
            
            String[] values = overrideMap.containsKey(key)?
                    overrideMap.get(key) : parameterMap.get(key);
            
            for (String query: values){
                s.append(key);
                s.append("=");
                s.append(query);
                s.append("&");
            }
            
        }
        return s.substring(0, s.length()-1);
    }
}
