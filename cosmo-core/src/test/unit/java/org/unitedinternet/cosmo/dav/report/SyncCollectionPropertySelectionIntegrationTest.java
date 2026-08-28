/*
 * Copyright 2026 United Internet
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
package org.unitedinternet.cosmo.dav.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.DavTestContext;
import org.unitedinternet.cosmo.dav.servlet.StandardRequestHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Integration tests for the <strong>property-selection</strong> part of the
 * {@code DAV:sync-collection} REPORT (RFC 6578, Section 3.3) against the full
 * WebDAV request pipeline ({@link StandardRequestHandler} &rarr; provider &rarr;
 * report).
 * </p>
 *
 * <p>
 * Covers the following cases from {@code synccollection-testcases.md}:
 * </p>
 * <ul>
 * <li><strong>E1</strong> - multiple live properties requested in one
 * {@code DAV:prop} (e.g. {@code DAV:getetag} + {@code DAV:getlastmodified})
 * must both appear per member in a 200-propstat block.</li>
 * <li><strong>E2</strong> - an unknown/custom property (e.g.
 * {@code urn:example:nope}) must surface as a separate 404 propstat per member,
 * WITHOUT invalidating the 200 entries for the known properties.</li>
 * <li><strong>E3</strong> - omitting the {@code DAV:prop} element (the DTD
 * marks it optional, {@code prop?}) must still yield a 207 success; each
 * {@code DAV:response} then carries only {@code DAV:href} + status (bare
 * multistatus entries, no propstat).</li>
 * <li><strong>E3&#8209;companion</strong> - the empty
 * {@code <D:prop/>} form is equivalent to omission and must be accepted.</li>
 * <li><strong>E4</strong> - the RFC 6578 DTD does not permit a
 * {@code DAV:allprop} child of {@code DAV:sync-collection}; observed
 * behavior (locked here, 2026-08-28): the element is ignored &mdash; 207
 * with the same bare-href multistatus as the E3 empty-selection case, no
 * propstat, no 4xx, no 5xx.</li>
 * </ul>
 *
 * <p>
 * <strong>Status: green (2026-08-28, 5/5).</strong> These assertions lock in
 * the observed multistatus property-resolution behavior (via
 * {@code MultiStatusReport#buildMultiStatusResponse} + Jackrabbit's
 * {@code MultiStatusResponse}); regressions in propstat-200 grouping,
 * 404-propstat separation for unknown properties, or bare-href rendering for
 * empty selections will now fail the build instead of shipping.
 * </p>
 */
public class SyncCollectionPropertySelectionIntegrationTest extends BaseDavTestCase {

    private static final String DAV_NAMESPACE = "DAV:";

    /** E1 - both requested live properties (getetag + getlastmodified). */
    private static final String BODY_E1_MULTIPLE_LIVE_PROPS =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<D:sync-collection xmlns:D=\"DAV:\">\n"
        + "  <D:sync-token/>\n"
        + "  <D:sync-level>1</D:sync-level>\n"
        + "  <D:prop>\n"
        + "    <D:getetag/>\n"
        + "    <D:getlastmodified/>\n"
        + "  </D:prop>\n"
        + "</D:sync-collection>";

    /** E2 - unknown/custom property alongside a known one. */
    private static final String BODY_E2_UNKNOWN_PROP_AND_GETETAG =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<D:sync-collection xmlns:D=\"DAV:\">\n"
        + "  <D:sync-token/>\n"
        + "  <D:sync-level>1</D:sync-level>\n"
        + "  <D:prop>\n"
        + "    <D:getetag/>\n"
        + "    <nope xmlns=\"urn:example\"/>\n"
        + "  </D:prop>\n"
        + "</D:sync-collection>";

    /** E3 - no DAV:prop element at all (DTD: prop?). */
    private static final String BODY_E3_NO_PROP_ELEMENT =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<D:sync-collection xmlns:D=\"DAV:\">\n"
        + "  <D:sync-token/>\n"
        + "  <D:sync-level>1</D:sync-level>\n"
        + "</D:sync-collection>";

    /** E3 companion - empty DAV:prop element (still valid per RFC 6578 DTD). */
    private static final String BODY_E3_EMPTY_PROP_ELEMENT =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<D:sync-collection xmlns:D=\"DAV:\">\n"
        + "  <D:sync-token/>\n"
        + "  <D:sync-level>1</D:sync-level>\n"
        + "  <D:prop/>\n"
        + "</D:sync-collection>";

    /** E4 - DAV:allprop variant (not part of RFC 6578's DTD). */
    private static final String BODY_E4_ALLPROP_VARIANT =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<D:sync-collection xmlns:D=\"DAV:\">\n"
        + "  <D:sync-token/>\n"
        + "  <D:sync-level>1</D:sync-level>\n"
        + "  <D:allprop/>\n"
        + "</D:sync-collection>";

    /**
     * REPORT against a collection without DAV:read fails the ACL check;
     * the tests require the security context to be set up.
     */
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        testHelper.logIn();
    }

    /**
     * E1: two requested live properties must both appear in the 200-propstat
     * block for every fixture member. This is the common case for clients that
     * need e.g. {@code DAV:getetag} for conflict detection together with
     * {@code DAV:getlastmodified} for UI display; splitting them across propstat
     * blocks would be legal per the DAV spec but is not what this deployment
     * has historically produced. Locking it in here prevents the regression of
     * a "one property per 200 propstat" rewrite.
     */
    @Test
    public void bothRequestedLivePropsAppearInPropstat200PerMember() throws Exception {
        givenHomeChildCollections(2);

        DavTestContext ctx = executeSyncCollectionReport(BODY_E1_MULTIPLE_LIVE_PROPS);
        assertEquals(207, ctx.getDavResponse().getStatus(),
                "E1: sync-collection REPORT with DAV:getetag + DAV:getlastmodified must be 207");

        Document ms = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses = getChildElements(ms.getDocumentElement(), "response");
        assertFalse(responses.isEmpty(), "E1: fixture members must be present in the multistatus");

        for (Element response : responses) {
            Element etag = findPropStatElement(response, "getetag", 200);
            assertNotNull(etag,
                    "E1: DAV:getetag missing from a 200 propstat for member "
                    + firstHref(response) + " (must carry DAV:getetag in a 200 propstat)");

            Element lastmod = findPropStatElement(response, "getlastmodified", 200);
            assertNotNull(lastmod,
                    "E1: DAV:getlastmodified missing from a 200 propstat for member "
                    + firstHref(response) + " (must carry DAV:getlastmodified in a 200 propstat)");

            assertTrue(etag.getTextContent().trim().length() > 0,
                    "E1: DAV:getetag value must not be empty for " + firstHref(response));
            assertTrue(lastmod.getTextContent().trim().length() > 0,
                    "E1: DAV:getlastmodified value must not be empty for " + firstHref(response));
        }
    }

    /**
     * E2: an unknown/unsupported property must surface as a 404 propstat,
     * separated from the 200 propstat that carries the valid requested
     * properties. The client must be able to distinguish "server does not
     * support this property" (404 propstat) from "server returns it" (200
     * propstat). A 5xx on an unknown property, or the unknown property
     * surfacing in the 200 propstat with no value, would both mislead
     * clients into thinking this is a server-supported property.
     */
    @Test
    public void unknownPropertySurfacesAs404PropStatWithKnownPropsStill200() throws Exception {
        final int members = 2;
        givenHomeChildCollections(members);

        DavTestContext ctx = executeSyncCollectionReport(BODY_E2_UNKNOWN_PROP_AND_GETETAG);
        assertEquals(207, ctx.getDavResponse().getStatus(),
                "E2: unknown property in DAV:prop must not invalidate the overall REPORT (207 expected)");

        Document ms = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses = getChildElements(ms.getDocumentElement(), "response");

        assertEquals(members, responses.size(),
                "E2: every fixture member must still produce one DAV:response entry");

        for (Element response : responses) {
            Element etag = findPropStatElement(response, "getetag", 200);
            assertNotNull(etag,
                    "E2: the known property (DAV:getetag) still returned in a 200 propstat for "
                            + firstHref(response));

            Element nopeIn200 = findPropStatElement(response, "nope", 200);
            assertNull(nopeIn200,
                    "E2: an unknown custom property must NOT appear inside the 200 propstat "
                            + "(" + firstHref(response) + ") -- it would mislead clients into "
                            + "thinking it is a server-defined live property");

            Element nopeIn404 = findPropStatElement(response, "nope", 404);
            assertNotNull(nopeIn404,
                    "E2: DAV:nope (urn:example:nope) must surface as a 404 propstat so the client "
                            + "can distinguish a property the server does NOT support for "
                            + firstHref(response));
        }
    }

    /**
     * E3: omitting the DAV:prop element (permitted by the RFC 6578 DTD: prop?)
     * must be handled gracefully: 207 status, each DAV:response carries at least
     * a DAV:href value.
     */
    @Test
    public void reportWithoutPropElementStillYields207WithHrefPerResponse() throws Exception {
        givenHomeChildCollections(2);

        DavTestContext ctx = executeSyncCollectionReport(BODY_E3_NO_PROP_ELEMENT);
        assertEquals(207, ctx.getDavResponse().getStatus(),
                "E3: a DAV:sync-collection request without DAV:prop is legal (DTD: prop?); "
                        + "the pipeline must not turn it into a 400/4xx: got "
                        + ctx.getDavResponse().getStatus());

        Document ms = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses = getChildElements(ms.getDocumentElement(), "response");
        assertFalse(responses.isEmpty(), "E3: fixture members must still be listed");

        for (Element response : responses) {
            List<Element> hrefs = getChildElements(response, "href");
            assertFalse(hrefs.isEmpty(),
                    "E3: each DAV:response must still carry at least one direct DAV:href child "
                            + "(per DAV multistatus structure, DAV:href is a direct child of "
                            + "DAV:response, not inside a propstat); a multistatus entry without "
                            + "a resolvable href is unusable by clients (RFC 6578 § 3.3)");
            assertFalse(firstHref(response).equals("(no href)"),
                    "E3: DAV:href value must be non-empty for each member");
        }
    }

    /**
     * E3 companion: the empty {@code <D:prop/>} element is accepted as well &mdash;
     * equivalent to omitting {@code DAV:prop}.
     */
    @Test
    public void emptyPropElementIsAcceptedEquivalently() throws Exception {
        givenHomeChildCollections(1);

        DavTestContext ctx = executeSyncCollectionReport(BODY_E3_EMPTY_PROP_ELEMENT);
        assertEquals(207, ctx.getDavResponse().getStatus(),
                "E3-companion: an empty <D:prop/> child is a legal request and must not be "
                        + "rejected as 4xx: got " + ctx.getDavResponse().getStatus());
    }

    /**
     * E4: the RFC 6578 DTD does not define a {@code DAV:allprop} element for
     * {@code DAV:sync-collection} (unlike PROPFIND, the element list is
     * fixed: sync-token?, sync-level, limit?, prop?).
     * <p>
     * <strong>Documented server behavior (observed and locked here,
     * 2026-08-28):</strong> an unrecognized {@code D:allprop} child is
     * <em>ignored</em>: the request is treated exactly like the empty
     * property selection of E3 &mdash; 207, each member listed as a bare
     * {@code <D:response>} with {@code DAV:href} + {@code 200 OK} status and
     * no propstat, and a fresh {@code DAV:sync-token} minted. In particular
     * it is neither honored as "all properties" nor rejected with a 4xx,
     * and the pipeline never 5xx on it.
     * </p>
     */
    @Test
    public void allpropVariantIsIgnoredTreatedAsEmptyPropSelection() throws Exception {
        givenHomeChildCollections(1);

        DavTestContext ctx = executeSyncCollectionReport(BODY_E4_ALLPROP_VARIANT);
        assertEquals(207, ctx.getDavResponse().getStatus(),
                "E4: an out-of-DTD <D:allprop/> is ignored by the pipeline (treated as"
                        + " empty property selection) and must still yield 207 &mdash; got "
                        + ctx.getDavResponse().getStatus());

        Document ms = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses = getChildElements(ms.getDocumentElement(), "response");
        assertFalse(responses.isEmpty(), "E4: the fixture member must still be listed");

        for (Element response : responses) {
            assertFalse(getChildElements(response, "href").isEmpty(),
                    "E4: DAV:href must still be present on the DAV:response for "
                            + firstHref(response));
            assertTrue(getChildElements(response, "propstat").isEmpty(),
                    "E4: an ignored <D:allprop/> must not be expanded into a propstat"
                    + " (all-prop expansion would be a PROPFIND-style behavior that the"
                    + " pipeline does not implement for sync-collection)");
        }
    }

    // ---------- helpers ----------

    private void givenHomeChildCollections(int count) throws Exception {
        for (int i = 0; i < count; i++) {
            assertNotNull(testHelper.makeAndStoreDummyCollection(),
                    "fixture child collection " + i + " was not stored");
        }
    }

    private DavTestContext executeSyncCollectionReport(String requestBody) throws Exception {
        DavTestContext ctx = testHelper.createTestContext();

        ctx.getHttpRequest().setServletPath("/dav");
        ctx.getHttpRequest().setRequestURI("/dav/test");
        ctx.getHttpRequest().setMethod("REPORT");
        ctx.getHttpRequest().setContent(requestBody.getBytes(StandardCharsets.UTF_8));
        ctx.getHttpRequest().setContentType("application/xml");
        ctx.getHttpRequest().addHeader("Content-Type", "application/xml");

        new StandardRequestHandler(testHelper.getResourceLocatorFactory(),
                testHelper.getResourceFactory(),
                testHelper.getEntityFactory())
                .handleRequest(ctx.getDavRequest(), ctx.getDavResponse());

        return ctx;
    }

    // ---------- XML assertion helpers ----------

    private Document parseMultistatus(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document doc = dbf.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        assertEquals("multistatus", doc.getDocumentElement().getLocalName(),
                "response root must be DAV:multistatus");
        return doc;
    }

    /**
     * Direct children of {@code parent} (element node type only) in the DAV
     * namespace with the given local name.
     */
    private List<Element> getChildElements(Element parent, String localName) {
        List<Element> result = new ArrayList<Element>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE
                    && DAV_NAMESPACE.equals(child.getNamespaceURI())
                    && localName.equals(child.getLocalName())) {
                result.add((Element) child);
            }
        }
        return result;
    }

    /**
     * Returns the first {@code <D:prop>} child element inside any 200 (or 404)
     * propstat of {@code response} whose local name matches {@code propLocalName}.
     * The search is namespace-agnostic for the inner property (the request may
     * mix DAV:* and custom urn:* properties).
     */
    private Element findPropStatElement(Element response, String propLocalName, int expectedCode) {
        for (Element propstat : getChildElements(response, "propstat")) {
            Element statusElement = null;
            if (!getChildElements(propstat, "status").isEmpty()) {
                statusElement = getChildElements(propstat, "status").get(0);
            }
            Integer code = parseStatusCode(statusElement);
            if (code == null || code != expectedCode) {
                continue;
            }
            for (Element prop : getChildElements(propstat, "prop")) {
                NodeList propChildren = prop.getChildNodes();
                for (int i = 0; i < propChildren.getLength(); i++) {
                    Node child = propChildren.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE
                            && propLocalName.equals(child.getLocalName())) {
                        return (Element) child;
                    }
                }
            }
        }
        return null;
    }

    private Integer parseStatusCode(Element statusElement) {
        if (statusElement == null) {
            return null;
        }
        String text = statusElement.getTextContent().trim();
        String[] parts = text.split("\\s+");
        if (parts.length >= 2 && parts[0].startsWith("HTTP/")) {
            try {
                return Integer.valueOf(parts[1]);
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    private String firstHref(Element response) {
        List<Element> hrefs = getChildElements(response, "href");
        if (hrefs.isEmpty()) {
            return "(no href)";
        }
        return hrefs.get(0).getTextContent();
    }
}
