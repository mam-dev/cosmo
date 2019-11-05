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
package org.unitedinternet.cosmo.security;

import org.unitedinternet.cosmo.model.Item;

/**
 * An exception indicating access to an Item failed because
 * the current principal lacks a required privilege.
 */
@SuppressWarnings("serial")
public class ItemSecurityException
    extends CosmoSecurityException {

    private Item item = null;
    private Permission permission;
    
    /**
     */
    public ItemSecurityException(Item item, String message, Permission permission) {
        super(message);
        this.item = item;
        this.permission = permission;
    }

    /**
     */
    public ItemSecurityException(Item item, String message, Throwable cause, Permission permission) {
        super(message, cause);
        this.item = item;
        this.permission = permission;
    }

    public Item getItem() {
        return item;
    }
    
    public Permission getPermission() {
        return permission;
    }

}
