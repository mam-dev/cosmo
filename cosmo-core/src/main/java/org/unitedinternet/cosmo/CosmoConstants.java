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
package org.unitedinternet.cosmo;

import java.util.Locale;

/**
 * Defines server-wide constant attributes.
 */
public final class CosmoConstants {
    
    /**
     * The "friendly" name of the product used for casual identification.
     */
    public static final String PRODUCT_NAME = "Cosmo";

    /**
     * A string identifier for Cosmo used to distinguish it from other software products.
     */
    public static final String PRODUCT_ID = "Cosmo";

    public static final Locale LANGUAGE_LOCALE = Locale.ENGLISH;

    private CosmoConstants() {
        // Avoid unneeded instances.
    }
}