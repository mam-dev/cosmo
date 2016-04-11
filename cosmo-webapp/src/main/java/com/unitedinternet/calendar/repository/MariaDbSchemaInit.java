package com.unitedinternet.calendar.repository;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.MariaDB4jService;

/**
 * Hook service that creates the <code>cosmo</code> schema. Note that by default the embedded MariaDB instance is
 * started with a 'root' user without a password. @see https://github.com/vorburger/MariaDB4j
 * 
 * @author daniel grigore
 * @author stefan popescu
 *
 */
public class MariaDbSchemaInit {

    private MariaDB4jService service;

    public MariaDbSchemaInit(MariaDB4jService service) {
        super();
        this.service = service;
    }

    public void init() throws ManagedProcessException {
        this.service.getDB().run("CREATE DATABASE IF NOT EXISTS cosmo DEFAULT CHARACTER SET utf8;");
    }
}
