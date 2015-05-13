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
package org.unitedinternet.cosmo.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Contains static helper methods for dealing with TriageStatus
 * objects.
 */
public class TriageStatusUtil {
    public static String label(Integer code) {
        if (code.equals(TriageStatus.CODE_NOW)) {
            return TriageStatus.LABEL_NOW;
        }
        if (code.equals(TriageStatus.CODE_LATER)) {
            return TriageStatus.LABEL_LATER;
        }
        if (code.equals(TriageStatus.CODE_DONE)) {
            return TriageStatus.LABEL_DONE;
        }
        throw new IllegalStateException("Unknown code " + code);
    }

    public static Integer code(String label) {
        if (label.equals(TriageStatus.LABEL_NOW)) {
            return Integer.valueOf(TriageStatus.CODE_NOW);
        }
        if (label.equals(TriageStatus.LABEL_LATER)) {
            return Integer.valueOf(TriageStatus.CODE_LATER);
        }
        if (label.equals(TriageStatus.LABEL_DONE)) {
            return Integer.valueOf(TriageStatus.CODE_DONE);
        }
        throw new IllegalStateException("Unknown label " + label);
    }
    
    public static TriageStatus initialize(TriageStatus ts) {
        ts.setCode(Integer.valueOf(TriageStatus.CODE_NOW));
        ts.setRank(getRank(System.currentTimeMillis()));
        ts.setAutoTriage(Boolean.TRUE);
        return ts;
    }
    
    public static BigDecimal getRank(long date) {
        String time = (date / 1000) + ".00";
        return new BigDecimal(time).negate();
    }
    
    public static Date getDateFromRank(BigDecimal rank) {
        return new Date(rank.negate().scaleByPowerOfTen(3).longValue());
    }
}
