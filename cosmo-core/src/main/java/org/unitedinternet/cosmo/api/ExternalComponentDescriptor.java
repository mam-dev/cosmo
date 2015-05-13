/*
 * ApiImplDescriptor.java Apr 24, 2015
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
public class ExternalComponentDescriptor<T> {
    private Class<? extends T> implementationClass;
    
    public Class<? extends T> getImplementationClass() {
        return implementationClass;
    }

    public ExternalComponentDescriptor(Class<? extends T> implementationClass){
        this.implementationClass = implementationClass;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ExternalComponentDescriptor)){
            return false;
        }
        return implementationClass.equals(((ExternalComponentDescriptor<?>)obj).getImplementationClass());
    }
    
    @Override
    public int hashCode() {
        return implementationClass.hashCode();
    }
}
