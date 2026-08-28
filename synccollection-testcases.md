# DAV:sync-collection (RFC 6578) — Required HTTP/XML Behavior & Test Cases

> **Status:** Partially implemented & automated — see "Implementation status" below.
> Companion document to `/cosmo/reconnaissance.md`.
>
> **Implementation status (2026-08-28):** automated & green on branch
> `feature/rfc-6578`: A1 (supported-report-set discovery), A2 (+ A2b home
> collection OPTIONS regression) (`SyncCollectionDiscoveryIntegrationTest`);
> B1, B2, B4 (initial sync), G4 (missing body → 400), nresults=0 truncation
> edge, href URI-validity check
> (`SyncCollectionInitialSyncIntegrationTest`); C1, C2, D1, C4 (as
> remove+add surrogate), F2 (pagination convergence), G token-rejection
> regression lock (`SyncCollectionIncrementalSyncIntegrationTest`);
> E1, E2, E3 (+ empty-`<D:prop/>` form), E4 (`<D:allprop/>` is ignored and
> treated as empty property selection → bare href-only multistatus, 207)
> (`SyncCollectionPropertySelectionIntegrationTest`);
> F1 (nresults=4 → pages of 4+4+2, token advances per page, no dupes/loss over
> 10 changes), F3 (nresults=1000 > 10 pending → single round drains all 10),
> F5 (`nresults=-5` and `nresults=abc` → 400 Bad Request)
> (`SyncCollectionLimitIntegrationTest`).
> All remaining cases are specification for future automation.
>
> **Observed property-selection behavior (locked by the E tests,
> 2026-08-28):**
> - requested live properties share the 200 propstat (per RFC 4918
>   § 14.24 propstat semantics);
> - an unsupported property surfaces in its own 404 propstat and is NOT
>   listed in the 200 propstat;
> - an empty or missing `DAV:prop` selection yields a 207 with one
>   `DAV:response` per member carrying only `DAV:href` + status 200 (no
>   propstat), plus a minted `DAV:sync-token`;
> - out-of-DTD children such as `DAV:allprop` are silently ignored (no
>   400, no 5xx).
> Behavior is derived from RFC 6578 and cross-checked against Cosmo's existing
> REPORT pipeline (`StandardRequestHandler` → `BaseProvider#report()` →
> `WebDavResource#getReport()` → `ReportBase`/`MultiStatusReport`).

---

## Part 1 — Required Request Behavior

### 1.1 HTTP framing

| Aspect | Requirement |
|---|---|
| Method | `REPORT` |
| Request-URI | MUST target an existing **collection** (e.g., a calendar collection). |
| `Content-Type` | `application/xml` (or `text/xml`) with a parseable XML entity body. A body is **mandatory**. |
| `Depth` header | Not used by RFC 6578; server MUST ignore it (scope comes from `DAV:sync-level`). |
| Authentication/ACL | Standard auth; requires `DAV:read` privilege on the collection (Cosmo enforces this in `BaseProvider#checkReportAccess()`). |

### 1.2 XML request body

```xml
<D:sync-collection xmlns:D="DAV:">
  <D:sync-token>https://cosmo.example/ns/sync/1234</D:sync-token>
  <D:sync-level>1</D:sync-level>
  <D:limit>
    <D:nresults>100</D:nresults>
  </D:limit>
  <D:prop>
    <D:getetag/>
    <X:custom-prop xmlns:X="urn:example"/>
  </D:prop>
</D:sync-collection>
```

| Element | Cardinality | Semantics |
|---|---|---|
| `DAV:sync-collection` | 1, root, `DAV:` namespace | Identifies the report type. |
| `DAV:sync-token` | 0–1 in request | **Empty element or absent ⇒ initial sync** (return all current members). Otherwise an opaque token previously returned by the server. Server MUST treat it as opaque (no client-side parsing assumptions). |
| `DAV:sync-level` | 1, REQUIRED | `"1"` = immediate children only (**MUST** be supported). `"infinity"` = whole subtree (**MAY** be supported); if unsupported the server rejects the request. |
| `DAV:limit` / `DAV:nresults` | 0–1 | Client hint: maximum number of `DAV:response` elements to return. Server MAY honor it (truncation) or ignore it. `nresults` must be a positive integer. |
| `DAV:prop` | 0–1 | Property selection following PROPFIND rules (named properties). Pseudo-properties such as `CALDAV:calendar-data` follow CalDAV report semantics (relevant for Cosmo's `CaldavMultiStatusReport` pattern). |

### 1.3 Cosmo touchpoints for parsing

- Body arrives via `StandardDavRequest#getReportInfo()` (Jackrabbit `ReportInfo`) — a
  `SyncCollectionReport` would extract token/level/limit from it.
- Registration point: add a `ReportType` and include it in `DavCollectionBase.REPORT_TYPES`
  (auto-advertised through `SupportedReportSet`).

---

## Part 2 — Required Response Behavior

### 2.1 Success — `207 MultiStatus`

```xml
HTTP/1.1 207 Multi-Status
Content-Type: application/xml; charset=utf-8

<D:multistatus xmlns:D="DAV:">
  <!-- changed/new member -->
  <D:response>
    <D:href>/dav/user/calendars/cal/event-1.ics</D:href>
    <D:propstat>
      <D:prop>
        <D:getetag>&quot;e1a2b3&quot;</D:getetag>
      </D:prop>
      <D:status>HTTP/1.1 200 OK</D:status>
    </D:propstat>
  </D:response>
  <!-- deleted member (tombstone): status form, NO propstat -->
  <D:response>
    <D:href>/dav/user/calendars/cal/gone.ics</D:href>
    <D:status>HTTP/1.1 404 Not Found</D:status>
  </D:response>
  <!-- requested property missing on one member -->
  <D:response>
    <D:href>/dav/user/calendars/cal/event-2.ics</D:href>
    <D:propstat>
      <D:prop><D:getetag>&quot;f4c5d6&quot;</D:getetag></D:prop>
      <D:status>HTTP/1.1 200 OK</D:status>
    </D:propstat>
    <D:propstat>
      <D:prop><X:custom-prop xmlns:X="urn:example"/></D:prop>
      <D:status>HTTP/1.1 404 Not Found</D:status>
    </D:propstat>
  </D:response>
  <!-- MUST be the LAST child of multistatus -->
  <D:sync-token>https://cosmo.example/ns/sync/1240</D:sync-token>
</D:multistatus>
```

Normative response rules:

1. One `DAV:response` per **changed or newly created** member, with selected properties
   returned via `DAV:propstat` (200), exactly like PROPFIND.
2. One `DAV:response` per **deleted** member containing only `DAV:status: 404` (no propstat),
   using the member's former href.
3. Exactly one `DAV:sync-token`, placed as the **last** child of `DAV:multistatus`,
   reflecting the state *after* the last change included in this response.
4. Initial sync: all current members appear as changed entries; no 404 entries.
5. Truncation: if `nresults` was honored and more changes remain, return at most that many
   responses plus a token corresponding to the last returned change; clients re-query until
   a non-truncated response is received.
6. Properties not present on a member → separate `propstat` with 404 for those properties;
   never fails the whole report.
7. Hrefs use the same URI style as PROPFIND responses and are URL-escaped.
8. A rename/move within scope is reported as a **delete at the old href + create at the new href**.
9. Changes to the collection resource itself do NOT produce an entry for the collection URI.
10. Error body uses WebDAV `DAV:error` convention where applicable.

### 2.2 Error responses

| Condition | Expected response |
|---|---|
| Report not supported on target resource | `403 Forbidden`, `DAV:error` with `DAV:supported-report` |
| Target does not exist | `404 Not Found` |
| Target exists but is not a collection | `403 Forbidden` |
| Missing / malformed XML body | `400 Bad Request` |
| Missing `DAV:sync-level` | `400 Bad Request` |
| Unsupported `DAV:sync-level` value | `400 Bad Request` |
| Malformed `DAV:nresults` (non-numeric, ≤ 0) | `400 Bad Request` |
| Unknown/expired/stale `sync-token` (server no longer holds history) | `403 Forbidden` — client must fall back to initial sync |
| Token belonging to another collection/principal | treated as unknown token → `403 Forbidden` |
| Authenticated but no read privilege | Cosmo convention (`checkReportAccess`): `404 Not Found` (existence hidden); generic DAV would allow 403 |

Cosmo-specific deviation to watch: today `BaseProvider#report()` silently returns (empty 200-ish)
when the body can't be parsed on a collection — the new report path must instead produce a 400.

---

## Part 3 — Test Cases

Conventions: base URI `C = /dav/{user}/calendars/{cal}`; all bodies use `xmlns:D="DAV:"`.

### Group A — Discovery

| ID | Scenario | Steps | Expected |
|---|---|---|---|
| A1 | supported-report-set advertises sync-collection | `PROPFIND C` Depth 0, prop `DAV:supported-report-set` | 207; response contains `<D:supported-report><D:report><D:sync-collection/>` |
| A2 | OPTIONS unchanged | `OPTIONS C` | 200; `Allow` includes REPORT; `DAV` header lists existing classes only (no new compliance token required) |

### Group B — Initial synchronization

| ID | Scenario | Steps | Expected |
|---|---|---|---|
| B1 | Empty sync-token | Create cal with members M1..M3; `REPORT C` with `<D:sync-collection><D:sync-token/><D:sync-level>1</D:sync-level><D:prop><D:getetag/></D:prop></D:sync-collection>` | 207; exactly 3 `DAV:response`s, all propstat-200 with etags; no 404 entries; trailing `DAV:sync-token` present and non-empty |
| B2 | Absent sync-token element | Same request without `<D:sync-token/>` | Same as B1 (absent ≡ empty) |
| B3 | Initial sync returns only immediate children | Sub-collection S inside C with member SM1 | 207 lists M* and S, but **not** SM1 (level "1") |
| B4 | Empty collection initial sync | `REPORT` on empty collection | 207; zero `DAV:response`s; valid `DAV:sync-token` still present |
| B5 | Initial sync with calendar-data pseudo-prop | Request `CALDAV:calendar-data` + `DAV:getetag` | Each response carries both props (200); iCalendar data well-formed (mirrors `MultigetReport` behavior) |

### Group C — Incremental synchronization

| ID | Scenario | Steps | Expected |
|---|---|---|---|
| C1 | No changes since token | Immediately repeat REPORT with T0 from B1 | 207; zero `DAV:response`s; `DAV:sync-token` ≥ T0 (may equal) |
| C2 | New member since token | Add M4; REPORT with T0 | 207; exactly 1 response for M4 with fresh etag |
| C3 | Modified member since token | PUT updated content over M1 (new ETag); REPORT with T0 | 207; exactly 1 response for M1; etag differs from B1's |
| C4 | Mixed changes | Add M4, modify M1, delete M2 (see D1); REPORT with T0 | 207; M4 & M1 as propstat-200 entries, M2 as 404 entry; 3 responses total |
| C5 | Rename reported as delete+create | MOVE M3 → M5 within C; REPORT with T0 | 207; M3 appears as 404 tombstone AND M5 appears as changed entry |
| C6 | Member property-only change counts | PROPPATCH live prop (e.g., displayname) on M1; REPORT with T0 | M1 listed as changed |
| C7 | Collection-level changes don't leak | PROPPATCH on collection itself; REPORT with T0 | No `DAV:response` whose href == C |

### Group D — Deletions & tombstones

| ID | Scenario | Steps | Expected |
|---|---|---|---|
| D1 | Deleted member reported once | Delete M2 after T0; REPORT with T0 | 207; one 404-status response for M2; no propstat inside it |
| D2 | Tombstone consumed by later token | REPORT with token T1 taken *after* D1's response | M2 no longer listed (history advanced past deletion) |
| D3 | Deleted-then-recreated same name | DELETE M2; PUT new M2; REPORT with T0 | M2 listed **once** as changed entry (not as 404), with new etag |
| D4 | Tombstone retention window | Configure/observe server history limit; REPORT with token older than retention | 403 invalid-token (client resyncs full) |

### Group E — Property selection

| ID | Scenario | Steps | Expected |
|---|---|---|---|
| E1 | Multiple live props | Request `DAV:getetag` + `DAV:getlastmodified` | Both returned per member in one propstat-200 |
| E2 | Unknown/dead property | Request `urn:example:nope` | Per-member second propstat with 404 for that prop; other props still 200; report overall succeeds |
| E3 | No `DAV:prop` element | Omit prop element | 207 succeeds; each response contains at least href (+ server-chosen default, e.g., getetag) |
| E4 | Propfind-style allprop/propname | Send `<D:allprop/>` variant | Either honored like PROPFIND or rejected 400 — behavior must be consistent & documented |

### Group F — Limit & truncation

| ID | Scenario | Steps | Expected |
|---|---|---|---|
| F1 | nresults honored | Make 10 changes; request `nresults=4` with old token | 207; exactly 4 responses; token reflects state after 4th change; remaining 6 retrievable by re-querying with returned token |
| F2 | Pagination loop converges | Repeat F1 query until done | Final page returns fewer-than-limit (or 0 extra) responses and final token; union of pages == full change set; no duplicates across pages |
| F3 | nresults larger than pending changes | `nresults=1000`, 10 changes | All 10 returned; token = latest; no truncation marker needed |
| F4 | Server may ignore limit | `nresults=1`, 10 changes (server policy: ignore limits) | Acceptable per RFC: all 10 returned — test asserts documented server behavior either way |
| F5 | Invalid nresults | `nresults=-5`, `abc` | 400 Bad Request |

### Group G — Errors / negative

| ID | Scenario | Steps | Expected |
|---|---|---|---|
| G1 | Unknown report type | REPORT body root `<D:foo/>` | 403 with `DAV:error` `DAV:supported-report` |
| G2 | Nonexistent collection | REPORT against `/dav/u/calendars/nope` | 404 |
| G3 | Non-collection target | REPORT against a member resource (event.ics) | 403 |
| G4 | Missing body | `REPORT C` with Content-Length 0 | **400** (note: current `BaseProvider#report()` returns silently for collections — must be fixed in implementation, covered as regression check) |
| G5 | Malformed XML | Body `<D:sync-collection><oops` | 400 |
| G6 | Missing sync-level | Body without `DAV:sync-level` | 400 |
| G7 | Unknown sync-level value | `<D:sync-level>2</D:sync-level>` | 400 |
| G8 | infinity when unsupported | `<D:sync-level>infinity</D:sync-level>` on level-1-only build | 400 (if server opts into supporting infinity: 200 with full-subtree results instead) |
| G9 | Stale/expired token | Use token older than retained history | 403 Forbidden; client expected to re-run initial sync (B1) afterwards and succeed |
| G10 | Foreign token | Token minted from collection X used against collection Y | 403 Forbidden |
| G11 | Garbage token string | `<D:sync-token>banana</D:sync-token>` | 403 Forbidden (opaque ⇒ any unparseable/unrecognized value is "invalid", never 500) |
| G12 | Unauthenticated | REPORT with no credentials | 401 (existing auth filter behavior) |
| G13 | No read privilege | Authenticated user w/o DAV:read on C | 404 per Cosmo's existence-hiding convention (`checkReportAccess`) |

### Group H — Concurrency & robustness

| ID | Scenario | Steps | Expected |
|---|---|---|---|
| H1 | Change during pagination | Modify M1 between F1 page 1 and page 2 | Page 2 either includes the newer change or a subsequent round catches it; final convergence guaranteed; tokens always monotonic |
| H2 | Concurrent deletes of tombstoned item | Item deleted twice / deleted while being paginated | No duplicate 404 entries within one response; no 500 |
| H3 | URL escaping | Member names with spaces/UTF-8 (`my event.ics`, `événement.ics`) | Hrefs percent-encoded consistently with PROPFIND output |
| H4 | Repeated identical requests | Replay exact request+token twice | Idempotent: same result set (or empty if consumed by token advance per server policy); never 5xx |
| H5 | Large collection sanity | ~1000 members initial sync | Completes 200/207 within configured timeout; memory bounded (streaming acceptable) |

### Group I — Cosmo integration specifics

| ID | Scenario | Steps | Expected |
|---|---|---|---|
| I1 | ACL enforcement path | Ticket-authenticated user WITH read ticket | Report executes (exercises `checkReportAccess` ticket branch) |
| I2 | External/subscription collections | Run REPORT against external collection (`ContentDaoExternal`-backed) | Defined behavior: either works over delegated items or clean 403 — must not 500 (these bypass Hibernate change tracking) |
| I3 | Multistatus ordering invariant | Any successful case | `DAV:sync-token` is last child; all `DAV:response`s precede it (requires custom `output()` override, since stock `MultiStatusReport#output()` cannot append it) |
| I4 | getctag consistency (advisory) | After any sync round | `CS:getctag` (collection entity tag) changed iff there were ≥1 reported changes — keeps CTag-based clients coherent |

---

## Part 4 — Traceability matrix (test groups ↔ RFC requirements)

| RFC req. | Covered by |
|---|---|
| R1 request format | B1–B5, E*, F*, G4–G8 |
| R2 changed-member multistatus | C1–C7, E1–E3 |
| R3 deletions/tombstones | D1–D3, C4–C5 |
| R4 sync-token lifecycle | B1, C1, F1–F2, G9–G11 |
| R5 initial sync | B1–B4 |
| R6 discovery | A1–A2 |
| R7 limit/truncation | F1–F5 |
| R8 invalid-token handling | G9–G11 |
| Cosmo-specific risk areas | G4 (silent-null quirk), I2 (external collections), I3 (multistatus writer) |
