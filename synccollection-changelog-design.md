# DAV:sync-collection Change Log — Design (RFC 6578 §3.4/§3.5)

## Goal
Replace the placeholder ETag token (`urn:cosmo:sync-token:<etag>`) with real
monotonic tokens backed by a persistent per-collection change log, enabling
incremental sync (C-group) and deletion tombstones (D-group).

## 1. Entity: `HibCollectionModification` (cosmo-core, model.hibernate)
| Field | Type | Purpose |
|---|---|---|
| id | long, PK, global sequence | **monotonic revision → sync-token value** |
| collectionUid | String (indexed) | owning collection |
| memberUid | String | changed member |
| memberName | String | href reconstruction (survives delete) |
| modType | char | `C`reated / `M`odified / `D`eleted (tombstone) |
| timestamp | Date | ordering tiebreaker |

Table: `cosmo_collection_modification`; index on `(collectionUid, id)`.

## 2. Write path (hooks)
- `ContentServiceImpl.createContent/createCollection` → append `C`
- update paths (saveContent/updateProperties) → append `M`
- `removeItemFromCollection/removeCollection` → append `D` **before** removal (name+uid kept)
- Batch: one row per affected parent collection.
- Retention: keep last N (e.g., 10k) rows per collection; prune on write.

## 3. Read path (SyncCollectionReport)
- Parse non-empty client token `urn:cosmo:sync-token:<id>`:
  - unknown/non-numeric/greater than current max → 403 (client resyncs)
- Query rows `WHERE collectionUid=? AND id>? ORDER BY id`, apply `nresults`.
- `C`/`M` → normal `DAV:response` (member must still exist); `D` → bare
  `<status>404</status>` response (tombstone).
- Response token = current global max id at query time.

## 4. DAO
`ModificationDao { void log(...); List<HibCollectionModification> findSince(uid, long rev, int limit); long currentRevision(); }`
Hibernate impl; wired via Spring like ContentDao.

## 5. Token semantics
- Format: `urn:cosmo:sync-token:<id>` (opaque to clients).
- Initial sync (empty) returns current max id as token.
- Stale vs invalid both → 403 per RFC 6578 §3.7 fallback rule.

## 6. Test plan (RED first)
- C1 add-member after initial sync → incremental round lists only it
- C2 modify-member → listed as updated
- C3 remove-member → 404 tombstone entry
- C4 rename = D(old-href) + C(new-href)
- F2 limit/pagination convergence across rounds
- G: garbage/future tokens → 403

## Implementation order
1. Entity + DAO + wiring (+ schema via hbm/migrations as repo convention dictates)
2. Service-layer hooks
3. Report read-path rewrite (keep initial-sync behavior identical)
4. New RED tests above → green
