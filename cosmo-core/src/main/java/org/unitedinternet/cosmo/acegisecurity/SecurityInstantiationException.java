/*
 * SecurityInstantiationException.java May 6, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.acegisecurity;

/**
 * 
 * @author corneliu dobrota
 *
 */
public class SecurityInstantiationException extends RuntimeException{

    private static final long serialVersionUID = 2587649201815923456L;

    public SecurityInstantiationException(String message){
        super(message);
    }
}
