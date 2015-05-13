/*
 * WebApplicationContextHolder.java May 7, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.api;

import org.springframework.web.context.WebApplicationContext;

/**
 * 
 * @author corneliu dobrota
 *
 */
public class WebApplicationContextHolder {
    private static final ThreadLocal<WebApplicationContext> HOLDER = new ThreadLocal<WebApplicationContext>();
    
    public static WebApplicationContext get(){
        return HOLDER.get();
    }
    public static void set(WebApplicationContext wac){
        HOLDER.set(wac);
    }
}
