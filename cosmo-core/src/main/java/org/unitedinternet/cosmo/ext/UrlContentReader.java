package org.unitedinternet.cosmo.ext;

import java.util.Set;

import org.unitedinternet.cosmo.model.NoteItem;

/**
 * Component able to read calendar content from URL-s in a configurable manner.
 * 
 * @author daniel grigore
 *
 */
public interface UrlContentReader {

    /**
     * Gets and validates the content from the specified <code>url</code>.
     * 
     * @param url
     *            URL where to get content from
     * @param timeoutInMillis
     *            how much time to wait before throwing an exception
     * @return a set of validated items
     */
    Set<NoteItem> getContent(String url, int timeoutInMillis) throws ExternalContentRuntimeException;

}