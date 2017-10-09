package org.unitedinternet.cosmo.model.hibernate;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.QName;

/**
 * 
 * @author daniel grigore
 *
 */
public final class CollectionItemConstants {

    // CollectionItem specific attributes
    public static final QName ATTR_EXCLUDE_FREE_BUSY_ROLLUP = new HibQName(CollectionItem.class,
            "excludeFreeBusyRollup");

    public static final QName ATTR_HUE = new HibQName(CollectionItem.class, "hue");

    private CollectionItemConstants() {
        // Avoid unneeded instances.
    }
}
