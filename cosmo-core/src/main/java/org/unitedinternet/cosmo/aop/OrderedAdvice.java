/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.aop;

import org.springframework.core.Ordered;

/**
 * Base class for advice that implements Ordered, allowing
 * the order of advice to be determined.
 */
public abstract class OrderedAdvice implements Ordered {
    private int order = 0;
    
    /**
     * Gets order.
     * {@inheritDoc}
     * @return The order.
     */
    public int getOrder() {
        return order;
    }
    /**
     * Sets order.
     * @param order The order.
     */
    public void setOrder(int order) {
        this.order = order;
    }
}
