package org.unitedinternet.cosmo.dav.acl.property;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.property.PrincipalUtils;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.model.Group;
import org.unitedinternet.cosmo.model.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

public class GroupMemberSet extends StandardDavProperty implements AclConstants {
    public GroupMemberSet(DavResourceLocator locator, Group group) {
        super(GROUPMEMBERSET, hrefs(locator, group), true);
    }

    public Set<String> getHrefs() { return (Set<String>) getValue();}

    private static HashSet<String> hrefs(DavResourceLocator locator, Group group) {
        HashSet<String> hrefs = new HashSet<>();
        for (User user : group.getUsers()) {
            hrefs.add(PrincipalUtils.href(locator, user));
        }
        return hrefs;
    }
    @Override
    public Element toXml(Document document) {
        Element name = getName().toXml(document);
        for (String href : getHrefs()) {
            Element e = DomUtil.createElement(document, XML_HREF, NAMESPACE);
            DomUtil.setText(e, href);
            name.appendChild(e);
        }

        return name;

    }
}
