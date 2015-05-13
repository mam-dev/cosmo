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
package org.unitedinternet.cosmo.service.account;


import org.unitedinternet.cosmo.model.PasswordRecovery;
import org.unitedinternet.cosmo.util.VersionFourGenerator;

public abstract class PasswordRecoverer {
    private VersionFourGenerator idGenerator;

    /**
     * 
     * @param passwordRecovery
     */
    public abstract void sendRecovery(PasswordRecovery passwordRecovery,
                             PasswordRecoveryMessageContext context);
    
    /**
     * 
     * @return
     */
    public String createRecoveryKey() {
        return idGenerator.nextStringIdentifier();
    }

    public VersionFourGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(VersionFourGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
    
}
