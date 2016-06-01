package org.unitedinternet.cosmo.ext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

/**
 * <code>Factory</code> that creates a <code>HttpHost</code> by reading the environment properties.
 * 
 * @author daniel grigore
 *
 */
public class ProxyFactoryBean implements ApplicationContextAware, FactoryBean<HttpHost> {

    static final String PROP_PROXY_HOST = "external.proxy.host";
    static final String PROP_PROXY_PORT = "external.proxy.port";

    private static final Log LOG = LogFactory.getLog(ProxyFactoryBean.class);

    private Environment env;

    public ProxyFactoryBean() {
        super();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.env = applicationContext.getEnvironment();
    }

    @Override
    public HttpHost getObject() throws Exception {
        String proxyHost = env.getProperty(PROP_PROXY_HOST);
        String proxyPort = env.getProperty(PROP_PROXY_PORT);
        if (proxyHost != null && !proxyHost.trim().isEmpty() && proxyPort != null && !proxyPort.trim().isEmpty()) {
            LOG.info("NET Setting proxy host: " + proxyHost + " and port: " + proxyPort
                    + " for getting external content.");
            return new HttpHost(proxyHost, new Integer(proxyPort));
        }
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return HttpHost.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
