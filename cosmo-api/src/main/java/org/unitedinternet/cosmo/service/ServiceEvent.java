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
package org.unitedinternet.cosmo.service;

/**
 * <p>
 * Encapsulates the state needed for a {@link ServiceListener} to act
 * when a service event occurs.
 * </p>
 * <p>
 * A service event has a string identifier that uniquely identifies
 * the event in question. Examples: <code>CreateUser</code>,
 * <code>RemoveContent</code>.
 * </p>
 * <p>
 * The state of a service event is provided as an array of
 * <code>Object</code>s. This allows each individual service method to
 * provide whatever objects are required for listeners to handle the
 * service event without causing a proliferation of event subclasses.
 * </p>
 */
public class ServiceEvent {

    private String id;
    private Object[] state;

    public ServiceEvent(String id) {
        this(id, new Object[] {});
    }

    public ServiceEvent(String id,
                        Object... state) {
        this.id = id;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public Object[] getState() {
        return state != null ? state.clone() : null;
    }
}
