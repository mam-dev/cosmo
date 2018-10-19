package org.unitedinternet.cosmo.dao;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * {@link FactoryBean} for creating a <code>ItemDao</code> implementation that delegates all the method invocations to
 * <code>ContentDaoInvocationHandler</code> instance.
 * 
 * @author daniel grigore
 * @see ContentDao
 * @see ContentDaoInvocationHandler
 */ 
@Configuration
public class ContentDaoProxyFactory{

    @Autowired
    private final ContentDaoInvocationHandler invocationHandler;

    public ContentDaoProxyFactory(ContentDaoInvocationHandler invocationHandler) {
        super();
        this.invocationHandler = invocationHandler;
    }

    @Primary
    @Bean("contentDao")    
    public ContentDao getObject() throws Exception {
        return (ContentDao) Proxy.newProxyInstance(this.invocationHandler.getClass().getClassLoader(),
                new Class<?>[] { ContentDao.class }, invocationHandler);
    }        
}
