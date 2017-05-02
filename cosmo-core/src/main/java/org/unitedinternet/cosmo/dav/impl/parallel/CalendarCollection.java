package org.unitedinternet.cosmo.dav.impl.parallel;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.dao.external.UuidExternalGenerator;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.LockedException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.acl.DavAce;
import org.unitedinternet.cosmo.dav.acl.DavAcl;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.report.PrincipalPropertySearchReport;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarLocationException;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarResourceException;
import org.unitedinternet.cosmo.dav.caldav.UidConflictException;
import org.unitedinternet.cosmo.dav.caldav.report.FreeBusyReport;
import org.unitedinternet.cosmo.dav.caldav.report.MultigetReport;
import org.unitedinternet.cosmo.dav.caldav.report.QueryReport;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;
import org.unitedinternet.cosmo.dav.impl.DavEvent;
import org.unitedinternet.cosmo.dav.impl.DavItemContent;
import org.unitedinternet.cosmo.dav.parallel.CalDavResource;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionLockedException;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.StampUtils;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

import com.google.common.collect.Sets;

public class CalendarCollection extends CalDavCollectionBase {

	    private static final Set<String> DEAD_PROPERTY_FILTER = new HashSet<String>();
	    private static final Set<ReportType> REPORT_TYPES = new HashSet<ReportType>();

	    private List<DavResource> members;

	    static {
	        registerLiveProperty(EXCLUDEFREEBUSYROLLUP);

	        REPORT_TYPES.add(FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY);
	        REPORT_TYPES.add(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET);
	        REPORT_TYPES.add(QueryReport.REPORT_TYPE_CALDAV_QUERY);
	        REPORT_TYPES.add(PrincipalMatchReport.REPORT_TYPE);
	        REPORT_TYPES.add(PrincipalPropertySearchReport.REPORT_TYPE_PRINCIPAL_PROPERTY_SEARCH);

	        DEAD_PROPERTY_FILTER.add(CollectionItem.class.getName());
	    }
	
	@Override
	public CalDavResource getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSupportedMethods() {
        // calendar collections not allowed inside calendar collections
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, PUT, COPY, DELETE, MOVE, MKTICKET, DELTICKET, REPORT";
    }

	@Override
	protected void saveContent(DavItemContent member) throws CosmoDavException {
        if (!(member instanceof DavCalendarResource)) {
            throw new IllegalArgumentException("member not DavCalendarResource");
        }

        if (member instanceof DavEvent) {
            saveEvent(member);
        } else {
            try {
                super.saveContent(member);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            }
        }
    }
	
	private void saveEvent(DavItemContent member) throws CosmoDavException {

		ContentItem content = (ContentItem) member.getItem();
		EventStamp event = StampUtils.getEventStamp(content);
		EntityConverter converter = new EntityConverter(getEntityFactory());
		Set<ContentItem> toUpdate = new LinkedHashSet<ContentItem>();

		try {
			// convert icalendar representation to cosmo data model
			toUpdate.addAll(converter.convertEventCalendar((NoteItem) content, event.getEventCalendar()));
		} catch (ModelValidationException e) {
			throw new InvalidCalendarResourceException(e.getMessage());
		}

		if (event.getCreationDate() != null) {

			try {
				getContentService().updateContentItems(content.getParents(), toUpdate);
			} catch (IcalUidInUseException e) {
				throw new UidConflictException(e);
			} catch (CollectionLockedException e) {
				throw new LockedException();
			}
		} else {

			try {
				getContentService().createContentItems((CollectionItem) getItem(), toUpdate);
			} catch (IcalUidInUseException e) {
				throw new UidConflictException(e);
			} catch (CollectionLockedException e) {
				throw new LockedException();
			}
		}

		member.setItem(content);
	}

	protected void removeContent(DavItemContent member) throws CosmoDavException {
		if (!(member instanceof DavCalendarResource)) {
			throw new IllegalArgumentException("member not DavCalendarResource");
		}

		ContentItem content = (ContentItem) member.getItem();
		CollectionItem parent = (CollectionItem) getItem();

		try {
			if (content instanceof NoteItem) {
				getContentService().removeItemFromCollection(content, parent);
			} else {
				getContentService().removeContent(content);
			}
		} catch (CollectionLockedException e) {
			throw new LockedException();
		}
	}

	private void validateDestination(org.apache.jackrabbit.webdav.DavResource destination) throws CosmoDavException {
		if (destination instanceof WebDavResource
				&& ((WebDavResource) destination).getParent() instanceof DavCalendarCollection) {
			throw new InvalidCalendarLocationException(
					"Parent collection of destination must not be a calendar collection");
		}
	}

	@Override
	protected Set<DavPrivilege> getCurrentPrincipalPrivileges() {
		if (hasExternalContent(getItem())) {
			return Sets.newHashSet(DavPrivilege.READ);
		}
		return super.getCurrentPrincipalPrivileges();
	}

	@Override
	protected DavAcl getAcl() {
		if (!hasExternalContent(getItem())) {
			return super.getAcl();
		}
		DavAcl result = new DavAcl();
		DavAce owner = new DavAce.PropertyAce(OWNER);
		owner.getPrivileges().add(DavPrivilege.READ);
		owner.setProtected(true);
		result.getAces().add(owner);

		return result;
	}

	private static boolean hasExternalContent(Item item) {
		return item instanceof CollectionItem && UuidExternalGenerator.containsExternalUid(item.getUid());
	}
}