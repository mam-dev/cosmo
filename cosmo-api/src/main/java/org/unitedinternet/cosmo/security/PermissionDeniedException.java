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
package org.unitedinternet.cosmo.security;


/**
 * Thrown when a principal attempts to perform an operation on an item
 * for which it does not have the appropriate permissions.
 */
@SuppressWarnings("serial")
public class PermissionDeniedException extends CosmoSecurityException {

    /**
     * 
     * @param message The exception message.
     */
    public PermissionDeniedException(String message) {
        super(message);
    }

    /**
     * 
     * @param message The exception message.
     * @param cause - If somethig is wrong this exception is thrown.
     */
    public PermissionDeniedException(String message,
                                     Throwable cause) {
        super(message, cause);
    }
}

