/*
 * RepositoryDescriptorImpl.java May 6, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package com.unitedinternet.calendar.repository;

import javax.sql.DataSource;

import org.unitedinternet.cosmo.db.DataSourceProvider;
import org.unitedinternet.cosmo.db.DataSourceType;
import org.unitedinternet.cosmo.metadata.CalendarRepository;

@CalendarRepository
public class DataSourceProviderImpl implements DataSourceProvider{
    
    private DataSource dataSource;
    private DataSourceType dataSourceType;
    
    public DataSourceProviderImpl(DataSource dataSource, DataSourceType dataSourceType){
        this.dataSource = dataSource;
        this.dataSourceType = dataSourceType;
    }
    
    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

}
