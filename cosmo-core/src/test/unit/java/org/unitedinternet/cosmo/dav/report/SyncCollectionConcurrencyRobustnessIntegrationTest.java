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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URLDecoder;
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

/**
 * <p>
 * Integration tests for the <strong>concurrency and robustness</strong>
 * (Group H) cases of the {@code DAV:sync-collection} REPORT
 * ({@code synccollection-testcases.md}).
 * </p>
 * <ul>
 * <li><strong>H1</strong> - a change made between two truncated rounds is
 * neither lost nor duplicated: the drain converges, and every issued
 * {@code DAV:sync-token} is strictly greater than the previous one.</li>
 * <li><strong>H2</strong> - deleting an already-deleted member does not
 * produce a second tombstone in the round and does not surface as a 5xx:
 * the round reports exactly one 404 for the member.</li>
 * <li><strong>H3</strong> - member names with spaces and non-ASCII
 * characters (UTF-8) are reported as valid, consistently percent-encoded
 * {@code DAV:href}s that decode back to the exact original names.</li>
 * <li><strong>H5</strong> - an initial synchronization of a collection with
 * roughly one thousand members completes as a 207 Multi-Status listing all
 * members and still issuing a {@code DAV:sync-token}.</li>
 * </ul>
 *
 * <p>
 * <strong>H4</strong> (replaying an identical token twice must be idempotent
 * and never error out) is intentionally not re-tested here: D2
 * ({@code tombstoneIsConsumedBySubsequentSyncToken}) already locks that a
 * repeated incremental round with an issued token answers 207 and never 5xx.
 * </p>
 */
public class SyncCollectionConcurrencyRobustnessIntegrationTest
        extends BaseDavTestCase {

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

    // H1

    /**
     * Test case H1: a change created after page 1 of a truncated incremental
     * synchronization must be reported exactly once in the full change set,
     * and every round must issue a strictly advanced continuation token:
     * <ul>
     * <li>an initial sync establishes a baseline token;</li>
     * <li>six NEW members are added after that token (the pending changes);</li>
     * <li>page 1 (nresults=4) lists exactly 4 of them and issues a
     * continuation token;</li>
     * <li>one more change is created after page 1 (the mid-pagination
     * change);</li>
     * <li>page 2 (nresults=4) lists exactly 3 — the two tail changes plus
     * the mid-pagination addition — with no change lost and none
     * duplicated;</li>
     * <li>a final round (no limit) drains the log and reports zero
     * entries;</li>
     * <li>each round's issued token revision is strictly greater than the
     * previous round's.</li>
     * </ul>
     *
     * <p>
     * Note the fixture ordering: a member created <em>before</em> the
     * baseline token is intentionally excluded from the incrementals (that
     * is the correct "caught up to now" semantics, per the passing
     * {@code F1} pagination test), so the six pending changes are created
     * strictly after the token.
     * </p>
     */
    @Test
    public void changesMadeDuringPaginationAreNotLostAndTokensAreMonotonic() throws Exception {
        String token0 = doInitialSyncAndGetToken();
        for (int i = 0; i < 6; i++) {
            assertNotNull(testHelper.makeAndStoreDummyCollection(),
                    "pending change " + i + " was not stored");
        }

        // Page 1: nresults=4 over 6 pending changes → 4 entries, continuation token
        DavTestContext page1 = executeSyncCollectionReport(incrementalBody(token0, 4));
        assertEquals(207, page1.getDavResponse().getStatus(),
                "H1: page 1 must be a 207");
        Document ms1 = parseMultistatus(page1.getHttpResponse().getContentAsString());
        List<Element> r1 = getChildElements(ms1.getDocumentElement(), "response");
        assertEquals(4, r1.size(),
                "H1: page 1 must cap at exactly nresults=4 entries");
        String token1 = requiredSyncToken(ms1, "H1 page 1");

        // Mid-pagination change (the H1 scenario):
        CollectionItem lateAdd = testHelper.makeAndStoreDummyCollection();
        assertNotNull(lateAdd, "H1: late-added fixture was not stored");

        // Page 2: nresults=4 over the 2 tail changes + the late add → 3 entries.
        DavTestContext page2 = executeSyncCollectionReport(incrementalBody(token1, 4));
        assertEquals(207, page2.getDavResponse().getStatus(),
                "H1: page 2 must be a 207");
        Document ms2 = parseMultistatus(page2.getHttpResponse().getContentAsString());
        List<Element> r2 = getChildElements(ms2.getDocumentElement(), "response");
        assertEquals(3, r2.size(),
                "H1: page 2 must list the two trailing changes plus the "
                        + "mid-pagination addition — no loss, no duplicate");
        String token2 = requiredSyncToken(ms2, "H1 page 2");

        // Drain: a further round with the drained token must report no entries.
        DavTestContext drain = executeSyncCollectionReport(incrementalBody(token2, null));
        assertEquals(207, drain.getDavResponse().getStatus(),
                "H1: empty drain round must still be a 207");
        Document ms3 = parseMultistatus(drain.getHttpResponse().getContentAsString());
        List<Element> r3 = getChildElements(ms3.getDocumentElement(), "response");
        assertTrue(r3.isEmpty(),
                "H1: after draining, a further round must report zero entries");

        // Monotonicity: every round's issued token revision is strictly
        // greater than the previous round's.
        long rev0 = tokenRevision(token0);
        long rev1 = tokenRevision(token1);
        long rev2 = tokenRevision(token2);
        assertTrue(rev1 > rev0,
                "H1: page 1's revision (" + rev1 + ") must advance past "
                        + "the initial token (" + rev0 + ")");
        assertTrue(rev2 > rev1,
                "H1: page 2's revision (" + rev2 + ") must advance past "
                        + "page 1 (" + rev1 + ")");
    }

    // H2

    /**
     * Test case H2: removing a member twice must not yield a double tombstone
     * and must not surface as an error in the incremental round:
     * <ul>
     * <li>the two removals are both processed without a 5xx;</li>
     * <li>the incremental round is a 207;</li>
     * <li>exactly ONE response is reported, and it is the bare-404 tombstone
     * (two D-rows are written to the change log — the second deletion
     * call is idempotent at the persistence layer but still logs — the
     * report-level dedup in {@code SyncCollectionReport#addTombstone}
     * suppresses the duplicate so the client sees a single 404).</li>
     * </ul>
     */
    @Test
    public void doubleDeletionReportsASingleTombstoneAndDoesNotFail() throws Exception {
        CollectionItem victim = testHelper.makeAndStoreDummyCollection();
        assertNotNull(victim, "fixture child collection was not stored");
        String initialToken = doInitialSyncAndGetToken();

        testHelper.getContentService().removeCollection(victim);
        // Second removal of the same (already removed) member.
        testHelper.getContentService().removeCollection(victim);

        DavTestContext ctx = executeSyncCollectionReport(incrementalBody(initialToken, null));
        int status = ctx.getDavResponse().getStatus();
        assertTrue(status >= 200 && status < 500,
                "H2: double-deletion round must not 5xx (got " + status + ")");
        assertEquals(207, status,
                "H2: the round must answer 207 Multi-Status (got " + status + ")");

        Document multistatus = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses =
                getChildElements(multistatus.getDocumentElement(), "response");
        assertEquals(1, responses.size(),
                "H2: exactly one of the (double) deletion attempts may be reported");

        Element response = responses.get(0);
        Element statusEl = findChildElement(response, "status");
        assertTrue(statusEl != null
                && statusEl.getTextContent().startsWith("HTTP/1.1 404")
                && !hasChildLocalName(response, "propstat"),
                "H2: the single reported entry must be a bare-404 tombstone of "
                        + "the deleted member");
        assertEquals(victim.getName(), decodedHref(response),
                "H2: the tombstone must preserve the deleted member's name");
    }

    // H3

    /**
     * Test case H3: member names containing spaces and non-ASCII (UTF-8)
     * characters must be reported as syntactically valid, percent-encoded
     * {@code DAV:href}s whose last path segment decodes back to the exact
     * original name.
     */
    @Test
    public void escapedMemberNamesArePercentEncodedAndDecodable() throws Exception {
        String[] trickyNames = {
                "my event.ics",          // space + dot
                "événement.ics",        // non-ASCII
                "a b c.d"               // multiple segments + spaces
        };
        List<String> created = new ArrayList<String>();
        for (String name : trickyNames) {
            CollectionItem c = testHelper.makeDummyCollection(testHelper.getUser());
            c.setName(name);
            c.setDisplayName(name);
            CollectionItem stored = testHelper.getContentService()
                    .createCollection(testHelper.getHomeCollection(), c);
            assertNotNull(stored, "fixture \"" + name + "\" was not stored");
            created.add(stored.getName());
        }

        DavTestContext ctx = executeSyncCollectionReport(INITIAL_SYNC_BODY);
        assertEquals(207, ctx.getDavResponse().getStatus(),
                "H3: initial sync with escaped names must be 207 (got "
                        + ctx.getDavResponse().getStatus() + ")");
        Document multistatus = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses =
                getChildElements(multistatus.getDocumentElement(), "response");
        assertEquals(3, responses.size(),
                "H3: all three escaped-name members must be listed");

        List<String> decoded = new ArrayList<String>();
        for (Element response : responses) {
            String hrefRaw = requiredHref(response);
            new URI(hrefRaw); // must be a syntactically valid URI reference
            String href = hrefRaw;
            href = URLDecoder.decode(href, StandardCharsets.UTF_8.name());
            while (href.endsWith("/")) {
                href = href.substring(0, href.length() - 1);
            }
            int slash = href.lastIndexOf('/');
            decoded.add(slash >= 0 ? href.substring(slash + 1) : href);
        }
        for (String createdName : created) {
            assertTrue(decoded.contains(createdName),
                    "H3: member \" " + createdName + " \" must round-trip through "
                            + "percent-encoding — decoded hrefs were " + decoded);
        }
    }

    // H5

    /**
     * Test case H5: an initial synchronization of a collection with roughly
     * one thousand members completes as a 207 Multi-Status, lists all one
     * thousand members, and still issues a {@code DAV:sync-token} suitable
     * for subsequent incremental rounds (a follow-up incremental round against
     * it must therefore also succeed with a 207).
     */
    @Test
    public void largeCollectionInitialSyncListsAllMembersAndIssuesToken() throws Exception {
        final int n = 1000;
        for (int i = 0; i < n; i++) {
            testHelper.makeAndStoreDummyCollection();
        }

        DavTestContext ctx = executeSyncCollectionReport(INITIAL_SYNC_BODY);
        assertEquals(207, ctx.getDavResponse().getStatus(),
                "H5: initial sync of a large collection must be 207 (got "
                        + ctx.getDavResponse().getStatus() + ")");
        Document multistatus = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses =
                getChildElements(multistatus.getDocumentElement(), "response");
        assertEquals(n, responses.size(),
                "H5: all 1000 members must be listed (got " + responses.size() + ")");
        String token = requiredSyncToken(multistatus, "H5 initial sync");

        // The minted token must be usable: an empty incremental round with it
        // must be an error-free 207.
        DavTestContext next = executeSyncCollectionReport(incrementalBody(token, null));
        assertEquals(207, next.getDavResponse().getStatus(),
                "H5: the sync-token issued by a large-collection round must be "
                        + "valid for the next round (got "
                        + next.getDavResponse().getStatus() + ")");
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

    /**
     * Builds an incremental sync-collection body around a client-held token,
     * with an optional {@code nresults} cap.
     */
    private static String incrementalBody(String token, Integer nresults) {
        StringBuilder body = new StringBuilder();
        body.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
            .append("<D:sync-collection xmlns:D=\"DAV:\">\n")
            .append("  <D:sync-token>").append(token).append("</D:sync-token>\n")
            .append("  <D:sync-level>1</D:sync-level>\n");
        if (nresults != null) {
            body.append("  <D:limit><D:nresults>")
                .append(nresults.intValue())
                .append("</D:nresults></D:limit>\n");
        }
        body.append("  <D:prop>\n")
            .append("    <D:getetag/>\n")
            .append("  </D:prop>\n")
            .append("</D:sync-collection>");
        return body.toString();
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

    private Element findChildElement(Element parent, String localName) {
        if (parent == null) {
            return null;
        }
        for (org.w3c.dom.Node n = parent.getFirstChild(); n != null;
             n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && localName.equals(n.getLocalName())) {
                return (Element) n;
            }
        }
        return null;
    }

    private boolean hasChildLocalName(Element parent, String localName) {
        return findChildElement(parent, localName) != null;
    }

    private String requiredHref(Element response) {
        Element href = findChildElement(response, "href");
        assertTrue(href != null,
                "every DAV:response must carry a DAV:href element");
        return href.getTextContent().trim();
    }

    /**
     * Percent-decodes the {@code DAV:href} of a response and returns only its
     * last path segment (the member name).
     */
    private String decodedHref(Element response) throws Exception {
        String hrefRaw = requiredHref(response);
        URI hrefUri = new URI(hrefRaw);
        String path = URLDecoder.decode(hrefUri.getPath(),
                StandardCharsets.UTF_8.name());
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private String requiredSyncToken(Document multistatus, String label) {
        Document root = multistatus;
        Element element = (Element) root.getDocumentElement();
        for (org.w3c.dom.Node n = element.getFirstChild(); n != null;
             n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && "sync-token".equals(n.getLocalName())
                    && DAV_NAMESPACE.equals(
                            n instanceof Element
                                    ? ((Element) n).getNamespaceURI() : null)) {
                String value = n.getTextContent().trim();
                assertTrue(value.length() > 0,
                        "round \"" + label + "\" must mint a non-empty DAV:sync-token");
                return value;
            }
        }
        throw new AssertionError(
                "round \"" + label + "\" did not carry a DAV:sync-token");
    }

    /**
     * Extracts the numeric suffix of a {@code urn:cosmo:sync-token:<rev>}
     * for monotonicity assertions.
     */
    private static long tokenRevision(String syncToken) {
        int idx = syncToken.lastIndexOf(':');
        assertTrue(idx >= 0 && idx + 1 < syncToken.length(),
                "tokens must carry a revision suffix, got: " + syncToken);
        return Long.parseLong(syncToken.substring(idx + 1));
    }

    private Document parseMultistatus(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(
                xml.getBytes(StandardCharsets.UTF_8)));
    }

    private List<Element> getChildElements(Element parent, String localName) {
        List<Element> children = new ArrayList<Element>();
        for (org.w3c.dom.Node n = parent.getFirstChild(); n != null;
             n = n.getNextSibling()) {
            if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
                    && localName.equals(n.getLocalName())) {
                children.add((Element) n);
            }
        }
        return children;
    }
}
