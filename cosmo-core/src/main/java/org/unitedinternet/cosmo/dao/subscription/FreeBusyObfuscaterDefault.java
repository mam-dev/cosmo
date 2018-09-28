package org.unitedinternet.cosmo.dao.subscription;

import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.util.FreeBusyUtil;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.ProdId;

import static org.unitedinternet.cosmo.util.FreeBusyUtil.*;

import org.springframework.stereotype.Component;

/**
 * 
 * @author daniel grigore
 *
 */
@Component
public class FreeBusyObfuscaterDefault implements FreeBusyObfuscater {

    public FreeBusyObfuscaterDefault() {
        // Default one.
    }

    @Override
    public void apply(User owner, ContentItem contentItem) {
        contentItem.setDisplayName(FREE_BUSY_TEXT);
        contentItem.setName(FREE_BUSY_TEXT);        
        EventStamp stamp = (EventStamp) contentItem.getStamp(EventStamp.class);
        if (stamp != null) {
            Calendar calendar = stamp.getEventCalendar();
            stamp.setEventCalendar(copy(calendar));

        }
        EventExceptionStamp exceptionStamp = (EventExceptionStamp) contentItem.getStamp(EventExceptionStamp.class);
        if (exceptionStamp != null) {
            Calendar original = exceptionStamp.getEventCalendar();
            exceptionStamp.setEventCalendar(copy(original));
        }
    }

    private static Calendar copy(Calendar original) {
        if (original != null) {
            ProdId prodId = original.getProductId();
            String productId = prodId.getValue() != null ? prodId.getValue() : "";
            return FreeBusyUtil.getFreeBusyCalendar(original, productId);
        }
        return null;
    }
}
