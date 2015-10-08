/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaValidator;
import org.springframework.jdbc.support.JdbcUtils;
import org.unitedinternet.cosmo.CosmoConstants;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.datasource.HibernateSessionFactoryBeanDelegate;
import org.unitedinternet.cosmo.model.ServerProperty;
import org.unitedinternet.cosmo.service.ServerPropertyService;
import org.unitedinternet.cosmo.service.impl.CosmoStartupDataInitializer;

/**
 * A helper class that initializes the Cosmo database schema and populates the
 * database with seed data.
 */
public class DbInitializer {
    private static final Log LOG = LogFactory.getLog(DbInitializer.class);


    private ServerPropertyService serverPropertyService;

    private HibernateSessionFactoryBeanDelegate localSessionFactory;

    private DataSource datasource;
    
    private boolean validateSchema = true;
    
    private CosmoStartupDataInitializer cosmoStartupDataInitializer;
    
    private Collection<DatabaseInitializationCallback> callbacks;
        

	/**
     * Performs initialization tasks if required.
     * 
     * @return <code>true</code> if initialization was required, *
     *         <code>false</code> otherwise>.
     */
    public boolean initialize() {

        // Create DB schema if not present
        if (!isSchemaInitialized()) {
            LOG.info("Creating database");                
            new SchemaExport(localSessionFactory.getConfiguration()).create(true, true);
            LOG.info("Initializing database");
            cosmoStartupDataInitializer.initializeStartupData();
            for(DatabaseInitializationCallback callback : callbacks){
            	callback.execute();
            }
            return true;
        } else {
            // Verify that db schema is supported by server
            // TODO: here is where we will eventually put auto-update
            // scripts. For now, the server will not start if an
            // unsupported version is found.
         //   checkSchemaVersion();
            
            // More thorough schema validation
            if(validateSchema) {
                validateSchema();
            }
            
            return false;
        }
    }

    public void setDataSource(DataSource datasource) {
        this.datasource = datasource;
    }
    
    public void setValidateSchema(boolean validateSchema) {
        this.validateSchema = validateSchema;
    }

    public void setLocalSessionFactory(
            HibernateSessionFactoryBeanDelegate hibernateLocalSessionFactoryDelegate) {
        this.localSessionFactory = hibernateLocalSessionFactoryDelegate;
    }

    public void setServerPropertyService(
            ServerPropertyService serverPropertyService) {
        this.serverPropertyService = serverPropertyService;
    }
    
    public void setCallbacks(Collection<DatabaseInitializationCallback> callbacks) {
		this.callbacks = callbacks;
	}

    //default to allow usage in tests
    /**
     * 
     * @return checks if schema is initialized
     */
    boolean isSchemaInitialized() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = datasource.getConnection();
            ps = conn.prepareStatement("select count(*) from server_properties");
            rs = ps.executeQuery();
            return true;
        } catch (SQLException e) {
            //if the schema is not created yet, conn.prepareStatement fails
            return false;
        } finally {
            try{
                // no rs created if the schema is not created
                if(rs!= null){
                    rs.close();
                }
            }catch (SQLException e) {
                LOG.error(e);
            }
            try {
                if(ps != null){
                    ps.close();
                }
            } catch (SQLException e) {
                LOG.error(e);
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOG.error(e);
            }
        }
    }

    /**
     * Chema version matches schema version in DB
     */
    private void checkSchemaVersion() {
        String schemaVersion = serverPropertyService
                .getServerProperty(ServerProperty.PROP_SCHEMA_VERSION);
        //Fix Log Forging - Java Fortify
        //Writing unvalidated user input to log files can allow an attacker to forge log
        //entries or inject malicious content into the logs.
        LOG.info("found schema version " + schemaVersion);
        if (!CosmoConstants.SCHEMA_VERSION.equals(schemaVersion)) {
            LOG.error("Schema version does not match (" + schemaVersion + ":"
                    + CosmoConstants.SCHEMA_VERSION);
            throw new CosmoException(
                    "Schema version found in database does not match schema version required by server",
                    new CosmoException());
        }
    }
    
    /**
     * Schema validation
     */
    private void validateSchema() {
        
        try {
            new SchemaValidator(localSessionFactory.getConfiguration()).validate();
            LOG.info("schema validation passed");
        } catch (HibernateException rte) {
            LOG.error("error validating schema", rte);
            throw rte;
        }
    }

    
    /**
     * Execute the given schema script on the given JDBC Connection.
     * <p>Note that the default implementation will log unsuccessful statements
     * and continue to execute. Override the <code>executeSchemaStatement</code>
     * method to treat failures differently.
     * @param con the JDBC Connection to execute the script on
     * @param sql the SQL statements to execute
     * @throws SQLException if thrown by JDBC methods
     * @see #executeSchemaStatement
     */
    protected void executeSchemaScript(Connection con, String[] sql) throws SQLException {
        if (sql != null && sql.length > 0) {
            boolean oldAutoCommit = con.getAutoCommit();
            if (!oldAutoCommit) {
                con.setAutoCommit(true);
            }
            try {
                Statement stmt = con.createStatement();
                try {
                    for (String sqlStmt : sql) {
                        executeSchemaStatement(stmt, sqlStmt);
                    }
                }
                finally {
                    JdbcUtils.closeStatement(stmt);
                }
            }
            finally {
                if (!oldAutoCommit) {
                    try{
                        con.setAutoCommit(false);
                    } catch(SQLException e){
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }
    }
    
    /**
     * Execute the given schema SQL on the given JDBC Statement.
     * <p>Note that the default implementation will log unsuccessful statements
     * and continue to execute. Override this method to treat failures differently.
     * @param stmt the JDBC Statement to execute the SQL on
     * @param sql the SQL statement to execute
     * @throws SQLException if thrown by JDBC methods (and considered fatal)
     */
    protected void executeSchemaStatement(Statement stmt, String sql) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing schema statement: " + sql);
        }
        try {
            stmt.executeUpdate(sql);
        }
        catch (SQLException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Unsuccessful schema statement: " + sql, ex);
            }
        }
    }

	public void setCosmoStartupDataInitializer(
			CosmoStartupDataInitializer cosmoStartupDataInitializer) {
		this.cosmoStartupDataInitializer = cosmoStartupDataInitializer;
	}
}