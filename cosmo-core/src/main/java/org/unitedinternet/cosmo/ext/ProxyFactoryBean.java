package org.unitedinternet.cosmo.ext;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import org.springframework.beans.factory.FactoryBean;

/**
 * TODO Configure it properly.
 * 
 * @author daniel grigore
 *
 */
public class ProxyFactoryBean implements FactoryBean<Proxy> {

    @Override
    public Proxy getObject() throws Exception {
        boolean noproxy = false;
        if (noproxy) {
            return Proxy.NO_PROXY;
        }
        return new Proxy(Type.HTTP, new InetSocketAddress("itproxy-dev.1and1.org", 3128));
    }

    @Override
    public Class<?> getObjectType() {
        return Proxy.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
