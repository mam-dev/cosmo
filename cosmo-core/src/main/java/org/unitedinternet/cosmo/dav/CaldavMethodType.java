/*
 * Copyright 2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 ("the "License"");
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
package org.unitedinternet.cosmo.dav;

import java.util.HashSet;

/**
 * Utility class used to decide the type(read or write) of the (extended) http methods.
 * 
 */
public class CaldavMethodType  {

    private static final HashSet<String> READ = new HashSet<String>();
    private static final HashSet<String> WRITE = new HashSet<String>();

    static {
        // HTTP methods
        READ.add("OPTIONS");
        READ.add("GET");
        READ.add("HEAD");

        // WebDAV methods
        WRITE.add("POST");
        WRITE.add("PUT");
        WRITE.add("DELETE");
        READ.add("PROPFIND");
        WRITE.add("PROPPATCH");
        WRITE.add("MKCOL");
        WRITE.add("COPY");
        WRITE.add("MOVE");

        // Ticket methods
        WRITE.add("MKTICKET");
        WRITE.add("DELTICKET");

        // CalDAV methods
        WRITE.add("MKCALENDAR");
    }

    public static boolean isReadMethod(String method) {
        return READ.contains(method);
    }

    public static boolean isWriteMethod(String method) {
        return WRITE.contains(method);
    }
}
