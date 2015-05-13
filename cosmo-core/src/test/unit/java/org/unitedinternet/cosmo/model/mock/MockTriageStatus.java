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
package org.unitedinternet.cosmo.model.mock;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.unitedinternet.cosmo.model.TriageStatus;

/**
 * Represents a compound triage status value.
 */
public class MockTriageStatus implements TriageStatus {
    
    private Integer code = null;
    
    private BigDecimal rank = null;
    
    private Boolean autoTriage = null;
    
    /**
     * Constructor.
     */
    public MockTriageStatus() {
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTriageStatus#getCode()
     */
    /**
     * Gets code.
     * @return The code.
     */
    public Integer getCode() {
        return code;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTriageStatus#setCode(java.lang.Integer)
     */
    /**
     * Sets code.
     * @param code The code.
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTriageStatus#getRank()
     */
    /**
     * Gets rank.
     * @return big decimal.
     */
    public BigDecimal getRank() {
        return rank;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTriageStatus#setRank(java.math.BigDecimal)
     */
    /**
     * Sets rank.
     * @param rank The big decimal.
     */
    public void setRank(BigDecimal rank) {
        this.rank = rank;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTriageStatus#getAutoTriage()
     */
    /**
     * Gets auto triage.
     * @return The boolean for auto triage.
     */
    public Boolean getAutoTriage() {
        return autoTriage;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTriageStatus#setAutoTriage(java.lang.Boolean)
     */
    /**
     * Sets auto triage.
     * @param autoTriage auto triage.
     */
    public void setAutoTriage(Boolean autoTriage) {
        this.autoTriage = autoTriage;
    }
        
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTriageStatus#copy()
     */
    /**
     * Copy.
     * @return Triage status.
     */
    public TriageStatus copy() {
        TriageStatus copy = new MockTriageStatus();
        copy.setCode(code);
        copy.setRank(rank);
        copy.setAutoTriage(autoTriage);
        return copy;
    }

    /**
     * ToString.
     * {@inheritDoc}
     * @return The string.
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("code", code).
            append("rank", rank).
            append("autoTriage", autoTriage).
            toString();
    }

    /**
     * Equals.
     * {@inheritDoc}
     * @param obj The object.
     * @return The boolean for equals.
     */
    public boolean equals(Object obj) {
        if (! (obj instanceof MockTriageStatus)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        MockTriageStatus ts = (MockTriageStatus) obj;
        return new EqualsBuilder().
            append(code, ts.code).
            append(rank, ts.rank).
            append(autoTriage, ts.autoTriage).
            isEquals();
    }
    
    /**
     * HashCode. {@inheritDoc}
     * 
     * @return The hashcode.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 23).append(rank).toHashCode();
    }

    /**
     * Creates initialized.
     * @return triage status.
     */
    public static TriageStatus createInitialized() {
        TriageStatus ts = new MockTriageStatus();
        ts.setCode(Integer.valueOf(CODE_NOW));
        // XXX there's gotta be a better way!
        String time = (System.currentTimeMillis() / 1000) + ".00";
        ts.setRank(new BigDecimal(time).negate());
        ts.setAutoTriage(Boolean.TRUE);
        return ts;
    }

    /**
     * The label.
     * @param code The label.
     * @return The label.
     */
    public static String label(Integer code) {
        if (code.equals(CODE_NOW)) {
            return LABEL_NOW;
        }
        if (code.equals(CODE_LATER)) {
            return LABEL_LATER;
        }
        if (code.equals(CODE_DONE)) {
            return LABEL_DONE;
        }
        throw new IllegalStateException("Unknown code " + code);
    }

    /**
     * Code.
     * @param label The label.
     * @return The code.
     */
    public static Integer code(String label) {
        if (label.equals(LABEL_NOW))
            return Integer.valueOf(CODE_NOW);
        if (label.equals(LABEL_LATER))
            return Integer.valueOf(CODE_LATER);
        if (label.equals(LABEL_DONE))
            return Integer.valueOf(CODE_DONE);
        throw new IllegalStateException("Unknown label " + label);
    }
}
