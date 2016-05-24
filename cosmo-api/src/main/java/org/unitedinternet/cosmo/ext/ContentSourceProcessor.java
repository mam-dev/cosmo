/**
 * 
 */
package org.unitedinternet.cosmo.ext;

import net.fortuna.ical4j.model.Calendar;

/**
 * 
 * Component that allows implementing applications to perform additional processing
 * on the <code>calendar</code> obtained from the <code>ContentSource</code>
 * 
 * @author stefan popescu
 *
 */
public interface ContentSourceProcessor {
    
    public void postProcess(Calendar calendar);

}
