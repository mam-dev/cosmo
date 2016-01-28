/*
 * InvalidExternalContentException.java Jan 25, 2016
 * 
 * Copyright (c) 2016 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.ext;

public class InvalidExternalContentException extends RuntimeException{

    private static final long serialVersionUID = 835901965749714415L;
    
    public InvalidExternalContentException(Throwable cause){
        super(cause);
    }
    
    public InvalidExternalContentException(){
            
    }
}
