/*
 * ApiInterfaceImplFactoryChain.java May 4, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.api;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 
 * @author corneliu dobrota
 *
 */
public class ExternalComponentFactoryChain implements ExternalComponentFactory{
    
    private Set<ExternalComponentFactory> factories = new LinkedHashSet<>();
    
    @Override
    public <T, R extends T> R instanceForDescriptor(ExternalComponentDescriptor<R> desc) {
        
        for(ExternalComponentFactory factory : factories){
            R result = factory.instanceForDescriptor(desc);
            if(result != null){
                return result;
            }
        }
        return null;
    }
    
    public ExternalComponentFactoryChain(ExternalComponentFactory...factories){
        if(factories == null){
            return;
        }
        for(ExternalComponentFactory factory :factories){
            this.factories.add(factory);
        }
    }
}
