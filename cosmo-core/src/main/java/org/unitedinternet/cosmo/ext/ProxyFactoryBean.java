package org.unitedinternet.cosmo.ext;

import java.net.URL;
import java.util.Set;

import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.unitedinternet.cosmo.api.ExternalComponentInstanceProvider;
import org.unitedinternet.cosmo.metadata.Callback;

/**
 * <code>Factory</code> that creates a <code>HttpHost</code> by reading the environment properties.
 * 
 * @author daniel grigore
 *
 */
public class ProxyFactoryBean implements FactoryBean<ProxyFactory> {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyFactoryBean.class);

    private static final ProxyFactory DEFAULT_PROXY_FACTORY = new ProxyFactory() {

        @Override
        public HttpHost getProxy(URL url) {
            return null;
        }
    };

    private final ExternalComponentInstanceProvider instanceProvider;

    public ProxyFactoryBean(ExternalComponentInstanceProvider instanceProvider) {
        super();
        this.instanceProvider = instanceProvider;
    }

    @Override
    public ProxyFactory getObject() throws Exception {
        Set<? extends ProxyFactory> proxyFactorySet = this.instanceProvider
                .getImplInstancesAnnotatedWith(Callback.class, ProxyFactory.class);
        if (proxyFactorySet.size() > 0) {
            if (proxyFactorySet.size() != 1) {
                throw new IllegalArgumentException("Found more implementation of: " + ProxyFactory.class.getName());
            }
            ProxyFactory proxyFactory = proxyFactorySet.iterator().next();
            LOG.info("NET use proxyFactory implementation ", proxyFactory.getClass());
            return proxyFactory;
        }
        LOG.info("NET use no proxy.");
        return DEFAULT_PROXY_FACTORY;
    }

    @Override
    public Class<?> getObjectType() {
        return ProxyFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
