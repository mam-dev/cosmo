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
package org.unitedinternet.cosmo.dav.acl.property;

import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.acl.DavAcl;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * <p>
 * Represents the DAV:acl property.
 * </p>
 * <p>
 * The property is protected. The value is the access control list for the
 * resource.
 * </p>
 */
public class Acl extends StandardDavProperty
    implements AclConstants {

    public Acl(DavAcl acl) {
        super(ACL, acl, true);
    }

    public DavAcl getAcl() {
        return (DavAcl) getValue();
    }

    public Element toXml(Document document) {
        return getAcl().toXml(document);
    }
}
