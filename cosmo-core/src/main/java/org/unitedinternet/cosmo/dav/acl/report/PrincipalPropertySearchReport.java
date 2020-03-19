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
package org.unitedinternet.cosmo.dav.acl.report;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.acl.property.PrincipalCollectionSet;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipal;
import org.unitedinternet.cosmo.dav.acl.resource.DavUserPrincipalCollection;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.dav.report.MultiStatusReport;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Represents the <code>DAV:principal-property-search</code> report that
 * provides a mechanism for finding principal resources whose property values
 * match given input strings.
 * </p>
 * <p>
 * For a given search spec, the resource must have every listed property
 * and the value of each property must match the search spec's match
 * string with a case-insensitive substring search. Every search spec must
 * match in order for the resource to be  added as a query result.
 * </p>
 * <p>
 * Both forms of the report may optionally include a <code>DAV:prop</code>
 * element specifying the names of properties that are to be included in the
 * response for each matching resource. If <code>DAV:prop</code> is not
 * included in the report, then only the href and response status are
 * provided for each resource.
 * </p>
 * <p>
 * As per RFC 3744, the report must be specified with depth 0. The report
 * must be targeted at a collection.
 * </p>
 */
public class PrincipalPropertySearchReport extends MultiStatusReport
    implements AclConstants {
    
    private static final Logger LOG = LoggerFactory.getLogger(PrincipalPropertySearchReport.class);

    public static final ReportType REPORT_TYPE_PRINCIPAL_PROPERTY_SEARCH =
        ReportType.register(ELEMENT_ACL_PRINCIPAL_PROPERTY_SEARCH, NAMESPACE,
                            PrincipalPropertySearchReport.class);

    private Set<SearchSpec> searchSpecs;
    private boolean searchPrincipalCollections;

    // Report methods

    public ReportType getType() {
        return REPORT_TYPE_PRINCIPAL_PROPERTY_SEARCH;
    }

    // ReportBase methods

    /**
     * Parses the report info, extracting search specifications, response
     * properties and "search principal collections" flag.
     */
    protected void parseReport(ReportInfo info)
        throws CosmoDavException {
        if (! getType().isRequestedReportType(info)) {
            throw new CosmoDavException("Report not of type " + getType());
        }

        if (! getResource().isCollection()) {
            throw new BadRequestException(getType() + " report must target a collection");
        }
        if (info.getDepth() != DEPTH_0) {
            throw new BadRequestException(getType() + " report must be made with depth 0");
        }

        setPropFindProps(info.getPropertyNameSet());
        setPropFindType(PROPFIND_BY_PROPERTY);

        searchSpecs = findSearchSpecs(info);
        searchPrincipalCollections = findSearchPrincipalCollections(info);
    }

    /**
     * <p>
     * Executes the report query and stores the result. Behaves like the
     * superclass method except that it does not check depth, since the
     * report by definition always uses depth 0.
     * </p>
     */
    protected void runQuery()
        throws CosmoDavException {
        DavCollection collection = (DavCollection) getResource();

        if (! searchPrincipalCollections) {
            doQueryChildren(collection);
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching DAV:principal-collection-set URLs");
        }

        PrincipalCollectionSet pcs = (PrincipalCollectionSet)
            collection.getProperty(PRINCIPALCOLLECTIONSET);
        if (pcs == null) {
            return;
        }
        for (String href : pcs.getHrefs()) {
            DavResourceLocator locator =
                collection.getResourceLocator().getFactory().
                    createResourceLocatorByUri(collection.getResourceLocator().getContext(),
                                               href);
            DavUserPrincipalCollection pc = (DavUserPrincipalCollection)
                collection.getResourceFactory().resolve(locator);
            if (pc == null) {
                throw new CosmoDavException("Principal collection " + href + " not resolved");
            }
            doQueryChildren(pc);
        }
    }

    /**
     * Does nothing.
     */
    protected void doQuerySelf(WebDavResource resource) {}

    /**
     * Tests every principal member of the collection to see if it matches
     * the report's search specs.
     */
    protected void doQueryChildren(DavCollection collection)
        throws CosmoDavException {
        for (DavResourceIterator i = collection.getMembers(); i.hasNext();) {
            WebDavResource member = (WebDavResource) i.nextResource();
            if (member instanceof DavUserPrincipal) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Testing {}" , member.getResourcePath());
                }
                if (matchPrincipal((DavUserPrincipal)member)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Matched {}", member.getResourcePath());
                    }
                    getResults().add(member);
                }
            }
        }
    }

    // our methods

    public Set<SearchSpec> getSearchSpecs() {
        return searchSpecs;
    }

    public boolean isSearchPrincipalCollections() {
        return searchPrincipalCollections;
    }

    private static Set<SearchSpec> findSearchSpecs(ReportInfo info)
        throws CosmoDavException {
        HashSet<SearchSpec> specs = new HashSet<SearchSpec>();

        ElementIterator psi =
              DomUtil.getChildren(getReportElementFrom(info),
                                  "property-search", NAMESPACE);
          if (! psi.hasNext()) {
              throw new BadRequestException("Expected at least one DAV:property-search child of DAV:principal-property-search");
          }
          while (psi.hasNext()) {
              specs.add(SearchSpec.createFromXml(psi.nextElement()));
          }

        return specs;
    }

    private static boolean findSearchPrincipalCollections(ReportInfo info)
        throws CosmoDavException {
        return DomUtil.hasChildElement(getReportElementFrom(info),
                                       "apply-to-principal-collection-set",
                                       NAMESPACE);
    }

    private boolean matchPrincipal(DavUserPrincipal principal)
        throws CosmoDavException {
        for (SearchSpec spec : searchSpecs) {
            if (! matchSpec(spec, principal)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchSpec(SearchSpec spec,
                              DavUserPrincipal principal)
        throws CosmoDavException {
        for (DavPropertyName name : spec.getProperties()) {
            if (! matchProperty((WebDavProperty)principal.getProperty(name),
                                spec.getMatch())) {
                return false;
            }
        }
        return true;
    }

    private boolean matchProperty(WebDavProperty prop,
                                  String match) {
        if (prop == null) {
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Matching {} against {}", prop.getName().toString() , match);
        }
        Object value = prop.getValue();
        if (value instanceof Element) {
            return matchText((Element) value, match);
        }
        return matchText(value.toString(), match);
    }

    private boolean matchText(Element parent,
                              String match) {
        NodeList children = parent.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) child;
                if (! matchText(e, match)) {
                    return false;
                }
            } else if (child.getNodeType() == Node.TEXT_NODE ||
                       child.getNodeType() == Node.CDATA_SECTION_NODE) {
                String data = ((CharacterData) child).getData();
                if (! matchText(data, match)) {
                    return false;
                }
            } // else we skip the node
        }
        return true;
    }

    private boolean matchText(String test,
                              String match) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Matching {} against {}", test,  match);
        }
        return StringUtils.containsIgnoreCase(test, match);
    }

    public static class SearchSpec {
        private Set<DavPropertyName> properties;
        private String match;

        public SearchSpec(Set<DavPropertyName> properties,
                          String match) {
            this.properties = properties;
            this.match = match;
        }

        public Set<DavPropertyName> getProperties() {
            return properties;
        }

        public String getMatch() {
            return match;
        }

        public static SearchSpec createFromXml(Element root)
            throws CosmoDavException {
            if (! DomUtil.matches(root, "property-search", NAMESPACE)) {
                throw new IllegalArgumentException("Expected root element DAV:property-search");
            }

            Element p = DomUtil.getChildElement(root, "prop", NAMESPACE);
            if (p == null) {
                throw new BadRequestException("Expected DAV:prop child of DAV:property-search");
            }
            ElementIterator pi = DomUtil.getChildren(p);
            if (! pi.hasNext()) {
                throw new BadRequestException("Expected at least one child of DAV:prop");
            }

            HashSet<DavPropertyName> properties =
                new HashSet<DavPropertyName>();
            while (pi.hasNext()) {
                DavPropertyName name =
                    DavPropertyName.createFromXml(pi.nextElement());
                properties.add(name);
            }

            Element m = DomUtil.getChildElement(root, "match", NAMESPACE);
            if (m == null) {
                throw new BadRequestException("Expected DAV:match child of DAV:property-search");
            }
            String match = DomUtil.getText(m);
            
            return new SearchSpec(properties, match);
        }
    }
}
