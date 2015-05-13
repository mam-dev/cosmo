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
package org.unitedinternet.cosmo.service.triage;

import java.util.Date;

import net.fortuna.ical4j.model.TimeZone;

import org.unitedinternet.cosmo.model.TriageStatus;

/**
 * Provides access to the information needed to process a triage status
 * query.
 */
public class TriageStatusQueryContext {

    private String triageStatus;
    private Date pointInTime;
    private TimeZone timezone;

    /**
     * @param triageStatus triage status label to match (ignored if null)
     * @param pointInTime time that is considered "NOW"
     * @param timezone Optional timezone to use in interpreting
     *                 floating times. If null, the system default
     *                 will be used.
     */
    public TriageStatusQueryContext(String triageStatus,
                                    Date pointInTime,
                                    TimeZone timezone) {
        this.triageStatus = triageStatus;
        this.pointInTime = pointInTime != null ? (Date)pointInTime.clone() : null;
        this.timezone = timezone;
    }

    public boolean isAll() {
        return triageStatus == null;
    }

    public boolean isNow() {
        return triageStatus != null &&
            TriageStatus.LABEL_NOW.equalsIgnoreCase(triageStatus);
    }

    public boolean isLater() {
        return triageStatus != null &&
            TriageStatus.LABEL_LATER.equalsIgnoreCase(triageStatus);
    }

    public boolean isDone() {
        return triageStatus != null &&
            TriageStatus.LABEL_DONE.equalsIgnoreCase(triageStatus);
    }

    public String getTriageStatus() {
        return triageStatus;
    }

    public Date getPointInTime() {
        return pointInTime != null ? (Date)pointInTime.clone() : null;
    }

    public TimeZone getTimeZone() {
        return timezone;
    }
}
