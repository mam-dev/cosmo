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
import org.unitedinternet.cosmo.model.User;

/**
 * <p>
 * An ACL evaluator for user principals.
 * </p>
 */
public class UserAclEvaluator implements AclEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(UserAclEvaluator.class);

    private User principal;

    public UserAclEvaluator(User principal) {
        this.principal = principal;
    }

    // AclEvaluator methods

    /**
     * <p>
     * Returns true if this evaluator's principal has the specified privilege
     * on the resource represented by the given item.
     * </p>
     * <p>
     * This implementation returns true in the following cases:
     * </p>
     * <ul>
     * <li> The principal is an administrator </li>
     * <li> The specified privilege is
     * <code>DAV:read-current-user-privilege-set</code>, since a user
     * automatically has that privilege for all items </li>
     * <li> The principal is the same as the owner of the given item, since
     * any user principal has all permissions on any item he owns </li>
     * </ul>
     */
    public boolean evaluate(Item item, DavPrivilege privilege) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Evaluating privilege {} against item {} owned by {} for principal {}", privilege, item.getName(),
                    item.getOwner().getUsername(), principal.getUsername());
        }
        if (principal.getAdmin()) {
            return true;
        }
        if (privilege.equals(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET)) {
            return true;
        }
        return item.getOwner().equals(principal);
    }

    /*
     * <p>
     * Returns this evaluator's principal.
     * </p>
     */
    public Object getPrincipal() {
        return principal;
    }

    // our methods

    /**
     * <p>
     * Returns true if this evaluator's principal has the specified privilege
     * on the user principal collection.
     * </p>
     * <p>
     * This implementation returns true in the following cases:
     * </p>
     * <ul>
     * <li> The principal an administrator </li>
     * <li> The specified privilege is <code>DAV:read</code>
     * <code>DAV:read-current-user-privilege-set</code>, since a user
     * automatically has that privilege for the user principal collection
     * </li>
     * </ul>
     */
    public boolean evaluateUserPrincipalCollection(DavPrivilege privilege) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Evaluating privilege " + privilege + " against user principal collection " +
            		"for principal " + principal.getUsername());
        }
        if (principal.getAdmin()) {
            return true;
        }
        return privilege.equals(DavPrivilege.READ) ||
               privilege.equals(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET);
    }

    /**
     * <p>
     * Returns true if this evaluator's principal has the specified privilege
     * on the principal resource represented by the given user.
     * </p>
     * <p>
     * This implementation returns true in the following cases:
     * </p>
     * <ul>
     * <li> The principal is an administrator </li>
     * <li> The specified privilege is
     * <code>DAV:read-current-user-privilege-set</code>, since a user
     * automatically has that privilege for all items </li>
     * <li> The principal is the same as the given user, since any user
     * principal has all permissions on his user principal resource </li>
     * </ul>
     */
    public boolean evaluateUserPrincipal(User user,
                                         DavPrivilege privilege) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Evaluating privilege " + privilege + " against user principal " + 
                    user.getUsername() + " for principal " + principal.getUsername());
        }
        if (principal.getAdmin()) {
            return true;
        }
        if (privilege.equals(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET)) {
            return true;
        }
        return user.equals(principal);
    }
}
