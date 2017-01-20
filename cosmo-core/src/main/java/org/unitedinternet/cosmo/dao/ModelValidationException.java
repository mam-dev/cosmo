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

import org.springframework.dao.DataIntegrityViolationException;

/**
 * An exception indicating that a model object's data is syntactically
 * incorrect.
 */
@SuppressWarnings("serial")
public class ModelValidationException
        extends DataIntegrityViolationException {

    public Object offendingObject = null;

    /**
     */
    public ModelValidationException(String message) {
        super(message);
    }

    /**
     */
    public ModelValidationException(Object offendingObject, String message) {
        super(message);
        this.offendingObject = offendingObject;
    }

    /**
     */
    public ModelValidationException(Object offendingObject, String message, Throwable cause) {
        super(message, cause);
        this.offendingObject = offendingObject;
    }

    public Object getOffendingObject() {
        return offendingObject;
    }
}
