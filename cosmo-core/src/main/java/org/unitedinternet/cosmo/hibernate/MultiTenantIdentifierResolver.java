package org.unitedinternet.cosmo.hibernate;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * Resolves the datasource which has to be used.
 *
 * @author Iulia
 */
public class MultiTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        return "EU1";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        // TODO Auto-generated method stub
        return false;
    }

}
