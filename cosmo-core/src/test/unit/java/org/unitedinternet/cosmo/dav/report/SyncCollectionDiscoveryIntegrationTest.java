/*
 * Copyright 2026 United Internet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.dav.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
 * Integration tests for <strong>Group A — Discovery</strong> of the
 * {@code DAV:sync-collection} REPORT (RFC 6578) against the full WebDAV
 * request pipeline
 * ({@link StandardRequestHandler} &rarr; provider &rarr; resource).
 * </p>
 *
 * <p>
 * Covers the following cases from {@code synccollection-testcases.md}:
 * </p>
 * <ul>
 * <li><strong>A1</strong> - {@code PROPFIND} (Depth 0) requesting
 * {@code DAV:supported-report-set} on a collection must answer
 * {@code 207 Multi-Status} in which the property is returned inside a
 * {@code 200} propstat and the report type {@code DAV:sync-collection}
 * appears as an advertised report:
 * {@code <D:supported-report><D:report><D:sync-collection/></D:report></D:supported-report>}
 * (RFC 6578 Section 5: clients discover the report type through
 * {@code supported-report-set}).</li>
 * <li><strong>A2</strong> - {@code OPTIONS} against a collection must keep
 * answering the pre-existing behavior: {@code 200}, an {@code Allow} header
 * that lists {@code REPORT}, and the standard {@code DAV} compliance-class
 * header (RFC 6578 requires no new compliance class token, so the
 * advertised class list is unchanged).</li>
 * </ul>
 *
 * <p>
 * Discovery is the standard client entry point: before issuing a
 * {@code REPORT} a DAV client is required to check
 * {@code supported-report-set} (RFC 4918 §14.14) and, for
 * CalDAV-style clients, the {@code OPTIONS} {@code Allow} header. Both
 * probes MUST advertise the new capability so conformant clients do not
 * fall back to polling.
 * </p>
 *
 * <p>
 * <strong>Expected status: A1 should already pass</strong> (the report type
 * is registered in {@code DavCollectionBase.REPORT_TYPES}, which
 * {@code DavResourceBase#loadProperties()} advertises as the
 * {@code supported-report-set} live property; the home collection extends
 * {@code DavCollectionBase}), <strong>A2 should also pass</strong> against
 * a calendar collection (its {@code getSupportedMethods()} includes
 * {@code REPORT}). A2 deliberately targets a calendar collection rather
 * than the home collection, because Cosmo's home collection intentionally
 * excludes {@code REPORT} from its method list.
 * </p>
 */
public class SyncCollectionDiscoveryIntegrationTest extends BaseDavTestCase {

    /** Calendar collection fixture name (under the user's home path). */
    private static final String COLLECTION_NAME = "cal";

    /**
     * PROPFIND body requesting only {@code DAV:supported-report-set}.
     * An explicit prop set (not {@code allprop}) keeps the assertion tight
     * and avoids pulling in ACL-related properties that can depend on
     * security context evaluation details.
     */
    private static final String PROPFIND_BODY =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<D:propfind xmlns:D=\"DAV:\">\n"
        + "  <D:prop>\n"
        + "    <D:supported-report-set/>\n"
        + "  </D:prop>\n"
        + "</D:propfind>";

    /**
     * Working provider methods require a security context so ACL checks pass.
     */
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        testHelper.logIn();
    }

    /**
     * Test case A1: a {@code PROPFIND} Depth 0 requesting
     * {@code DAV:supported-report-set} must return 207 Multi-Status with
     * the property inside a 200 propstat, advertising
     * {@code DAV:sync-collection} as a supported report.
     *
     * Target: a calendar collection (RFC 6578 targets collection
     * resources).
     */
    @Test
    public void supportedReportSetMustAdvertiseSyncCollection() throws Exception {
        testHelper.makeAndStoreDummyCalendarCollection(COLLECTION_NAME);

        DavTestContext ctx = executeRequest("PROPFIND",
                                            "/dav/test/" + COLLECTION_NAME,
                                            PROPFIND_BODY,
                                            "application/xml",
                                            "Depth", "0");

        assertEquals(207, ctx.getDavResponse().getStatus(),
                "PROPFIND Depth 0 must be answered with 207 Multi-Status "
                + "(got " + ctx.getDavResponse().getStatus() + ")");

        String body = ctx.getHttpResponse().getContentAsString();
        Document multistatus = parseMultistatus(body);
        Element response = firstChildElement(multistatus.getDocumentElement(), "response");
        assertNotNull(response,
                "multistatus must contain at least one DAV:response (depth-0 self); "
                + "body: " + body);

        // A depth-0 response must carry exactly one DAV:href (the collection
        // itself).
        List<Element> hrefs = getChildElements(response, "href");
        assertEquals(1, hrefs.size(),
                "depth-0 PROPFIND must carry exactly one DAV:href per response; "
                + "body: " + body);

        Element srsProp = findPropStatProp(response, "supported-report-set", 200);
        assertNotNull(srsProp,
                "DAV:supported-report-set must be returned inside a 200 propstat; "
                + "if absent, the server is not advertising supported reports "
                + "(RFC 4918 Section 14.14). Full response body: " + body);

        boolean advertisesSyncCollection = false;
        List<Element> supportedReports = allDescendants(srsProp, "supported-report");
        for (Element supportedReport : supportedReports) {
            for (Element report : getChildElements(supportedReport, "report")) {
                if (!getChildElements(report, "sync-collection").isEmpty()) {
                    advertisesSyncCollection = true;
                }
            }
        }

        assertTrue(advertisesSyncCollection,
                "DAV:supported-report-set must advertise <D:report>"
                        + "<D:sync-collection/></D:report>; "
                        + "actual property content: " + dump(srsProp)
                        + " (this is the discovery mechanism required by "
                        + "RFC 6578 Section 5 for DAV:sync-collection)");
    }

    /**
     * Test case A2: {@code OPTIONS} against a calendar collection must
     * still answer the way Cosmo has always answered for collections:
     * {@code 200}, {@code Allow} header listing supported methods (which
     * must include {@code REPORT} so clients know the server accepts it),
     * and the standard {@code DAV} compliance-class header.
     *
     * RFC 6578 adds no new compliance class token and no new OPTIONS
     * extension, so the response shape must be unchanged — the discovery
     * for {@code sync-collection} is done via
     * {@code supported-report-set} (A1), not via the DAV header.
     */
    @Test
    public void optionsMustStillAdvertiseReportMethodAndExistingDavClass() throws Exception {
        assertNotNull(testHelper.makeAndStoreDummyCalendarCollection(COLLECTION_NAME),
                "fixture calendar collection was not created");

        DavTestContext ctx = executeRequest("OPTIONS",
                                            "/dav/test/" + COLLECTION_NAME,
                                            null, null, null, null);

        assertEquals(200, ctx.getDavResponse().getStatus(),
                "OPTIONS must be answered with 200 OK (got "
                        + ctx.getDavResponse().getStatus() + ")");

        String allow = ctx.getHttpResponse().getHeader("Allow");
        assertNotNull(allow,
                "OPTIONS response must carry an Allow header listing supported methods");
        assertTrue(containsMethod(allow, "REPORT"),
                "Allow header must include REPORT so RFC 6578 clients discover "
                + "the collection accepts REPORT (Allow: " + allow + ")");

        String dav = ctx.getHttpResponse().getHeader("DAV");
        assertNotNull(dav,
                "OPTIONS response must carry a DAV header listing compliance classes");
        assertTrue(dav.startsWith("1") || dav.contains(" 1")
                || dav.contains(",1") || dav.contains(";1"),
                "DAV header must list compliance class 1 (the base WebDAV class); "
                        + "got: " + dav);
        // RFC 6578 does not require a new class token; assert nothing
        // surprising was added.
        assertTrue(dav.contains("calendar-access") || dav.contains("calendar"),
                "for a calendar collection the DAV header must still list the "
                + "CalDAV class (existing behavior); got: " + dav);
    }

    /**
     * Test case A2b (supplementary): the home collection intentionally does
     * NOT list REPORT in its {@code Allow} header (see
     * {@code DavHomeCollection#getSupportedMethods()}). This is by design —
     * RFC 6578 targets regular collections — but the DAV compliance-class
     * header must still be intact (RFC 6578 adds no new token, so the
     * existing class list is preserved).
     */
    @Test
    public void optionsOnHomeCollectionKeepsExistingComplianceClass() throws Exception {
        DavTestContext ctx = executeRequest("OPTIONS", "/dav/test",
                                            null, null, null, null);

        assertEquals(200, ctx.getDavResponse().getStatus(),
                "OPTIONS against the home collection must be answered with 200");

        String dav = ctx.getHttpResponse().getHeader("DAV");
        assertNotNull(dav,
                "home collection OPTIONS must carry the DAV compliance header");
        assertTrue(dav.contains("1"),
                "DAV header must list class 1 (existing behavior); got: " + dav);
    }

    // request helper

    private DavTestContext executeRequest(String method, String uri,
                                          String requestBody, String contentType,
                                          String ...headerPairs) throws Exception {
        DavTestContext ctx = testHelper.createTestContext();

        ctx.getHttpRequest().setServletPath("/dav");
        ctx.getHttpRequest().setRequestURI(uri);
        ctx.getHttpRequest().setMethod(method);

        if (contentType != null) {
            ctx.getHttpRequest().setContentType(contentType);
            ctx.getHttpRequest().addHeader("Content-Type", contentType);
        }
        if (requestBody != null) {
            ctx.getHttpRequest().setContent(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        // optional simple headers (pairs of name, value); skip pairs whose
        // name or value is null so callers can pass a trailing null sentinel.
        if (headerPairs != null) {
            for (int i = 0; i + 1 < headerPairs.length; i += 2) {
                String name  = headerPairs[i];
                String value = headerPairs[i + 1];
                if (name != null && value != null) {
                    ctx.getHttpRequest().addHeader(name, value);
                }
            }
        }

        new StandardRequestHandler(testHelper.getResourceLocatorFactory(),
                testHelper.getResourceFactory(),
                testHelper.getEntityFactory())
                .handleRequest(ctx.getDavRequest(), ctx.getDavResponse());

        return ctx;
    }

    private static boolean containsMethod(String allow, String method) {
        for (String part : allow.split("[,;]")) {
            if (part.trim().equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
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
                    && localName.equals(child.getLocalName())) {
                result.add((Element) child);
            }
        }
        return result;
    }

    private Element firstChildElement(Element parent, String localName) {
        List<Element> result = getChildElements(parent, localName);
        return result.isEmpty() ? null : result.get(0);
    }

    private List<Element> allDescendants(Element ancestor, String localName) {
        List<Element> result = new ArrayList<Element>();
        collectDescendants(ancestor, localName, result);
        return result;
    }

    private void collectDescendants(Element ancestor, String localName, List<Element> out) {
        NodeList children = ancestor.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element) child;
            if (localName.equals(e.getLocalName())) {
                out.add(e);
            }
            collectDescendants(e, localName, out);
        }
    }

    /**
     * Finds the named property inside the response's propstat with the
     * expected status code. Status text is of the form {@code "HTTP/1.1 200 OK"}
     * (one or more lines). We accept any line of the form {@code "HTTP/x.y COD ..."}.
     */
    private Element findPropStatProp(Element response, String propLocalName, int expectedCode) {
        for (Element propstat : getChildElements(response, "propstat")) {
            Element statusElement = firstChildElement(propstat, "status");
            if (statusElement == null) {
                continue;
            }
            if (!statusHasCode(statusElement.getTextContent(), expectedCode)) {
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
     * Returns true iff the status text contains the line form
     * {@code "HTTP/<version> <code> ..."} on any of its lines.
     */
    private static boolean statusHasCode(String statusText, int expectedCode) {
        if (statusText == null) return false;
        for (String line : statusText.split("[\\r\\n]+")) {
            line = line.trim();
            // split on whitespace and pull out the 3rd token
            String[] parts = line.split("\\s+");
            if (parts.length >= 3 && parts[0].startsWith("HTTP/")) {
                try {
                    if (Integer.parseInt(parts[1]) == expectedCode) {
                        return true;
                    }
                } catch (NumberFormatException nfe) {
                    // not a status line; try the next line
                }
            }
        }
        return false;
    }

    /** Minimal XML dump for assertion messages. */
    private String dump(Element e) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            // null Transformer = built-in identity transform
            Transformer t = tf.newTransformer(null);
            StringWriter sw = new StringWriter();
            t.transform(new DOMSource(e), new StreamResult(sw));
            return sw.toString();
        } catch (Exception x) {
            return e.toString();
        }
    }
}
