package org.unitedinternet.cosmo.boot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

/**
 * 
 * @author daniel grigore
 *
 */
@Configuration
public class HttpConfig {

    /**
     * Provide a less restrictive firewall by default and allow applications to overwrite it.
     * 
     * @return <code>HttpFirewall</code> instance.
     */
    @Bean
    public HttpFirewall httpFirewall() {
	return new DefaultHttpFirewall();
    }
}
