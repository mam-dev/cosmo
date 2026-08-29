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
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.DavTestContext;
import org.unitedinternet.cosmo.dav.servlet.StandardRequestHandler;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.NoteItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Integration tests for the <strong>initial</strong> {@code DAV:sync-collection}
 * REPORT (RFC 6578, Section 3) against the full WebDAV request pipeline
 * ({@link StandardRequestHandler} &rarr; provider &rarr; report).
 * </p>
 *
 * <p>
 * Covers the following cases from {@code synccollection-testcases.md}:
 * </p>
 * <ul>
 * <li><strong>B1</strong> - REPORT with an empty {@code DAV:sync-token} returns all current
 * members (with their requested properties, e.g. {@code DAV:getetag}) inside a 207 Multi-Status
 * whose last child is a fresh, non-empty {@code DAV:sync-token}.</li>
 * <li><strong>B2</strong> - omitting the {@code DAV:sync-token} element behaves exactly like an
 * empty one (initial synchronization).</li>
 * <li><strong>B4</strong> - initial sync against an empty collection yields zero
 * {@code DAV:response} elements but still carries a valid {@code DAV:sync-token}.</li>
 * </ul>
 *
 * <p>
 * <strong>Status: green.</strong> All tests pass since the
 * {@code DAV:sync-collection} report type was registered on collection resources
 * via {@link SyncCollectionReport} (RFC 6578 support). Before registration the
 * pipeline answered {@code 422 Unprocessable Entity} ("Unknown report") instead
 * of the required {@code 207 Multi-Status}; some assertion messages deliberately
 * retain that historical failure-mode context to aid diagnosis.
 * </p>
 */
public class SyncCollectionInitialSyncIntegrationTest extends BaseDavTestCase {

    private static final String DAV_NAMESPACE = "DAV:";

    private static final String SYNC_COLLECTION_NAME = "sync-initial";

    /** RFC 6578 Section 3.1 initial-synchronization request (empty sync-token). */
    private static final String INITIAL_SYNC_BODY =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<D:sync-collection xmlns:D=\"DAV:\">\n"
        + "  <D:sync-token/>\n"
        + "  <D:sync-level>1</D:sync-level>\n"
        + "  <D:prop>\n"
        + "    <D:getetag/>\n"
        + "  </D:prop>\n"
        + "</D:sync-collection>";

    /** Same request without any DAV:sync-token element (must behave identically). */
    private static final String INITIAL_SYNC_BODY_WITHOUT_TOKEN =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<D:sync-collection xmlns:D=\"DAV:\">\n"
        + "  <D:sync-level>1</D:sync-level>\n"
        + "  <D:prop>\n"
        + "    <D:getetag/>\n"
        + "  </D:prop>\n"
        + "</D:sync-collection>";

    /**
     * Working provider methods require a security context so ACL checks pass.
     */
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        testHelper.logIn();
    }

    /**
     * Test case B1: an initial sync-collection request (empty sync-token) must return
     * every current member of the collection with its DAV:getetag property and finish
     * the multistatus with a fresh, non-empty DAV:sync-token.
     */
    @Test
    public void initialSyncWithEmptyTokenReturnsAllCurrentMembersAndNewSyncToken()
            throws Exception {
        final int memberCount = 3;
        givenHomeChildCollections(memberCount);

        DavTestContext ctx = executeSyncCollectionReport(INITIAL_SYNC_BODY);

        assertEquals(207, ctx.getDavResponse().getStatus(),
                "initial sync-collection REPORT must be answered with 207 Multi-Status "
                + "(got " + ctx.getDavResponse().getStatus() + "; if this is 422, the "
                + "DAV:sync-collection report type is not registered yet)");

        Document multistatus = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses = getChildElements(multistatus.getDocumentElement(), "response");
        List<Element> tombstones = responsesDeletedMembers(responses);

        assertEquals(memberCount, responses.size(),
                "initial sync must list all current members as DAV:response elements");
        assertTrue(tombstones.isEmpty(),
                "initial sync must not contain deleted-member (404) entries");

        for (Element response : responses) {
            Element etagProp = findPropStatProp(response, "getetag", 200);
            assertNotNull(etagProp,
                    "every member response must carry DAV:getetag within a 200 propstat");
            assertFalse(etagProp.getTextContent().trim().isEmpty(),
                    "DAV:getetag value must not be empty");
        }

        Element syncToken = findDirectSyncToken(multistatus.getDocumentElement());
        assertNotNull(syncToken,
                "the multistatus MUST contain a DAV:sync-token element");
        assertFalse(syncToken.getTextContent().trim().isEmpty(),
                "the returned DAV:sync-token must not be empty");
        assertSyncTokenIsLastChild(multistatus.getDocumentElement(), syncToken);
    }

    /**
     * Test case B2: omitting the DAV:sync-token element entirely is equivalent to sending
     * an empty one - both trigger an initial (full-list) synchronization.
     */
    @Test
    public void initialSyncWithoutSyncTokenElementBehavesLikeEmptyToken() throws Exception {
        final int memberCount = 2;
        givenHomeChildCollections(memberCount);

        DavTestContext ctx = executeSyncCollectionReport(INITIAL_SYNC_BODY_WITHOUT_TOKEN);

        assertEquals(207, ctx.getDavResponse().getStatus(),
                "REPORT without DAV:sync-token must still perform an initial sync (207)");

        Document multistatus = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses = getChildElements(multistatus.getDocumentElement(), "response");

        assertEquals(memberCount, responses.size(),
                "absent sync-token must behave like an empty one (full member listing)");

        Element syncToken = findDirectSyncToken(multistatus.getDocumentElement());
        assertNotNull(syncToken, "response must still carry a DAV:sync-token");
        assertFalse(syncToken.getTextContent().trim().isEmpty(),
                "returned DAV:sync-token must not be empty");
    }

    /**
     * Test case B4: initial sync against an empty collection returns zero DAV:response
     * elements but still provides a valid DAV:sync-token for subsequent rounds.
     */
    @Test
    public void initialSyncOnEmptyCollectionReturnsOnlySyncToken() throws Exception {
        // pristine home collection: no child members yet

        DavTestContext ctx = executeSyncCollectionReport(INITIAL_SYNC_BODY);

        assertEquals(207, ctx.getDavResponse().getStatus(),
                "initial sync of an empty collection must still be a 207 Multi-Status");

        Document multistatus = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses = getChildElements(multistatus.getDocumentElement(), "response");

        assertTrue(responses.isEmpty(),
                "no DAV:response elements expected for an empty collection");

        Element syncToken = findDirectSyncToken(multistatus.getDocumentElement());
        assertNotNull(syncToken,
                "even an empty result set requires a DAV:sync-token for the next round");
        assertFalse(syncToken.getTextContent().trim().isEmpty(),
                "DAV:sync-token for an empty collection must not be empty");
    }

    /**
     * Test case G-a: a REPORT request whose body is missing entirely is invalid
     * and must be answered with 400 Bad Request - never silently ignored.
     *
     * Regression guard: BaseProvider.report() used to return silently when a
     * collection received a body-less REPORT, leaving the response at the
     * default 200.
     */
    @Test
    public void reportWithoutRequestBodyMustBeAnsweredWith400() throws Exception {
        DavTestContext ctx = testHelper.createTestContext();
        ctx.getHttpRequest().setServletPath("/dav");
        ctx.getHttpRequest().setRequestURI("/dav/test");
        ctx.getHttpRequest().setMethod("REPORT");
        ctx.getHttpRequest().setContentType("application/xml");

        new StandardRequestHandler(testHelper.getResourceLocatorFactory(),
                testHelper.getResourceFactory(),
                testHelper.getEntityFactory())
                .handleRequest(ctx.getDavRequest(), ctx.getDavResponse());

        int status = ctx.getDavResponse().getStatus();
        assertTrue(status >= 400 && status < 500,
                "a body-less REPORT must be rejected as a client error "
                + "(got " + status + "; 200 means the request was silently ignored)");
        assertEquals(400, status,
                "missing report body must yield 400 Bad Request");
    }

    /**
     * Test case F-edge: DAV:nresults accepts any non-negative integer; 0 means
     * "no member entries", yet the response must still be a valid 207 multistatus
     * carrying a usable DAV:sync-token.
     *
     * Historical note: an early draft of SyncCollectionReport.parseReport()
     * rejected nresults <= 0 with 400 Bad Request; shipped behavior treats
     * 0 as a valid request for zero member entries.
     */
    @Test
    public void nresultsZeroReturnsEmptyMultistatusWithToken() throws Exception {
        givenHomeChildCollections(3);

        String body =
              "<D:sync-collection xmlns:D=\"DAV:\">"
            + "<D:sync-token/>"
            + "<D:sync-level>1</D:sync-level>"
            + "<D:limit><D:nresults>0</D:nresults></D:limit>"
            + "<D:prop><D:getetag/></D:prop>"
            + "</D:sync-collection>";

        DavTestContext ctx = executeSyncCollectionReport(body);

        assertEquals(207, ctx.getDavResponse().getStatus(),
                "nresults=0 is a valid truncation request and must yield 207");

        Document multistatus = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses = getChildElements(multistatus.getDocumentElement(), "response");
        assertTrue(responses.isEmpty(),
                "nresults=0 must truncate the result list to zero entries");

        Element syncToken = findDirectSyncToken(multistatus.getDocumentElement());
        assertNotNull(syncToken, "even a fully truncated response needs a DAV:sync-token");
    }

    /**
     * Test case E/H hybrid: every DAV:href in the multistatus must be a valid
     * RFC 3986 URI reference. Fixture member names deliberately contain spaces;
     * emitting them unencoded into DAV:href breaks the XML/URI contract.
     *
     * Guards against a report framework that copies raw item names into hrefs
     * without percent-encoding.
     */
    @Test
    public void memberHrefsMustBeValidUriReferences() throws Exception {
        givenHomeChildCollections(3);

        DavTestContext ctx = executeSyncCollectionReport(INITIAL_SYNC_BODY);

        assertEquals(207, ctx.getDavResponse().getStatus());

        Document multistatus = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses = getChildElements(multistatus.getDocumentElement(), "response");
        assertFalse(responses.isEmpty(), "fixture members must be listed");

        for (Element response : responses) {
            List<Element> hrefs = getChildElements(response, "href");
            assertFalse(hrefs.isEmpty(), "each DAV:response needs a DAV:href");
            for (Element href : hrefs) {
                String value = href.getTextContent().trim();
                try {
                    new URI(value);
                } catch (java.net.URISyntaxException e) {
                    fail("DAV:href \"" + value + "\" is not a valid URI reference: "
                            + e.getMessage());
                }
            }
        }
    }

    // fixture helpers

    /**
     * RFC 6578 applies to any collection, so we sync the user's HOME collection
     * ("/dav/test", always resolvable, no name escaping issues) and create plain
     * sub-collections as its members. Sub-collections are regular DAV collection
     * resources - unlike bare notes they carry no calendar stamp, avoiding
     * DavCalendarResource's calendar conversion during live-property loading.
     */
    private void givenHomeChildCollections(int count) throws Exception {
        for (int i = 0; i < count; i++) {
            assertNotNull(testHelper.makeAndStoreDummyCollection(),
                    "fixture child collection " + i + " was not stored");
        }
    }

    /**
     * Sends a REPORT request against the fixture collection through the complete
     * {@link StandardRequestHandler} pipeline (real request/response wrappers over
     * Spring mock servlet objects).
     */
    private DavTestContext executeSyncCollectionReport(String requestBody) throws Exception {
        DavTestContext ctx = testHelper.createTestContext();

        // mimic the production layout: DAV servlet mounted at /dav,
        // home collection of user 'test' at /dav/test
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

    // XML assertion helpers

    private Document parseMultistatus(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document = dbf.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        assertEquals("multistatus", document.getDocumentElement().getLocalName(),
                "response root element must be DAV:multistatus");
        return document;
    }

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
     * Returns the responses that signal a deleted member via a bare DAV:status 404.
     */
    private List<Element> responsesDeletedMembers(List<Element> responses) {
        List<Element> tombstones = new ArrayList<Element>();
        for (Element response : responses) {
            List<Element> statuses = getChildElements(response, "status");
            for (Element status : statuses) {
                if (status.getTextContent().contains("404")) {
                    tombstones.add(response);
                }
            }
        }
        return tombstones;
    }

    /**
     * Finds the named DAV: property inside the response's 200-propstat block.
     */
    private Element findPropStatProp(Element response, String propLocalName, int expectedCode) {
        for (Element propstat : getChildElements(response, "propstat")) {
            Integer code = null;
            Element statusElement = null;
            for (Element child : getChildElements(propstat, "status")) {
                statusElement = child;
            }
            if (statusElement != null && statusElement.getTextContent().trim().matches(
                    "^HTTP/[0-9.]+ (" + expectedCode + ").*")) {
                code = expectedCode;
            }
            if (!Integer.valueOf(expectedCode).equals(code)) {
                continue;
            }
            for (Element prop : getChildElements(propstat, "prop")) {
                for (Element candidate : getChildElements(prop, propLocalName)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Finds the direct DAV:sync-token child of the multistatus root (RFC 6578 requires it
     * to appear once, directly below multistatus).
     */
    private Element findDirectSyncToken(Element multistatusRoot) {
        List<Element> tokens = getChildElements(multistatusRoot, "sync-token");
        if (tokens.isEmpty()) {
            return null;
        }
        return tokens.get(tokens.size() - 1);
    }

    /**
     * RFC 6578: the sync-token element appears as the LAST child of multistatus.
     */
    private void assertSyncTokenIsLastChild(Element multistatusRoot, Element syncToken) {
        Node lastElementChild = null;
        NodeList children = multistatusRoot.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                lastElementChild = child;
                break;
            }
        }
        assertNotNull(lastElementChild, "multistatus must have element children");
        assertTrue(lastElementChild.isSameNode(syncToken),
                "DAV:sync-token must be the last child of DAV:multistatus");
    }
}