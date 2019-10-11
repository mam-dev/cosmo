package org.unitedinternet.cosmo.model.hibernate;

import org.unitedinternet.cosmo.model.AddressbookCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.Stamp;

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
