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
package org.unitedinternet.cosmo.dav.acl;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

import org.unitedinternet.cosmo.dav.ExtendedDavConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A WebDAV access control entry. An ACE specifies the set of privileges to be
 * either granted or denied to a single principal.
 *
 * <pre>
 * <!ELEMENT ace ((principal | invert), (grant|deny), protected?,
 *                inherited?)>
 *
 * <!ELEMENT principal (href | all | authenticated | unauthenticated 
 * | property | self)>
 * <!ELEMENT all EMPTY> 
 * <!ELEMENT authenticated EMPTY> 
 * <!ELEMENT unauthenticated EMPTY> 
 * <!ELEMENT property ANY> 
 * <!ELEMENT self EMPTY> 
 *
 * <!ELEMENT invert principal> 
 *
 * <!ELEMENT grant (privilege+)> 
 * <!ELEMENT deny (privilege+)> 
 * <!ELEMENT privilege ANY>
 *
 * <!ELEMENT protected EMPTY>
 *
 * <!ELEMENT inherited (href)> 
 * </pre>
 */
public abstract class DavAce
    implements ExtendedDavConstants, XmlSerializable {

    private boolean inverted = false;
    private boolean denied = false;
    private DavPrivilegeSet privileges = new DavPrivilegeSet();
    private boolean isProtected = false;
    private String inherited;

    public DavAce() {
    }

    // XmlSerializable methods

    public Element toXml(Document document) {
        Element root =
            DomUtil.createElement(document, "ace", NAMESPACE);

        Element principalParent = root;
        if (inverted) {
            principalParent =
                DomUtil.createElement(document, "invert", NAMESPACE);
            root.appendChild(principalParent);
        }
        Element principal =
            DomUtil.createElement(document, "principal", NAMESPACE);
        principal.appendChild(principalXml(document));
        principalParent.appendChild(principal);

        Element grantOrDeny =
            DomUtil.createElement(document, denied ? "deny" : "grant",
                                  NAMESPACE);
        grantOrDeny.appendChild(privileges.toXml(document));
        root.appendChild(grantOrDeny);

        if (isProtected) {
            Element isProtected =
                DomUtil.createElement(document, "protected", NAMESPACE);
            root.appendChild(isProtected);
        }

        if (inherited != null) {
            Element href = DomUtil.createElement(document, "href", NAMESPACE);
            DomUtil.setText(href, inherited);

            Element isInherited =
                DomUtil.createElement(document, "inherited", NAMESPACE);
            isInherited.appendChild(href);

            root.appendChild(isInherited);
        }

        return root;
    }

    // our methods

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public boolean isDenied() {
        return denied;
    }

    public void setDenied(boolean denied) {
        this.denied = denied;
    }

    public DavPrivilegeSet getPrivileges() {
        return privileges;
    }

    public void setPrivileges(DavPrivilegeSet privileges) {
        this.privileges = privileges;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public String getInherited() {
        return inherited;
    }

    public void setInherited(String inherited) {
        this.inherited = inherited;
    }

    protected abstract Element principalXml(Document document);

    public static class HrefAce extends DavAce {
        private String href = null;

        public HrefAce(String href) {
            this.href = href;
        }

        public String getHref() {
            return href;
        }

        protected Element principalXml(Document document) {
            Element root = DomUtil.createElement(document, "href", NAMESPACE);
            DomUtil.setText(root, href);
            return root;
        }
    }

    public static class AllAce extends DavAce {
        protected Element principalXml(Document document) {
            return DomUtil.createElement(document, "all", NAMESPACE);
        }
    }

    public static class AuthenticatedAce extends DavAce {
        protected Element principalXml(Document document) {
            return DomUtil.createElement(document, "authenticated", NAMESPACE);
        }
    }

    public static class UnauthenticatedAce extends DavAce {
        protected Element principalXml(Document document) {
            return DomUtil.createElement(document, "unauthenticated", NAMESPACE);
        }
    }

    public static class PropertyAce extends DavAce {
        private DavPropertyName property = null;

        public PropertyAce(DavPropertyName property) {
            this.property = property;
        }

        public DavPropertyName getProperty() {
            return property;
        }

        protected Element principalXml(Document document) {
            Element root = DomUtil.createElement(document, "property", NAMESPACE);
            root.appendChild(property.toXml(document));
            return root;
        }
    }

    public static class SelfAce extends DavAce {
        protected Element principalXml(Document document) {
            return DomUtil.createElement(document, "self", NAMESPACE);
        }
    }
}
