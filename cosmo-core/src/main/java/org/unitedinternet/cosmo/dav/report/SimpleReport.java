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
package org.unitedinternet.cosmo.dav.report;

import java.io.InputStream;

import org.apache.jackrabbit.webdav.DavServletResponse;
import org.springframework.util.FileCopyUtils;
import org.unitedinternet.cosmo.dav.CosmoDavException;

/**
 * Base class for reports that return simple single-entity responses.
 */
public abstract class SimpleReport extends ReportBase {

    private String contentType;
    private String encoding;
    private InputStream stream;

    // Report methods

    public abstract boolean isMultiStatusReport();

    protected void output(DavServletResponse response)
        throws CosmoDavException {
        try {
            response.setStatus(DavServletResponse.SC_OK);
            response.setContentType(contentType);
            response.setCharacterEncoding(encoding);
            FileCopyUtils.copy(stream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            throw new CosmoDavException(e);
        }
    }

    // our methods

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }
}
