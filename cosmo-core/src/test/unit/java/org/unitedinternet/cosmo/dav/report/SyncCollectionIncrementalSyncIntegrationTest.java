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
import static org.junit.jupiter.api.Assertions.fail;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Integration tests for <strong>incremental</strong> {@code DAV:sync-collection}
 * REPORT rounds (RFC 6578 Sections 3.4/3.5) against the full WebDAV pipeline
 * ({@link StandardRequestHandler} &rarr; provider &rarr; report).
 * </p>
 *
 * <p>
 * Each test performs an initial synchronization to obtain a real sync-token,
 * mutates the collection through {@link org.unitedinternet.cosmo.service.ContentService}
 * (the same layer the HTTP verbs use), and then issues an incremental REPORT
 * carrying the previously issued token.
 * </p>
 *
 * <p>
 * Covers the following cases from {@code synccollection-testcases.md}:
 * </p>
 * <ul>
 * <li><strong>C1</strong> - a member added after an initial sync is the only entry
 * listed by the next incremental round.</li>
 * <li><strong>C2</strong> - a modified member (display name change) is listed as an
 * updated (non-tombstone) entry.</li>
 * <li><strong>D1</strong> - a removed member appears exactly once as a deletion
 * tombstone: a {@code DAV:response} with a bare {@code DAV:status} of 404.</li>
 * <li><strong>C4</strong> - a removal followed by an addition inside the same
 * synchronization window yields BOTH the tombstone and the new member in one
 * round (this is the change pattern a rename produces; the fixture simulates it
 * as remove+add because the mock DAO index cannot resolve renamed items).</li>
 * <li><strong>F2</strong> - truncation via {@code DAV:nresults} converges: successive
 * limited rounds eventually drain the change log and an empty round confirms
 * convergence without losing entries.</li>
 * <li><strong>G</strong> - garbage tokens and syntactically valid but unknown/future
 * tokens are rejected with 403 Forbidden (client falls back to initial sync).</li>
 * </ul>
 *
 * <p>
 * <strong>These tests are expected to FAIL until the persistent change log and
 * real monotonic tokens are implemented (see
 * {@code synccollection-changelog-design.md}).</strong> Today any non-empty
 * {@code DAV:sync-token} is answered with {@code 403 Forbidden}, so every
 * incremental-round test fails with {@code expected: <207> but was: <403>}.
 * Only the G-group validation test passes today; it is kept as a regression
 * lock proving that real-token support does NOT loosen token validation.
 * No production code was changed to produce these tests.
 * </p>
 */
public class SyncCollectionIncrementalSyncIntegrationTest extends BaseDavTestCase {

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

    // C1

    /**
     * Test case C1: after an initial synchronization, adding exactly one member
     * makes the next incremental round list ONLY that new member as a regular
     * (200 propstat) DAV:response, together with a fresh non-empty sync-token.
     */
    @Test
    public void incrementalSyncAfterAddedMemberListsOnlyNewMember() throws Exception {
        givenHomeChildCollections(1);
        String initialToken = doInitialSyncAndGetToken();

        CollectionItem added = testHelper.makeAndStoreDummyCollection();
        assertNotNull(added, "fixture member was not stored");

        DavTestContext ctx = executeSyncCollectionReport(
                incrementalBody(initialToken, null));

        int status = ctx.getDavResponse().getStatus();
        assertEquals(207, status,
                "an incremental sync-collection REPORT with a previously issued token "
                + "must be answered with 207 Multi-Status (got " + status + "; if this "
                + "is 403 the server does not recognize its own tokens yet)");

        Document multistatus = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        Element root = multistatus.getDocumentElement();
        List<Element> responses = getChildElements(root, "response");

        assertEquals(1, responses.size(),
                "exactly one changed member (the addition) must be reported");
        assertTrue(responsesDeletedMembers(responses).isEmpty(),
                "a created member must NOT be reported as a 404 tombstone");

        Element etagProp =
                findPropStatProp(responses.get(0), "getetag", 200);
        assertNotNull(etagProp,
                "the added member must carry DAV:getetag within a 200 propstat");
        assertTrue(hrefDecodesTo(responses.get(0), added.getName()),
                "the reported member must be the newly added one");

        Element syncToken = findDirectSyncToken(root);
        assertNotNull(syncToken, "the incremental response must carry a new DAV:sync-token");
        assertFalse(syncToken.getTextContent().trim().isEmpty(),
                "the new DAV:sync-token must not be empty");
    }

    // C2

    /**
     * Test case C2: modifying an existing member between rounds lists that member
     * exactly once as an updated (non-tombstone) entry.
     */
    @Test
    public void incrementalSyncAfterModifiedMemberListsUpdatedMember() throws Exception {
        List<CollectionItem> members = givenHomeChildCollections(2);
        String initialToken = doInitialSyncAndGetToken();

        CollectionItem modified = members.get(0);
        modified.setDisplayName(modified.getName() + " modified");
        testHelper.getContentService().updateCollection(modified);

        DavTestContext ctx = executeSyncCollectionReport(
                incrementalBody(initialToken, null));

        assertEquals(207, ctx.getDavResponse().getStatus(),
                "an incremental round after a member modification must yield 207");

        Document multistatus = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        Element root = multistatus.getDocumentElement();
        List<Element> responses = getChildElements(root, "response");

        assertEquals(1, responses.size(),
                "exactly one changed member (the modification) must be reported");
        assertTrue(responsesDeletedMembers(responses).isEmpty(),
                "a modified member must still exist; no 404 tombstone allowed");
        assertNotNull(findPropStatProp(responses.get(0), "getetag", 200),
                "the modified member must carry DAV:getetag within a 200 propstat");
        assertTrue(hrefDecodesTo(responses.get(0), modified.getName()),
                "the reported member must be the modified one");
    }

    // D1

    /**
     * Test case D1: removing a member between rounds reports it exactly once as
     * a deletion tombstone - a DAV:response whose only content is a bare
     * DAV:status of 404 - preserving the old href of the deleted member.
     *
     * Expected to FAIL initially (403 instead of 207): no tombstones exist yet.
     */
    @Test
    public void removedMemberIsReportedAs404Tombstone() throws Exception {
        List<CollectionItem> members = givenHomeChildCollections(2);
        String initialToken = doInitialSyncAndGetToken();

        CollectionItem removed = members.get(0);
        String removedName = removed.getName();
        testHelper.getContentService().removeCollection(removed);

        DavTestContext ctx = executeSyncCollectionReport(
                incrementalBody(initialToken, null));

        int status = ctx.getDavResponse().getStatus();
        assertEquals(207, status,
                "an incremental round after a removal must be a 207 Multi-Status "
                + "(got " + status + "; RFC 6578 Section 3.5 requires deleted-member "
                + "tombstones instead of dropping the change silently)");

        Document multistatus = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        Element root = multistatus.getDocumentElement();
        List<Element> responses = getChildElements(root, "response");

        assertEquals(1, responses.size(),
                "exactly one change (the removal) must be reported");

        Element tombstone = responses.get(0);
        List<Element> statuses = getChildElements(tombstone, "status");
        assertFalse(statuses.isEmpty(),
                "a deleted member must be signaled via a bare DAV:status");
        assertTrue(statuses.get(0).getTextContent().contains("404"),
                "the deletion status line must be 404 Not Found");
        assertNullPropstat(tombstone,
                "a tombstone response must not carry any propstat blocks");
        assertTrue(hrefDecodesTo(tombstone, removedName),
                "the tombstone must preserve the href of the deleted member");
    }

    // C4 (rename surrogate: remove + add inside one window)

    /**
     * Test case C4: a removal followed by an addition within the same
     * synchronization window must surface BOTH changes in one incremental
     * round: the 404 tombstone for the old member and a regular entry for
     * the new one. This is exactly the change pattern a rename produces;
     * the fixture models it as remove+add because renamed items are not
     * resolvable through the mock DAO index.
     */
    @Test
    public void removalAndAdditionInSameWindowAreBothReported() throws Exception {
        List<CollectionItem> members = givenHomeChildCollections(1);
        String initialToken = doInitialSyncAndGetToken();

        CollectionItem removed = members.get(0);
        String removedName = removed.getName();
        testHelper.getContentService().removeCollection(removed);

        CollectionItem added = testHelper.makeAndStoreDummyCollection();
        assertNotNull(added, "fixture replacement member was not stored");

        DavTestContext ctx = executeSyncCollectionReport(
                incrementalBody(initialToken, null));

        assertEquals(207, ctx.getDavResponse().getStatus());

        Document multistatus = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses =
                getChildElements(multistatus.getDocumentElement(), "response");

        assertEquals(2, responses.size(),
                "both the removal and the addition must appear in the same round");

        boolean sawTombstone = false;
        boolean sawAddition = false;
        for (Element response : responses) {
            if (!responsesDeletedMembers(java.util.Collections.singletonList(response))
                    .isEmpty()) {
                sawTombstone = true;
                assertTrue(hrefDecodesTo(response, removedName),
                        "the tombstone must reference the removed member's href");
            } else {
                sawAddition = true;
                assertTrue(hrefDecodesTo(response, added.getName()),
                        "the regular entry must reference the added member");
                assertNotNull(findPropStatProp(response, "getetag", 200),
                        "the added member must carry DAV:getetag in a 200 propstat");
            }
        }
        assertTrue(sawTombstone, "one entry must be the removal tombstone");
        assertTrue(sawAddition, "one entry must be the addition");
    }

    // F2

    /**
     * Test case F2: truncation via DAV:nresults converges. With three pending
     * changes and nresults=2, the first incremental round returns two entries
     * and a NEW continuation token; the second round returns the remaining
     * entry; a third round returns zero entries, proving that no change was
     * lost by the earlier truncations.
     *
     * Expected to FAIL initially (403 instead of 207).
     */
    @Test
    public void limitPaginationConvergesWithoutLosingChanges() throws Exception {
        String initialToken = doInitialSyncAndGetToken();

        CollectionItem a = testHelper.makeAndStoreDummyCollection();
        CollectionItem b = testHelper.makeAndStoreDummyCollection();
        CollectionItem c = testHelper.makeAndStoreDummyCollection();
        assertNotNull(a);
        assertNotNull(b);
        assertNotNull(c);

        // round 1: truncated to two entries
        DavTestContext round1 = executeSyncCollectionReport(
                incrementalBody(initialToken, Integer.valueOf(2)));
        assertEquals(207, round1.getDavResponse().getStatus(),
                "truncated incremental rounds must still answer 207");
        Document ms1 = parseMultistatus(round1.getHttpResponse().getContentAsString());
        List<Element> responses1 =
                getChildElements(ms1.getDocumentElement(), "response");
        assertEquals(2, responses1.size(),
                "nresults=2 must cap the first incremental round at two entries");
        String token1 = requiredSyncToken(ms1, "round 1");

        // round 2: the remaining single change
        DavTestContext round2 = executeSyncCollectionReport(
                incrementalBody(token1, Integer.valueOf(2)));
        assertEquals(207, round2.getDavResponse().getStatus());
        Document ms2 = parseMultistatus(round2.getHttpResponse().getContentAsString());
        List<Element> responses2 =
                getChildElements(ms2.getDocumentElement(), "response");
        assertEquals(1, responses2.size(),
                "the second round must report exactly the one remaining change");
        String token2 = requiredSyncToken(ms2, "round 2");

        // round 3: converged - nothing left
        DavTestContext round3 = executeSyncCollectionReport(incrementalBody(token2, null));
        assertEquals(207, round3.getDavResponse().getStatus());
        Document ms3 = parseMultistatus(round3.getHttpResponse().getContentAsString());
        List<Element> responses3 =
                getChildElements(ms3.getDocumentElement(), "response");
        assertTrue(responses3.isEmpty(),
                "after draining all changes a further round must report none");
    }

    // G (regression lock: passes today, must KEEP passing)

    /**
     * Test case G: garbage tokens and syntactically valid but unknown or
     * future tokens must be rejected with 403 Forbidden so clients repeat
     * the initial synchronization (RFC 6578 Section 3.7 fallback rule).
     *
     * This test passes TODAY because every non-empty token is rejected;
     * it guards that real-token support does not weaken validation.
     */
    @Test
    public void garbageAndFutureTokensAreRejectedWith403() throws Exception {
        givenHomeChildCollections(1);

        String[] badTokens = {
                "totally-bogus",
                "urn:cosmo:sync-token:not-a-number",
                "urn:cosmo:sync-token:-5",
                "urn:cosmo:sync-token:999999999"
        };
        for (String badToken : badTokens) {
            DavTestContext ctx = executeSyncCollectionReport(
                    incrementalBody(badToken, null));
            assertEquals(403, ctx.getDavResponse().getStatus(),
                    "token \"" + badToken + "\" must be rejected with 403 Forbidden "
                    + "(got " + ctx.getDavResponse().getStatus() + ")");
        }
    }

    // fixture helpers

    /**
     * RFC 6578 applies to any collection, so we sync the user's HOME collection
     * ("/dav/test", always resolvable) with plain sub-collections as members -
     * the proven fixture from the initial-sync suite (sub-collections avoid
     * DavCalendarResource's calendar conversion during live-property loading).
     */
    private List<CollectionItem> givenHomeChildCollections(int count) throws Exception {
        List<CollectionItem> created = new ArrayList<CollectionItem>(count);
        for (int i = 0; i < count; i++) {
            CollectionItem c = testHelper.makeAndStoreDummyCollection();
            assertNotNull(c, "fixture child collection " + i + " was not stored");
            created.add(c);
        }
        return created;
    }

    /**
     * Performs an initial synchronization and returns the issued sync-token
     * for use in subsequent incremental rounds.
     */
    private String doInitialSyncAndGetToken() throws Exception {
        DavTestContext ctx = executeSyncCollectionReport(INITIAL_SYNC_BODY);
        assertEquals(207, ctx.getDavResponse().getStatus(),
                "initial synchronization must succeed before incremental rounds "
                + "(got " + ctx.getDavResponse().getStatus() + ")");
        Document multistatus = parseMultistatus(ctx.getHttpResponse().getContentAsString());
        return requiredSyncToken(multistatus, "initial sync");
    }

    /**
     * Builds an incremental sync-collection request body around a client-held
     * token, optionally requesting truncation via DAV:nresults.
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

    private String requiredSyncToken(Document multistatus, String roundLabel) {
        Element syncToken =
                findDirectSyncToken(multistatus.getDocumentElement());
        assertNotNull(syncToken, roundLabel + " response must carry a DAV:sync-token");
        String value = syncToken.getTextContent().trim();
        assertFalse(value.isEmpty(), roundLabel + " DAV:sync-token must not be empty");
        return value;
    }

    /**
     * True if the response's DAV:href, percent-decoded, references the given
     * member name (member names contain spaces and therefore arrive encoded).
     * Trailing slashes are ignored: RFC 4918 Section 5.2 recommends that
     * collection hrefs end with "/", so a collection member may legally be
     * referenced both as ".../name" and ".../name/".
     */
    private boolean hrefDecodesTo(Element response, String memberName) {
        List<Element> hrefs = getChildElements(response, "href");
        if (hrefs.isEmpty()) {
            return false;
        }
        String href = hrefs.get(hrefs.size() - 1).getTextContent().trim();
        try {
            new URI(href);
        } catch (java.net.URISyntaxException e) {
            fail("DAV:href \"" + href + "\" is not a valid URI reference: "
                    + e.getMessage());
        }
        String decoded = URLDecoder.decode(href, StandardCharsets.UTF_8);
        while (decoded.endsWith("/")) {
            decoded = decoded.substring(0, decoded.length() - 1);
        }
        int slash = decoded.lastIndexOf('/');
        String lastSegment = slash >= 0 ? decoded.substring(slash + 1) : decoded;
        return memberName.equals(lastSegment);
    }

    private void assertNullPropstat(Element response, String message) {
        List<Element> propstats = getChildElements(response, "propstat");
        assertTrue(propstats.isEmpty(), message);
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
            Element statusElement = null;
            for (Element child : getChildElements(propstat, "status")) {
                statusElement = child;
            }
            if (statusElement == null || !statusElement.getTextContent().trim()
                    .matches("^HTTP/[0-9.]+ (" + expectedCode + ").*")) {
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
}
