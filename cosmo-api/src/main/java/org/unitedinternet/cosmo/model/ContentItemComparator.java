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

/**
 * Compares instances of {@link ContentItem}.
 */
public class ContentItemComparator extends AuditableComparator {
    
    
    public static final int ATTRIBUTE_TRIAGE_RANK = 0;

    public ContentItemComparator() {
        this(false, ATTRIBUTE_TRIAGE_RANK);
    }

    public ContentItemComparator(boolean reverse) {
        this(reverse, ATTRIBUTE_TRIAGE_RANK);
    }

    public ContentItemComparator(int attribute) {
        this(false, attribute);
    }

    public ContentItemComparator(boolean reverse,
                                 int attribute) {
        super(reverse, attribute);
    }

    public int compare(ContentItem o1,
                       ContentItem o2) {
        if (o1.equals(o2)) {
            return 0;
        }
        if (o1.getTriageStatus().getRank() == null) {
            return -1;
        }
        if (o2.getTriageStatus().getRank() == null) {
            return 1;
        }
        return o1.getTriageStatus().getRank().
            compareTo(o2.getTriageStatus().getRank());
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof ContentItemComparator) {
            return true;
        }
        return super.equals(obj);
    }
    
    public int hashCode() {
        return 1;
    }

    public static boolean supports(int attribute) {
        return (attribute == ATTRIBUTE_TRIAGE_RANK);
    }
}
