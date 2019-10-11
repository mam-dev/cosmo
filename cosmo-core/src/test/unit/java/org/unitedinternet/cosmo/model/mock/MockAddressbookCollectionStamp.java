package org.unitedinternet.cosmo.model.mock;

import org.unitedinternet.cosmo.model.*;

import java.io.Serializable;

public class MockAddressbookCollectionStamp extends MockStamp
implements Serializable, AddressbookCollectionStamp {

    //AddressbookCollection specific attributes
    public static final QName ATTR_ADDRESSBOOK_DESCRIPTION = new MockQName(AddressbookCollectionStamp.class, "description");

    /**
     * Gets type
     * @return
     */
    @Override
    public String getType() {
        return "addressbook";
    }

    @Override
    public Stamp copy() {
        Stamp stamp = new MockAddressbookCollectionStamp();
        return stamp;
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }

    @Override
    public String getDescription() {
        return MockStringAttribute.getValue(getItem(), ATTR_ADDRESSBOOK_DESCRIPTION);
    }

    @Override
    public void setDescription(String description) {
        MockStringAttribute.setValue(getItem(), ATTR_ADDRESSBOOK_DESCRIPTION, description);
    }

    public static AddressbookCollectionStamp getStamp(Item item) {
        return (AddressbookCollectionStamp) item.getStamp(AddressbookCollectionStamp.class);
    }


}
