package org.unitedinternet.cosmo.model.filter;

import org.unitedinternet.cosmo.model.QName;

/**
 * TODO
 * 
 * @author daniel grigore
 *
 */
public class StringAttributeFilter extends AttributeFilter {
    FilterCriteria value = null;

    public StringAttributeFilter() {
    }

    public StringAttributeFilter(QName qname) {
        this.setQname(qname);
    }

    public FilterCriteria getValue() {
        return value;
    }

    /**
     * Match a TextAttribute with a string
     * 
     * @param value
     */
    public void setValue(FilterCriteria value) {
        this.value = value;
    }
}
