package org.unitedinternet.cosmo.hibernate.validator;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ValidationConfig {

    @Value("${cosmo.event.validation.summary.min.length}")
    private int summaryMinLength;

    @Value("${cosmo.event.validation.summary.max.length}")
    private int summaryMaxLength;

    @Value("${cosmo.event.validation.location.min.length}")
    private int locationMinLength;

    @Value("${cosmo.event.validation.location.max.length}")
    private int locationMaxLength;

    @Value("${cosmo.event.validation.description.min.length}")
    private int descriptionMinLength;

    @Value("${cosmo.event.validation.description.max.length}")
    private int descriptionMaxLength;

    @Value("${cosmo.event.validation.attendees.max.length}")
    private int attendeesMaxSize;

    @Value("${cosmo.event.validation.allowed.recurrence.frequencies}")
    private String[] allowedRecurrenceFrequencies;

    /**
     * Default constructor.
     */
    public ValidationConfig() {

    }

    @PostConstruct
    public void initEventValidator() {
        EventValidator.setValidationConfig(this);
    }

    public int getSummaryMinLength() {
        return summaryMinLength;
    }

    public int getSummaryMaxLength() {
        return summaryMaxLength;
    }

    public int getLocationMinLength() {
        return locationMinLength;
    }

    public int getLocationMaxLength() {
        return locationMaxLength;
    }

    public int getDescriptionMinLength() {
        return descriptionMinLength;
    }

    public int getDescriptionMaxLength() {
        return descriptionMaxLength;
    }

    public int getAttendeesMaxSize() {
        return attendeesMaxSize;
    }

    public List<String> getAllowedRecurrenceFrequencies() {
        return Arrays.asList(this.allowedRecurrenceFrequencies);
    }
}
