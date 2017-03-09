/*
 * Copyright 2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

/**
 * Simple ConnectionProvider implementation that relies on a 
 * ThreadLocal containing the Connection.  
 */
public class SimpleConnectionProvider implements ConnectionProvider {

    private static final long serialVersionUID = 5322027394043358569L;
    private static ThreadLocal<Connection> connectionLocal = 
        new ThreadLocal<Connection>();
    
    public SimpleConnectionProvider() {
    }

    public static void setConnection(Connection conn) {
        connectionLocal.set(conn);
    }
    
    public void close() throws HibernateException {
        connectionLocal.remove();
    }

    public void closeConnection(Connection conn) throws SQLException {
    }

    public void configure(Properties props) throws HibernateException {
    }

    public Connection getConnection() throws SQLException {
        return connectionLocal.get();
    }

    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(@SuppressWarnings("rawtypes") Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }

}