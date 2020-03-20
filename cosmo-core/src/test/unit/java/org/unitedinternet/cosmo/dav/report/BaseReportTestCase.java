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

import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.w3c.dom.Document;

/**
 * Base class for report tests.
 */
public abstract class BaseReportTestCase extends BaseDavTestCase {
    
    /**
     * Makes target.
     * @param clazz Class
     * @return The dav resource.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @SuppressWarnings("unchecked")
    protected WebDavResource makeTarget(@SuppressWarnings("rawtypes") Class clazz)
        throws Exception {
        return (WebDavResource)
            clazz.getConstructor(DavResourceLocator.class,
                                 DavResourceFactory.class,
                                 EntityFactory.class).
                newInstance(testHelper.getHomeLocator(),
                            testHelper.getResourceFactory(),
                            testHelper.getEntityFactory());
    }

    /**
     * Makes report.
     * @param clazz The class.
     * @param reportXml The report xml.
     * @param depth The depth.
     * @param target The target.
     * @return The report base.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    protected ReportBase makeReport(@SuppressWarnings("rawtypes") Class clazz,
                                    String reportXml,
                                    int depth,
                                    WebDavResource target)
        throws Exception {
        ReportBase report = (ReportBase) clazz.newInstance();
        report.init(target, makeReportInfo(reportXml, depth));
        return report;
    }

    /**
     * Makes report info.
     * @param reportXml The report xml.
     * @param depth The path.
     * @return The report info.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    protected ReportInfo makeReportInfo(String reportXml,
                                        int depth)
        throws Exception {
        Document doc = testHelper.loadXml(reportXml);
        return new ReportInfo(doc.getDocumentElement(), depth);
    }
}
