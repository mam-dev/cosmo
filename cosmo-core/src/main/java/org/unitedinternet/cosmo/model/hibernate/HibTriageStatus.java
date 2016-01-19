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
package org.unitedinternet.cosmo.model.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.unitedinternet.cosmo.model.TriageStatus;

/**
 * Hibernate persistent TriageStatus.
 */
@SuppressWarnings("serial")
@Embeddable
public class HibTriageStatus implements TriageStatus, Serializable {

    @Column(name = "triagestatuscode")
    private Integer code = null;
    
    @Column(name = "triagestatusrank", precision = 12, scale = 2)
    @Type(type="org.hibernate.type.BigDecimalType")
    private BigDecimal rank = null;
    
    @Column(name = "isautotriage")
    private Boolean autoTriage = null;
    
    /**
     * Constructor.
     */
    public HibTriageStatus() {
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.TriageStatus#getCode()
     */
    public Integer getCode() {
        return code;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.TriageStatus#setCode(java.lang.Integer)
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.TriageStatus#getRank()
     */
    public BigDecimal getRank() {
        return rank;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.TriageStatus#setRank(java.math.BigDecimal)
     */
    public void setRank(BigDecimal rank) {
        this.rank = rank;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.TriageStatus#getAutoTriage()
     */
    public Boolean getAutoTriage() {
        return autoTriage;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.TriageStatus#setAutoTriage(java.lang.Boolean)
     */
    public void setAutoTriage(Boolean autoTriage) {
        this.autoTriage = autoTriage;
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.TriageStatus#copy()
     */
    public TriageStatus copy() {
        TriageStatus copy = new HibTriageStatus();
        copy.setCode(code);
        copy.setRank(rank);
        copy.setAutoTriage(autoTriage);
        return copy;
    }

    public String toString() {
        return new ToStringBuilder(this).
            append("code", code).
            append("rank", rank).
            append("autoTriage", autoTriage).
            toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof HibTriageStatus)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        HibTriageStatus ts = (HibTriageStatus) obj;
        return new EqualsBuilder().
            append(code, ts.code).
            append(rank, ts.rank).
            append(autoTriage, ts.autoTriage).
            isEquals();
    }
    
    

    public static TriageStatus createInitialized() {
        TriageStatus ts = new HibTriageStatus();
        ts.setCode(Integer.valueOf(CODE_NOW));
        // XXX there's gotta be a better way!
        String time = (System.currentTimeMillis() / 1000) + ".00";
        ts.setRank(new BigDecimal(time).negate());
        ts.setAutoTriage(Boolean.TRUE);
        return ts;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((autoTriage == null) ? 0 : autoTriage.hashCode());
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((rank == null) ? 0 : rank.hashCode());
        return result;
    }

   
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

    public static Integer code(String label) {
        if (label.equals(LABEL_NOW)) {
            return Integer.valueOf(CODE_NOW);
        }
        if (label.equals(LABEL_LATER)) {
            return Integer.valueOf(CODE_LATER);
        }
        if (label.equals(LABEL_DONE)) {
            return Integer.valueOf(CODE_DONE);
        }
        throw new IllegalStateException("Unknown label " + label);
    }
}
