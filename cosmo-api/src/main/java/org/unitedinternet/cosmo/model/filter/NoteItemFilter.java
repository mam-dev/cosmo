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
package org.unitedinternet.cosmo.model.filter;

import org.unitedinternet.cosmo.model.NoteItem;

/**
 * Adds NoteItem specific criteria to ItemFilter.
 * Matches only NoteItem instances.
 */
public class NoteItemFilter extends ContentItemFilter {
    private FilterCriteria icalUid = null;
    private FilterCriteria body = null;
    private FilterCriteria reminderTime = null;
    private FilterCriteria modifiedSince = null;
    
    private Boolean hasModifications = null;
    private Boolean isModification = null;
    
    private NoteItem masterNoteItem = null;
    
    public NoteItemFilter() {}
    
    /**
     * Match notes with a body that matches a given String.
     * @param body body string to match
     */
    public void setBody(FilterCriteria body) {
        this.body = body;
    }

    public FilterCriteria getBody() {
        return body;
    }

    public FilterCriteria getIcalUid() {
        return icalUid;
    }
    
    /**
     * Match notes with an specific icalUid
     * @param icalUid
     */
    public void setIcalUid(FilterCriteria icalUid) {
        this.icalUid = icalUid;
    }

    public Boolean getHasModifications() {
        return hasModifications;
    }

    /**
     * Matches notes with or without modifications.
     * @param hasModifications if present, matches notes with or without
     *                         modifications
     */
    public void setHasModifications(Boolean hasModifications) {
        this.hasModifications = hasModifications;
    }

    public Boolean getIsModification() {
        return isModification;
    }

    /**
     * Matches notes that are or are not a modification
     * @param isModification if present, matches notes that are or are not
     *                        a modification
     */
    public void setIsModification(Boolean isModification) {
        this.isModification = isModification;
    }

    public NoteItem getMasterNoteItem() {
        return masterNoteItem;
    }

    /**
     * Matches the specified NoteItem and any modifications to that NoteItem
     * @param masterNoteItem
     */
    public void setMasterNoteItem(NoteItem masterNoteItem) {
        this.masterNoteItem = masterNoteItem;
    }

    public FilterCriteria getReminderTime() {
        return reminderTime;
    }

    /**
     * Matches notes with reminderTime matching the specified criteria.
     * @param reminderTime
     */
    public void setReminderTime(FilterCriteria reminderTime) {
        this.reminderTime = reminderTime;
    }

    public FilterCriteria getModifiedSince() {
        return modifiedSince;
    }

    public void setModifiedSince(FilterCriteria modifiedSince) {
        this.modifiedSince = modifiedSince;
    }
}
