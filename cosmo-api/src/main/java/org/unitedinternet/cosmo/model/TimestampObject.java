package org.unitedinternet.cosmo.model;

import java.util.Date;

public interface TimestampObject {
    /**
     * Update modification date and other dependent attributes with current system time.
     */
    public void updateTimestamp();

    /**
     * @return date object was created
     */
    public Date getCreationDate();

    /**
     * @return date object was last updated
     */
    public Date getModifiedDate();


}
