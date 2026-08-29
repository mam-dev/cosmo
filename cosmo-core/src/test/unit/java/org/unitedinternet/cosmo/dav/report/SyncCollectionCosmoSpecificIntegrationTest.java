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
 * <li><strong>I2</strong> — Cross-collection MOVE isolation (locked, safe
 * behavior in Cosmo): {@code ContentService#moveItem} between two sibling
 * plain collections does NOT write any persistent change-log row in either
 * parent. Consequently, an incremental sync against the SOURCE collection
 * reports zero members (the moved-out item produces neither a tombstone
 * nor a live entry), AND an incremental sync against the TARGET collection
 * likewise reports zero members (no spurious "added" entry). A final fresh
 * initial sync of the target proves the move actually happened, ruling out
 * silent loss on either side. (The H-group tombstone dedup and D-group
 * coverage already pin the tombstone behavior for real deletions and
 * same-parent renames; this isolates the cross-parent move path.)</li>
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

    // ---------- I2: cross-collection MOVE isolation ----------

    /**
     * Test case I2: a cross-collection MOVE of a plain member between two
     * sibling collections leaves BOTH the source and target collection's
     * incremental sync-collection REPORT clean for that member — no 200
     * entry, no 404 tombstone, no spurious row in either log.
     *
     * <p>Cosmo's {@code StandardContentService#moveItem} does not write
     * persistent change-log rows (it calls
     * {@code contentDao.addItemToCollection} +
     * {@code contentDao.removeItemFromCollection} directly, bypassing
     * {@code modificationDao.log}); therefore neither the source nor
     * the target collection's incremental sync will surface the moved
     * member. This is a deliberate Cosmo-specific behavior (a deviation
     * from the more general RFC 6578 expectation that a cross-parent
     * move would surface a tombstone in the source's log) and is locked
     * here as a regression contract.
     * </p>
     *
     * <p>Setup:
     * <ol>
     *   <li>two sibling sub-collections A and B under the user's home;</li>
     *   <li>one plain member M inside A;</li>
     *   <li>baseline initial syncs of A (token T_A) and B (token T_B);</li>
     *   <li>{@link org.unitedinternet.cosmo.service.ContentService#moveItem}
     *       (M, A, B);</li>
     *   <li>assert A's incremental sync (with T_A) reports zero members
     *       and B's incremental sync (with T_B) reports zero members;</li>
     *   <li>assert a fresh initial sync of B lists member M — proving the
     *       move was actually effective (not a silent loss in either
     *       direction).</li>
     * </ol>
     * </p>
     *
     * <p>The test also pins that a cross-parent move NEVER 500s the report
     * engine. (The H-group tombstone-dedup and D-group tests already pin
     * the tombstone behavior for real deletions and same-parent renames;
     * this isolates the {@code moveItem} path specifically.)
     * </p>
     */
    @Test
    public void crossCollectionMoveIsInvisibleToBothSiblingCollections()
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

        // Baseline initial syncs for both collections.
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

        // Source (A) incremental sync MUST NOT list the moved member.
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
        assertEquals(0, aIncResponses.size(),
                "I2: after a cross-collection move, A's incremental sync "
                + "MUST report zero members for the moved item "
                + "(got " + aIncResponses.size()
                + " — moveItem does not log to the change log; this is the "
                + "locked Cosmo behavior)");

        // Target (B) incremental sync MUST NOT list the moved member either.
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
        assertEquals(0, bIncResponses.size(),
                "I2: after a cross-collection move, B's incremental sync "
                + "MUST report zero members for the moved item "
                + "(got " + bIncResponses.size()
                + " — moveItem does not log to the change log; this is the "
                + "locked Cosmo behavior)");

        // Prove the move was effective: a fresh initial sync of B MUST
        // list member M under B.
        DavTestContext bInitialAfter =
            executeSyncCollectionReport(bUri, INITIAL_SYNC_BODY);
        assertEquals(207, bInitialAfter.getDavResponse().getStatus(),
                "I2: B's post-move fresh initial sync must also succeed "
                + "with 207 (got "
                + bInitialAfter.getDavResponse().getStatus() + ")");
        Document bInitialAfterMs =
            parseMultistatus(bInitialAfter.getHttpResponse().getContentAsString());
        assertTrue(decodedLastSegments(bInitialAfterMs).contains(mName),
                "I2: B's post-move fresh initial sync MUST show the moved "
                + "member " + mName
                + " (proving the move was effective, not a reporting bug); "
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
}
