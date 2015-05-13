/*
 * DocumentNotIndexedException.java Apr 30, 2014
 * 
 * Copyright (c) 2014 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.spi.search;


/**
 * Thrown when an error occured in operations with indexing service event.
 * @author izidaru
 *
 */
public class SearchException extends RuntimeException {

        
    
    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }
    
   

}
