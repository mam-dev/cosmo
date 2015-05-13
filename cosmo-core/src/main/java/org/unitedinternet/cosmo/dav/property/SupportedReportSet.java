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

import java.util.TreeSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the DAV:supported-report-set property.
 */
public class SupportedReportSet extends StandardDavProperty {

    public SupportedReportSet(Set<ReportType> reports) {
        super(SUPPORTEDREPORTSET, reports, true);
    }

    public Set<ReportType> getReportTypes() {
        return (Set<ReportType>) getValue();
    }

    public String getValueText() {
        TreeSet<String> types = new TreeSet<String>();
        for (ReportType rt : getReportTypes()) {
            types.add(rt.getReportName());
        }
        return StringUtils.join(types, ", ");
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        for (ReportType rt : getReportTypes()) {
            Element sr = DomUtil.addChildElement(name, "supported-report", NAMESPACE);
            Element r = DomUtil.addChildElement(sr, "report", NAMESPACE);
            r.appendChild(rt.toXml(document));
        }

        return name;
    }
}
