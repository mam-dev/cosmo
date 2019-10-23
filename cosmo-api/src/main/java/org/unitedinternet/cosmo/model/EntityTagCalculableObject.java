package org.unitedinternet.cosmo.model;

import java.sql.Time;
import java.util.Date;

/**
 * This interface implies that entity tag depends on date and updates timestamp accordingly.
 */
public interface EntityTagCalculableObject extends TimestampObject {
        /**
     * Calculates object's entity tag. Returns the empty string. Subclasses should override this.
     */
    String calculateEntityTag();

    void setCreationDate(Date creationDate);

    void setModifiedDate(Date modifiedDate);

    void setEntityTag(String etag);

    @Override
    default void updateTimestamp() {
        setModifiedDate(new Date());
        if (getCreationDate() == null) {
            setCreationDate(new Date());
        }
        setModifiedDate(new Date());
        setEntityTag(calculateEntityTag());
    }



}
