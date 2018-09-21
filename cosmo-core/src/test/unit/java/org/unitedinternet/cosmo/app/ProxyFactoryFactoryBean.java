package org.unitedinternet.cosmo.app;

import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;
import org.unitedinternet.cosmo.ext.ProxyFactory;

public class ProxyFactoryFactoryBean implements FactoryBean<ProxyFactory> {

    @Override
    public ProxyFactory getObject() throws Exception {
        return Mockito.mock(ProxyFactory.class);
    }

    @Override
    public Class<?> getObjectType() {
        return ProxyFactory.class;
    }

}
