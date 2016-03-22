package org.unitedinternet.cosmo.ext;

import java.net.Proxy;

/**
 * Test implementation that creates a <code>Proxy.NO_PROXY</code> object.
 * 
 * @author daniel grigore
 *
 */
public class NoProxyFactory implements ProxyFactory {

    @Override
    public Proxy get(String host) {
        return Proxy.NO_PROXY;
    }

}
