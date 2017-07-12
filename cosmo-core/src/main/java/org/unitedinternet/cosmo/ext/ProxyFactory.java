package org.unitedinternet.cosmo.ext;

import java.net.URL;

import org.apache.http.HttpHost;


public interface ProxyFactory {
    HttpHost getProxy(URL url);
}
