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
package org.unitedinternet.cosmo.server;

import org.unitedinternet.cosmo.CosmoException;

/**
 * Encapsulates a view in time of the server status.
 */
public class StatusSnapshot {

    private long totalMemory;
    private long freeMemory;
    private long usedMemory;
    private long maxMemory;

    /**
     */
    public StatusSnapshot() {
        Runtime runtime = Runtime.getRuntime();

        totalMemory = (long) (runtime.totalMemory() / 1024);
        freeMemory = (long) (runtime.freeMemory() / 1024);
        usedMemory = (long) totalMemory - freeMemory;
        maxMemory = (long) (runtime.maxMemory() / 1024);
    }

    /**
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append("jvm.memory.max=").append(maxMemory).append("\n");
        buf.append("jvm.memory.total=").append(totalMemory).append("\n");
        buf.append("jvm.memory.used=").append(usedMemory).append("\n");
        buf.append("jvm.memory.free=").append(freeMemory).append("\n");

        return buf.toString();
    }

    /**
     */
    public byte[] toBytes() {
        try {
            return toString().getBytes("UTF-8");
        } catch (Exception e) {
            throw new CosmoException("UTF-8 not supported?", e);
        }
    }

    /**
     */
    public long getTotalMemory() {
        return totalMemory;
    }

    /**
     */
    public long getFreeMemory() {
        return freeMemory;
    }

    /**
     */
    public long getUsedMemory() {
        return usedMemory;
    }

    /**
     */
    public long getMaxMemory() {
        return maxMemory;
    }
}
