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
package org.unitedinternet.cosmo.dav.acegisecurity;


import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.springframework.security.access.AccessDeniedException;

/**
 * <p>
 * An exception indicating that a principal was denied a privilege for a
 * DAV resource.
 * </p>
 */
@SuppressWarnings("serial")
public class DavAccessDeniedException extends AccessDeniedException {
    private String href;
    private transient DavPrivilege  privilege;

    public DavAccessDeniedException(String href,
                                    DavPrivilege privilege) {
        super(null);
        this.href = href;
        this.privilege = privilege;
    }

    public String getHref() {
        return href;
    }

    public DavPrivilege getPrivilege() {
        return privilege;
    }
}
