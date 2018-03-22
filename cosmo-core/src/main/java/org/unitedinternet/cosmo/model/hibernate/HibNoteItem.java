/*
 * Copyright 2006 Open Source Applications Foundation
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

import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import net.fortuna.ical4j.model.Calendar;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.unitedinternet.cosmo.hibernate.validator.Task;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.QName;

/**
 * Hibernate persistent NoteItem.
 */
@Entity
@DiscriminatorValue("note")
public class HibNoteItem extends HibICalendarItem implements NoteItem {

    public static final QName ATTR_NOTE_BODY = new HibQName(
            NoteItem.class, "body");
    
    public static final QName ATTR_REMINDER_TIME = new HibQName(
            NoteItem.class, "reminderTime");
    
    private static final long serialVersionUID = -6100568628972081120L;
    
    private static final Set<NoteItem> EMPTY_MODS = Collections
            .unmodifiableSet(new HashSet<NoteItem>(0));

    @OneToMany(targetEntity=HibNoteItem.class, mappedBy = "modifies", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.DELETE} )
    //@BatchSize(size=50)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<NoteItem> modifications = new HashSet<NoteItem>(0);
    
    @ManyToOne(targetEntity=HibNoteItem.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiesitemid")
    private NoteItem modifies = null;
    
    @Column(name= "hasmodifications")
    private boolean hasModifications = false;
    
    /**
     * Constructor.
     */
    public HibNoteItem() {
    }

    // Property accessors
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.NoteItem#getBody()
     */
    public String getBody() {
        return HibTextAttribute.getValue(this, ATTR_NOTE_BODY);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.NoteItem#setBody(java.lang.String)
     */
    public void setBody(String body) {
        // body stored as TextAttribute on Item
        HibTextAttribute.setValue(this, ATTR_NOTE_BODY, body);
    }
  
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.NoteItem#setBody(java.io.Reader)
     */
    public void setBody(Reader body) {
        // body stored as TextAttribute on Item
        HibTextAttribute.setValue(this, ATTR_NOTE_BODY, body);
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.NoteItem#getReminderTime()
     */
    public Date getReminderTime() {
        return HibTimestampAttribute.getValue(this, ATTR_REMINDER_TIME);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.NoteItem#setReminderTime(java.util.Date)
     */
    public void setReminderTime(Date reminderTime) {
        // reminderDate stored as TimestampAttribute on Item
        HibTimestampAttribute.setValue(this, ATTR_REMINDER_TIME, reminderTime);
    }
   
    @Task
    public Calendar getTaskCalendar() {
        // calendar stored as ICalendarAttribute on Item
        return getCalendar();
    }
    
    public void setTaskCalendar(Calendar calendar) {
        setCalendar(calendar);
    }
   
    public Item copy() {
        NoteItem copy = new HibNoteItem();
        copyToItem(copy);
        return copy;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.NoteItem#getModifications()
     */
    public Set<NoteItem> getModifications() {
        if(hasModifications) {
            return Collections.unmodifiableSet(modifications);
        }
        else {
            return EMPTY_MODS;
        }
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.NoteItem#addModification(org.unitedinternet.cosmo.model.NoteItem)
     */
    public void addModification(NoteItem mod) {
        modifications.add(mod);
        hasModifications = true;
    }
  
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.NoteItem#removeModification(org.unitedinternet.cosmo.model.NoteItem)
     */
    public boolean removeModification(NoteItem mod) {
        boolean removed = modifications.remove(mod);
        hasModifications = modifications.size()!=0;
        return removed;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.NoteItem#removeAllModifications()
     */
    public void removeAllModifications() {
        modifications.clear();
        hasModifications = false;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.NoteItem#getModifies()
     */
    public NoteItem getModifies() {
        return modifies;
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.NoteItem#setModifies(org.unitedinternet.cosmo.model.NoteItem)
     */
    public void setModifies(NoteItem modifies) {
        this.modifies = modifies;
    }
    
    public boolean hasModifications() {
        return hasModifications;
    }
    
    @Override
    public String calculateEntityTag() {
        String uid = getUid() != null ? getUid() : "-";
        String modTime = getModifiedDate() != null ?
            Long.valueOf(getModifiedDate().getTime()).toString() : "-";
         
        StringBuilder etag = new StringBuilder(uid + ":" + modTime);
        
        // etag is constructed from self plus modifications
        if(modifies==null) {
            for(NoteItem mod: getModifications()) {
                uid = mod.getUid() != null ? mod.getUid() : "-";
                modTime = mod.getModifiedDate() != null ?
                        Long.valueOf(mod.getModifiedDate().getTime()).toString() : "-";
                etag.append("," + uid + ":" + modTime);
            }
        }
      
        return encodeEntityTag(etag.toString().getBytes(Charset.forName("UTF-8")));
    }
}
