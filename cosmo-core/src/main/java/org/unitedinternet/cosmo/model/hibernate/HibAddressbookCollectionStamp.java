package org.unitedinternet.cosmo.model.hibernate;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitedinternet.cosmo.model.AddressbookCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.Stamp;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("addressbook") //https://docs.jboss.org/hibernate/stable/annotations/reference/en/html_single/#d0e1168
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibAddressbookCollectionStamp extends HibStamp implements AddressbookCollectionStamp {

    public static final QName ATTR_ADDRESSBOOK_DESCRIPTION = new HibQName(AddressbookCollectionStamp.class, "description");


    public HibAddressbookCollectionStamp() {}

    public HibAddressbookCollectionStamp(CollectionItem collection) {
        this();
        setItem(collection);
    }
    @Override
    public String getDescription() {
        return HibStringAttribute.getValue(getItem(), ATTR_ADDRESSBOOK_DESCRIPTION);
    }

    @Override
    public void setDescription(String description) {
        HibStringAttribute.setValue(getItem(), ATTR_ADDRESSBOOK_DESCRIPTION, description);
    }

    @Override
    public String getType() {
        return "addressbook";
    }

    @Override
    public Stamp copy() {
        Stamp stamp = new HibAddressbookCollectionStamp();
        return stamp;
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
