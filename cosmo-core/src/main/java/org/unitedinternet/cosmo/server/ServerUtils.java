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
package org.unitedinternet.cosmo.server;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility methods related to the protocols and interfaces presented
 * to clients.
 */
public class ServerUtils implements ServerConstants {

    /**
     * @param request HttpServletRequest.
     * @return All ticket keys found in the request, both in the
     * {@link #HEADER_TICKET} header and the {@link #PARAM_TICKET}
     * parameter.
     */
    public static Set<String> findTicketKeys(HttpServletRequest request) {
        HashSet<String> keys = new HashSet<String>();

        Enumeration<String> headerValues = request.getHeaders(HEADER_TICKET);
        if (headerValues != null) {
            while (headerValues.hasMoreElements()) {
                String value = (String) headerValues.nextElement();
                String[] atoms = value.split(", ");
                for (int i=0; i<atoms.length; i++) {
                    keys.add(atoms[i]);
                }
            }
        }

        String[] paramValues = request.getParameterValues(PARAM_TICKET);
        if (paramValues != null) {
            for (int i=0; i<paramValues.length; i++) {
                keys.add(paramValues[i]);
            }
        }

        return keys;
    }
}
