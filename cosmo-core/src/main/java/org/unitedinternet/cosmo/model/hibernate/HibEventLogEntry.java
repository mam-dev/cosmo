/*
 * Copyright 2008 Open Source Applications Foundation
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * Persistent event log entry. 
 * TODO - Remove this unused entity.
 */
@Entity
@Table(name="event_log")
public class HibEventLogEntry extends BaseModelObject {

    private static final long serialVersionUID = 5943782247928186605L;

    @Column(name = "entrydate")
    @Type(type="long_timestamp")
    private Date entryDate = new Date();
    
    @Column(name = "eventtype", nullable=false, length=64)
    private String type;
    
    @Column(name = "authtype", nullable=false, length=64)
    private String authType;    
    
    @Column(name = "authid", nullable=false)
    private Long authId;
    
    @Column(name = "id1", nullable=true)
    private Long id1;
    
    @Column(name = "id2", nullable=true)
    private Long id2;
    
    @Column(name = "id3", nullable=true)
    private Long id3;
    
    @Column(name = "id4", nullable=true)
    private Long id4;
    
    @Column(name = "uid1", nullable=true, length=255)
    private String uid1;
    
    @Column(name = "uid2", nullable=true, length=255)
    private String uid2;
    
    @Column(name = "uid3", nullable=true, length=255)
    private String uid3;
    
    @Column(name = "uid4", nullable=true, length=255)
    private String uid4;
    
    @Column(name = "strval1", nullable=true, length=255)
    private String strval1;
    
    @Column(name = "strval2", nullable=true, length=255)
    private String strval2;
    
    @Column(name = "strval3", nullable=true, length=255)
    private String strval3;
    
    @Column(name = "strval4", nullable=true, length=255)
    private String strval4;

    public Date getDate() {
        return entryDate;
    }

    public void setDate(Date entryDate) {
        this.entryDate = entryDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public Long getAuthId() {
        return authId;
    }

    public void setAuthId(Long authId) {
        this.authId = authId;
    }

    public Long getId1() {
        return id1;
    }

    public void setId1(Long id1) {
        this.id1 = id1;
    }

    public Long getId2() {
        return id2;
    }

    public void setId2(Long id2) {
        this.id2 = id2;
    }

    public Long getId3() {
        return id3;
    }

    public void setId3(Long id3) {
        this.id3 = id3;
    }

    public Long getId4() {
        return id4;
    }

    public void setId4(Long id4) {
        this.id4 = id4;
    }

    public String getUid1() {
        return uid1;
    }

    public void setUid1(String uid1) {
        this.uid1 = uid1;
    }

    public String getUid2() {
        return uid2;
    }

    public void setUid2(String uid2) {
        this.uid2 = uid2;
    }

    public String getUid3() {
        return uid3;
    }

    public void setUid3(String uid3) {
        this.uid3 = uid3;
    }

    public String getUid4() {
        return uid4;
    }

    public void setUid4(String uid4) {
        this.uid4 = uid4;
    }

    public String getStrval1() {
        return strval1;
    }

    public void setStrval1(String strval1) {
        this.strval1 = strval1;
    }

    public String getStrval2() {
        return strval2;
    }

    public void setStrval2(String strval2) {
        this.strval2 = strval2;
    }

    public String getStrval3() {
        return strval3;
    }

    public void setStrval3(String strval3) {
        this.strval3 = strval3;
    }

    public String getStrval4() {
        return strval4;
    }

    public void setStrval4(String strval4) {
        this.strval4 = strval4;
    }
    
    
}
