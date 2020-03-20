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
package org.unitedinternet.cosmo.dav.acl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Ticket;

/**
 * <p>
 * An ACL evaluator for Ticket principals.
 * </p>
 */
public class TicketAclEvaluator implements AclEvaluator {
    
    private static final Logger LOG = LoggerFactory.getLogger(TicketAclEvaluator.class);

    private Ticket principal;
    private DavPrivilegeSet privileges;

    public TicketAclEvaluator(Ticket principal) {
        this.principal = principal;
        this.privileges = new DavPrivilegeSet(principal);
    }

    /**
     * <p>
     * Returns true if this evaluator's principal has the specified privilege
     * on the resource represented by the given item.
     * </p>
     * <p>
     * This implementation returns true in the following cases:
     * </p>
     * <ul>
     * <li> The specified privilege is
     * <code>DAV:read-current-user-privilege-set</code>, since a principal
     * automatically has that privilege for all items </li>
     * <li> The principal's privilege set contains the specified privilege. </li>
     * </ul>
     * <p>
     * It is assumed that the authentication procedure validated that the
     * principal is granted to this resource.
     * </p>
     */
    public boolean evaluate(Item item, DavPrivilege privilege) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Evaluating privilege {} against item {} for ticket {} with privileges {} ", privilege,
                    item.getName(), principal.getKey(), privileges);
        }
        if (privilege.equals(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET)) {
            return true;
        }
        return privileges.containsRecursive(privilege);
    }

    /*
     * <p>
     * Returns this evaluator's principal.
     * </p>
     */
    public Object getPrincipal() {
        return principal;
    }
}
