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
package org.unitedinternet.cosmo.model;

/**
 * <p>
 * A single entry in a collection's persistent change log. Each record captures
 * one membership-affecting change ({@link #MOD_TYPE_CREATED creation},
 * {@link #MOD_TYPE_MODIFIED modification} or {@link #MOD_TYPE_DELETED deletion}
 * of a member) together with enough information to reconstruct the member's
 * DAV:href even after deletion.
 * </p>
 *
 * <p>
 * The {@link #getId() id} is a globally monotonic server-assigned revision.
 * It is the value encoded in RFC 6578 <code>DAV:sync-token</code>s issued by
 * Cosmo ("urn:cosmo:sync-token:&lt;id&gt;") and orders all changes made to a
 * collection.
 * </p>
 */
public interface CollectionModification {

    /** Change type: the member was created in the collection. */
    char MOD_TYPE_CREATED = 'C';

    /** Change type: an existing member of the collection changed. */
    char MOD_TYPE_MODIFIED = 'M';

    /** Change type (tombstone): the member was removed from the collection. */
    char MOD_TYPE_DELETED = 'D';

    /**
     * Globally monotonic revision assigned by the server. Doubles as the
     * numeric part of sync tokens and as the ordering key of the log.
     */
    Long getId();

    /** Uid of the collection whose membership changed. */
    String getCollectionUid();

    /** Uid of the affected member item. */
    String getMemberUid();

    /**
     * Name of the affected member item at the time of the change. Kept for
     * deleted members so their old DAV:href can still be reported as a
     * tombstone.
     */
    String getMemberName();

    /** One of {@link #MOD_TYPE_CREATED}, {@link #MOD_TYPE_MODIFIED}, {@link #MOD_TYPE_DELETED}. */
    char getModType();

    /** Wall-clock timestamp of the change (informational; ordering uses the id). */
    Long getTimestamp();
}
