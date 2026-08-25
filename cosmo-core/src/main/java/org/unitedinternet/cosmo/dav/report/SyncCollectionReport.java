/*
 * Copyright 2026 Open Source Applications Foundation
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.impl.DavItemResource;
import org.unitedinternet.cosmo.model.CollectionModification;
import org.unitedinternet.cosmo.service.ContentService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Represents the <code>DAV:sync-collection</code> report defined by
 * RFC 6578 (Collection Synchronization for WebDAV).
 * </p>
 *
 * <p>
 * An empty or absent <code>DAV:sync-token</code> requests an initial
 * synchronization: the full list of current collection members (RFC 6578
 * Section 3.3). A token previously issued by this server requests an
 * incremental synchronization (Section 3.4): only members created, modified
 * or deleted since that token are reported, removals appearing as deletion
 * tombstones (Section 3.5). The scope is always the immediate children
 * (<code>DAV:sync-level "1"</code>), independent of any HTTP
 * <code>Depth</code> header.
 * </p>
 *
 * <p>
 * Tokens have the form <code>urn:cosmo:sync-token:&lt;revision&gt;</code>
 * where revision is a value from the persistent per-collection change log
 * ({@link CollectionModification}). Tokens that are syntactically invalid,
 * unknown or ahead of the current revision are answered with
 * <code>403 Forbidden</code> so clients fall back to an initial
 * synchronization (Section 3.7 fallback rule).
 * </p>
 *
 * <p>
 * The response is a <code>207 Multi-Status</code> whose last child is a fresh
 * opaque <code>DAV:sync-token</code>. When the result set was truncated by
 * <code>DAV:nresults</code>, the issued token reflects the last entry
 * <em>returned</em>, so no change is lost between paginated rounds; otherwise
 * it reflects the newest known revision.
 * </p>
 */
public class SyncCollectionReport extends MultiStatusReport {

    /** Request element name. */
    public static final String ELEMENT_SYNC_COLLECTION = "sync-collection";
    /** Response/request token element name. */
    public static final String ELEMENT_SYNC_TOKEN = "sync-token";
    /** Required request element specifying the synchronization scope. */
    public static final String ELEMENT_SYNC_LEVEL = "sync-level";
    /** Optional truncation hint container element. */
    public static final String ELEMENT_LIMIT = "limit";
    /** Optional maximum number of DAV:response elements. */
    public static final String ELEMENT_NRESULTS = "nresults";

    /** The only synchronization scope currently supported ("immediate children"). */
    private static final String SYNC_LEVEL_ONE = "1";

    /**
     * Prefix for the opaque sync tokens minted by this server. Clients MUST NOT
     * attempt to interpret anything after the prefix.
     */
    private static final String SYNC_TOKEN_PREFIX = "urn:cosmo:sync-token:";

    public static final ReportType REPORT_TYPE_DAV_SYNC_COLLECTION =
        ReportType.register(ELEMENT_SYNC_COLLECTION, NAMESPACE,
                            SyncCollectionReport.class);

    private long limit = -1;

    /**
     * Numeric revision parsed from a non-empty client sync-token;
     * <code>null</code> for initial synchronization requests.
     */
    private Long requestedRevision;

    // Report methods

    public ReportType getType() {
        return REPORT_TYPE_DAV_SYNC_COLLECTION;
    }

    // ReportBase methods

    /**
     * <p>
     * Parses the report info:
     * </p>
     * <pre>
     * &lt;!ELEMENT sync-collection (sync-token?, sync-level, limit?, prop?)&gt;
     * </pre>
     *
     * @throws CosmoDavException if the report info is malformed or unsupported
     */
    protected void parseReport(ReportInfo info) throws CosmoDavException {
        if (! getType().isRequestedReportType(info)) {
            throw new CosmoDavException("Report not of type " +
                    getType().getReportName());
        }

        setPropFindProps(info.getPropertyNameSet());
        setPropFindType(PROPFIND_BY_PROPERTY);

        Element levelElement =
            info.getContentElement(ELEMENT_SYNC_LEVEL, NAMESPACE);
        if (levelElement == null) {
            throw new BadRequestException(
                    "Expected DAV:" + ELEMENT_SYNC_LEVEL + " element");
        }
        String level = DomUtil.getTextTrim(levelElement);
        if (! SYNC_LEVEL_ONE.equals(level)) {
            throw new BadRequestException(
                    "Unsupported DAV:" + ELEMENT_SYNC_LEVEL + " value \"" +
                    level + "\"; only \"" + SYNC_LEVEL_ONE + "\" is supported");
        }

        Element tokenElement =
            info.getContentElement(ELEMENT_SYNC_TOKEN, NAMESPACE);
        String requestedToken =
            tokenElement == null ? null : DomUtil.getTextTrim(tokenElement);
        if (requestedToken != null && requestedToken.length() > 0) {
            requestedRevision = parseSyncToken(requestedToken);
        } else {
            requestedRevision = null;
        }

        Element limitElement =
            info.getContentElement(ELEMENT_LIMIT, NAMESPACE);
        if (limitElement != null) {
            Element nresultsElement =
                DomUtil.getChildElement(limitElement, ELEMENT_NRESULTS, NAMESPACE);
            if (nresultsElement != null) {
                try {
                    limit = Long.parseLong(DomUtil.getTextTrim(nresultsElement));
                } catch (NumberFormatException e) {
                    throw new BadRequestException(
                            "Invalid DAV:" + ELEMENT_NRESULTS + " value");
                }
                // RFC 6578 Section 3.6: DAV:nresults holds a non-negative
                // integer; 0 legally truncates the result list to zero entries.
                if (limit < 0) {
                    throw new BadRequestException(
                            "DAV:" + ELEMENT_NRESULTS + " must not be negative");
                }
            }
        }
    }

    /**
     * Validates a client-supplied sync token and extracts its revision.
     * Garbage tokens, well-formed tokens never issued by this server and
     * tokens ahead of the current revision are all rejected with
     * 403 Forbidden (RFC 6578 Section 3.7: the client repeats the initial
     * synchronization).
     */
    private long parseSyncToken(String token) throws ForbiddenException {
        if (! token.startsWith(SYNC_TOKEN_PREFIX)) {
            throw new ForbiddenException(
                    "Unknown DAV:sync-token; repeat the initial synchronization");
        }
        String suffix = token.substring(SYNC_TOKEN_PREFIX.length());
        long revision;
        try {
            revision = Long.parseLong(suffix);
        } catch (NumberFormatException e) {
            throw new ForbiddenException(
                    "Unknown DAV:sync-token; repeat the initial synchronization");
        }
        if (revision < 0 || revision > getContentService().getModificationRevision()) {
            throw new ForbiddenException(
                    "Unknown DAV:sync-token; repeat the initial synchronization");
        }
        return revision;
    }

    /**
     * Executes the query phase. Initial synchronization enumerates the
     * immediate children of the target collection regardless of the HTTP
     * Depth header; incremental synchronization replays the collection's
     * change log since the client's token.
     */
    protected void doQuerySelf(WebDavResource resource)
        throws CosmoDavException {
        if (! (resource instanceof DavCollection)) {
            throw new UnprocessableEntityException(getType() +
                    " report is only supported against collections");
        }

        if (requestedRevision == null) {
            doInitialSync(resource);
        } else {
            doIncrementalSync((DavCollection) resource);
        }
    }

    /**
     * Initial synchronization: enumerate current members up to the optional
     * limit. Behavior unchanged from the initial-sync-only implementation.
     */
    private void doInitialSync(WebDavResource resource) {
        long count = 0;
        DavResourceIterator members = ((DavCollection) resource).getMembers();
        while (members.hasNext()) {
            if (limit >= 0 && count >= limit) {
                break;
            }
            getResults().add((WebDavResource) members.nextResource());
            count++;
        }
    }

    /**
     * Incremental synchronization: replay change-log rows newer than the
     * client's revision. Created/modified members whose items still resolve
     * become regular multistatus responses via the stock result list;
     * deletions (and creations whose member can no longer be resolved)
     * become tombstones rendered by {@link #output}.
     */
    private void doIncrementalSync(DavCollection collection)
        throws CosmoDavException {
        List<CollectionModification> modifications =
            getContentService().findModificationsSince(
                    ((DavItemResource) collection).getItem().getUid(),
                    requestedRevision.longValue(),
                    (int) Math.min(limit, Integer.MAX_VALUE));

        Map<String, WebDavResource> membersByUid =
            indexMembersByUid(collection);

        long lastReturnedRevision = -1;
        for (CollectionModification modification : modifications) {
            WebDavResource member =
                membersByUid.get(modification.getMemberUid());
            boolean deleted = modification.getModType()
                    == CollectionModification.MOD_TYPE_DELETED;
            if (deleted || member == null) {
                addTombstone(modification.getMemberName());
            } else {
                getResults().add(member);
            }
            lastReturnedRevision = modification.getId().longValue();
        }

        // Truncation must never lose changes: when entries were returned,
        // continue from the last returned row next round; when none were
        // returned (caught-up or fully truncated), stay at the requested
        // revision. Initial sync always advertises the newest revision.
        responseTokenRevision =
            lastReturnedRevision >= 0
                ? lastReturnedRevision
                : requestedRevision.longValue();
    }

    private Map<String, WebDavResource> indexMembersByUid(DavCollection collection) {
        Map<String, WebDavResource> membersByUid =
            new HashMap<String, WebDavResource>();
        DavResourceIterator members = collection.getMembers();
        while (members.hasNext()) {
            WebDavResource member = (WebDavResource) members.nextResource();
            if (! (member instanceof DavItemResource)) {
                continue;
            }
            membersByUid.put(
                    ((DavItemResource) member).getItem().getUid(), member);
        }
        return membersByUid;
    }

    protected void doQueryChildren(DavCollection collection)
        throws CosmoDavException {
        // scope is fully handled by doQuerySelf
    }

    /**
     * Sends the multistatus with any deletion tombstones and the fresh
     * DAV:sync-token appended as its last child, as mandated by RFC 6578.
     */
    protected void output(DavServletResponse response)
        throws CosmoDavException {
        try {
            buildMultistatus();
            response.sendXmlResponse(
                    new TokenizedMultiStatus(getMultiStatus(), tombstoneHrefs,
                            newSyncToken()),
                    207);
        } catch (CosmoDavException e) {
            throw e;
        } catch (Exception e) {
            throw new CosmoDavException(e);
        }
    }

    /**
     * Records a deletion tombstone to render as a bare 404 DAV:response.
     */
    private void addTombstone(String memberName) throws CosmoDavException {
        String href = memberHref(memberName);
        if (tombstoneHrefs.length() > 0) {
            tombstoneHrefs.append(' ');
        }
        tombstoneHrefs.append(href);
    }

    /**
     * Builds the DAV:href of a (possibly deleted) member from the target
     * collection's href plus the percent-encoded member name.
     */
    private String memberHref(String memberName) throws CosmoDavException {
        String collectionHref = getResource().getHref();
        if (! collectionHref.endsWith("/")) {
            collectionHref = collectionHref + "/";
        }
        try {
            return URI.create(collectionHref)
                    .resolve(new URI(null, null, memberName, null))
                    .toString();
        } catch (URISyntaxException e) {
            throw new CosmoDavException(e);
        }
    }

    private String newSyncToken() {
        return SYNC_TOKEN_PREFIX +
            (responseTokenRevision >= 0
                ? Long.toString(responseTokenRevision)
                : Long.toString(getContentService().getModificationRevision()));
    }

    private ContentService getContentService() {
        return ((DavResourceFactory) getResource().getResourceFactory())
                .getContentService();
    }

    // state initialized per report run

    /** Hrefs of deletion tombstones, space-separated (empty when none). */
    private StringBuilder tombstoneHrefs = new StringBuilder();

    /** Revision encoded into the response token; -1 = use newest revision. */
    private long responseTokenRevision = -1;

    /**
     * Wraps a {@link org.apache.jackrabbit.webdav.MultiStatus}, appends one
     * bare-404 DAV:response element per deletion tombstone and finishes with
     * the DAV:sync-token as the last child of the serialized DAV:multistatus
     * element.
     */
    private static final class TokenizedMultiStatus implements XmlSerializable {

        private static final String STATUS_404_LINE =
                "HTTP/1.1 404 Not Found";

        private final org.apache.jackrabbit.webdav.MultiStatus multistatus;
        private final String[] tombstoneHrefs;
        private final String token;

        TokenizedMultiStatus(org.apache.jackrabbit.webdav.MultiStatus multistatus,
                             CharSequence tombstoneHrefs,
                             String token) {
            this.multistatus = multistatus;
            this.tombstoneHrefs = tombstoneHrefs.length() == 0
                    ? new String[0]
                    : tombstoneHrefs.toString().split(" ");
            this.token = token;
        }

        public Element toXml(Document document) {
            Element root = multistatus.toXml(document);
            for (String href : tombstoneHrefs) {
                Element tombstone =
                        DomUtil.addChildElement(root, "response", NAMESPACE);
                DomUtil.setText(
                        DomUtil.addChildElement(tombstone, "href", NAMESPACE),
                        href);
                DomUtil.setText(
                        DomUtil.addChildElement(tombstone, "status", NAMESPACE),
                        STATUS_404_LINE);
            }
            Element tokenElement =
                DomUtil.addChildElement(root, ELEMENT_SYNC_TOKEN, NAMESPACE);
            DomUtil.setText(tokenElement, token);
            return root;
        }
    }
}
