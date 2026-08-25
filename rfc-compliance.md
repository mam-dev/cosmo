# RFC 6578 Compliance Matrix — Cosmo CalDAV Server

**Document status:** Living document · updated 2026-08-23 after TDD cycles #1–#3 (all 12 integration tests green)
**Scope:** RFC 6578 *Collection Synchronization for WebDAV* (+ adjacent 6638 row kept from original stub)
**Related docs:** `reconnaissance.md` (architecture mapping), `synccollection-testcases.md` (45-case suite, groups A–I), `synccollection-changelog-design.md` (cycle-3 design; contains the truncation-safe token correction)

---

## 1. Compliance status

| RFC | Section | Requirement | Status | Evidence / Tests |
|-----|---------|-------------|--------|------------------|
| 6578 | 3 | `DAV:sync-collection` REPORT accepted on collections | ✅ **Implemented** | `SyncCollectionReport` registered in `DavCollectionBase.REPORT_TYPES`; dispatched through generic `BaseProvider.report()` |
| 6578 | 3.1 | Request parsing: required `sync-level`, optional `sync-token`, `limit/nresults`, `prop` | ✅ **Implemented** (partial validation) | Level ≠ `"1"` → 400; missing level → 400; token grammar `urn:cosmo:sync-token:<revision>` parsed; limit ≥ 0 enforced. Foreign XML namespaces not yet rejected |
| 6578 | 3.2 | Response: 207 multistatus listing changed/new members with requested props | ✅ **Implemented** (initial + incremental) | `doQuerySelf()` branches: initial enumeration via `DavCollectionBase.getMembers()`, incremental replay via change log; per-member propstat via `MultiStatusReport.buildMultiStatusResponse()` |
| 6578 | 3.2 | Trailing `DAV:sync-token` as last child of multistatus | ✅ **Implemented** | `TokenizedMultiStatus` wrapper appends `<D:sync-token>` as last child; value `urn:cosmo:sync-token:<revision>` where revision is a **real globally monotonic change-log id** (cycle #3; was an ETag placeholder through cycle #2) |
| 6578 | 3.3 | Initial sync (empty/absent token) returns all current members | ✅ **Implemented** | Tests B1, B2 green (`SyncCollectionInitialSyncIntegrationTest`); initial round advertises current max revision |
| 6578 | 3.4 | Incremental sync: changed/new members since token | ✅ **Implemented** (cycle #3) | Persistent per-collection change log `cosmo_collection_modification`; replay rows with `id > tokenRevision` ascending; created/modified members resolved against live collection → regular 200-propstat responses. Tests C1 (add), C2 (modify), C4 (remove+add same window) green |
| 6578 | 3.5 | Deleted members reported with 404 tombstones | ✅ **Implemented** (cycle #3) | `D` rows written strictly **before** removal with member name preserved; rendered as `<D:response>` carrying only `<D:href>` + bare `<D:status>HTTP/1.1 404 Not Found</D:status>`, preserving the old href. Tests D1, C4 green |
| 6578 | 3.6 | `limit/nresults` truncation honored | ✅ **Implemented** | Positive values truncate; `nresults=0` legally yields empty listing + valid token; negatives → 400. **Truncation-safe continuation:** truncated rounds return the *last-returned* row's revision as the next token, so paged rounds can never lose changes (deviation from original design doc, pinned by test F2's three-round convergence) |
| 6578 | 3 / HTTP | Body-less REPORT rejected with 400 (never silently ignored) | ✅ **Implemented** (cycle #2) | `BaseProvider.report()` unified: missing entity body always throws `BadRequestException` |
| 6578 | 3.7 | Unknown/stale token rejected so client resyncs | ✅ **Implemented** (cycle #3) | Non-numeric, negative, unknown, and future (> current max) revisions → **403 Forbidden**, mandating full resync per spec fallback. Regression lock G proves real-token support did not weaken blanket rejection |
| 6578 | 4 | Discovery via `supported-report-set` PROPFIND | ✅ **Implemented** (free via registration) | `ReportType.register("sync-collection", DAV:, …)` + static block in `DavCollectionBase` auto-advertises |
| 6578 | 5 | Depth/scope semantics (sync-level governs scope, Depth ignored) | ✅ **Implemented** | Only immediate children enumerated; Depth header ignored |
| 6578 | — | Property selection incl. unknown props → per-member 404 propstat | 🟡 Partial | Inherited from `MultiStatusReport`; unknown-prop behavior follows existing framework defaults |
| 6638 | — | CalDAV scheduling *(pre-existing stub row)* | 🟡 Partial | Out of scope of this effort; unchanged |

---

## 2. Implementation inventory

### Cycles #1–#2 (initial sync foundation)

| File | Change type | Purpose |
|------|-------------|---------|
| `cosmo-core/src/main/java/org/unitedinternet/cosmo/dav/report/SyncCollectionReport.java` | new | Report class: parse, initial-sync query, tokenized output |
| `cosmo-core/src/main/java/org/unitedinternet/cosmo/dav/impl/DavCollectionBase.java` | +2 lines | Import + `REPORT_TYPES` registration |
| `cosmo-core/src/main/java/org/unitedinternet/cosmo/dav/provider/BaseProvider.java` | ~4 lines | Body-less REPORT answered with 400 (was silent no-op on collections) |
| `cosmo-core/src/test/unit/java/org/unitedinternet/cosmo/dav/report/SyncCollectionInitialSyncIntegrationTest.java` | new (test) | B1/B2/B4 + G-a (body-less 400), F-edge (`nresults=0`), E/H (href URI-validity lock) |

### Cycle #3 (change log, real tokens, tombstones)

| File | Change type | Purpose |
|------|-------------|---------|
| `cosmo-api/src/main/java/org/unitedinternet/cosmo/model/CollectionModification.java` | new | API interface for one change-log entry: mod type `C`/`M`/`D`, collection uid, member uid + member name (preserved for deleted-member href reconstruction), timestamp; `getId()` = globally monotonic revision used as sync-token numeric part |
| `cosmo-api/src/main/java/org/unitedinternet/cosmo/dao/ModificationDao.java` | new | API: `log(...)`, `findSince(collectionUid, sinceRevision, limit)` (exclusive lower bound, ascending, negative limit = unlimited), `currentRevision()` (0 when empty) |
| `cosmo-api/src/main/java/org/unitedinternet/cosmo/service/ContentService.java` | +2 methods | `findModificationsSince(...)`, `getModificationRevision()` |
| `cosmo-core/src/main/java/org/unitedinternet/cosmo/model/hibernate/HibCollectionModification.java` | new | `@Entity @Table(cosmo_collection_modification)` extends `BaseModelObject` — inherits `@GeneratedValue(IDENTITY)` Long id, giving the global monotonic revision for free (no custom sequence) |
| `cosmo-core/src/main/java/org/unitedinternet/cosmo/dao/hibernate/ModificationDaoImpl.java` | new | `@Repository`, `@PersistenceContext EntityManager`; JPQL `where collectionUid=? and id>? order by id asc` + `setMaxResults` |
| `cosmo-core/src/main/resources/db/cosmo-schema.sql` | appended | `CREATE TABLE cosmo_collection_modification` (AUTO_INCREMENT id, indexed `(collectionuid, id)`) |
| `cosmo-core/src/main/java/org/unitedinternet/cosmo/service/impl/StandardContentService.java` | modified | 4th constructor dependency `ModificationDao`; **11 change-log hooks**: createCollection ×2, createContent, createContentItems, updateCollection, updateContent, updateContentItems (C/M/D branch), removeItemFromCollection, removeCollection, removeContent — `D` rows written strictly **before** removal, parent set captured pre-mutation |
| `cosmo-core/src/main/java/org/unitedinternet/cosmo/dav/report/SyncCollectionReport.java` | rewritten | `parseSyncToken` (prefix + numeric + range check else 403); `doIncrementalSync` replays log rows, resolves members by uid, routes C/M → regular responses and D/unresolvable → tombstones; `TokenizedMultiStatus` renders tombstone `<D:response>`s then the sync-token as last child; **truncation-safe tokens** (last-returned row id when entries were returned) |
| `cosmo-core/src/test/unit/java/org/unitedinternet/cosmo/dao/mock/MockModificationDao.java` | new (test) | `AtomicLong` + `TreeMap` replication of the revision semantics for the mock stack |
| `cosmo-core/src/test/unit/java/org/unitedinternet/cosmo/MockHelper.java` | rewired | injects `MockModificationDao` into `StandardContentService` |
| `cosmo-core/src/test/unit/java/org/unitedinternet/cosmo/dav/report/SyncCollectionIncrementalSyncIntegrationTest.java` | new (test) | C1/C2/D1/C4/F2/G — 6 integration tests through the full `StandardRequestHandler` pipeline |
| `ContextServiceExtensionsAdviceTest` · `SecurityAdviceTest` · `StandardContentServiceTest` | +import, +ctor arg | compile compatibility with the new 4-arg `StandardContentService` constructor (all `new StandardContentService(` sites repo-wide audited) |

---

## 3. Test coverage

| Case group | Cases | State |
|------------|-------|-------|
| B1 empty token → full listing + token | 1 | ✅ automated (integration) |
| B2 absent token ≡ empty token | 1 | ✅ automated |
| B4 empty collection → zero responses + token | 1 | ✅ automated |
| G-a body-less REPORT → 400 | 1 | ✅ automated (cycle #2) |
| F-edge `nresults=0` → empty 207 + token | 1 | ✅ automated (cycle #2) |
| E/H member hrefs are valid URI references | 1 | ✅ automated (cycle #2) |
| C1 incremental: added member listed once, regular entry + fresh token | 1 | ✅ automated (cycle #3) |
| C2 incremental: modified member listed once | 1 | ✅ automated (cycle #3) |
| D1 removal → single bare-404 tombstone preserving href | 1 | ✅ automated (cycle #3) |
| C4 removal + addition in same window both reported | 1 | ✅ automated (cycle #3) |
| F2 `nresults` pagination converges without losing changes (truncation-safe tokens) | 1 | ✅ automated (cycle #3) |
| G garbage / non-numeric / negative / future tokens → 403 (regression lock) | 1 | ✅ automated (cycle #3) |
| A discovery, remaining C/D/E/F/G cases, H concurrency, I Cosmo-specific | 33 | 📋 specified in `synccollection-testcases.md`, not yet automated |

Run:

```bash
mvn -pl cosmo-core -am test \
    "-Dtest=SyncCollection*IntegrationTest" \
    -Dsurefire.failIfNoSpecifiedTests=false
```

Results: **Tests run: 12, Failures: 0, Errors: 0** (2026-08-23).

Note: the cycle-3 suite initially failed 3 assertions (C1/C2/C4) due to over-strict href comparison in the test helper — collection member hrefs legitimately end with `/` (RFC 4918 §5.2); the helper now tolerates the trailing slash before extracting the decoded last segment. Production behavior required no change.

---

## 4. Known limitations

1. **Change-log coverage gaps.** The batch variants (`createBatch`, `updateBatch`, `removeBatchContentItems`) and move/copy operations do not write log rows yet — mutations performed exclusively through those paths are invisible to incremental sync until hooks are added.
2. **No retention policy.** `cosmo_collection_modification` grows unboundedly; production deployment needs a pruning strategy. Pruning is resync-safe: pruned-away revisions simply make old tokens unknown → 403 → client repeats initial synchronization (already the mandated fallback).
3. **Tombstone href fidelity for collections.** Deleted *collection* members are reported without the RFC 4918 §5.2 trailing `/`, because the change log does not record whether a removed member was a collection. Clients should match tombstone hrefs slash-insensitively; future refinement: persist the member kind.
4. **Pre-existing `tombstones` table is unrelated.** Cosmo's `HibTombstone`/`HibItemTombstone` machinery (table `tombstones`) carries no revision column and is never populated by the mock DAO stack — it cannot back sync tokens. Superseded by the dedicated change log; documented here to prevent future confusion.
5. **Mock-environment constraint:** bare notes wrap into calendar resources whose live-property loading requires initialized stamps/triage state; tests therefore exercise sync against collections-as-members (RFC-valid, any collection may be synced).
6. **Request strictness:** foreign XML namespaces inside the report body are not yet rejected.

## 5. Roadmap to full compliance

1. ~~Change-log entity + ContentService hooks~~ ✅ cycle #3
2. ~~Real monotonic tokens + stale/garbage-token fallback~~ ✅ cycle #3
3. ~~Tombstone reporting (bare 404 responses)~~ ✅ cycle #3
4. Automate remaining groups A, E, rest of F, rest of G from the test-case catalog (~33 cases)
5. Production hardening: batch/move/copy hooks, change-log retention pruning, tombstone-href trailing-slash fidelity, namespace strictness
