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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * Represents the data of a piece of Content. Data is stored as a BufferedContent, either in memory (small content) or
 * on disk (large content).
 */
@Entity
@Table(name = "content_data")
public class HibContentData extends BaseModelObject {

    private static final long serialVersionUID = -5014854905531456753L;

    @Column(name = "content", length = 102400000)
    @Lob
    private byte[] content = null;

    public HibContentData() {
    }

    public String toString() {
        if (content != null) {
            return new String(content);
        }
        return null;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * @return the size of the data read, or -1 for no data present
     */
    public long getSize() {
        if (content != null) {
            return content.length;
        } else {
            return -1;
        }
    }
}
