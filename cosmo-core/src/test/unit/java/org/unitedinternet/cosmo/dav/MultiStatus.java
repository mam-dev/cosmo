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
package org.unitedinternet.cosmo.dav;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Bean that encapsulates information about a DAV multistatus
 * response.
 */
public class MultiStatus {
    private static final Namespace NS = Namespace.getNamespace("D", "DAV:");

    private HashSet<MultiStatusResponse> responses;
    private String responseDescription;

    /**
     * Constructor.
     */
    public MultiStatus() {
        responses = new HashSet<MultiStatusResponse>();
    }

    /**
     * Gets responses.
     * @return the responses.
     */
    public Set<MultiStatusResponse> getResponses() {
        return responses;
    }

    /**
     * Finds response.
     * @param href The href.
     * @return Multi status response.
     */
    public MultiStatusResponse findResponse(String href) {
        for (MultiStatusResponse msr : responses) {
            if (msr.getHref().equals(href)) {
                return msr;
            }
        }
        return null;
    }

    /**
     * Gets response description.
     * @return The response description.
     */
    public String getResponseDescription() {
        return responseDescription;
    }

    /**
     * Sets response description.
     * @param responseDescription The response description.
     */
    public void setResponseDescription(String responseDescription) {
        this.responseDescription = responseDescription;
    }

    /**
     * Creates from xml.
     * @param doc The document.
     * @return Multi status.
     */
    public static MultiStatus createFromXml(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("null document");
        }

        Element mse = doc.getDocumentElement();
        if (! DomUtil.matches(mse, "multistatus", NS)) {
            throw new IllegalArgumentException("root element not DAV:multistatus");
        }

        MultiStatus ms = new MultiStatus();

        ElementIterator i = DomUtil.getChildren(mse, "response", NS);
        while (i.hasNext()) {
            ms.getResponses().
                add(MultiStatusResponse.createFromXml(i.nextElement()));
        }

        String msrd = DomUtil.getChildTextTrim(mse, "responsedescription", NS);
        ms.setResponseDescription(msrd);

        return ms;
    }

    /**
     * toString.
     * {@inheritDoc}
     * @return The string.
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("responses", responses).
            append("responseDescription", responseDescription).
            toString();
    }

    /**
     * Class MultiStatusResponse.
     *
     */
    public static class MultiStatusResponse {
        private String href;
        private Status status;
        private HashSet<PropStat> propstats;
        private String responseDescription;

        /**
         * Constructor.
         */
        public MultiStatusResponse() {
            propstats = new HashSet<PropStat>();
        }

        /**
         * Gets href.
         * @return The string.
         */
        public String getHref() {
            return href;
        }

        /**
         * Sets href.
         * @param href The href.
         */
        public void setHref(String href) {
            this.href = href;
        }

        /**
         * Gets status.
         * @return The status.
         */
        public Status getStatus() {
            return status;
        }

        /**
         * Sets status.
         * @param status The status.
         */
        public void setStatus(Status status) {
            this.status = status;
        }

        /**
         * Gets prop stats.
         * @return set.
         */
        public Set<PropStat> getPropStats() {
            return propstats;
        }

        /**
         * Finds prop stat.
         * @param code The code.
         * @return The prop stat.
         */
        public PropStat findPropStat(int code) {
            for (PropStat ps : propstats) {
                if (ps.getStatus().getCode() == code) {
                    return ps;
                }
            }
            return null;
        }

        /**
         * Gets response description.
         * @return The description.
         */
        public String getResponseDescription() {
            return responseDescription;
        }

        /**
         * Sets response description.
         * @param responseDescription The response description.
         */
        public void setResponseDescription(String responseDescription) {
            this.responseDescription = responseDescription;
        }

        /**
         * Creates from xml.
         * @param e The element.
         * @return The multi status response.
         */
        public static MultiStatusResponse createFromXml(Element e) {
            if (e == null) {
                throw new IllegalArgumentException("null DAV:response element");
            }

            MultiStatusResponse msr = new MultiStatusResponse();

            Element he = DomUtil.getChildElement(e, "href", NS);
            if (he == null) {
                throw new IllegalArgumentException("expected DAV:href child for DAV:response element");
            }
            msr.setHref(DomUtil.getTextTrim(he));

            String statusLine = DomUtil.getChildTextTrim(e, "status", NS);
            if (statusLine != null) {
                msr.setStatus(Status.createFromStatusLine(statusLine));
            }

            ElementIterator i = DomUtil.getChildren(e, "propstat", NS);
            while (i.hasNext()) {
                msr.getPropStats().add(PropStat.createFromXml(i.nextElement()));
            }

            String msrrd =
                DomUtil.getChildTextTrim(e, "responsedescription", NS);
            msr.setResponseDescription(msrrd);

            return msr;
        }

        /**
         * toString.
         * {@inheritDoc}
         * @return The string.
         */
        public String toString() {
            return new ToStringBuilder(this).
                append("href", href).
                append("status", status).
                append("propstats", propstats).
                append("responseDescription", responseDescription).
                toString();
        }
    }

    /**
     * Class propStat.
     */
    public static class PropStat {
        private HashSet<Element> props;
        private Status status;
        private String responseDescription;

        /**
         * Constructor.
         */
        public PropStat() {
            props = new HashSet<Element>();
        }

        /**
         * Gets props.
         * @return The set.
         */
        public Set<Element> getProps() {
            return props;
        }

        /**
         * Finds Prop.
         * @param name The name.
         * @param ns The namespace.
         * @return The element.
         */
        public Element findProp(String name,  Namespace ns) {
            for (Element prop : props) {
                if (DomUtil.matches(prop, name, ns)) {
                    return prop;
                }
            }
            return null;
        }

       /**
        * Gets status.
        * @return The status.
        */
        public Status getStatus() {
            return status;
        }

        /**
         * Sets status.
         * @param status The status.
         */
        public void setStatus(Status status) {
            this.status = status;
        }

        /**
         * Gets response description.
         * @return The response description.
         */
        public String getResponseDescription() {
            return responseDescription;
        }

        /**
         * Sets response description.
         * @param responseDescription The response description.
         */
        public void setResponseDescription(String responseDescription) {
            this.responseDescription = responseDescription;
        }

        /**
         * Creates from xml.
         * @param e The element.
         * @return Propstat.
         */
        public static PropStat createFromXml(Element e) {
            if (e == null) {
                throw new IllegalArgumentException("null DAV:propstat element");
            }

            PropStat ps = new PropStat();

            Element pe = DomUtil.getChildElement(e, "prop", NS);
            if (pe == null) {
                throw new IllegalArgumentException("expected DAV:prop child for DAV:propstat element");
            }

            ElementIterator i = DomUtil.getChildren(pe);
            while (i.hasNext()) {
                ps.getProps().add(i.nextElement());
            }

            String statusLine = DomUtil.getChildTextTrim(e, "status", NS);
            if (statusLine != null) {
                ps.setStatus(Status.createFromStatusLine(statusLine));
            }

            String psrd =
                DomUtil.getChildTextTrim(e, "responsedescription", NS);
            ps.setResponseDescription(psrd);

            return ps;
        }

        /**
         * toString.
         * {@inheritDoc}
         * @return String.
         */
        public String toString() {
            return new ToStringBuilder(this).
                append("props", props).
                append("status", status).
                append("responseDescription", responseDescription).
                toString();
        }
    }

    /**
     * Status.
     *
     */
    public static class Status {
        private String protocol;
        private int code;
        private String reasonPhrase;

        /**
         * Gets protocol.
         * @return The protocol.
         */
        public String getProtocol() {
            return protocol;
        }

        /**
         * Sets protocol.
         * @param protocol The protocol
         */
        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        /**
         * Gets code.
         * @return The code.
         */
        public int getCode() {
            return code;
        }

        /**
         * Sets code.
         * @param code The code.
         */
        public void setCode(int code) {
            this.code = code;
        }

        /**
         * Gets reason phrase.
         * @return The reason path.
         */
        public String getReasonPhrase() {
            return reasonPhrase;
        }

        /**
         * Sets reason phrase.
         * @param reasonPhrase The reason phrase.
         */
        public void setReasonPhrase(String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
        }

        /**
         * Creates from status line.
         * @param line The line.
         * @return The status.
         */
        public static Status createFromStatusLine(String line) {
            if (line == null) {
                throw new IllegalArgumentException("null status line");
            }

            String[] chunks = line.trim().split("\\s", 3);
            if (chunks.length < 3) {
                throw new IllegalArgumentException("status line " + line + " does not contain proto/version, "
                        + "code, reason phrase");
            }

            Status status = new Status();

            status.setProtocol(chunks[0]);
            status.setCode(Integer.parseInt(chunks[1]));
            status.setReasonPhrase(chunks[2]);

            return status;
        }

        /**
         * toString.
         * {@inheritDoc}
         * @return String.
         */
        public String toString() {
            return new ToStringBuilder(this).
                append("protocol", protocol).
                append("code", code).
                append("reasonPhrase", reasonPhrase).
                toString();
        }
    }
}
