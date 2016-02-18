package org.unitedinternet.cosmo.ext;

import java.net.Proxy;

/**
 * <code>java.net.Proxy</code> factory that creates the appropriate Proxy for each specific host.
 * 
 * @author daniel grigore
 *
 */
public interface ProxyFactory {

    Proxy get(String host);

}