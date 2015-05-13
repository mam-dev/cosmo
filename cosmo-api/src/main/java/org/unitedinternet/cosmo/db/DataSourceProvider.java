/*
 * RepositoryDescriptor.java May 6, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.db;

import javax.sql.DataSource;

public interface DataSourceProvider {
    DataSource getDataSource();
    DataSourceType getDataSourceType();
}
