package org.unitedinternet.cosmo.service.interceptors;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Demo class for showing interceptors are injected in desired order.
 * 
 * @author daniel grigore
 *
 */
@Configuration
public class InterceptorConfigurationClass {

    private Logger LOG = LoggerFactory.getLogger(InterceptorConfigurationClass.class);

    @Autowired(required = false)
    private List<EventAddHandler> addHandlers = new ArrayList<>();

    @Bean
    public String noGoodBean() {
        LOG.info("\n\n\t[EventAddHandlers] injectd in the order: \n{}\n", addHandlers);
        return addHandlers.toString();
    }
}
