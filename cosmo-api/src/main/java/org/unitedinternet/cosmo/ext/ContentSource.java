/*
 * ContentSource.java Dec 7, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.ext;

import java.util.Set;

import org.unitedinternet.cosmo.model.NoteItem;

/**
 * Component that can serve calendar content from an <code>uri</code>.
 * 
 * @author corneliu dobrota
 */
public interface ContentSource {

    Set<NoteItem> getContent(String uri) throws ExternalContentTooLargeException, ExternalContentInvalidException;
}
