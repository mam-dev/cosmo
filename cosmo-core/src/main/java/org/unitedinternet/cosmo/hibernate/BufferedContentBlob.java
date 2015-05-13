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
package org.unitedinternet.cosmo.hibernate;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.sql.BlobTypeDescriptor;
import org.unitedinternet.cosmo.util.BufferedContent;

/**
 * Custom Hibernate type that persists BufferedContent to BLOB field.
 */

public class BufferedContentBlob
                extends AbstractSingleColumnStandardBasicType<BufferedContent>  {

    private static final long serialVersionUID = -8684598762195576301L;


    /**
     * Constructor used by Hibernate: fetches config-time LobHandler and
     * config-time JTA TransactionManager from LocalSessionFactoryBean.
     *
     */
    public BufferedContentBlob() {
        super(BlobTypeDescriptor.STREAM_BINDING, BufferedContentTypeDescriptor.INSTANCE);
    }


    @Override
    public String getName() {
        return "bufferedcontent_blob";
    }

}
