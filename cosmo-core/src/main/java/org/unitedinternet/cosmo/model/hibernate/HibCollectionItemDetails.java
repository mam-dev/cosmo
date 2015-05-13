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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionItemDetails;
import org.unitedinternet.cosmo.model.Item;

/**
 * Hibernate persistent CollectionItemDetails, which is
 * used to store extra attributes in the many-to-many
 * association of collection<-->item.  Extra information
 * that is stored include the date the item was added
 * to the collection.
 */
@Entity
@Table(name="collection_item")
//@AssociationOverrides({
//@AssociationOverride(name="primaryKey.collection", joinColumns = @JoinColumn(name="collectionid")),
//@AssociationOverride(name="primaryKey.item", joinColumns = @JoinColumn(name="itemid"))
//})
public class HibCollectionItemDetails implements CollectionItemDetails {

    @Id
    private CollectionItemPK primaryKey = new CollectionItemPK();
    
    @Column(name = "createdate", nullable=false)
    @Type(type="long_timestamp")
    private Date creationDate = new Date();
 
    public HibCollectionItemDetails() {}
    
    public HibCollectionItemDetails(CollectionItem collection,
            Item item) {
        primaryKey.collection = collection;
        primaryKey.item = item;
    }
    
    public void setCollection(CollectionItem collection) {
        primaryKey.collection = collection;
    }
    
    public CollectionItem getCollection() {
        return primaryKey.collection;
    }

    public void  setItem(Item item) {
        primaryKey.item = item;
    }
    
    public Item getItem() {
        return primaryKey.item;
    }

    public Date getTimestamp() {
        return creationDate;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null) {
            return false;
        }
        if( ! (obj instanceof HibCollectionItemDetails)) {
            return false;
        }
        
        HibCollectionItemDetails cid = (HibCollectionItemDetails) obj;
        return primaryKey.collection.equals(cid.getCollection()) &&
            primaryKey.item.equals(cid.getItem());
    }

    @Override
    public int hashCode() {
        return primaryKey.hashCode();
    }

    /**
     * PrimaryKey of CollectionItemDetails consists of two
     * foreign keys, the collection, and the item.
     */
    @Embeddable
    private static class CollectionItemPK implements Serializable {
        
        private static final long serialVersionUID = -8144072492198688087L;

        @ManyToOne(targetEntity = HibCollectionItem.class, fetch = FetchType.EAGER)
        @JoinColumn(name = "collectionid", nullable = false)
        public CollectionItem collection;

        @ManyToOne(targetEntity = HibItem.class)
        @JoinColumn(name = "itemid", nullable = false)
        public Item item;
        
        public CollectionItemPK() {}

        @Override
        public boolean equals(Object obj) {
            if(obj==null || item==null || collection==null) {
                return false;
            }
            if( ! (obj instanceof CollectionItemPK)) {
                return false;
            }
            
            CollectionItemPK pk = (CollectionItemPK) obj;
            return collection.equals(pk.collection) && item.equals(pk.item);
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(13,73 ).appendSuper(item.hashCode())
            .appendSuper(collection.hashCode()).toHashCode();
        }
    }
}
