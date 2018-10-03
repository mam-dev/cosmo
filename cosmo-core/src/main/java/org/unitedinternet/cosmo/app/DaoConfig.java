package org.unitedinternet.cosmo.app;

import java.net.URL;

import org.apache.http.HttpHost;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.unitedinternet.cosmo.ext.ContentSourceProcessor;
import org.unitedinternet.cosmo.ext.ProxyFactory;

import net.fortuna.ical4j.model.Calendar;

/**
 * TODO - Move this to web app submodule or to better packages.
 * 
 * @author daniel grigore
 *
 */
@Configuration
public class DaoConfig {

    @Bean
    public ProxyFactory proxy() {
        return new ProxyFactory() {
            @Override
            public HttpHost getProxy(URL url) {
                return null;
            }
        };
    }

    @Bean
    public ContentSourceProcessor processors() {
        return new ContentSourceProcessor() {

            @Override
            public void postProcess(Calendar calendar) {
                // DO nothing
            }
        };
    }

}
