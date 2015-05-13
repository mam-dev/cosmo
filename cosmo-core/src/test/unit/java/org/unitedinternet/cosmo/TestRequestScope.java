/*
 * SolrScope.java Dec 2, 2010
 * 
 * Copyright (c) 2010 1&1 Internet AG. All rights reserved.
 * 
 * $Id: TestRequestScope.java 9131 2014-08-13 14:23:33Z izein $
 */
package org.unitedinternet.cosmo;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * Custom scope. It defines the lifecicle of beans used with REQUEST scope.
 * It is used ONLY for testing purpose. 
 * @author iulia
 *
 */
@Ignore
public class TestRequestScope implements Scope{
    
    /**
     * Constructor.
     */
    public TestRequestScope() {
        super();
    }

    /**
     * This map contains for each bean name or ID the created object. The objects
     * are created with a spring object factory.
     */
    private Map<String , Object> objectMap = new HashMap<String , Object>();
    
    /**
     * 
     * {@inheritDoc}
     * @param name The name.
     * @param objectFactory The object factory.
     * @return object.
     */
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Object object = objectMap.get(name);
        if (null == object) {
            object = objectFactory.getObject();
            objectMap.put(name, object);
        }
        return object;
    }
    
    /**
     * 
     * {@inheritDoc}
     * @return The conversation id.
     */
    @Override
    public String getConversationId() {
        return null;
    }

    /**
     * Add the given bean to the list of disposable beans in this factory,
     * registering its DisposableBean interface and/or the given destroy method
     * to be called on factory shutdown (if applicable). Only applies to singletons.
     * {@inheritDoc}
     * @param name The name.
     * @param callback The runnable.
     */
    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        //do not register
    }

    /**
     * 
     * {@inheritDoc}
     * @param name - The name.
     * @return object.
     */
    @Override
    public Object remove(String name) {       
        return objectMap.remove(name);
    }
    
    /**
     * 
     * {@inheritDoc}
     * @param key - The key.
     * @return - The object.
     */
    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }
    
    /**
     * Destroys the context, i.e. removes all current beans with "request" scope.
     */
    public void cleanContext(){
        objectMap = new HashMap<String , Object>();
    }
 
}
