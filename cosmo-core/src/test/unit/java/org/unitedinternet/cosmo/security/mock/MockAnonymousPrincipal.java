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

/**
 */

public class MockAnonymousPrincipal implements Principal {
    private String name;

    /**
     * Constructor.
     * @param name The name.
     */
    public MockAnonymousPrincipal(String name) {
        this.name = name;
    }

    /**
     * Equals.
     * {@inheritDoc}
     * @param another another.
     * @return The result.
     */
    public boolean equals(Object another) {
        if (!(another instanceof MockAnonymousPrincipal)) {
            return false;
        }
        return name.equals(((MockAnonymousPrincipal)another).getName());
    }

    /**
     * toString.
     * {@inheritDoc}
     * @return The string.
     */
    public String toString() {
        return name;
    }
    
    /**
     * Hashcode.
     * {@inheritDoc}
     * @return The hashcode.
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Gets name.
     * {@inheritDoc}
     * @return The name.
     */
    public String getName() {
        return name;
    }
}
