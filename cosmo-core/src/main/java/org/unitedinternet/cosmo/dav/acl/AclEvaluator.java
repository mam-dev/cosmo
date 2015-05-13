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
package org.unitedinternet.cosmo.dav.acl;

import org.unitedinternet.cosmo.model.Item;

/**
 * <p>
 * An interface for components that evaluate DAV access control lists.
 * </p>
 */
public interface AclEvaluator {

    /**
     * <p>
     * Checks an item's access control list to see if it contains an
     * access control entry that grants this evaluator's principal a
     * particular privilege.
     * </p>
     */
    boolean evaluate(Item item,
                            DavPrivilege privilege);

    /*
     * <p>
     * Returns this evaluator's principal.
     * </p>
     */
    Object getPrincipal();
}
