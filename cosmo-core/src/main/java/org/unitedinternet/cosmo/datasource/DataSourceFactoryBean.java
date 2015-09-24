/*
 * DataSourceFactoryBean.java May 7, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.datasource;

import java.util.Collection;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.unitedinternet.cosmo.api.ExternalComponentInstanceProvider;
import org.unitedinternet.cosmo.db.DataSourceProvider;
import org.unitedinternet.cosmo.metadata.CalendarRepository;

public class DataSourceFactoryBean implements FactoryBean<DataSource>{
    
    private ExternalComponentInstanceProvider instanceProvider;
    
    public DataSourceFactoryBean(ExternalComponentInstanceProvider instanceProvider){
        this.instanceProvider = instanceProvider;
    }
    
    @Override
    public DataSource getObject() throws Exception {
        Collection<? extends DataSourceProvider> dsps = instanceProvider.getImplInstancesAnnotatedWith(CalendarRepository.class, DataSourceProvider.class);
        if(dsps.size() != 1){
            throw new IllegalStateException("One DataSourceProvider implementation must exist. Found [" + dsps.size() + "].");
        }
        DataSourceProvider dsp = dsps.iterator().next(); 

        DataSource ds = dsp.getDataSource();
        
        return ds;
    }

    @Override
    public Class<DataSource> getObjectType() {
        return DataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}