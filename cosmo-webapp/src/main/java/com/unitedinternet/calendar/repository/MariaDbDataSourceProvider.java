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
public class MariaDbDataSourceProvider implements DataSourceProvider {

    private DataSource dataSource;

    public MariaDbDataSourceProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public DataSourceType getDataSourceType() {
        return DataSourceType.MySQL5InnoDB;
    }

}
