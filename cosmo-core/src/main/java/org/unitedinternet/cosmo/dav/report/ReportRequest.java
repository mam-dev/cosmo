/*
 * Copyright 2005-2006 Open Source Applications Foundation
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

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;

/**
 * Provides request functionality related to reporting.
 */
public interface ReportRequest {

    /**
     * Return the report information, if any, included in the
     * request.
     *
     * @throws DavException if there is no report information in the
     * request or if the report information is invalid
     */
    ReportInfo getReportInfo()
        throws DavException;
}
