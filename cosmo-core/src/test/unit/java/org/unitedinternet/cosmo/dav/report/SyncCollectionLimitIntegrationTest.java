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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.unitedinternet.cosmo.model.CollectionItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Integration tests for the <strong>limit / truncation</strong> behavior of
 * the {@code DAV:sync-collection} REPORT (RFC 6578 Section 3.6) against the
 * full WebDAV request pipeline.
 * </p>
 *
 * <p>
 * Complements the already-committed F2 pagination-convergence test
 * ({@link SyncCollectionIncrementalSyncIntegrationTest}) with the
 * remaining cases from {@code synccollection-testcases.md}:
 * </p>
 * <ul>
 * <li><strong>F1</strong> - {@code nresults} is honored exactly: with 10
 * pending changes and {@code nresults=4} the first round lists exactly 4
 * members; the issued sync-token reflects the state after the 4th change, so
 * a second round with the same limit returns the next 4; a third round
 * returns the remaining 2 (proving the token advanced correctly and no
 * change was lost or duplicated across pages).</li>
 * <li><strong>F3</strong> - an {@code nresults} larger than the number of
 * pending changes is silently capped: all 10 changes are returned in one
 * round and the issued token is the fully-drained one (a re-query then
 * reports zero entries).</li>
 * <li><strong>F5</strong> - invalid {@code nresults} values (negative and
 * non-numeric) are rejected with {@code 400 Bad Request} instead of being
 * misinterpreted as limits.</li>
 * </ul>
 *
 * <p>
 * <strong>Status: written red-first (2026-08-28); expected green because the
 * implementation ({@link SyncCollectionReport}) already parses and applies
 * the limit — these are regression locks for the exact page-size and
 * token-advance semantics.</strong>
 * </p>
 */
public class SyncCollectionLimitIntegrationTest extends BaseDavTestCase {

    private static final String DAV_NAMESPACE = "DAV:";

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

    /**
     * Working provider methods require a security context so ACL checks pass.
     */
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        testHelper.logIn();
    }

    // F1

    /**
     * Test case F1: with 10 pending changes and {@code nresults=4}, the
     * incremental rounds return 4 + 4 + 2 entries. In particular:
     * <ul>
     * <li>each round lists at most {@code nresults} members;</li>
     * <li>every member appears in exactly ONE round (no loss, no duplicate);</li>
     * <li>each truncated round still issues a non-empty continuation
     * {@code DAV:sync-token};</li>
     * <li>a final round after draining reports zero entries.</li>
     * </ul>
     */
    @Test
    public void nresultsLimitsEachRoundAndTokenAdvancesPerPage() throws Exception {
        givenHomeChildCollections(1);
        String initialToken = doInitialSyncAndGetToken();

        List<CollectionItem> changes = new ArrayList<CollectionItem>(10);
        for (int i = 0; i < 10; i++) {
            CollectionItem c = testHelper.makeAndStoreDummyCollection();
            assertNotNull(c, "fixture change " + i + " was not stored");
            changes.add(c);
        }

        // round 1: 4 of 10
        DavTestContext round1 = executeSyncCollectionReport(
                incrementalBody(initialToken, 4));
        assertEquals(207, round1.getDavResponse().getStatus(),
                "F1: truncated round must be a 207 Multi-Status");
        Document ms1 = parseMultistatus(round1.getHttpResponse().getContentAsString());
        List<Element> responses1 =
                getChildElements(ms1.getDocumentElement(), "response");
        assertEquals(4, responses1.size(),
                "F1: nresults=4 must cap round 1 at exactly 4 entries (got "
                        + responses1.size() + ")");
        String token1 = requiredSyncToken(ms1, "F1 round 1");

        // round 2: next 4
        DavTestContext round2 = executeSyncCollectionReport(
                incrementalBody(token1, 4));
        assertEquals(207, round2.getDavResponse().getStatus(),
                "F1: continuation rounds must be 207");
        Document ms2 = parseMultistatus(round2.getHttpResponse().getContentAsString());
        List<Element> responses2 =
                getChildElements(ms2.getDocumentElement(), "response");
        assertEquals(4, responses2.size(),
                "F1: round 2 must list the next 4 changes (got "
                        + responses2.size() + ")");
        String token2 = requiredSyncToken(ms2, "F1 round 2");

        // round 3: the remaining 2, i.e. fewer than nresults
        DavTestContext round3 = executeSyncCollectionReport(
                incrementalBody(token2, 4));
        assertEquals(207, round3.getDavResponse().getStatus());
        Document ms3 = parseMultistatus(round3.getHttpResponse().getContentAsString());
        List<Element> responses3 =
                getChildElements(ms3.getDocumentElement(), "response");
        assertEquals(2, responses3.size(),
                "F1: round 3 must list exactly the remaining 2 changes "
                        + "(token must reflect the state after the 8th change)");

        // the union of the three pages is exactly the 10 changes, each once
        List<String> pageHrefs = new ArrayList<String>();
        pageHrefs.addAll(hrefSegments(responses1));
        pageHrefs.addAll(hrefSegments(responses2));
        pageHrefs.addAll(hrefSegments(responses3));
        assertEquals(10, pageHrefs.size(),
                "F1: the pages must jointly contain all 10 changes");
        assertEquals(10, new java.util.HashSet<String>(pageHrefs).size(),
                "F1: no change may be reported in more than one page "
                        + "(duplicate across truncation boundary)");

        // round 4: drained
        DavTestContext round4 = executeSyncCollectionReport(
                incrementalBody(requiredSyncToken(ms3, "F1 round 3"), 4));
        assertEquals(207, round4.getDavResponse().getStatus());
        Document ms4 = parseMultistatus(round4.getHttpResponse().getContentAsString());
        assertTrue(getChildElements(ms4.getDocumentElement(), "response").isEmpty(),
                "F1: after all changes are drained a further round must list none");

        // every member actually appears exactly once across all pages
        for (CollectionItem change : changes) {
            int occurrences = 0;
            for (String href : pageHrefs) {
                if (change.getName().equals(href)) {
                    occurrences++;
                }
            }
            assertEquals(1, occurrences,
                    "F1: member " + change.getName()
                            + " must be reported exactly once across all pages");
        }
    }

    // F3

    /**
     * Test case F3: {@code nresults} set well above the number of pending
     * changes must be silently capped — all 10 changes come back in a single
     * round, the issued token is the fully-drained one, so a token-less
     * re-query reports nothing more.
     */
    @Test
    public void nresultsLargerThanPendingChangesIsCappedWithoutTruncation() throws Exception {
        givenHomeChildCollections(1);
        String initialToken = doInitialSyncAndGetToken();

        for (int i = 0; i < 10; i++) {
            assertNotNull(testHelper.makeAndStoreDummyCollection(),
                    "fixture change " + i + " was not stored");
        }

        DavTestContext round1 = executeSyncCollectionReport(
                incrementalBody(initialToken, 1000));
        assertEquals(207, round1.getDavResponse().getStatus(),
                "F3: an oversized nresults must not fail the request");
        Document ms1 = parseMultistatus(round1.getHttpResponse().getContentAsString());
        List<Element> responses1 =
                getChildElements(ms1.getDocumentElement(), "response");
        assertEquals(10, responses1.size(),
                "F3: all 10 pending changes must be returned in one round when "
                        + "nresults (1000) exceeds the pending count");
        String drainedToken = requiredSyncToken(ms1, "F3 round 1");

        DavTestContext round2 = executeSyncCollectionReport(
                incrementalBody(drainedToken, 1000));
        assertEquals(207, round2.getDavResponse().getStatus());
        Document ms2 = parseMultistatus(round2.getHttpResponse().getContentAsString());
        assertTrue(getChildElements(ms2.getDocumentElement(), "response").isEmpty(),
                "F3: the single round must have drained the change log; a re-query "
                        + "with the issued token must report no further entries");
    }

    // F5

    /**
     * Test case F5: invalid {@code DAV:nresults} values must be rejected with
     * {@code 400 Bad Request} rather than being clamped, ignored, or causing a
     * 5xx:
     * <ul>
     * <li>{@code nresults=-5} (negative, RFC 6578 Section 3.6 requires a
     * non-negative integer);</li>
     * <li>{@code nresults=abc} (non-numeric).</li>
     * </ul>
     */
    @Test
    public void invalidNresultsValuesAreRejectedWith400() throws Exception {
        givenHomeChildCollections(1);

        DavTestContext ctx = executeSyncCollectionReport(
                incrementalBodyRawNresults("", "-5"));
        assertEquals(400, ctx.getDavResponse().getStatus(),
                "F5: a negative DAV:nresults must be rejected with 400 Bad Request "
                        + "(got " + ctx.getDavResponse().getStatus() + ")");

        DavTestContext ctx2 = executeSyncCollectionReport(
                incrementalBodyRawNresults("", "abc"));
        assertEquals(400, ctx2.getDavResponse().getStatus(),
                "F5: a non-numeric DAV:nresults must be rejected with 400 Bad Request "
                        + "(got " + ctx2.getDavResponse().getStatus() + ")");
    }

    // ---------- fixture helpers ----------

    /**
     * Performs an initial synchronization of the fixture home collection and
     * returns the issued sync-token.
     */
    private String doInitialSyncAndGetToken() throws Exception {
        DavTestContext ctx = executeSyncCollectionReport(INITIAL_SYNC_BODY);
        assertEquals(207, ctx.getDavResponse().getStatus(),
                "initial synchronization must succeed first (got "
                        + ctx.getDavResponse().getStatus() + ")");
        return requiredSyncToken(
                parseMultistatus(ctx.getHttpResponse().getContentAsString()),
                "initial sync");
    }

    private int givenHomeChildCollections(int count) throws Exception {
        for (int i = 0; i < count; i++) {
            assertNotNull(testHelper.makeAndStoreDummyCollection(),
                    "fixture child collection " + i + " was not stored");
        }
        return count;
    }

    /**
     * Builds an incremental sync-collection body with a numeric nresults.
     */
    private static String incrementalBody(String token, int nresults) {
        return incrementalBodyRawNresults(token, String.valueOf(nresults));
    }

    /**
     * Builds an incremental sync-collection body with an arbitrary nresults
     * literal (the raw form is needed for F5's invalid values).
     */
    private static String incrementalBodyRawNresults(String token, String nresultsRaw) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<D:sync-collection xmlns:D=\"DAV:\">\n"
            + "  <D:sync-token>" + token + "</D:sync-token>\n"
            + "  <D:sync-level>1</D:sync-level>\n"
            + "  <D:limit><D:nresults>" + nresultsRaw + "</D:nresults></D:limit>\n"
            + "  <D:prop>\n"
            + "    <D:getetag/>\n"
            + "  </D:prop>\n"
            + "</D:sync-collection>";
    }

    /**
     * Sends a REPORT request against the fixture collection through the
     * complete {@link StandardRequestHandler} pipeline.
     */
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

    private String requiredSyncToken(Document multistatus, String roundLabel) {
        Element syncToken = findDirectSyncToken(multistatus.getDocumentElement());
        assertNotNull(syncToken, roundLabel + " response must carry a DAV:sync-token");
        String value = syncToken.getTextContent().trim();
        assertFalse(value.isEmpty(), roundLabel + " DAV:sync-token must not be empty");
        return value;
    }

    private Element findDirectSyncToken(Element multistatusRoot) {
        List<Element> tokens = getChildElements(multistatusRoot, "sync-token");
        return tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);
    }

    /**
     * Last URL path segment of each response's DAV:href, decoded and with a
     * trailing slash stripped — stable identity of each reported member.
     */
    private List<String> hrefSegments(List<Element> responses) {
        List<String> segments = new ArrayList<String>();
        for (Element response : responses) {
            List<Element> hrefs = getChildElements(response, "href");
            if (!hrefs.isEmpty()) {
                String href = hrefs.get(0).getTextContent().trim();
                String decoded = java.net.URLDecoder.decode(
                        href, java.nio.charset.StandardCharsets.UTF_8);
                while (decoded.endsWith("/")) {
                    decoded = decoded.substring(0, decoded.length() - 1);
                }
                int slash = decoded.lastIndexOf('/');
                segments.add(slash >= 0 ? decoded.substring(slash + 1) : decoded);
            }
        }
        return segments;
    }

    private Document parseMultistatus(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document = dbf.newDocumentBuilder()
                .parse(new java.io.ByteArrayInputStream(
                        xml.getBytes(StandardCharsets.UTF_8)));
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
}
