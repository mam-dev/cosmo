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
package org.unitedinternet.cosmo.service.impl;

import org.unitedinternet.cosmo.service.ServiceEvent;
import org.unitedinternet.cosmo.service.ServiceListener;

/**
 * Base class for implementations of <code>Service</code>.
 */
public abstract class BaseService {

    protected void fireBeforeEvent(ServiceEvent event,
                                   ServiceListener[] listeners) {
        if (listeners == null) {
            return;
        }
        for (ServiceListener listener : listeners) {
            listener.before(event);
        }
    }

    protected void fireAfterEvent(ServiceEvent event,
                                  ServiceListener[] listeners) {
        if (listeners == null) {
            return;
        }
        for (ServiceListener listener : listeners) {
            listener.after(event);
        }
    }
}
