/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.security.mock;

import java.security.Principal;

import org.unitedinternet.cosmo.model.User;

/**
 */
public class MockUserPrincipal implements Principal {
    private User user;

    /**
     * Constructor.
     * @param user The user.
     */
    public MockUserPrincipal(User user) {
        this.user = user;
    }

    /**
     * Equals.
     * {@inheritDoc}
     * @param another another.
     * @return The boolean equals.
     */
    public boolean equals(Object another) {
        if (!(another instanceof MockUserPrincipal)) {
            return false;
        }
        return user.equals(((MockUserPrincipal)another).getUser());
    }

    /**
     * ToString.
     * {@inheritDoc}
     * @return The string.
     */
    public String toString() {
        return user.toString();
    }

    /**
     * HashCode.
     * {@inheritDoc}
     * @return The hashcode.
     */
    public int hashCode() {
        return user.hashCode();
    }

    /**
     * Gets name.
     * {@inheritDoc}
     * @return The name.
     */
    public String getName() {
        return user.getUsername();
    }

    /**
     * Gets user.
     * @return The user.
     */
    public User getUser() {
        return user;
    }
}
