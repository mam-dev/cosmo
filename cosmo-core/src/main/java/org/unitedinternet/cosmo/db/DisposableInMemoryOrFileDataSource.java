/*
 * DisposableDerbyDataSOurce.java Mar 9, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id: DisposableInMemoryOrFileDataSource.java 9122 2014-08-13 12:49:29Z izein $
 */
package org.unitedinternet.cosmo.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * Class used to wrap Derby datasource in order to shutdown the database when
 * the bean is disposed.
 * 
 * @author iulia
 * 
 */
public class DisposableInMemoryOrFileDataSource extends DelegatingDataSource implements DisposableBean {
    private static final Log LOG = LogFactory.getLog(DisposableInMemoryOrFileDataSource.class);

    private static final String SHUTDOWN_COMMAND = "SHUTDOWN";

    @Override
    public void destroy() throws Exception {
        try {
            new JdbcTemplate(getTargetDataSource()).execute(SHUTDOWN_COMMAND);
        } catch (Exception e) {
            LOG.error(e);
        }
    }
}
