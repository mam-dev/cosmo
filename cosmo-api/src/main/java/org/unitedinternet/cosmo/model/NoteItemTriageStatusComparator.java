package org.unitedinternet.cosmo.model;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compare NoteItems using a rank calculated from TriageStatus.rank
 * or event startDate, or last modified date.
 */
@SuppressWarnings("serial")
public class NoteItemTriageStatusComparator implements Comparator<NoteItem>, Serializable {

    boolean reverse = false;
    
    public NoteItemTriageStatusComparator() {
    }
     
    public NoteItemTriageStatusComparator(boolean reverse) {
        this.reverse = reverse;
    }
    
    public int compare(NoteItem note1, NoteItem note2) {
        if(note1.getUid().equals(note2.getUid())) {
            return 0;
        }
     
        // Calculate a rank depending on the type of item and
        // the attributes present
        long rank1 = getRank(note1);
        long rank2 = getRank(note2);
        
        if(rank1>rank2) {
            return reverse? -1 : 1;
        }
        else {
            return reverse? 1 : -1;
        }
    }
    
    /**
     * Calculate rank of NoteItem.  Rank is the absolute value of the
     * triageStatus rank.  If triageStatus rank is not present, then
     * it is the value in milliseconds of the start time of the event.
     * If the note is not an event then it is the last modified date.
     */
    private long getRank(NoteItem note) {
        // Use triageStatusRank * 1000 to normalize to
        // unix timestamp in milliseconds. 
        if(note.getTriageStatus()!=null && note.getTriageStatus().getRank()!=null) {
            return note.getTriageStatus().getRank().scaleByPowerOfTen(3).longValue();
        }
        
        // otherwise use startDate * -1
        BaseEventStamp eventStamp = StampUtils.getBaseEventStamp(note);
        if(eventStamp!=null) {
            return eventStamp.getStartDate().getTime()*-1;
        }
        
        // or occurrence date * -1
        if(note instanceof NoteOccurrence) {
            return ((NoteOccurrence) note).getOccurrenceDate().getTime()*-1;
        }
        
        // use modified date * -1 as a last resort
        return note.getModifiedDate().getTime()*-1;
    }
}
