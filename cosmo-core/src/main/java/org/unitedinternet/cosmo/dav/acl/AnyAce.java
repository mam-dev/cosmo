package org.unitedinternet.cosmo.dav.acl;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.NoSuchElementException;
import java.util.Set;

public class AnyAce extends DavAce {

    static Element hrefXml(Document document, String href) {
            Element root = DomUtil.createElement(document, "href", NAMESPACE);
            DomUtil.setText(root, href);
            return root;
    }

    static Element allXml(Document document) {
        return DomUtil.createElement(document, "authenticated", NAMESPACE);
    }

    static Element authenticatedXml(Document document) {
        return DomUtil.createElement(document, "unauthenticated", NAMESPACE);
    }

    static Element propertyXml(Document document, DavPropertyName property) {
        Element root = DomUtil.createElement(document, "property", NAMESPACE);
        root.appendChild(property.toXml(document));
        return root;
    }
    static Element selfXml(Document document) {
        return DomUtil.createElement(document, "self", NAMESPACE);
    }

    static Element unauthenticatedXml(Document document) {
        return DomUtil.createElement(document, "unauthenticated", NAMESPACE);
    }

    private AcePrincipal acePrincipal;

    public AcePrincipal getAcePrincipal() {
        return acePrincipal;
    }

    public void setAcePrincipal(AcePrincipal acePrincipal) {
        this.acePrincipal = acePrincipal;
    }

    @Override
    protected Element principalXml(Document document) {
        switch (acePrincipal.getType()) {
            case SELF:
                return selfXml(document);
            case ALL:
                return allXml(document);
            case AUTHENTICATED:
                return authenticatedXml(document);
            case UNAUTHENTICATED:
                return unauthenticatedXml(document);
            case PROPERTY:
                return propertyXml(document, acePrincipal.getPropertyName());
            default:
                throw  new CosmoException();
        }
    }

    public static AnyAce fromXml(Element aceElement) {
        AnyAce ace = new AnyAce();
        // <D:ace/>
        if (!DomUtil.matches(aceElement, "ace", NAMESPACE)) {
            throw new IllegalArgumentException("Expected DAV:ace element");
        }
        boolean grant = false, deny = false;
        // <D:grant/>
        if (DomUtil.hasChildElement(aceElement, "grant", NAMESPACE)) {
            grant = true;
        }
        // <D:deny>
        if (DomUtil.hasChildElement(aceElement, "deny", NAMESPACE)) {
            deny = true;
            ace.setDenied(true);
        }
        if (grant && deny) {
            throw new IllegalArgumentException("DAV:ace should contain either DAV:grant or DAV:deny, not both");
        }
        if (!grant && !deny) {
            throw new IllegalArgumentException("DAV:ace should contain either DAV:grant or DAV:deny, found nothing");
        }
        String category = grant ? "grant" : "deny";
        Element categoryElement = getOneNamespaceElement(aceElement, category);
        Element principalElement;
        if (DomUtil.hasChildElement(aceElement, "invert", NAMESPACE)) {
            Element invertElement = getOneNamespaceElement(aceElement, "invert");
            ace.setInverted(true);
            principalElement = getOneNamespaceElement(invertElement, "principal");
        } else {
            ace.setInverted(false);
            principalElement = getOneNamespaceElement(aceElement, "principal");
        }
        DavPrivilegeSet privileges =  DavPrivilegeSet.createFromXml(categoryElement);
        ace.setPrivileges(privileges);

        AcePrincipal principal = AcePrincipal.fromXml(principalElement);
        ace.setAcePrincipal(principal);

        return ace;
    }

    private static Element getOneNamespaceElement(Element root, String childLocalName) {
        ElementIterator principalIterator = DomUtil.getChildren(root, childLocalName, NAMESPACE);
        Element principalElement;
        try {
            principalElement = principalIterator.nextElement();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(root.getNodeName() + " should contain DAV:" + childLocalName);
        }
        if (principalIterator.hasNext()) {
            throw new IllegalArgumentException(root.getNodeName() + " shouldn't contain multiple DAV:" + childLocalName+ "elements");
        }
        return principalElement;
    }
}
