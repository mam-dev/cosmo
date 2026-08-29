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

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.DavTestContext;
import org.unitedinternet.cosmo.dav.servlet.StandardRequestHandler;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.NoteItem;

/**
 * <p>
 * Integration tests for the <strong>error / negative path</strong> of the
 * {@code DAV:sync-collection} REPORT (RFC 6578), covering test cases
 * G1, G2, G3, G5, G6, G7 and G8 from
 * {@code /cosmo/synccollection-testcases.md}.
 * </p>
 *
 * <p>
 * Every test drives the full {@link StandardRequestHandler} pipeline
 * against a logged-in fixture user, so asserted status codes are the
 * server's real client-error responses rendered through the standard
 * DAV error-document handling in
 * {@code StandardRequestHandler#handleRequest}.
 * </p>
 *
 * <p>
 * Behavior locked by these tests:
 * </p>
 * <ul>
 * <li><strong>G1</strong> - a REPORT body whose root element is not a
 * server-supported report ({@code <D:foo/>}) must be rejected with a
 * 4xx client error and must never yield 207 or a 5xx. This build's
 * pipeline rejects unsupported reports with
 * {@code UnprocessableEntityException} (422) from
 * {@code DavResourceBase#getReport}; the assertion pins exactly that.</li>
 * <li><strong>G2</strong> - a REPORT against a non-existent path returns
 * 404 Not Found ({@code BaseProvider#report} checks existence first).</li>
 * <li><strong>G3</strong> - a REPORT against a non-collection member
 * resource returns a clean 4xx client error and never 207 (which would
 * falsify a successful sync) or 5xx.</li>
 * <li><strong>G5</strong> - a syntactically malformed XML body is
 * rejected with 400 Bad Request ({@code getSafeRequestDocument} wraps
 * parse errors into {@code BadRequestException}).</li>
 * <li><strong>G6</strong> - a sync-collection body without the mandatory
 * {@code DAV:sync-level} is rejected with 400 (explicit check in
 * {@code SyncCollectionReport#parseReport}).</li>
 * <li><strong>G7</strong> - {@code DAV:sync-level=2} is rejected with
 * 400 (this build supports level 1 only); the RFC 6578 400-or-415
 * client-error contract is satisfied.</li>
 * <li><strong>G8</strong> - {@code DAV:sync-level=infinity} on this
 * level-1-only build is rejected with 400 (the spec allows either 400
 * or 207-with-descendants; Cosmo pins 400).</li>
 * </ul>
 *
 * <p>
 * <strong>Coverage gaps, intentionally not automated:</strong>
 * <ul>
 * <li>G9 (stale token) - in Cosmo's global-revision token scheme a
 * stale-but-valid token is indistinguishable at the wire level from a
 * current one; the committed token-rejection regression lock already
 * covers "403 &rarr; client falls back to initial sync".</li>
 * <li>G10 (cross-collection token) - cosmo sync-tokens are global
 * revision numbers ({@code urn:cosmo:sync-token:<rev>}), not
 * per-collection secrets, so "foreign" is not a distinct wire state.</li>
 * <li>G11 (garbage token) - already covered by
 * {@code SyncCollectionIncrementalSyncIntegrationTest}
 * (garbageAndFutureTokensAreRejectedWith403).</li>
 * <li>G4 (missing body) - already covered by
 * {@code SyncCollectionInitialSyncIntegrationTest}.</li>
 * <li>G12 (401) / G13 (404-no-read-privilege) - Cosmo-wide ACL behavior,
 * out of scope for the sync-collection feature surface.</li>
 * </ul>
 *
 * <p>
 * <strong>Status: written red-first (2026-08-28).</strong>
 * </p>
 */
public class SyncCollectionErrorPathIntegrationTest extends BaseDavTestCase {

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";

    /**
     * Working provider methods require a security context so ACL checks pass.
     */
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        testHelper.logIn();
    }

    // ---------- G1: unknown report type ----------

    /**
     * Test case G1 - a REPORT body whose root element is not one of the
     * server-supported report types must be rejected with a 4xx client
     * error and must never produce a 207 multistatus or a 5xx.
     *
     * <p>On this build the rejection lands in
     * {@code DavResourceBase#getReport} as
     * {@code UnprocessableEntityException} (422), after
     * {@code BaseProvider#report} has already verified target existence
     * and ACL. A compliant DAV client treats 4xx identically: fall back
     * to PROPFIND or the appropriate supported report.
     */
    @Test
    public void unknownReportTypeIsRejectedWithClientError() throws Exception {
        DavTestContext ctx = executeReport("/dav/test",
                XML_HEADER + "<D:foo xmlns:D=\"DAV:\"/>");

        int status = ctx.getDavResponse().getStatus();
        assertTrue(status >= 400 && status < 500,
                "G1: unknown REPORT type must be a 4xx client error, got "
                        + status);
        assertEquals(422, status,
                "G1: this build rejects unsupported reports with "
                        + "422 Unprocessable Entity (DavResourceBase#getReport)");
    }

    // ---------- G2: nonexistent target ----------

    /**
     * Test case G2 - a REPORT against a path that does not exist must be
     * rejected with 404 Not Found, never 400/403/500.
     */
    @Test
    public void nonexistentTargetYields404() throws Exception {
        DavTestContext ctx = executeReport("/dav/test/definitely-not-here-7f",
                INITIAL_SYNC_BODY());

        assertEquals(404, ctx.getDavResponse().getStatus(),
                "G2: REPORT against a non-existent path must return "
                        + "404 Not Found, got " + ctx.getDavResponse().getStatus());
    }

    // ---------- G3: non-collection target ----------

    /**
     * Test case G3 - a REPORT against a non-collection member resource
     * must be rejected with a clean 4xx client error and must never
     * produce a 207 multistatus (which would falsely assert a successful
     * sync) or a 5xx.
     *
     * <p>A member item is created deterministically under the fixture
     * home collection ({@code makeAndStoreDummyItem(parent, name)}) and
     * the REPORT is targeted at its URI.
     */
    @Test
    public void nonCollectionTargetIsRejectedWithClientError() throws Exception {
        HomeCollectionItem home = testHelper.getHomeCollection();
        NoteItem member = testHelper.makeAndStoreDummyItem(home, "g3-member");
        assertNotNull(member, "G3: fixture member item must be created");

        DavTestContext ctx = executeReport("/dav/test/" + member.getName(),
                INITIAL_SYNC_BODY());

        int status = ctx.getDavResponse().getStatus();
        assertTrue(status >= 400 && status < 500,
                "G3: REPORT against a non-collection member must be a "
                        + "4xx client error (got " + status + "), never "
                        + "207 or 5xx");
    }

    // ---------- G5: malformed XML body ----------

    /**
     * Test case G5 - a syntactically malformed (non well-formed) XML
     * request body must be rejected with 400 Bad Request, never 5xx.
     */
    @Test
    public void malformedXmlBodyIsRejectedWith400() throws Exception {
        // deliberately unterminated element
        String body = XML_HEADER + "<D:sync-collection xmlns:D=\"DAV:\"><oops";
        DavTestContext ctx = executeReport("/dav/test", body);

        assertEquals(400, ctx.getDavResponse().getStatus(),
                "G5: malformed XML body must yield 400 Bad Request, "
                        + "got " + ctx.getDavResponse().getStatus());
    }

    // ---------- G6: missing sync-level ----------

    /**
     * Test case G6 - a sync-collection body without the mandatory
     * {@code DAV:sync-level} element must be rejected with
     * 400 Bad Request.
     */
    @Test
    public void missingSyncLevelIsRejectedWith400() throws Exception {
        String body = XML_HEADER
                + "<D:sync-collection xmlns:D=\"DAV:\">\n"
                + "  <D:sync-token/>\n"
                + "</D:sync-collection>";
        DavTestContext ctx = executeReport("/dav/test", body);

        assertEquals(400, ctx.getDavResponse().getStatus(),
                "G6: sync-collection body without DAV:sync-level must "
                        + "yield 400, got " + ctx.getDavResponse().getStatus());
    }

    // ---------- G7: unsupported sync-level value ----------

    /**
     * Test case G7 - {@code DAV:sync-level=2} is not supported by this
     * level-1 build and must be rejected with 400 Bad Request
     * (RFC 6578 permits 400 for an unsupported sync-level).
     */
    @Test
    public void unsupportedSyncLevelValueIsRejectedWith400() throws Exception {
        String body = XML_HEADER
                + "<D:sync-collection xmlns:D=\"DAV:\">\n"
                + "  <D:sync-token/>\n"
                + "  <D:sync-level>2</D:sync-level>\n"
                + "</D:sync-collection>";
        DavTestContext ctx = executeReport("/dav/test", body);

        assertEquals(400, ctx.getDavResponse().getStatus(),
                "G7: DAV:sync-level=2 must be rejected with 400, "
                        + "got " + ctx.getDavResponse().getStatus());
    }

    // ---------- G8: infinity when unsupported ----------

    /**
     * Test case G8 - {@code DAV:sync-level=infinity} on a level-1-only
     * build must be rejected with 400. (The spec permits either a 400
     * rejection or a 207 with full-subtree results; Cosmo pins the
     * rejection.)
     */
    @Test
    public void infinitySyncLevelIsRejectedWith400OnLevel1OnlyBuild() throws Exception {
        String body = XML_HEADER
                + "<D:sync-collection xmlns:D=\"DAV:\">\n"
                + "  <D:sync-token/>\n"
                + "  <D:sync-level>infinity</D:sync-level>\n"
                + "</D:sync-collection>";
        DavTestContext ctx = executeReport("/dav/test", body);

        assertEquals(400, ctx.getDavResponse().getStatus(),
                "G8: DAV:sync-level=infinity must be rejected with 400 "
                        + "on this level-1-only build, "
                        + "got " + ctx.getDavResponse().getStatus());
    }

    // ---------- helpers ----------

    /**
     * Sends a REPORT request against the given DAV URI with the given
     * XML body through the full {@link StandardRequestHandler} pipeline.
     */
    private DavTestContext executeReport(String uri, String body) throws Exception {
        DavTestContext ctx = testHelper.createTestContext();

        ctx.getHttpRequest().setServletPath("/dav");
        ctx.getHttpRequest().setRequestURI(uri);
        ctx.getHttpRequest().setMethod("REPORT");
        ctx.getHttpRequest().setContent(body.getBytes(StandardCharsets.UTF_8));
        ctx.getHttpRequest().setContentType("application/xml");
        ctx.getHttpRequest().addHeader("Content-Type", "application/xml");

        new StandardRequestHandler(
                testHelper.getResourceLocatorFactory(),
                testHelper.getResourceFactory(),
                testHelper.getEntityFactory())
                .handleRequest(ctx.getDavRequest(), ctx.getDavResponse());

        return ctx;
    }

    private static String INITIAL_SYNC_BODY() {
        return XML_HEADER
                + "<D:sync-collection xmlns:D=\"DAV:\">\n"
                + "  <D:sync-token/>\n"
                + "  <D:sync-level>1</D:sync-level>\n"
                + "</D:sync-collection>";
    }
}
