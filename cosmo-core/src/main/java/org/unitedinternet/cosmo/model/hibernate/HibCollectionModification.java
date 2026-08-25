/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model.hibernate;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.unitedinternet.cosmo.model.CollectionModification;

/**
 * <p>
 * Hibernate persistent collection change-log record. The inherited id is a
 * global AUTO_INCREMENT value whose monotonic sequence backs RFC 6578 sync
 * tokens.
 * </p>
 */
@Entity
@Table(name = "cosmo_collection_modification")
public class HibCollectionModification extends BaseModelObject
        implements CollectionModification {

    private static final long serialVersionUID = -4812590347712958138L;

    @Column(name = "collectionuid", nullable = false, length = 255)
    private String collectionUid;

    @Column(name = "memberuid", nullable = false, length = 255)
    private String memberUid;

    @Column(name = "membername", nullable = false, length = 255)
    private String memberName;

    @Column(name = "modtype", nullable = false, length = 1)
    private char modType;

    @Column(name = "moddate", nullable = false)
    private Long timestamp;

    /**
     * Constructor.
     */
    public HibCollectionModification() {
    }

    /**
     * Constructor.
     *
     * @param collectionUid uid of the collection whose membership changed
     * @param memberUid uid of the affected member item
     * @param memberName name of the affected member at change time
     * @param modType C/M/D change type
     */
    public HibCollectionModification(String collectionUid, String memberUid,
                                     String memberName, char modType) {
        this.collectionUid = collectionUid;
        this.memberUid = memberUid;
        this.memberName = memberName;
        this.modType = modType;
        this.timestamp = Long.valueOf(new Date().getTime());
    }

    public String getCollectionUid() {
        return collectionUid;
    }

    public void setCollectionUid(String collectionUid) {
        this.collectionUid = collectionUid;
    }

    public String getMemberUid() {
        return memberUid;
    }

    public void setMemberUid(String memberUid) {
        this.memberUid = memberUid;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public char getModType() {
        return modType;
    }

    public void setModType(char modType) {
        this.modType = modType;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
