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

/**
 * Test case for Password recovery class.
 *
 */
public class MockPasswordRecoverer extends PasswordRecoverer {

    int nextRecoveryKey = 0;
    int nextPassword = 10000; // password must be min 5 chars
    
    /**
     * Creates recovery key.
     * @return The recovery key.
     */
    @Override
    public String createRecoveryKey() {
        nextRecoveryKey++;
        return Integer.toString(nextRecoveryKey);
    }

    /**
     * Sends recovery key.
     * @param passwordRecovery The recovery password.
     * @param context The password recovery message context.
     */
    public void sendRecovery(PasswordRecovery passwordRecovery,  PasswordRecoveryMessageContext context) {
        assert(passwordRecovery != null);
        assert(context != null);
    }

}
