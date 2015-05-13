/*
 * NonManagedDataSourceProvider.java May 7, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package com.unitedinternet.calendar.repository;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.unitedinternet.cosmo.db.DataSourceProvider;
import org.unitedinternet.cosmo.db.DataSourceType;
import org.unitedinternet.cosmo.metadata.CalendarRepository;

//@CalendarRepository
public class NonManagedDataSourceProvider implements DataSourceProvider{
    
    DataSource ds;
    public NonManagedDataSourceProvider() {
        BasicDataSource dataSource = new BasicDataSource();
        
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost/begenda_os?autoReconnect=true");
        dataSource.setUsername("begenda");
        dataSource.setPassword("*");
        dataSource.setMaxActive(100);
        dataSource.setMaxIdle(20);
        dataSource.setMaxWait(10000);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setDefaultAutoCommit(false);
        
        this.ds = dataSource;
    }

    @Override
    public DataSource getDataSource() {
        return this.ds;
    }

    @Override
    public DataSourceType getDataSourceType() {
        return null;
    }
}
