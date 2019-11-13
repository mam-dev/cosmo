package org.unitedinternet.cosmo.dav.acl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dav.*;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipal;
import org.unitedinternet.cosmo.dav.property.PrincipalUtils;
import org.unitedinternet.cosmo.model.Ace;
import org.unitedinternet.cosmo.security.Permission;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

import static org.unitedinternet.cosmo.dav.acl.PermissionPrivilegeConstants.PERMISSION_TO_PRIVILEGE;
import static org.unitedinternet.cosmo.dav.acl.PermissionPrivilegeConstants.PRIVILEGE_TO_PERMISSION;

@Component
public class AnyAce extends DavAce {

    private  static final Log LOG = LogFactory.getLog(AnyAce.class);


    private AnyAce() { //used in fromAce

    }
    public  AnyAce (@NonNull AcePrincipal principal, @NonNull Collection<DavPrivilege> privileges) {
        getPrivileges().addAll(privileges);
        setAcePrincipal(principal);
    }
    public AnyAce(@NonNull AcePrincipal principal, @NonNull DavPrivilege[] privileges) {
        this(principal, Arrays.asList(privileges));
    }

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

    public AcePrincipal getPrincipal() {
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
            case HREF:
                return hrefXml(document, acePrincipal.getValue());
            default:
                throw  new CosmoException();
        }
    }

    public  static AnyAce fromAce (Ace ace, DavResourceLocator locator) {
        AnyAce anyAce = new AnyAce();

        anyAce.setDenied(ace.isDeny());
        anyAce.setProtected(false);
        DavPrivilegeSet dps = new DavPrivilegeSet();
        for (Permission perm : ace.getPermissions()) {
            dps.add(PERMISSION_TO_PRIVILEGE.get(perm));
        }
        anyAce.setPrivileges(dps);

        AcePrincipal principal = new AcePrincipal();
        switch (ace.getType()) {
            case AUTHENTICATED:
                principal.setType(AcePrincipalType.AUTHENTICATED);
                break;
            case USER:
                principal.setHref(PrincipalUtils.href(locator, ace.getUser()));
                break;
        }
        anyAce.setAcePrincipal(principal);
        return anyAce;
    }

    public static AnyAce fromXml(Element aceElement) throws CosmoDavException {
        AnyAce ace = new AnyAce();
        // <D:ace/>
        if (!DomUtil.matches(aceElement, "ace", NAMESPACE)) {
            throw new BadRequestException("Expected DAV:ace element");
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
            throw new BadRequestException("DAV:ace should contain either DAV:grant or DAV:deny, not both");
        }
        if (!grant && !deny) {
            throw new BadRequestException("DAV:ace should contain either DAV:grant or DAV:deny, found nothing");
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
        DavPrivilegeSet privileges =  DavPrivilegeSet.fromXmlAcl(categoryElement);
        ace.setPrivileges(privileges);

        AcePrincipal principal = AcePrincipal.fromXml(principalElement);
        ace.setAcePrincipal(principal);

        return ace;
    }

    private static Element getOneNamespaceElement(Element root, String childLocalName) throws CosmoDavException {
        ElementIterator principalIterator = DomUtil.getChildren(root, childLocalName, NAMESPACE);
        Element principalElement;
        try {
            principalElement = principalIterator.nextElement();
        } catch (NoSuchElementException e) {
            throw new BadRequestException(root.getNodeName() + " should contain DAV:" + childLocalName);
        }
        if (principalIterator.hasNext()) {
            throw new BadRequestException(root.getNodeName() + " shouldn't contain multiple DAV:" + childLocalName+ "elements");
        }
        return principalElement;
    }

    public void toAce(Ace destination, DavResourceLocator locator, DavResourceFactory factory) throws NotAllowedPrincipalException, NotRecognizedPrincipalException {
        LOG.debug("Converting AnyAce object " + this + "to Ace (DB-stored) object");
        if (getPrincipal() == null) {
            throw  new IllegalArgumentException("This object does not have an acePrincipal filled properly");
        }
        // Clear privileges
        destination.getPermissions().clear();
        //Set privileges
        for (DavPrivilege privilege : getPrivileges()) {
            if (PRIVILEGE_TO_PERMISSION.containsKey(privilege)) {
                destination.getPermissions().add(PRIVILEGE_TO_PERMISSION.get(privilege));
            } else {
                LOG.debug("Skipping PRIVILEGE: " + privilege);
            }
        }
        switch (getPrincipal().getType()) {
            case AUTHENTICATED:
                destination.setType(Ace.Type.AUTHENTICATED);
                destination.setUser(null);
                break;
            case HREF:
                destination.setType(Ace.Type.USER);
                LOG.debug("Finding user: " + getPrincipal().getValue());
                // Match user
                DavUserPrincipal principal = PrincipalUtils.findUserPrincipal(getPrincipal().getValue(), locator, factory);
                destination.setUser(principal.getUser());
                break;
            default:
                throw new NotAllowedPrincipalException("Principal with type " + getPrincipal().getType() + " is not allowed in unprotected ACEs");
        }


    }
}
