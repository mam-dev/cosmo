package org.unitedinternet.cosmo.ext;

import java.net.URL;

import org.apache.http.HttpHost;
import org.springframework.stereotype.Component;

/**
 * Default factory that uses no proxy. Other implementations might provide another primary bean for this.
 * 
 * @author daniel grigore
 *
 */
@Component
public class ProxyFactoryDefault implements ProxyFactory {

    public ProxyFactoryDefault() {

    }

    @Override
    public HttpHost getProxy(URL url) {
        return null;
    }
}
