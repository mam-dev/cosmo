/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.acl;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.util.CosmoQName;

/**
 * Provides constants for media types, XML namespaces, names and
 * values, DAV properties and resource types defined by the WebDAV ACL
 * spec.
 */
public interface AclConstants extends DavConstants {

    /** The ACL XML element name <DAV:principal> */
    String ELEMENT_ACL_PRINCIPAL = "principal";
    /** The ACL XML element name <DAV:alternate-URI-set> */
    String ELEMENT_ACL_ALTERNATE_URI_SET =
        "alternate-URI-set";
    /** The ACL XML element name <DAV:principal-URL> */
    String ELEMENT_ACL_PRINCIPAL_URL =
        "principal-URL";
    /** The ACL XML element name <DAV:group-membership> */
    String ELEMENT_ACL_GROUP_MEMBERSHIP =
        "group-membership";
    String ELEMENT_ACL_PRINCIPAL_COLLECTION_SET =
        "principal-collection-set";
    String ELEMENT_ACL_PRINCIPAL_MATCH =
        "principal-match";
    String ELEMENT_ACL_SELF = "self";
    String QN_ACL_SELF =
        DomUtil.getExpandedName(ELEMENT_ACL_SELF, NAMESPACE);
    String ELEMENT_ACL_PRINCIPAL_PROPERTY =
        "principal-property";
    String QN_ACL_PRINCIPAL_PROPERTY =
        DomUtil.getExpandedName(ELEMENT_ACL_PRINCIPAL_PROPERTY, NAMESPACE);
    String ELEMENT_ACL_PRINCIPAL_PROPERTY_SEARCH =
        "principal-property-search";
    String QN_ACL_PRINCIPAL_PROPERTY_SEARCH =
        DomUtil.getExpandedName(ELEMENT_ACL_PRINCIPAL_PROPERTY_SEARCH,
                                 NAMESPACE);

    /** The ACL property name DAV:alternate-URI-set */
    String PROPERTY_ACL_ALTERNATE_URI_SET =
        "alternate-URI-set";
    /** The ACL property name DAV:principal-URL-set */
    String PROPERTY_ACL_PRINCIPAL_URL =
        "principal-URL";
    /** The ACL property name DAV:group-membership */
    String PROPERTY_ACL_GROUP_MEMBERSHIP =
        "group-membership";
    String PROPERTY_ACL_PRINCIPAL_COLLECTION_SET =
        "principal-collection-set";
    String PROPERTY_ACL_CURRENT_USER_PRIVILEGE_SET =
        "current-user-privilege-set";
    String PROPERTY_ACL_ACL = "acl";

    /** The ACL property DAV:alternate-URI-set */
    DavPropertyName ALTERNATEURISET =
        DavPropertyName.create(PROPERTY_ACL_ALTERNATE_URI_SET, NAMESPACE);
    /** The ACL property DAV:principal-URL */
    DavPropertyName PRINCIPALURL =
        DavPropertyName.create(PROPERTY_ACL_PRINCIPAL_URL, NAMESPACE);
    /** The ACL property DAV:group-membership */
    DavPropertyName GROUPMEMBERSHIP =
        DavPropertyName.create(PROPERTY_ACL_GROUP_MEMBERSHIP, NAMESPACE);
    DavPropertyName PRINCIPALCOLLECTIONSET =
        DavPropertyName.create(PROPERTY_ACL_PRINCIPAL_COLLECTION_SET,
                               NAMESPACE);
    DavPropertyName CURRENTUSERPRIVILEGESET =
        DavPropertyName.create(PROPERTY_ACL_CURRENT_USER_PRIVILEGE_SET,
                               NAMESPACE);
    DavPropertyName ACL =
        DavPropertyName.create(PROPERTY_ACL_ACL, NAMESPACE);

    CosmoQName RESOURCE_TYPE_PRINCIPAL =
        new CosmoQName(NAMESPACE.getURI(), ELEMENT_ACL_PRINCIPAL,
                  NAMESPACE.getPrefix());
}
