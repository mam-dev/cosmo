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
package org.unitedinternet.cosmo.model;

/**
 * An exception indicating that the value provided for an item
 * attribute was too large or too small.
 */
@SuppressWarnings("serial")
public class DataSizeException extends RuntimeException {

    /**
     */
    public DataSizeException(String message) {
        super(message);
    }

    /**
     */
    public DataSizeException(String message, Throwable cause) {
        super(message, cause);
    }
}
