package org.unitedinternet.cosmo.dav.property;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.model.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by izein on 8/20/14.
 */
public class CurrentUserPrincipal extends StandardDavProperty {

    public CurrentUserPrincipal(DavResourceLocator locator,
                                User user) {
        super(CURRENTUSERPRINCIPAL, href(locator, user), true);
    }

    public String getHref() {
        return (String) getValue();
    }

    private static String href(DavResourceLocator locator,
                               User user) {
        return TEMPLATE_USER.bindAbsolute(locator.getBaseHref(),
                user.getUsername());
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        Element href = DomUtil.createElement(document, XML_HREF, NAMESPACE);
        DomUtil.setText(href, getHref());
        name.appendChild(href);

        return name;
    }

}
