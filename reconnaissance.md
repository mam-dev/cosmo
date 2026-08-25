# Reconnaissance: RFC 6578 (Collection Synchronization for WebDAV) vs. Cosmo

> **Scope:** Read-only analysis of the existing codebase at `/cosmo` against the requirements of
> RFC 6578 — *Collection Synchronization for WebDAV*.
> **No source code was modified** in producing this document.

---

## 1. RFC 6578 in a nutshell

RFC 6578 defines an incremental synchronization protocol for WebDAV collections, built on one new
REPORT type:

| # | Requirement | Key normative element |
|---|---|---|
| R1 | `DAV:sync-collection` REPORT on collections | Request body: `sync-token`, `sync-level` (`"1"` MUST be supported; `infinity` MAY), optional `limit/nresults`, `prop` |
| R2 | Multistatus response listing **changed/new members** with requested properties | `207 MultiStatus` + `DAV:response` elements |
| R3 | Multistatus lists **deleted members** with status 404 | Requires server-side tombstones |
| R4 | Response ends with a fresh **`DAV:sync-token`** for the next round | Persistent, monotonically-advancing per-collection change state |
| R5 | Initial sync via empty/absent token returns all current members | Full-list capability |
| R6 | Discovery via `DAV:supported-report-set` PROPFIND | Report advertisement |
| R7 | Honor `limit` / truncation semantics | Partial-result handling |
| R8 | Reject stale/invalid tokens so clients can perform a full resync | Error mapping |

The hard part is **R3 + R4**: the server must retain a per-collection change log or revision
counter plus deletion tombstones.

---

## 2. Relevant architecture in Cosmo

```
cosmo-core/src/main/java/org/unitedinternet/cosmo/
├── dav/servlet/StandardRequestHandler.java   ← HTTP method dispatcher
├── dav/provider/                              ← method execution (BaseProvider et al.)
├── dav/report/                                ← report framework (ReportBase, MultiStatusReport)
├── dav/caldav/report/                         ← CalDAV reports (Query, Multiget, FreeBusy)
├── dav/acl/report/                            ← ACL reports
├── dav/impl/                                  ← resource adapters (DavCollectionBase…)
├── dav/caldav/property/GetCTag.java           ← CS:getctag (closest thing to sync state)
├── model/hibernate/HibAuditableObject.java    ← persisted ETag machinery
└── dao/hibernate/ContentDaoImpl.java          ← persistence + timestamp bumping
```

The DAV layer delegates persistence to `ContentService` / DAOs over Hibernate entities
(`HibItem`, `HibCollectionItem`, …) in `cosmo-core/src/main/java/org/unitedinternet/cosmo/model/hibernate/`.

---

## 3. Class inventory

### 3a) WebDAV REPORT handling

| Class / Method | Role |
|---|---|
| `dav.servlet.StandardRequestHandler#process()` | Dispatches `REPORT` → `provider.report(request, response, resource)` |
| `dav.provider.BaseProvider#report()` | Existence check, parses `ReportInfo`, ACL check (`checkReportAccess`), delegates to `((ReportBase) resource.getReport(info)).run(response)` |
| `dav.WebDavResource#getReport(ReportInfo)` | Interface hook — each resource declares which reports it supports |
| `dav.report.ReportBase` | Abstract framework: `parseReport()` → `runQuery()` (self / children / descendants by depth) → `output()` |
| `dav.report.MultiStatusReport` | Builds and sends the 207 multistatus; base class for all property-bearing reports |
| Registered reports | CalDAV: `QueryReport`, `MultigetReport`, `FreeBusyReport`; ACL: `PrincipalMatchReport`, `PrincipalPropertySearchReport`, `PrincipalSearchPropertySetReport` |

Reports are registered as static Jackrabbit `ReportType`s and listed in
`DavCollectionBase.REPORT_TYPES`. There is **no** sync-collection report.

### 3b) Collection resources

| Class | Role |
|---|---|
| `dav.impl.DavCollectionBase` | Generic collection adapter over `CollectionItem`: `getMembers()`, `getCollectionMembers()`, `addContent()`, `addCollection()`, `removeMember()`, `findMember()`; holds static `REPORT_TYPES` |
| `dav.impl.DavCalendarCollection` | Calendar collection: CalDAV live properties incl. `CS:getctag`; `findMembers(CalendarFilter)`; overrides `saveContent/removeContent` |
| `dav.impl.DavHomeCollection`, `DavInboxCollection`, `DavOutboxCollection` | Special-purpose collections |
| Interfaces `DavCollection`, `DavItemCollection`; providers `CollectionProvider`, `CalendarCollectionProvider`, `HomeCollectionProvider`, … | Provider-per-resource-type pattern selected in `StandardRequestHandler#createProvider()` |

### 3c) Resource creation / deletion

| Operation | Chain of classes |
|---|---|
| MKCOL | `StandardResourceFactory#resolve()` instantiates `DavCollectionBase` → `CollectionProvider#mkcol()` → `DavCollectionBase#addCollection()/saveSubcollection()` → `ContentService#createCollection()` |
| MKCALENDAR | `StandardResourceFactory` → `DavCalendarCollection` → `CalendarCollectionProvider#mkcalendar()` → same service path |
| PUT (new content) | `StandardResourceFactory#resolve()` creates `DavEvent` inside calendar collections (else `FileProvider`) → `put()` → `DavCollectionBase#addContent()/saveContent()` → `ContentService#createContent()/createContentItems()` |
| DELETE | `BaseProvider#delete()` (Depth infinity enforced) → `resource.getParent().removeMember(resource)` → `DavCollectionBase#removeMember()` → `ContentService#removeItemFromCollection()` / `removeCollection()`; calendar variant `DavCalendarCollection#removeContent()` |
| COPY / MOVE | `BaseProvider#copy()/move()`; destination overwrite via `removeMember()` |

### 3d) Synchronization-related state

| Class | State provided |
|---|---|
| `model.hibernate.HibAuditableObject` | Persisted `etag` field; `updateTimestamp()` bumps `modifiedDate` |
| `model.hibernate.HibItem#calculateEntityTag()` | ETag = Base64-SHA1(`uid + ":" + modifiedDate`) |
| `model.hibernate.AuditableObjectInterceptor` | Recomputes ETag automatically on Hibernate flush after any modification |
| `dav.impl.DavCalendarCollection#loadLiveProperties()` | Exposes `CS:getctag` = `item.getEntityTag()` — the collection's own ETag doubles as a CTag |
| `dav.caldav.property.GetCTag` + `CaldavConstants.GET_CTAG` | Property plumbing (protected, cannot be PROPPATCHed) |
| `dao.hibernate.ContentDaoImpl` (~lines 198, 570) | Calls `collection.updateTimestamp()` when content is created/updated → parent collection ETag/CTag changes on membership changes ✅ |
| `dav.property.Etag` + `DavItemResourceBase#getETag()` | Per-member `DAV:getetag` exposure |

❌ Nothing beyond the above exists: no change log, no revision counter, no tombstones,
no sync token anywhere in the codebase. The only "SyncToken" occurrence is `X-MorseCode-SyncToken`
in `cosmo-core/src/main/perl/Cosmo/MC.pm`, a Perl test-harness header unrelated to WebDAV sync.

---

## 4. Requirement ↔ implementation mapping

| RFC 6578 requirement | Existing implementation anchor | Verdict |
|---|---|---|
| **R1a** `DAV:sync-collection` REPORT exists | Report dispatch chain is fully generic (`StandardRequestHandler` → `BaseProvider.report` → `getReport`) and extensible via Jackrabbit `ReportType.register(...)`; but no `SyncCollectionReport` class exists and none is registered in `DavCollectionBase.REPORT_TYPES` | 🔴 Missing (clean extension point available) |
| **R1b** `sync-level=1` | Equivalent depth-1 member enumeration already exists: `DavCollectionBase#getMembers()` / `getCollectionMembers()` | 🟡 Building block present |
| **R2** Changed-member responses w/ props | `MultiStatusReport#buildMultiStatusResponse()` produces exactly this shape (href + requested props) | 🟡 Reusable as-is |
| **R3** Deleted members as 404 responses | DELETE path (`BaseProvider.delete` → `removeMember` → `ContentService.removeItemFromCollection`) discards the member entirely; no tombstone table | 🔴 Missing — hardest gap |
| **R4** `DAV:sync-token` in response | No token concept at model level. Nearest analogs: `HibItem#calculateEntityTag()` (uid+modifiedDate hash), `AuditableObjectInterceptor`, `CS:getctag` = collection entity tag | 🔴 Missing. CTag answers *"did anything change?"*, not *"what changed?"* — insufficient for RFC 6578 |
| **R5** Initial full sync | Full member enumeration works today (PROPFIND Depth-1 equivalent); needs "empty token ⇒ return everything" logic | 🟡 Feasible with existing DAO queries |
| **R6** Discovery via `supported-report-set` | Fully implemented: `dav.property.SupportedReportSet`, `ExtendedDavConstants.SUPPORTEDREPORTSET`; registering a new `ReportType` auto-advertises it | 🟢 Ready |
| **R7** `limit` / truncation | No result-limiting support in any existing report | 🔴 Missing |
| **R8** Invalid token rejection | Exception infrastructure exists (`BadRequestException`, `ForbiddenException`, `StandardRequestHandler.ExceptionMapper`) | 🟡 Trivial once tokens exist |
| Compliance advertisement | `WebDavResource.COMPLIANCE_CLASS = "1, 3, access-control, calendar-access, ticket"` — RFC 6578 defines no DAV-header token (discovery is via R6), so nothing to add | 🟢 N/A / fine |

Legend: 🟢 ready · 🟡 partial building blocks · 🔴 missing

---

## 5. Bottom line

- Cosmo has a **complete, cleanly layered REPORT framework**
  (dispatch → ACL check → parse → query → multistatus output) and full collection CRUD.
  Implementing RFC 6578 means adding a `SyncCollectionReport` and registering it;
  discovery (R6) comes free via `SupportedReportSet`.
- The decisive gap is **state, not plumbing**: there is no persistent change log, revision counter
  (`sync-token` source), or tombstone store (R3/R4). The existing ETag/CTag mechanism
  (`HibItem#calculateEntityTag`, `AuditableObjectInterceptor`,
  `ContentDaoImpl#updateTimestamp()` on parents, exposed as `CS:getctag`) proves that
  collection-change *detection* works, but it yields a single opaque value — it cannot enumerate
  *which* members changed or were deleted since a given point.
- Practical implication: an RFC 6578 implementation would require
  1. a new persistent entity holding ordered per-collection change records **including deletions**,
  2. hooks in the `ContentService` / DAO create/delete paths to append to it, and
  3. a custom report subclass — note that `MultiStatusReport#output()` sends a plain 207 multistatus,
     whereas RFC 6578 requires appending `DAV:sync-token` to the multistatus body, so the new report
     needs its own `output()` override rather than reusing the stock one.
