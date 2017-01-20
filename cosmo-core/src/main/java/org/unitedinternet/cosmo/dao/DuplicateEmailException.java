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
package org.unitedinternet.cosmo.dao;

import org.unitedinternet.cosmo.model.User;

/**
 * An exception indicating that an existing user is already using the
 * specified email.
 */
@SuppressWarnings("serial")
public class DuplicateEmailException extends ModelValidationException {

    /**
     */
    public DuplicateEmailException(String userEmail) {
        super(userEmail, "duplicate email: " + userEmail);
    }

    /**
     */
    public DuplicateEmailException(User user, String message) {
        super(user, message);
    }

    /**
     */
    public DuplicateEmailException(User user, String message, Throwable cause) {
        super(user, message, cause);
    }
}
