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
package org.unitedinternet.cosmo.dav.property;

import org.unitedinternet.cosmo.util.ContentTypeUtil;
import org.apache.jackrabbit.webdav.property.DavPropertyName;

/**
 * Represents the DAV:getcontenttype property.
 */
public class ContentType extends StandardDavProperty {

    public ContentType(String type) {
        this(type, null);
    }

    public ContentType(String type,
                       String encoding) {
        super(DavPropertyName.GETCONTENTTYPE, mt(type, encoding), false);
    }

    private static String mt(String type,
                             String encoding) {
        return ContentTypeUtil.buildContentType(type, encoding);
    }
}
