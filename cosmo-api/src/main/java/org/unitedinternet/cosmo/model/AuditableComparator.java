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
package org.unitedinternet.cosmo.model;

import java.util.Comparator;
import java.util.Date;

/**
 * Compares instances of {@link AuditableObject} based on their
 * created or modified dates.
 */
public class AuditableComparator implements Comparator<AuditableObject> {
    
    public static final int ATTRIBUTE_CREATED = 0;
    public static final int ATTRIBUTE_MODIFIED = 1;

    private boolean reverse;
    private int attribute;

    public AuditableComparator() {
        this(false, ATTRIBUTE_MODIFIED);
    }

    public AuditableComparator(boolean reverse) {
        this(reverse, ATTRIBUTE_MODIFIED);
    }

    public AuditableComparator(int attribute) {
        this(false, attribute);
    }

    public AuditableComparator(boolean reverse,
                               int attribute) {
        if (! supports(attribute)) {
            throw new IllegalArgumentException("unknown attribute " + attribute);
        }
        this.reverse = reverse;
        this.attribute = attribute;
    }

    public int compare(AuditableObject o1,
                       AuditableObject o2) {
        if(o1.equals(o2)) {
            return 0;
        }
        if (attribute == ATTRIBUTE_CREATED) {
            return compare(o1.getCreationDate(), o2.getCreationDate());
        }
        return compare(o1.getModifiedDate(), o2.getModifiedDate());
    }

    protected int compare(Date d1,
                          Date d2) {
        if (d1.after(d2)) {
            return reverse ? -1 : 1;
        }
        return reverse ? 1 : -1;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof AuditableComparator) {
            return true;
        }
        return super.equals(obj);
    }
    
    public int hashCode() {
        return 1;
    }

    public static boolean supports(int attribute) {
        return (attribute == ATTRIBUTE_CREATED ||
                attribute == ATTRIBUTE_MODIFIED);
    }
}
