package org.unitedinternet.cosmo.ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.unitedinternet.cosmo.ext.ProxyFactoryBean.PROP_PROXY_HOST;
import static org.unitedinternet.cosmo.ext.ProxyFactoryBean.PROP_PROXY_PORT;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;

/**
 * 
 * @author daniel grigore
 * @see ProxyFactoryBean
 */
public class EnvironmentProxyFactoryTest {

    @Test
    public void shouldGetNoProxyWhenNoPropertyIsSet() throws Exception {
        ProxyFactoryBean factory = withProperties(new HashMap<String, Object>());
        factory.setApplicationContext(new StaticApplicationContext());
        HttpHost proxy = factory.getObject();
        assertNull(proxy);
    }    

    @Test
    public void voidShouldGetDifferentProxiesWhenPropertiesAreSet() throws Exception {
        String proxyHost = "some_host";
        String proxyPort = "8080";

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROP_PROXY_HOST, proxyHost);
        properties.put(PROP_PROXY_PORT, proxyPort);
        
        ProxyFactoryBean factory = withProperties(properties);

        HttpHost proxy = factory.getObject();
        assertNotNull(proxy);
        assertEquals(proxyHost, proxy.getHostName());
        assertEquals(new Integer(proxyPort).intValue(), proxy.getPort());

    }

    static ProxyFactoryBean withProperties(Map<String, Object> properties) {
        StaticApplicationContext context = new StaticApplicationContext();
        StandardEnvironment env = (StandardEnvironment) context.getEnvironment();
        MutablePropertySources sources = env.getPropertySources();
        sources.addFirst(new MapPropertySource("defaultSource", properties));
        ProxyFactoryBean factory = new ProxyFactoryBean();
        factory.setApplicationContext(context);
        return factory;
    }
}
