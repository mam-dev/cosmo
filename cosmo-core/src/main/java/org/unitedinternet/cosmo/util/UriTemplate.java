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
package org.unitedinternet.cosmo.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.text.StrTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.CosmoException;

/**
 * <p>
 * Models a URI pattern such that candidate URIs can be matched against the template to extract interesting information
 * from them.
 * </p>
 * <p>
 * A URI pattern looks like <code>/collection/{uid}/{projection}?/{format}?/*</code>. Each path segment can be either a
 * literal or a variable (the latter enclosed in curly braces}. A segment can be further denoted as optional, in which
 * case the segment is trailed by a question mark. A final segment of <code>*</code> indicates that the remainder of the
 * candidate URI after the previous segment is matched.
 * </p>
 * <p>
 * Inspired by the .NET UriTemplate class.
 * </p>
 */
public class UriTemplate {
    
    private static final Logger LOG = LoggerFactory.getLogger(UriTemplate.class);

    private String pattern;
    private ArrayList<Segment> segments;

    public UriTemplate(String pattern) {
        this.pattern = pattern;
        this.segments = new ArrayList<Segment>();

        StrTokenizer tokenizer = new StrTokenizer(pattern, '/');
        while (tokenizer.hasNext()) {
            segments.add(new Segment(tokenizer.nextToken()));
        }
    }

    /**
     * Generates an absolute-path relative URI using the same algorithm as
     * {@link #bindAbsolute(boolean, String, String...)}. Bound values are escaped.
     *
     * @param values the unescaped values to be bound
     * @return a URI with variables replaced by bound values
     * @throws IllegalArgumentException if more or fewer values are provided than are needed by the template or if a
     *                                  null value is provided for a mandatory variable
     */
    public String bind(String... values) {
        return bind(true, values);
    }

    /**
     * Generates an absolute-path relative URI using the same algorithm as
     * {@link #bindAbsolute(boolean, String, String...)}. Bound values are optionally escaped.
     *
     * @param escape a flag determining whether or not bound variables are to be escaped
     * @param values the unescaped values to be bound
     * @return a URI with variables replaced by bound values
     * @throws IllegalArgumentException if more or fewer values are provided than are needed by the template or if a
     *                                  null value is provided for a mandatory variable
     */
    public String bind(boolean escape, String... values) {
        return bindAbsolute(escape, "", values);
    }

    /**
     * Generates a absolute URI relative to <code>base</code> using the same algorithm as
     * {@link #bindAbsolute(boolean, String, String...)}. Bound values are escaped.
     *
     * @param escape a flag determining whether or not bound variables are to be escaped
     * @param base   the optional escaped base to prepend to the generated path
     * @param values the unescaped values to be bound
     * @return a URI with variables replaced by bound values
     * @throws IllegalArgumentException if more or fewer values are provided than are needed by the template or if a
     *                                  null value is provided for a mandatory variable
     */
    public String bindAbsolute(String base, String... values) {
        return bindAbsolute(true, base, values);
    }

    /**
     * Generates a absolute URI relative to <code>base</code> by replacing the variable segments of the template with
     * the provided values. All literal segments, optional or no, are always included. Values are bound into the
     * template in the order in which they are provided. If a value is not provided for an optional variable segment,
     * the segment is not included. Bound values are optionally escaped.
     *
     * @param escape a flag determining whether or not bound variables are to be escaped
     * @param base   the optional escaped base to prepend to the generated path
     * @param values the unescaped values to be bound
     * @return a URI with variables replaced by bound values
     * @throws IllegalArgumentException if more or fewer values are provided than are needed by the template or if a
     *                                  null value is provided for a mandatory variable
     */
    public String bindAbsolute(boolean escape, String base, String... values) {
        if (base == null) {
            throw new IllegalArgumentException("Base cannot be null");
        }
        StringBuilder buf = new StringBuilder(base);
        buf.append("/");

        List<String> variables = Arrays.asList(values);
        Iterator<String> vi = variables.iterator();

        Iterator<Segment> si = segments.iterator();
        Segment segment = null;
        while (si.hasNext()) {
            segment = si.next();

            if (segment.isVariable()) {
                String value = null;
                if (vi.hasNext()) {
                    value = vi.next();
                }
                if (value == null) {
                    if (segment.isOptional()) {
                        continue;
                    }
                    throw new IllegalArgumentException("Not enough values");
                }
                buf.append(escape ? escapeSegment(value) : value);
            } else if (!segment.isAll()) {
                buf.append("/").append(segment.getData());
            }

            if (si.hasNext() && vi.hasNext()) {
                buf.append("/");
            }
        }

        if (vi.hasNext()) {
            if (segment.isAll()) {
                while (vi.hasNext())
                    buf.append(escape ? escapeSegment(vi.next()) : vi.next());
            } else {
                throw new IllegalArgumentException("Too many values");
            }
        }

        // buf.append("/");
        return buf.toString().replaceAll("/{2,}", "/");
    }

    /**
     * <p>
     * Matches an escaped candidate uri-path against the template using the same algorithm as {@link match(boolean,
     * String)}. Returns a <code>Match</code> instance containing the names and values of all variables found in the
     * uri-path as specified by the template.
     * </p>
     *
     * @param path the candidate uri-path
     * @return a <code>Match</code>, or <code>null</code> if the path did not successfully match
     */
    public Match match(String path) {
        return match(true, path);
    }

    /**
     * <p>
     * Matches a possibly-escaped candidate uri-path against the template. Returns a <code>Match</code> instance
     * containing the names and values of all variables found in the uri-path as specified by the template.
     * </p>
     * <p>
     * Each literal segment in the template must match the corresponding segment in the uri-path unless the segment is
     * optional. For each variable segment in the template, an entry is added to the <code>Match</code> to be returned;
     * the entry key is the variable name from the template, and the entry value is the corresponding (unescaped) token
     * from the uri-path. If the template includes an "all" segment, a match entry with key <code>*</code> is also
     * included containing the remainder of the uri-path after the last matching segment.
     * </p>
     *
     * @param escaped whether or not the uri-path is escaped
     * @param path    the candidate uri-path
     * @return a <code>Match</code>, or <code>null</code> if the path did not successfully match
     */
    public Match match(boolean escaped, String path) {
        Match match = new Match(path);

        // if (log.isDebugEnabled())
        // log.debug("matching " + path + " to " + pattern);

        StrTokenizer candidate = new StrTokenizer(path, '/');
        Iterator<Segment> si = segments.iterator();

        Segment segment = null;
        while (si.hasNext() || (segment != null && segment.isAll())) {
            if (si.hasNext()) {
                segment = si.next();
            }
            if (!candidate.hasNext()) {
                // if the segment is consuming all remaining data, then we're
                // done, since there is no more data
                if (segment.isAll()) {
                    break;
                }
                // if the segment is optional, the candidate doesn't
                // have to have a matching segment
                if (segment.isOptional()) {
                    continue;
                }
                // mandatory segment - not a match
                return null;
            }

            String token = candidate.nextToken();

            if (segment.isAll()) {
                StringBuilder saved = new StringBuilder(match.get("*"));
                if (match.get("*") == null) {
                    saved = new StringBuilder();
                }
                saved.append("/");
                saved.append(escaped ? unescapeSegment(token) : token);
                match.put("*", saved.toString());
            } else if (segment.isVariable()) {
                match.put(segment.getData(), escaped ? unescapeSegment(token) : token);
            } else if (!segment.getData().equals(token)) {
                // literal segment doesn't match, so path is not a match
                return null;
            }
        }

        if (candidate.hasNext() && !segment.isAll()) {
            // candidate has more but our pattern is done
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("matched {}", pattern);
        }

        return match;
    }

    public String getPattern() {
        return pattern;
    }

    public static final String escapeSegment(String raw) {
        try {
            return new URI(null, null, raw, null).toASCIIString();
        } catch (Exception e) {
            throw new CosmoException("Could not escape string " + raw, e);
        }
    }

    public static final String unescapeSegment(String escaped) {
        try {
            // URI doesn't unescape '+' as a space
            escaped = escaped.replace('+', ' ');
            return new URI(null, null, "/" + escaped, null).getPath().substring(1);
        } catch (Exception e) {
            throw new CosmoException("Could not unescape string " + escaped, e);
        }
    }

    private static class Segment {
        private String data;
        private boolean variable = false;
        private boolean optional = false;
        private boolean all = false;

        public Segment(String data) {
            if (data.startsWith("{")) {
                if (data.endsWith("}?")) {
                    variable = true;
                    optional = true;
                    this.data = data.substring(1, data.length() - 2);
                } else if (data.endsWith("}")) {
                    variable = true;
                    this.data = data.substring(1, data.length() - 1);
                }
            } else if (data.endsWith("?")) {
                optional = true;
                this.data = data.substring(0, data.length() - 1);
            } else if (data.equals("*")) {
                all = true;
            }

            if (this.data == null && !all) {
                this.data = data;
            }
        }

        public String getData() {
            return data;
        }

        public boolean isVariable() {
            return variable;
        }

        public boolean isOptional() {
            return optional;
        }

        public boolean isAll() {
            return all;
        }
    }

    public static class Match extends HashMap<String, String> {
        private static final long serialVersionUID = 8091355783493490510L;
        private String path;

        public Match(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public String get(String key) {// NOPMD
            return super.get(key);
        }
    }
}