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
    private ExternalComponentsManager manager;
    
    @Override
    public <T, R extends T> R instanceForDescritor(ExternalComponentDescriptor<R> desc) {
        
        for(ExternalComponentFactory factory : factories){
            R result = factory.instanceForDescritor(desc);
            if(result != null){
                manager.add(desc, result);
                return result;
            }
        }
        return null;
    }
    
    public ExternalComponentFactoryChain(ExternalComponentsManager manager, ExternalComponentFactory...factories){
        if(factories == null){
            return;
        }
        this.manager = manager;
        for(ExternalComponentFactory factory :factories){
            this.factories.add(factory);
        }
    }
}
