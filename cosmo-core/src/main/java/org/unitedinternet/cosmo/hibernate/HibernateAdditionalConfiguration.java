package org.unitedinternet.cosmo.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;
import org.unitedinternet.cosmo.model.hibernate.AuditableObjectInterceptor;
import org.unitedinternet.cosmo.model.hibernate.EventStampInterceptor;

/**
 * Standard way of registering hibernate interceptors in Sprint boot applications.
 * 
 * @author daniel grigore
 *
 */
@Configuration
public class HibernateAdditionalConfiguration implements HibernatePropertiesCustomizer {

    @Autowired
    private AuditableObjectInterceptor auditableObjectInterceptor;

    @Autowired
    private EventStampInterceptor eventStampInterceptor;

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        CompoundInterceptor compoundInterceptor = new CompoundInterceptor();
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.add(auditableObjectInterceptor);
        interceptors.add(eventStampInterceptor);
        compoundInterceptor.setInterceptors(interceptors);
        hibernateProperties.put("hibernate.session_factory.interceptor", compoundInterceptor);
    }
}
