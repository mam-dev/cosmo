/*
 * AbstractDaoImpl.java Feb 16, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.dao.hibernate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.StatelessSession;

/**
 * Abstract Dao implementation. It is used for general purpose operations.
 *
 * @author cristina coman
 */
public abstract class AbstractDaoImpl {

    private static final Log LOG = LogFactory.getLog(AbstractDaoImpl.class);
    
    @PersistenceContext
    private EntityManager entityManager;

    
    
    public AbstractDaoImpl() {
        
    }

    

    /**
     * @return Returns the current Hibernate session.
     */
    protected Session getSession() {
        return (Session) this.entityManager;
    }

    /**
     * Gets a <b>new</b> Hibernate state less session which needs to be closed after usage, otherwise it will cause
     * connection leakage.
     * 
     * @return a <b>new</b> Hibernate state less session which needs to be closed after usage.
     */
    protected StatelessSession openStatelessSession() {
        return (StatelessSession) this.entityManager;
    }

    /**
     * Logs constraint violeation exception
     *
     * @param cve
     *            - if something is wrong this exception is thrown.
     */
    protected void logConstraintViolationException(ConstraintViolationException cve) {
        // log more info about the constraint violation
        if (LOG.isDebugEnabled()) {
            LOG.debug(cve.getLocalizedMessage());
            for (ConstraintViolation<?> cv : cve.getConstraintViolations()) {
                LOG.debug("property name: " + cv.getPropertyPath() + " property: " + cv.getInvalidValue());
            }
        }
    }

}
