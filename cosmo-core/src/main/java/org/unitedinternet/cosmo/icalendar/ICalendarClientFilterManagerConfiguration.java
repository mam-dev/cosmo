package org.unitedinternet.cosmo.icalendar;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ICalendarClientFilterManagerConfiguration {

	
	private ICal3ClientFilter iCal3ClientFilter;
	
	public ICalendarClientFilterManagerConfiguration(ICal3ClientFilter iCal3ClientFilter) {
		this.iCal3ClientFilter = iCal3ClientFilter;
	}
	
	@Bean
	public ICalendarClientFilterManager getICalendarClientFilterManager() {
		ICalendarClientFilterManager iCalendarClientFilterManager = new ICalendarClientFilterManager();
		
		Map<String, ICalendarClientFilter> clientFilters = new HashMap<>();
		clientFilters.put("ical2", iCal3ClientFilter);
		clientFilters.put("ical3", iCal3ClientFilter);
		iCalendarClientFilterManager.setClientFilters(clientFilters);
		
		return iCalendarClientFilterManager;
	}
}
