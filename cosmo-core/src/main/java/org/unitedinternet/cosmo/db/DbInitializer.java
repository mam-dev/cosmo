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

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * XXX - Run this spring context is about to start. A helper class that initializes the Cosmo database schema and populates the
 * database with seed data.
 */
public class DbInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(DbInitializer.class);

    private static final String PATH_SCHEMA = "/db/cosmo-schema.sql";

    private EntityManagerFactory localSessionFactory;

    private DataSource datasource;

    private Collection<? extends DatabaseInitializationCallback> callbacks = Collections.emptyList();

    /**
     * Performs initialization tasks if required.
     * 
     * @return <code>true</code> if initialization was required, * <code>false</code> otherwise>.
     */
    public void initialize() {
        // Create DB schema if not present
        if (!isSchemaInitialized()) {
            this.executeStatements(PATH_SCHEMA);
            LOG.info("[DB-startup] Cosmo database structure created successfully.");
            for (DatabaseInitializationCallback callback : callbacks) {
                callback.execute();
            }
        }
        // More thorough schema validation
        validateSchema();
    }

    public void executeStatements(String resource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(this.datasource);
        for (String statement : this.readStatements(resource)) {
            LOG.info("\n" + statement);
            jdbcTemplate.update(statement);
        }
    }

    private List<String> readStatements(String resource) {
        List<String> statements = new ArrayList<>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(this.getClass().getResourceAsStream(resource), StandardCharsets.UTF_8.name());
            scanner.useDelimiter(";");
            while (scanner.hasNext()) {
                String statement = scanner.next().replace("\n", " ").trim();
                if (!statement.isEmpty()) {
                    statements.add(statement);
                }
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return statements;
    }

    public void setDataSource(DataSource datasource) {
        this.datasource = datasource;
    }

    public void setCallbacks(Collection<? extends DatabaseInitializationCallback> callbacks) {
        this.callbacks = callbacks;
    }

    // default to allow usage in tests
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
            // if the schema is not created yet, conn.prepareStatement fails
            return false;
        } finally {
            try {
                // no rs created if the schema is not created
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                LOG.error("", e);
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                LOG.error("", e);
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Schema validation
     */
    // TODO
    private void validateSchema() {
        try {
            SessionFactory factory = this.localSessionFactory.unwrap(SessionFactory.class);
            StandardServiceRegistry registry = factory.getSessionFactoryOptions().getServiceRegistry();
            MetadataSources sources = new MetadataSources(registry);
            sources.addPackage("org.unitedinternet.cosmo.model.hibernate");
            Metadata metadata = sources.buildMetadata(registry);
            new SchemaValidator().validate(metadata);
            LOG.info("Schema validation passed");
        } catch (HibernateException e) {
            LOG.error("error validating schema", e);
            throw e;
        }
    }
}