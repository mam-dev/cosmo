package org.unitedinternet.cosmo.dao;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;

/**
 * {@link FactoryBean} for creating a <code>ItemDao</code> implementation that delegates all the method invocations to
 * <code>ContentDaoInvocationHandler</code> instance.
 * 
 * @author daniel grigore
 * @see ContentDao
 * @see ContentDaoInvocationHandler
 */
public class ContentDaoProxyFactory implements FactoryBean<ContentDao> {

    private final ContentDaoInvocationHandler invocationHandler;

    public ContentDaoProxyFactory(ContentDaoInvocationHandler invocationHandler) {
        super();
        this.invocationHandler = invocationHandler;
    }

    @Override
    public ContentDao getObject() throws Exception {
        return (ContentDao) Proxy.newProxyInstance(this.invocationHandler.getClass().getClassLoader(),
                new Class<?>[] { ContentDao.class }, invocationHandler);
    }

    @Override
    public Class<?> getObjectType() {
        return ItemDao.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
