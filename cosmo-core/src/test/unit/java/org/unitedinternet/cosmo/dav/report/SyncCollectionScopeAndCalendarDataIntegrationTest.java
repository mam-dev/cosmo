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
import org.unitedinternet.cosmo.CosmoConstants;
import org.unitedinternet.cosmo.dav.caldav.report.CaldavMultiStatusReport;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavEvent;
import org.unitedinternet.cosmo.dav.servlet.StandardRequestHandler;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

/**
 * <p>
 * Integration tests for the <strong>scoped initial synchronization</strong>
 * (level-1 scoping, RFC 6578 &sect;3.3) and the
 * <strong>calendar-data pseudo-property behavior</strong> of the
 * {@code DAV:sync-collection} REPORT against the full WebDAV request
 * pipeline ({@link StandardRequestHandler} &rarr; provider &rarr; report).
 * </p>
 *
 * <p>
 * Covers the following cases from {@code synccollection-testcases.md}:
 * </p>
 * <ul>
 * <li><strong>B3</strong> - an initial sync of a collection that contains
 * a sub-collection (which itself has a member) MUST list the
 * sub-collection as a member but MUST NOT list the sub-collection's
 * member. This is the RFC 6578 {@code DAV:sync-level "1"} scope: only
 * immediate children of the target collection are visible. Deeper
 * sync-levels are rejected by this build (see
 * {@code SyncCollectionErrorPathIntegrationTest}, G7/G8).</li>
 * <li><strong>B5</strong> - requesting the CalDAV pseudo-property
 * {@code CALDAV:calendar-data} together with a live property
 * ({@code DAV:getetag}) in a sync-collection request. The live property
 * resolves normally (200 propstat); the pseudo-property surfaces in a
 * per-member 404 propstat - the same treatment the E-series tests already
 * pinned for unknown properties. See the class Javadoc below for the
 * design rationale and the deviation from the idealized spec text.</li>
 * </ul>
 *
 * <p>
 * <strong>B5 design note (deviation from the spec's ideal wording):</strong>
 * RFC 6578 is CalDAV-agnostic; the spec text's ideal of "each response
 * carries both props (200); iCalendar data well-formed (mirrors
 * MultigetReport behavior)" depends on the CalDAV pseudo-property
 * extension that Cosmo implements <em>only</em> in
 * {@link CaldavMultiStatusReport} (used by Multiget / Query / FreeBusy).
 * The {@code DAV:sync-collection} report extends the plain
 * {@link MultiStatusReport} (Base-DAV family) because it MUST be
 * resolvable on ANY collection - including non-calendar ones - and
 * therefore deliberately does NOT special-case
 * {@code CALDAV:calendar-data}.
 * </p>
 *
 * <p>
 * <em>Observed behavior (locked by this test):</em> requesting
 * {@code CALDAV:calendar-data} in a sync-collection REPORT yields, for
 * each member: a 200 propstat carrying {@code DAV:getetag} and a separate
 * 404 propstat carrying {@code CALDAV:calendar-data}. The report still
 * succeeds overall (207), the pseudo-property never fails the request, and
 * the response ends with a usable {@code DAV:sync-token}.
 * </p>
 *
 * <p>
 * If a client needs calendar bodies on sync, the sanctioned pairing is a
 * sync-collection round (identify changed members) followed by a
 * {@code CALDAV:calendar-multiget} (fetch bodies) - or a future production
 * change that conditionally adopts the {@link CaldavMultiStatusReport}
 * calendar-data behavior on calendar collections (out of scope here; no
 * production change made).
 * </p>
 *
 * <p>
 * <strong>Status: pinned to observed behavior on 2026-08-28.</strong>
 * </p>
 */
public class SyncCollectionScopeAndCalendarDataIntegrationTest extends BaseDavTestCase {

    private static final String DAV_NAMESPACE = "DAV:";

    /** Fixture member names for the B3 hierarchy. */
    private static final String PLAIN_MEMBER_NAME = "b3-m1";
    private static final String SUB_COLLECTION_MEMBER_NAME = "b3-sm1";

    /** RFC 6578 &sect;3.1 initial sync request body (empty sync-token). */
    private static final String INITIAL_SYNC_BODY_GETETAG =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<D:sync-collection xmlns:D=\"DAV:\">\n"
        + "  <D:sync-token/>\n"
        + "  <D:sync-level>1</D:sync-level>\n"
        + "  <D:prop>\n"
        + "    <D:getetag/>\n"
        + "  </D:prop>\n"
        + "</D:sync-collection>";

    /**
     * B5 request body: asks for both the live DAV:getetag and the CalDAV
     * pseudo-property CALDAV:calendar-data. The pseudo-property is
     * expected to surface as a 404 propstat per member (see class Javadoc).
     */
    private static final String INITIAL_SYNC_BODY_CALENDAR_DATA =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<D:sync-collection xmlns:D=\"DAV:\""
        + " xmlns:C=\"urn:ietf:params:xml:ns:caldav\">\n"
        + "  <D:sync-token/>\n"
        + "  <D:sync-level>1</D:sync-level>\n"
        + "  <D:prop>\n"
        + "    <D:getetag/>\n"
        + "    <C:calendar-data/>\n"
        + "  </D:prop>\n"
        + "</D:sync-collection>";

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        testHelper.logIn();
    }

    // ---------- B3: initial sync lists only immediate children ----------

    /**
     * Test case B3: a collection that contains a plain member AND a
     * sub-collection (which itself carries a member) must be reported by
     * an initial sync-collection as having exactly its immediate children.
     * The grand-child member MUST NOT appear as a DAV:response - scoping
     * is level "1" (immediate children only).
     */
    @Test
    public void initialSyncDoesNotListSubCollectionMembers() throws Exception {
        final String subCollectionName = setupB3Hierarchy();

        DavTestContext ctx =
            executeSyncCollectionReport("/dav/test", INITIAL_SYNC_BODY_GETETAG);

        assertEquals(207, ctx.getDavResponse().getStatus(),
                "initial sync-collection on a collection with nested "
                + "content must still succeed with 207 (got "
                + ctx.getDavResponse().getStatus() + ")");

        Document multistatus =
            parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses =
            getChildElements(multistatus.getDocumentElement(), "response");
        List<String> hrefs = collectHrefs(responses);

        assertFalse(hrefs.isEmpty(),
                "the response MUST list immediate children (a plain member "
                + "and a sub-collection)");

        assertTrue(hrefsContain(hrefs, PLAIN_MEMBER_NAME),
                "immediate member " + PLAIN_MEMBER_NAME
                + " MUST appear in the sync-collection (got hrefs: "
                + hrefs + ")");
        assertTrue(hrefsContain(hrefs, subCollectionName),
                "immediate sub-collection " + subCollectionName
                + " MUST appear in the sync-collection (got hrefs: "
                + hrefs + ")");
        assertFalse(hrefsContain(hrefs, SUB_COLLECTION_MEMBER_NAME),
                "level-1 scope MUST NOT include grand-child member "
                + SUB_COLLECTION_MEMBER_NAME + " (got hrefs: "
                + hrefs + ")");

        // The sub-collection that IS listed must carry a 200 propstat for
        // the requested live property, proving sync treats it like any
        // other member resource.
        Element subResponse =
            findResponseByHrefFragment(responses, subCollectionName);
        assertNotNull(subResponse, "sub-collection response was not found");
        assertNotNull(findPropStatProp(subResponse, "getetag", 200),
                "the listed sub-collection must carry DAV:getetag in a "
                + "200 propstat");
    }

    // ---------- B5: calendar-data pseudo-prop on the sync-collection path ----------

    /**
     * Test case B5: requesting {@code CALDAV:calendar-data} in a
     * sync-collection REPORT against a {@link DavCalendarCollection}
     * containing a {@link DavEvent} member.
     *
     * <p>Observed behavior (see class Javadoc for design rationale and
     * the deviation from the spec's ideal):</p>
     * <ul>
     *   <li>the report overall still succeeds (207);</li>
     *   <li>{@code DAV:getetag} returns a 200 propstat for the event;</li>
     *   <li>{@code CALDAV:calendar-data} surfaces in a per-member
     *       <em>404</em> propstat (same treatment the E-series tests
     *       already pinned for unknown properties);</li>
     *   <li>the response ends with a usable {@code DAV:sync-token}.</li>
     * </ul>
     */
    @Test
    public void calendarDataPseudoPropertySurfacesAsUnknownInSyncCollection()
            throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("b5-cal");
        assertEquals("b5-cal", dcc.getItem().getName(),
                "B5: fixture calendar collection must be named b5-cal");

        DavEvent de = testHelper.initializeDavEvent(dcc, "b5-event");
        assertNotNull(de, "B5: fixture DavEvent must be created");

        DavTestContext ctx =
            executeSyncCollectionReport(
                    "/dav/test/" + dcc.getItem().getName(),
                    INITIAL_SYNC_BODY_CALENDAR_DATA);

        assertEquals(207, ctx.getDavResponse().getStatus(),
                "B5: sync-collection on a calendar collection must still "
                + "succeed (207), got " + ctx.getDavResponse().getStatus());

        Document multistatus =
            parseMultistatus(ctx.getHttpResponse().getContentAsString());
        List<Element> responses =
            getChildElements(multistatus.getDocumentElement(), "response");

        Element eventResponse =
            findResponseByHrefFragment(responses, "b5-event");
        assertNotNull(eventResponse,
                "B5: the event MUST appear in the sync-collection response "
                + "(hrefs: " + collectHrefs(responses) + ")");

        // The live property resolves normally.
        assertNotNull(findPropStatProp(eventResponse, "getetag", 200),
                "B5: DAV:getetag MUST be in a 200 propstat for the event; "
                + "the pseudo-property must not drag down the live one");

        // The CalDAV pseudo-property is NOT specially resolved on the
        // sync-collection path; it surfaces as an unknown property in a
        // 404 propstat (same treatment as E2 for unknown props).
        assertNotNull(findPropStatProp(eventResponse, "calendar-data", 404),
                "B5: observed behavior locks CALDAV:calendar-data as a 404 "
                + "propstat on the sync-collection path (plain "
                + "MultiStatusReport base; no CaldavMultiStatusReport "
                + "override). See class Javadoc. If this now 200s, the "
                + "report has acquired CalDAV special treatment and this "
                + "test must be updated accordingly.");

        // The pseudo-property must not be silently folded into the 200
        // propstat either.
        assertFalse(isPropInPropstatWithCode(
                eventResponse, "calendar-data", 200),
                "B5: CALDAV:calendar-data MUST NOT appear in a 200 "
                + "propstat of the sync-collection path");

        Element syncToken =
            findDirectSyncToken(multistatus.getDocumentElement());
        assertNotNull(syncToken,
                "B5: response must still carry a DAV:sync-token");
        assertFalse(syncToken.getTextContent().trim().isEmpty(),
                "B5: sync-token must not be empty");
    }

    // ---------- fixture helpers ----------

    /**
     * Builds the B3 hierarchy under the fixture user's home collection:
     * a plain member "b3-m1" and a sub-collection that itself carries
     * member "b3-sm1" (the grand-child that level-1 sync must exclude).
     *
     * @return the stored name of the created sub-collection
     */
    private String setupB3Hierarchy() throws Exception {
        CollectionItem home = testHelper.getHomeCollection();

        NoteItem plain =
            testHelper.makeAndStoreDummyItem(home, PLAIN_MEMBER_NAME);
        assertNotNull(plain, "B3: plain member " + PLAIN_MEMBER_NAME
                + " must be created");

        CollectionItem sub = testHelper.makeAndStoreDummyCollection(home);
        assertNotNull(sub, "B3: sub-collection must be created");

        NoteItem grandChild =
            testHelper.makeAndStoreDummyItem(sub, SUB_COLLECTION_MEMBER_NAME);
        assertNotNull(grandChild, "B3: grand-child "
                + SUB_COLLECTION_MEMBER_NAME + " must be created");

        return sub.getName();
    }

    // ---------- request + XML helpers ----------

    /**
     * Sends a REPORT request against the given DAV URI with the given XML
     * body through the full {@link StandardRequestHandler} pipeline.
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

    private Document parseMultistatus(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl",
            true);
        Document document = dbf.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        assertEquals("multistatus",
            document.getDocumentElement().getLocalName(),
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

    private List<String> collectHrefs(List<Element> responses) {
        List<String> hrefs = new ArrayList<String>();
        for (Element response : responses) {
            for (Element href : getChildElements(response, "href")) {
                hrefs.add(href.getTextContent().trim());
            }
        }
        return hrefs;
    }

    private Element findResponseByHrefFragment(List<Element> responses,
        String fragment) {
        for (Element response : responses) {
            for (Element href : getChildElements(response, "href")) {
                if (decodedHrefText(href.getTextContent()).contains(fragment)) {
                    return response;
                }
            }
        }
        return null;
    }

    /**
     * Finds the named property inside a propstat block of the given
     * response whose status matches the expected code (e.g. 200 or 404).
     *
     * <p>The inner property element is matched by local name in <em>any</em>
     * namespace: DAV live properties (e.g. {@code getetag}) are
     * {@code DAV:}-prefixed, but CalDAV pseudo-properties
     * ({@code calendar-data}) are {@code urn:ietf:params:xml:ns:caldav}-
     * prefixed and must still be locatable here. The {@code DAV:}-only
     * {@link #getChildElements} helper would otherwise never see them.</p>
     */
    private Element findPropStatProp(Element response,
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
                for (Element candidate :
                        findPropertyElementAnyNamespace(prop, propLocalName)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Returns the element(s) that are direct children of {@code parent} with
     * the given local name in <em>any</em> (including no) namespace. Used for
     * properties whose namespace is not the DAV one (e.g. CalDAV
     * pseudo-properties) that {@link #getChildElements} cannot match.
     */
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

    /**
     * True if the named property appears inside ANY propstat of the
     * response whose status is the given code.
     */
    private boolean isPropInPropstatWithCode(Element response,
        String propLocalName, int expectedCode) {
        return findPropStatProp(response, propLocalName, expectedCode) != null;
    }

    private Element findDirectSyncToken(Element multistatusRoot) {
        List<Element> tokens =
            getChildElements(multistatusRoot, "sync-token");
        return tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);
    }

    private boolean hrefsContain(List<String> hrefs, String fragment) {
        for (String href : hrefs) {
            if (decodedHrefText(href).contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Decodes the percent-encoded / plus-encoded DAV:href text so that
     * fragment matching works against the resource's plain name. DAV
     * hrefs encode spaces as {@code +} (or {@code %20}); fixture names such
     * as "test collection 2" arrive as "test+collection+2". Decoding is
     * best-effort: any decoding failure yields the original text, so this
     * helper can never throw.
     */
    private String decodedHrefText(String href) {
        String text = href.trim();
        try {
            return URLDecoder.decode(text, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return text;
        }
    }
}
