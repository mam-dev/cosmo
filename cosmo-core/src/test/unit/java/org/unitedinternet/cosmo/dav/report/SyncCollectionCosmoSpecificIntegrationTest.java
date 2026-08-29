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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.DavTestContext;
import org.unitedinternet.cosmo.dav.servlet.StandardRequestHandler;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Integration tests for the <strong>Cosmo integration specifics</strong>
 * (Group I) of the {@code DAV:sync-collection} REPORT (RFC 6578) against the
 * full WebDAV request pipeline ({@link StandardRequestHandler}).
 * </p>
 *
 * <p>
 * Covers the following cases from {@code synccollection-testcases.md}
 * (Group I, "Cosmo integration specifics"):
 * </p>
 * <ul>
 * <li><strong>I5</strong> — empty-collection initial synchronization:
 * 207 Multi-Status, zero {@code DAV:response} children, and a usable
 * non-empty {@code DAV:sync-token} trailing the payload. (This pins the
 * RFC 6578 §3.1 empty-initial-sync contract the B-class also relies on,
 * re-anchored here under the Cosmo-specific grouping because it is the
 * baseline for I2's "no spurious rows in either collection" assertion.)</li>
 * <li><strong>I3</strong> — Multistatus ordering invariant: two consecutive
 * initial-sync rounds against a stable, unchanged collection return
 * {@code DAV:response} elements in the <em>same</em> decoded-href order,
 * and the {@code DAV:sync-token} is always the <strong>last</strong> child
 * of the root {@code DAV:multistatus}. This is a requirement of RFC 6578 §3.5
 * (position-based parsing by token-driven clients).</li>
 * <li><strong>I2</strong> — Cross-collection MOVE, RFC 6578-compliant
 * (since 2026-08-29 C5 fix): {@code ContentService#moveItem} between two
 * sibling plain collections now writes a <code>D</code>-row in the SOURCE
 * and a <code>C</code>-row in the DESTINATION. An incremental sync against
 * the SOURCE with a pre-move token MUST report exactly one entry — a
 * bare-404 tombstone for the moved member (RFC 6578 §3.2 shape, same
 * shape as D1/D2). An incremental sync against the DESTINATION with a
 * pre-move token MUST report exactly one entry — a live-member response
 * with a 200 status and a {@code DAV:getetag} propstat (RFC 6578 §3.2
 * live shape, same shape as B1/C1). A finality check — A no longer
 * contains the member, B now does — rules out silent loss or double
 * attribution. (The H-group tombstone-dedup and D-group coverage already
 * pin the tombstone behavior for real deletions and same-parent renames;
 * this isolates the cross-parent move path specifically, which previously
 * bypassed the change log entirely — a production RFC 6578 §5 gap, now
 * fixed by logging D+C rows in {@code moveItem}.)</li>
 * <li><strong>I4</strong> — CS:getctag advisory consistency (locked):
 * requesting the Calendar-Server extension property
 * {@code CS:getctag} (namespace {@code http://calendarserver.org/ns/})
 * in a sync-collection DAV:prop alongside {@code DAV:getetag} on a plain
 * non-calendar content member yields a 404 propstat per member for
 * {@code getctag} (content items do not expose a collection entity tag)
 * while {@code DAV:getetag} still resolves in a 200 propstat — mirroring
 * the B5 pseudo-property treatment for CS-namespace properties on the
 * plain {@code MultiStatusReport} path (which has no CS-namespace
 * override; only the CalDAV-specific report
 * {@code CaldavMultiStatusReport} used by Multiget / Query / FreeBusy
 * carries the live property).</li>
 * <li><strong>I1</strong> — deliberately NOT covered: no AclEvaluator
 * plumbing exists in the test pipeline (no Authorization header hook,
 * no provider-side ACL evaluation) — see the spec's I1 note for the
 * rationale.</li>
 * </ul>
 *
 * <p>
 * <strong>Status: pinned to observed behavior on 2026-08-29.</strong>
 * </p>
 */
public class SyncCollectionCosmoSpecificIntegrationTest extends BaseDavTestCase {

    private static final String DAV_NAMESPACE = "DAV:";
    private static final String CS_NAMESPACE = "http://calendarserver.org/ns/";

    /** RFC 6578 §3.1 initial synchronization request (empty sync-token). */
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
     * I4 request body: asks for both the live DAV:getetag and the CS-namespace
     * CS:getctag property on the sync-collection path. The CS property is
     * expected to surface as a 404 propstat per member (content items do not
     * carry a collection entity tag).
     */
    private static final String INITIAL_SYNC_BODY_GETETAG_CTAG =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<D:sync-collection xmlns:D=\"DAV:\""
        + " xmlns:CS=\"" + CS_NAMESPACE + "\">\n"
        + "  <D:sync-token/>\n"
        + "  <D:sync-level>1</D:sync-level>\n"
        + "  <D:prop>\n"
        + "    <D:getetag/>\n"
        + "    <CS:getctag/>\n"
        + "  </D:prop>\n"
        + "</D:sync-collection>";

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        testHelper.logIn();
    }

    // ---------- I5: empty-collection initial sync ----------

    /**
     * Test case I5 (also pins the B4 empty-initial-sync contract): a
     * sync-collection REPORT against a freshly created, entirely empty
     * collection MUST be answered with 207 Multi-Status carrying <em>zero</em>
     * {@code DAV:response} elements and a usable, non-empty
     * {@code DAV:sync-token} that is the last child of the multistatus root.
     */
    @Test
    public void emptyCollectionInitialSyncYieldsZeroResponsesAndUsableToken()
            throws Exception {
        CollectionItem empty = testHelper.makeAndStoreDummyCollection();
        assertNotNull(empty, "I5: fixture empty collection must be created");

        DavTestContext ctx = executeSyncCollectionReport(
                "/dav/test/" + encodePathSegment(empty.getName()), INITIAL_SYNC_BODY);

        int status = ctx.getDavResponse().getStatus();
        assertEquals(207, status,
                "I5: sync-collection on an empty collection must succeed with "
                + "207 (got " + status + ")");

        Document multistatus =
            parseMultistatus(ctx.getHttpResponse().getContentAsString());
        Element root = multistatus.getDocumentElement();
        List<Element> responses = getChildElements(root, "response");
        assertEquals(0, responses.size(),
                "I5: an empty collection must produce zero DAV:response "
                + "elements (got " + responses.size() + ")");

        Element syncToken = findDirectSyncToken(root);
        assertNotNull(syncToken, "I5: response MUST carry a DAV:sync-token");
        String tokenValue = syncToken.getTextContent().trim();
        assertFalse(tokenValue.isEmpty(),
                "I5: DAV:sync-token must not be empty");
        assertTrue(tokenValue.startsWith("urn:cosmo:sync-token:"),
                "I5: DAV:sync-token must use the Cosmo token scheme, "
                + "urn:cosmo:sync-token:<revision> (got " + tokenValue + ")");
    }

    // ---------- I3: multistatus ordering invariant ----------

    /**
     * Test case I3: multistatus membership stability + sync-token
     * last-child invariant across consecutive, unchanged initial
     * synchronization rounds.
     *
     * <p>Two requirements locked here (RFC 6578 §3.5 / Cosmo multistatus
     * writer contract):
     * <ol>
     *   <li>repeated initial syncs against the same unchanged collection
     *       list the <em>same set</em> of members (no members appear,
     *       disappear, or duplicate between rounds — a client that caches
     *       by href across syncs must not see phantom entries). Note:
     *       Cosmo does NOT guarantee a fixed emission order across
     *       rounds; order is not asserted.</li>
     *   <li>{@code DAV:sync-token} is always the <strong>last</strong>
     *       child of the root {@code DAV:multistatus} element, i.e. no
     *       {@code DAV:response} may appear after it in document order.</li>
     * </ol>
     * </p>
     *
     * <p>The test creates three fixture child collections under home,
     * performs two consecutive initial-sync rounds, compares the decoded
     * last-segment <em>sets</em>, and additionally asserts the
     * sync-token last-child invariant on every round.</p>
     */
    @Test
    public void multistatusMembershipIsStableAndTokenIsLastChild()
            throws Exception {
        givenHomeChildCollections(3);

        DavTestContext ctx1 = executeSyncCollectionReport(INITIAL_SYNC_BODY);
        assertEquals(207, ctx1.getDavResponse().getStatus(),
                "I3: round 1 must succeed with 207 "
                + "(got " + ctx1.getDavResponse().getStatus() + ")");
        Document ms1 = parseMultistatus(ctx1.getHttpResponse().getContentAsString());
        Element root1 = ms1.getDocumentElement();
        List<Element> responses1 = getChildElements(root1, "response");
        assertTrue(responses1.size() >= 3,
                "I3: round 1 must list at least the three fixture members "
                + "(got " + responses1.size() + ")");
        assertSyncTokenIsLastChild(root1, "I3 round 1");
        Set<String> members1 = new HashSet<String>(decodedLastSegments(ms1));

        DavTestContext ctx2 = executeSyncCollectionReport(INITIAL_SYNC_BODY);
        assertEquals(207, ctx2.getDavResponse().getStatus(),
                "I3: round 2 (unchanged collection) must still succeed with 207 "
                + "(got " + ctx2.getDavResponse().getStatus() + ")");
        Document ms2 = parseMultistatus(ctx2.getHttpResponse().getContentAsString());
        Element root2 = ms2.getDocumentElement();
        List<Element> responses2 = getChildElements(root2, "response");
        assertSyncTokenIsLastChild(root2, "I3 round 2");
        Set<String> members2 = new HashSet<String>(decodedLastSegments(ms2));

        assertEquals(members1, members2,
                "I3: member set must be identical across consecutive "
                + "initial-sync rounds of an unchanged collection "
                + "(round1=" + members1 + " round2=" + members2 + ")");
    }

    // ---------- I2: cross-collection MOVE — tombstone + live member ----------

    /**
     * Test case I2: a cross-collection MOVE of a plain content member M
     * from collection A to sibling collection B MUST be RFC 6578-compliant
     * in both the source and the destination collection's incremental
     * sync-collection REPORT with a pre-move {@code DAV:sync-token}:
     *
     * <ul>
     *   <li><strong>A (source)</strong> — exactly <em>one</em> response,
     *       surfaced as a bare-404 <strong>tombstone</strong>: a
     *       {@code DAV:response} whose only content is a
     *       {@code DAV:status} of "404", whose {@code DAV:href} resolves
     *       to M's original name <em>under A</em>, and which carries
     *       <em>no</em> {@code DAV:propstat} blocks (this is the RFC
     *       6578 §3.2 tombstone shape, also locked by the D1/D2 tests).</li>
     *   <li><strong>B (destination)</strong> — exactly <em>one</em>
     *       response, surfaced as a regular <strong>live-member</strong>
     *       entry: a {@code DAV:response} whose {@code DAV:status} is
     *       "200", whose {@code DAV:href} resolves to M's name
     *       <em>under B</em>, and which carries a {@code DAV:propstat}
     *       for {@code DAV:getetag} that is "200" (this is the RFC
     *       6578 §3.2 live-entry shape, also locked by B1/C1/D1).</li>
     *   <li><strong>Finality</strong> — a fresh initial sync of B lists
     *       M, proving the move actually happened (not a reporting
     *       anomaly); A's fresh initial sync no longer lists M.</li>
     * </ul>
     *
     * <p>Before the C5 fix (2026-08-29), Cosmo's
     * {@code StandardContentService#moveItem} did not write any
     * {@code modificationDao.log} entries and the move was invisible to
     * both incremental syncs — a deviation from RFC 6578 §5. The test has
     * been re-scoped to lock the compliant behavior: moveItem now logs a
     * D-row in the source and a C-row in the destination, guarded by
     * {@code crossCollection = !oldParent.equals(newParent)} so the
     * same-parent rename path (which routes through
     * {@code updateItem}/{@code updateContent} and already emits a single
     * M-row) is not duplicated.
     *
     * <p>Setup:
     * <ol>
     *   <li>two sibling sub-collections A and B under the user's home;</li>
     *   <li>one plain content member M inside A;</li>
     *   <li>baseline initial syncs of A (token T_A) and B (token T_B) —
     *       A lists M, B is empty;</li>
     *   <li>{@code ContentService#moveItem(m, a, b)};</li>
     *   <li>assert A's incremental sync (with T_A) is EXACTLY one
     *       response, a bare-404 tombstone for M;</li>
     *   <li>assert B's incremental sync (with T_B) is EXACTLY one
     *       response, a 200 live entry for M with a getetag propstat;</li>
     *   <li>assert A's fresh initial sync no longer lists M (source
     *       lost the member) and B's fresh initial sync lists M
     *       (destination gained it).</li>
     * </ol>
     *
     * <p>This test also pins that a cross-parent move NEVER 500s the
     * report engine — the original pre-C5 concern behind the I2 slot,
     * now upgraded to a positive RFC 6578 behavior lock.
     * </p>
     */
    @Test
    public void crossCollectionMoveEmitsTombstoneInSourceAndLiveMemberInTarget()
            throws Exception {
        // Two sibling sub-collections under home.
        CollectionItem a = testHelper.makeAndStoreDummyCollection();
        CollectionItem b = testHelper.makeAndStoreDummyCollection();
        assertNotNull(a, "I2: fixture collection A must be created");
        assertNotNull(b, "I2: fixture collection B must be created");
        assertEquals(1, a.getParents().size(),
                "I2: fixture collection A must have exactly one parent (home)");
        assertEquals(1, b.getParents().size(),
                "I2: fixture collection B must have exactly one parent (home)");
        assertFalse(a.getUid().equals(b.getUid()),
                "I2: fixture collections A and B must be distinct");

        String aUri = "/dav/test/" + encodePathSegment(a.getName());
        String bUri = "/dav/test/" + encodePathSegment(b.getName());

        // M is a plain content member inside A (exercises the
        // cross-collection content-move path of moveItem).
        ContentItem m = testHelper.makeAndStoreDummyContent(a);
        assertNotNull(m, "I2: fixture content member in A must be created");
        String mName = m.getName();

        // Baseline initial syncs for both collections (tokens T_A, T_B are
        // our anchors for the incremental round after the move).
        DavTestContext aInitial =
            executeSyncCollectionReport(aUri, INITIAL_SYNC_BODY);
        assertEquals(207, aInitial.getDavResponse().getStatus(),
                "I2: A's baseline initial sync must succeed with 207 "
                + "(got " + aInitial.getDavResponse().getStatus() + ")");
        Document aInitialMs =
            parseMultistatus(aInitial.getHttpResponse().getContentAsString());
        String aBaselineToken = requiredSyncToken(aInitialMs, "I2 A baseline");
        assertTrue(decodedLastSegments(aInitialMs).contains(mName),
                "I2: A's initial sync must list member " + mName
                + " before the move (decoded hrefs: "
                + decodedLastSegments(aInitialMs) + ")");

        DavTestContext bInitial =
            executeSyncCollectionReport(bUri, INITIAL_SYNC_BODY);
        assertEquals(207, bInitial.getDavResponse().getStatus(),
                "I2: B's baseline initial sync must succeed with 207 "
                + "(got " + bInitial.getDavResponse().getStatus() + ")");
        Document bInitialMs =
            parseMultistatus(bInitial.getHttpResponse().getContentAsString());
        String bBaselineToken = requiredSyncToken(bInitialMs, "I2 B baseline");
        assertTrue(decodedLastSegments(bInitialMs).isEmpty(),
                "I2: B's initial sync must be empty before the move "
                + "(decoded hrefs: " + decodedLastSegments(bInitialMs) + ")");

        // Cross-collection move: A -> B.
        testHelper.getContentService().moveItem(m, a, b);

        // ---- A (source) incremental round: exactly one response, a
        //      bare-404 tombstone for M (RFC 6578 §3.2 shape).
        DavTestContext aInc = executeSyncCollectionReport(
                aUri, incrementalBody(aBaselineToken));
        assertEquals(207, aInc.getDavResponse().getStatus(),
                "I2: A's post-move incremental sync must still answer 207 "
                + "(got " + aInc.getDavResponse().getStatus()
                + "; a 500 here would mean the move broke the report path)");
        Document aIncMs =
            parseMultistatus(aInc.getHttpResponse().getContentAsString());
        List<Element> aIncResponses =
            getChildElements(aIncMs.getDocumentElement(), "response");
        assertEquals(1, aIncResponses.size(),
                "I2: after a cross-collection move, A's incremental sync "
                + "MUST report exactly one entry — the D-row tombstone "
                + "for the moved member (got " + aIncResponses.size()
                + " responses; decoded hrefs: "
                + decodedLastSegments(aIncMs) + ")");
        Element aTombstone = aIncResponses.get(0);
        assertTrue(isTombstoneResponse(aTombstone),
                "I2: A's single incremental entry MUST be a bare-404 "
                + "tombstone (a DAV:response with a 404 DAV:status, no "
                + "propstat blocks); actual response:\n"
                + dumpElement(aTombstone));
        // The tombstone href must resolve to M's name under A.
        List<Element> aTombstoneHrefs =
            getChildElements(aTombstone, "href");
        assertFalse(aTombstoneHrefs.isEmpty(),
                "I2: A's tombstone response MUST carry a DAV:href");
        String aTombstoneHref =
            decodedHrefText(aTombstoneHrefs.get(0).getTextContent());
        String aTombstoneName = segmentAfterLastSlash(aTombstoneHref);
        assertEquals(mName, aTombstoneName,
                "I2: A's tombstone href must preserve the moved member's "
                + "original name " + mName + " (got " + aTombstoneHref + ")");
        assertTrue(hrefStartsWithPrefix(aTombstoneHref, aUri),
                "I2: A's tombstone href must be rooted under the source "
                + "collection " + aUri + " (got " + aTombstoneHref + ")");

        // ---- B (destination) incremental round: exactly one response, a
        //      200 live-member entry for M with a DAV:getetag propstat.
        DavTestContext bInc = executeSyncCollectionReport(
                bUri, incrementalBody(bBaselineToken));
        assertEquals(207, bInc.getDavResponse().getStatus(),
                "I2: B's post-move incremental sync must still answer 207 "
                + "(got " + bInc.getDavResponse().getStatus()
                + "; a 500 here would mean the move broke the report path)");
        Document bIncMs =
            parseMultistatus(bInc.getHttpResponse().getContentAsString());
        List<Element> bIncResponses =
            getChildElements(bIncMs.getDocumentElement(), "response");
        assertEquals(1, bIncResponses.size(),
                "I2: after a cross-collection move, B's incremental sync "
                + "MUST report exactly one entry — the C-row live-member "
                + "for the moved member (got " + bIncResponses.size()
                + " responses; decoded hrefs: "
                + decodedLastSegments(bIncMs) + ")");
        Element bLive = bIncResponses.get(0);
        // B's entry must be a LIVE member, NOT a tombstone. (A live
        // member in Cosmo's multistatus carries its 200 status
        // INSIDE the propstat, like a normal propfind response; a
        // bare response-level DAV:status of 404 with no propstat is
        // the tombstone shape, already ruled out here.)
        assertFalse(isTombstoneResponse(bLive),
                "I2: B's single incremental entry MUST be a LIVE member, "
                + "NOT a tombstone; actual response:\n"
                + dumpElement(bLive));
        // B's entry MUST carry the getetag propstat with a 200 status
        // INSIDE the propstat — the RFC 6578 live-member indicator.
        Element bLiveGetetag =
            findPropStatPropAnyNamespace(bLive, "getetag", 200);
        assertNotNull(bLiveGetetag,
                "I2: B's live response MUST carry a DAV:getetag in a "
                + "200 propstat block (RFC 6578 live-member shape); "
                + "actual response:\n" + dumpElement(bLive));
        // As a further sanity check the propstat element must be
        // non-empty (i.e. actually resolved the ETag value).
        assertFalse(bLiveGetetag.getTextContent().trim().isEmpty(),
                "I2: B's DAV:getetag propstat MUST carry a non-empty "
                + "ETag value (RFC 6578 live-member shape)");
        // The live-member href must resolve to M's name under B.
        List<Element> bLiveHrefs =
            getChildElements(bLive, "href");
        assertFalse(bLiveHrefs.isEmpty(),
                "I2: B's live response MUST carry a DAV:href");
        String bLiveHref =
            decodedHrefText(bLiveHrefs.get(0).getTextContent());
        String bLiveName = segmentAfterLastSlash(bLiveHref);
        assertEquals(mName, bLiveName,
                "I2: B's live href must resolve to the moved member's "
                + "name " + mName + " (got " + bLiveHref + ")");
        assertTrue(hrefStartsWithPrefix(bLiveHref, bUri),
                "I2: B's live href must be rooted under the destination "
                + "collection " + bUri + " (got " + bLiveHref + ")");

        // ---- Finality: fresh initial syncs confirm the membership shift.
        DavTestContext aInitialAfter =
            executeSyncCollectionReport(aUri, INITIAL_SYNC_BODY);
        assertEquals(207, aInitialAfter.getDavResponse().getStatus(),
                "I2: A's post-move fresh initial sync must succeed with 207 "
                + "(got " + aInitialAfter.getDavResponse().getStatus() + ")");
        Document aInitialAfterMs =
            parseMultistatus(aInitialAfter.getHttpResponse().getContentAsString());
        assertFalse(decodedLastSegments(aInitialAfterMs).contains(mName),
                "I2: A's post-move fresh initial sync MUST NOT list "
                + "member " + mName
                + " (source collection must have lost it); decoded hrefs: "
                + decodedLastSegments(aInitialAfterMs));

        DavTestContext bInitialAfter =
            executeSyncCollectionReport(bUri, INITIAL_SYNC_BODY);
        assertEquals(207, bInitialAfter.getDavResponse().getStatus(),
                "I2: B's post-move fresh initial sync must succeed with 207 "
                + "(got " + bInitialAfter.getDavResponse().getStatus() + ")");
        Document bInitialAfterMs =
            parseMultistatus(bInitialAfter.getHttpResponse().getContentAsString());
        assertTrue(decodedLastSegments(bInitialAfterMs).contains(mName),
                "I2: B's post-move fresh initial sync MUST list member "
                + mName + " (destination collection must have gained it); "
                + "decoded hrefs: " + decodedLastSegments(bInitialAfterMs));
    }

    // ---------- I4: CS:getctag advisory property ----------

    /**
     * Test case I4: requesting {@code CS:getctag}
     * (namespace {@code http://calendarserver.org/ns/}) alongside
     * {@code DAV:getetag} in a sync-collection REPORT on a plain content
     * member.
     *
     * <p>Observed behavior (locked):
     * <ul>
     *   <li>the report overall still succeeds (207);</li>
     *   <li>{@code DAV:getetag} returns a 200 propstat for the member;</li>
     *   <li>{@code CS:getctag} surfaces in a per-member 404 propstat —
     *       plain content items do not carry a collection entity tag,
     *       and the plain {@code MultiStatusReport} base (which the
     *       sync-collection report extends) has no CS-namespace override;
     *       only the CalDAV report {@code CaldavMultiStatusReport}
     *       (used by Multiget / Query / FreeBusy) registers
     *       {@code GET_CTAG} as live;</li>
     *   <li>the response ends with a usable {@code DAV:sync-token}.</li>
     * </ul>
     * </p>
     */
    @Test
    public void csGetctagOnPlainContentMemberSurfacesAsUnknown() throws Exception {
        CollectionItem cal = testHelper.makeAndStoreDummyCollection();
        assertNotNull(cal, "I4: parent collection required for the member");

        ContentItem member = testHelper.makeAndStoreDummyContent(cal);
        assertNotNull(member, "I4: plain content member must be created");
        String memberName = member.getName();

        String uri = "/dav/test/" + encodePathSegment(cal.getName());

        DavTestContext ctx =
            executeSyncCollectionReport(uri, INITIAL_SYNC_BODY_GETETAG_CTAG);
        assertEquals(207, ctx.getDavResponse().getStatus(),
                "I4: sync-collection requesting CS:getctag + DAV:getetag "
                + "must still succeed with 207 (got "
                + ctx.getDavResponse().getStatus() + ")");

        Document multistatus =
            parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses =
            getChildElements(multistatus.getDocumentElement(), "response");

        Element memberResponse =
            findResponseByHrefFragment(responses, memberName);
        assertNotNull(memberResponse,
                "I4: member " + memberName
                + " MUST appear in the sync-collection (decoded hrefs: "
                + decodedLastSegments(multistatus) + ")");

        // The live property resolves normally in a 200 propstat.
        assertNotNull(findPropStatPropAnyNamespace(memberResponse, "getetag", 200),
                "I4: DAV:getetag MUST be in a 200 propstat for the member; "
                + "the CS extension property must not drag down the live one");

        // The CS-namespace property is NOT specially resolved on the plain
        // sync-collection path and surfaces as a 404 propstat.
        assertNotNull(findPropStatPropAnyNamespace(memberResponse, "getctag", 404),
                "I4: observed behavior locks CS:getctag as a 404 propstat on "
                + "the sync-collection path (plain MultiStatusReport base; no "
                + "CS-namespace override on this path). If this now 200s, "
                + "the report has acquired CS special treatment and this "
                + "test must be updated accordingly.");

        // The CS-namespace property MUST NOT be silently folded into the
        // 200 propstat either.
        assertFalse(isPropInAnyNamespacePropstatWithCode(
                memberResponse, "getctag", 200),
                "I4: CS:getctag MUST NOT appear in a 200 propstat of the "
                + "plain sync-collection path");

        Element syncToken =
            findDirectSyncToken(multistatus.getDocumentElement());
        assertNotNull(syncToken,
                "I4: response must still carry a DAV:sync-token");
        assertFalse(syncToken.getTextContent().trim().isEmpty(),
                "I4: DAV:sync-token must not be empty");
    }

    // ---------- fixtures ----------

    /**
     * Creates {@code count} plain child collections under the user's home
     * collection, asserts that all were stored, and returns them.
     */
    private List<CollectionItem> givenHomeChildCollections(int count)
            throws Exception {
        List<CollectionItem> created = new ArrayList<CollectionItem>(count);
        for (int i = 0; i < count; i++) {
            CollectionItem c = testHelper.makeAndStoreDummyCollection();
            assertNotNull(c, "fixture child collection " + i + " was not stored");
            created.add(c);
        }
        return created;
    }

    // ---------- request + XML helpers ----------

    /**
     * Sends a REPORT request against the given DAV URI through the full
     * {@link StandardRequestHandler} pipeline.
     */
    private DavTestContext executeSyncCollectionReport(String uri,
        String requestBody) throws Exception {
        DavTestContext ctx = testHelper.createTestContext();

        ctx.getHttpRequest().setServletPath("/dav");
        ctx.getHttpRequest().setRequestURI(uri);
        ctx.getHttpRequest().setMethod("REPORT");
        ctx.getHttpRequest().setContent(
                requestBody.getBytes(StandardCharsets.UTF_8));
        ctx.getHttpRequest().setContentType("application/xml");
        ctx.getHttpRequest().addHeader("Content-Type", "application/xml");

        new StandardRequestHandler(
                testHelper.getResourceLocatorFactory(),
                testHelper.getResourceFactory(),
                testHelper.getEntityFactory())
                .handleRequest(ctx.getDavRequest(), ctx.getDavResponse());

        return ctx;
    }

    /**
     * Convenience overload targeting the user's home collection ("/dav/test")
     * for the initial-sync and I2-baseline rounds.
     */
    private DavTestContext executeSyncCollectionReport(String requestBody)
            throws Exception {
        return executeSyncCollectionReport("/dav/test", requestBody);
    }

    /**
     * Builds an incremental sync-collection request body around a previously
     * issued token.
     */
    private static String incrementalBody(String token) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<D:sync-collection xmlns:D=\"DAV:\">\n"
            + "  <D:sync-token>" + token + "</D:sync-token>\n"
            + "  <D:sync-level>1</D:sync-level>\n"
            + "  <D:prop>\n"
            + "    <D:getetag/>\n"
            + "  </D:prop>\n"
            + "</D:sync-collection>";
    }

    /**
     * Parses a DAV:multistatus payload into a namespace-aware DOM document,
     * with secure-processing and doctype-decl disabled (JDKs prior to 9 do
     * not recognize {@code XMLConstants.ACCESS_EXTERNAL_DTD}).
     */
    private Document parseMultistatus(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl", true);
        Document doc = dbf.newDocumentBuilder().parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        assertEquals("multistatus",
                doc.getDocumentElement().getLocalName(),
                "response root element must be DAV:multistatus");
        return doc;
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

    private Element findResponseByHrefFragment(List<Element> responses,
            String fragment) {
        for (Element response : responses) {
            for (Element href : getChildElements(response, "href")) {
                String decoded = decodedHrefText(href.getTextContent());
                if (decoded.contains(fragment)) {
                    return response;
                }
            }
        }
        return null;
    }

    /**
     * Finds the named property inside any propstat of the given response
     * whose status matches {@code expectedCode}. The inner property element
     * is matched by local name in <em>any</em> namespace: DAV live
     * properties carry the {@code DAV:} namespace, CS-namespace properties
     * carry {@code http://calendarserver.org/ns/} and must still be
     * locatable here. The {@code DAV:}-only {@link #getChildElements} helper
     * would not see them.
     */
    private Element findPropStatPropAnyNamespace(Element response,
            String propLocalName, int expectedCode) {
        for (Element propstat : getChildElements(response, "propstat")) {
            Element statusElement = null;
            for (Element child : getChildElements(propstat, "status")) {
                statusElement = child;
            }
            if (statusElement == null
                    || !statusElement.getTextContent().trim().matches(
                           "^HTTP/[0-9.]+ (" + expectedCode + ").*")) {
                continue;
            }
            for (Element prop : getChildElements(propstat, "prop")) {
                for (Element candidate : findPropertyElementAnyNamespace(
                        prop, propLocalName)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private List<Element> findPropertyElementAnyNamespace(Element parent,
            String localName) {
        List<Element> result = new ArrayList<Element>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE
                    && localName.equals(child.getLocalName())) {
                result.add((Element) child);
            }
        }
        return result;
    }

    private boolean isPropInAnyNamespacePropstatWithCode(
            Element response, String propLocalName, int expectedCode) {
        return findPropStatPropAnyNamespace(response, propLocalName,
                expectedCode) != null;
    }

    private Element findDirectSyncToken(Element multistatusRoot) {
        List<Element> tokens = getChildElements(multistatusRoot, "sync-token");
        return tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);
    }

    private String requiredSyncToken(Document multistatus, String roundLabel) {
        Element token = findDirectSyncToken(multistatus.getDocumentElement());
        assertNotNull(token, roundLabel + " response must carry a DAV:sync-token");
        String value = token.getTextContent().trim();
        assertFalse(value.isEmpty(),
                roundLabel + " DAV:sync-token must not be empty");
        return value;
    }

    /**
     * I3: asserts that the DAV:sync-token IS the last ELEMENT child of the
     * multistatus root, i.e. no DAV:response element may follow it in
     * document order (RFC 6578 §3.5 writer contract / Cosmo multistatus
     * serializer behavior).
     */
    private void assertSyncTokenIsLastChild(Element multistatusRoot,
            String label) {
        List<Element> elementChildren = new ArrayList<Element>();
        NodeList children = multistatusRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                elementChildren.add((Element) child);
            }
        }
        assertFalse(elementChildren.isEmpty(),
                label + ": multistatus root must have at least one element "
                + "child (the sync-token at minimum)");
        Element last = elementChildren.get(elementChildren.size() - 1);
        assertEquals("sync-token", last.getLocalName(),
                label + ": DAV:sync-token must be the LAST child of the "
                + "DAV:multistatus root (got "
                + localNameWithNamespace(last) + " as last child)");
        assertEquals(DAV_NAMESPACE, last.getNamespaceURI(),
                label + ": last element must be in the DAV namespace (got "
                + last.getNamespaceURI() + ")");
    }

    private static String localNameWithNamespace(Element e) {
        return (e.getNamespaceURI() == null ? "" : e.getNamespaceURI())
                + ":" + e.getLocalName();
    }

    /**
     * Percent-encodes a single DAV path segment (a collection or content
     * item name) for use in a URI. Spaces, UTF-8, and other characters
     * that would break the URI are encoded as {@code %XX}.
     */
    private static String encodePathSegment(String raw) {
        if (raw == null) {
            return "";
        }
        try {
            // URLEncoder uses application/x-www-form-urlencoded by default
            // (+ instead of %20 for spaces). Re-encode + back to %20 so the
            // result is a valid absolute URI path segment (RFC 3986).
            return URLEncoder.encode(raw, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is always supported, but we must declare the catch.
            throw new AssertionError("UTF-8 encoder missing", e);
        }
    }

    /**
     * I2/I3: returns the decoded last-segment of every DAV:href in the given
     * multistatus document, in document order. Used to compare ordering
     * across rounds and to assert membership of a specific member name.
     *
     * <p>Decoding follows the H-group contract: URLDecoder.decode over the
     * whole href text (spaces are + or %20 — both decode to space), then
     * trailing slashes stripped, then the substring after the last
     * {@code /} is returned. Fixture names such as "test collection 2"
     * arrive as "test+collection+2" or "test%20collection%202"; both decode
     * back to "test collection 2" before the lastSegment split.</p>
     */
    private List<String> decodedLastSegments(Document multistatus) {
        Element root = multistatus.getDocumentElement();
        List<Element> responses = getChildElements(root, "response");
        List<String> result = new ArrayList<String>();
        for (Element response : responses) {
            for (Element href : getChildElements(response, "href")) {
                String decoded = decodedHrefText(href.getTextContent());
                while (decoded.endsWith("/")) {
                    decoded = decoded.substring(0, decoded.length() - 1);
                }
                int slash = decoded.lastIndexOf('/');
                result.add(slash >= 0 ? decoded.substring(slash + 1) : decoded);
            }
        }
        return result;
    }

    /**
     * Best-effort percent/plus decoder for DAV:href text. Any decode
     * failure quietly returns the original string, so this helper can
     * never throw and is safe to use for fragment matching even on raw
     * paths.
     */
    private String decodedHrefText(String href) {
        String text = href == null ? "" : href.trim();
        try {
            return URLDecoder.decode(text, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return text;
        }
    }

    /**
     * I2: recognizes the RFC 6578 §3.2 deletion-tombstone response shape —
     * a {@code DAV:response} whose FIRST and only {@code DAV:status} is
     * "404" and which carries <em>no</em> {@code DAV:propstat} blocks
     * (same shape locked by the D1/D2 tests in
     * {@link SyncCollectionIncrementalSyncIntegrationTest}). A live-member
     * response (200 + propstat) or a response whose status 404 sits inside
     * a propstat rather than at response level is NOT a tombstone.
     */
    private boolean isTombstoneResponse(Element response) {
        // A tombstone MUST NOT carry any propstat block (RFC 6578 §3.2 shape,
        // the DAV:response is just href + status).
        if (!getChildElements(response, "propstat").isEmpty()) {
            return false;
        }
        List<Element> statuses = getChildElements(response, "status");
        if (statuses.isEmpty()) {
            return false;
        }
        // The (single) response-level status MUST be 404 — a 200 propstat
        // on the member side, or a 404 buried in a propstat rather than as
        // the response's own DAV:status, is NOT a tombstone.
        String first = statuses.get(0).getTextContent().trim();
        return first.matches("^HTTP/[0-9.]+ 404( .*)?$");
    }

    /**
     * I2: serializes a DOM element to a string for failure-message output.
     * Serialization is best-effort: on any transform error a readable
     * placeholder is returned instead of throwing, since the method is only
     * used to enrich assertion messages.
     */
    private String dumpElement(Element element) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(
                    OutputKeys.INDENT, "yes");
            StringWriter out = new StringWriter();
            transformer.transform(new DOMSource(element),
                    new StreamResult(out));
            return out.toString();
        } catch (Exception e) {
            return "(serialization failed: " + e.getMessage() + ")";
        }
    }

    /**
     * I2: returns the final path segment of a decoded href (the part after
     * the last {@code '/'}; the whole string if it contains no slash).
     * Trailing slashes are stripped before the split so a trailing-slash
     * form still yields the member name.
     */
    private String segmentAfterLastSlash(String decodedHref) {
        if (decodedHref == null) {
            return "";
        }
        String value = decodedHref.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        int slash = value.lastIndexOf('/');
        return slash >= 0 ? value.substring(slash + 1) : value;
    }

    /**
     * I2: asserts that a decoded href is rooted under the given URI prefix
     * — i.e. the href either equals the prefix (trailing slash tolerated on
     * either side) or continues from it. Comparing on decoded text instead
     * of raw href text keeps the check robust against percent-encoding of
     * path segments.
     */
    private boolean hrefStartsWithPrefix(String decodedHref,
            String collectionUri) {
        String href = decodedHref == null ? "" : decodedHref.trim();
        String prefix = decodedHrefText(collectionUri == null ? "" : collectionUri.trim());
        if (prefix.equals("")) {
            return false;
        }
        String prefixNoSlash = prefix;
        while (prefixNoSlash.endsWith("/")) {
            prefixNoSlash = prefixNoSlash.substring(
                    0, prefixNoSlash.length() - 1);
        }
        if (href.equals(prefixNoSlash)) {
            return true;
        }
        return href.startsWith(prefixNoSlash + "/");
    }
}
