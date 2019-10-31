package org.unitedinternet.cosmo.dav.acl;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.unitedinternet.cosmo.CosmoException;
import org.w3c.dom.Element;

import java.util.NoSuchElementException;
import java.util.Objects;
public class AcePrincipal implements DavConstants {
    private String value;
    private String namespacePrefix;
    private String namespaceUri;
    private  AcePrincipalType type;

    public static AcePrincipal fromXml(Element root) {
        AcePrincipal principal = new AcePrincipal();
        if (!DomUtil.matches(root, "principal", NAMESPACE)) {
            throw new IllegalArgumentException("Expected DAV:principal element");
        }
        ElementIterator children = DomUtil.getChildren(root);
        Element principalChild;
        try {
            principalChild = children.nextElement();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("DAV:principal should have one child, found none");
        }
        if (children.hasNext()) {
            throw new IllegalArgumentException("DAV:principal should have one child, found more than one");
        }
        if (DomUtil.matches(principalChild, "self", NAMESPACE)) {
            principal.setType(AcePrincipalType.SELF);
        } else if (DomUtil.matches(principalChild, "all", NAMESPACE)) {
            principal.setType(AcePrincipalType.ALL);
        } else if (DomUtil.matches(principalChild, "authenticated", NAMESPACE)) {
            principal.setType(AcePrincipalType.AUTHENTICATED);
        } else if (DomUtil.matches(principalChild, "unauthenticated", NAMESPACE)) {
            principal.setType(AcePrincipalType.UNAUTHENTICATED);
        } else if (DomUtil.matches(principalChild, "property", NAMESPACE)) {
            ElementIterator elementIterator = DomUtil.getChildren(principalChild);
            Element property;
            try {
                property = elementIterator.nextElement();
            } catch (NoSuchElementException e) {
                throw new IllegalArgumentException("DAV:property should contain one child");
            }
            if (elementIterator.hasNext()) {
                throw new IllegalArgumentException("DAV:property shouldn't contain multiple children");
            }
            DavPropertyName davPropertyName = DavPropertyName.createFromXml(property);
            principal.setPropertyName(davPropertyName);
        } else if (DomUtil.matches(principalChild, "href", NAMESPACE)) {
            principal.setHref(DomUtil.getText(principalChild));
        }
        return principal;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }

    public String getNamespaceUri() {
        return namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
    }

    public void setHref(String href) {
        setType(AcePrincipalType.HREF);
        setNamespaceUri("");
        setNamespacePrefix("");
        setValue(href);
    }

    public DavPropertyName getPropertyName() {
        if (type != AcePrincipalType.PROPERTY) {
            throw new CosmoException();
        }
        return DavPropertyName.create(this.value, Namespace.getNamespace(this.namespacePrefix, this.namespaceUri));

    }

    public void setPropertyName(DavPropertyName property) {
        setType(AcePrincipalType.PROPERTY);
        setNamespacePrefix(property.getNamespace().getPrefix());
        setNamespaceUri(property.getNamespace().getURI());
        setValue(property.getName());
    }


    @Override
    public int hashCode() {
        return Objects.hash(value, namespacePrefix, namespaceUri, type);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcePrincipal acePrincipal = (AcePrincipal) o;
        return acePrincipal.type.equals(type) &&
                acePrincipal.namespacePrefix.equals(namespacePrefix) &&
                acePrincipal.namespaceUri.equals(namespaceUri) &&
                acePrincipal.value.equals(value);

    }

    public void setType(AcePrincipalType type) {
        this.type = type;
    }

    public AcePrincipalType getType() {
        return type;
    }
}
