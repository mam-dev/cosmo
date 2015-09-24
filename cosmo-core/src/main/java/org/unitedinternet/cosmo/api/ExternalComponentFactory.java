/*
 * ExternalComponentsFactory.java May 5, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.api;

/**
 * 
 * @author corneliu dobrota
 *
 */
public interface ExternalComponentFactory {
    <T, R extends T> R instanceForDescriptor(ExternalComponentDescriptor<R> desc);
}
