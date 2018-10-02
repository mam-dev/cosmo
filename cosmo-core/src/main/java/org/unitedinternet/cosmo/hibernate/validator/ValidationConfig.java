package org.unitedinternet.cosmo.hibernate.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ValidationConfig implements EnvironmentAware {

    private static final Log LOG = LogFactory.getLog(ValidationConfig.class);

    private static final String ALLOWED_RECURRENCES_FREQUENCIES_KEY = "cosmo.event.validation.allowed.recurrence.frequencies";
    private static final String FREQUENCIES_SEPARATOR = ",";

    private static final String SUMMARY_MIN_LENGTH_KEY = "cosmo.event.validation.summary.min.length";
    private static final String SUMMARY_MAX_LENGTH_KEY = "cosmo.event.validation.summary.max.length";

    private static final String LOCATION_MIN_LENGTH_KEY = "cosmo.event.validation.location.min.length";
    private static final String LOCATION_MAX_LENGTH_KEY = "cosmo.event.validation.location.max.length";

    private static final String DESCRIPTION_MIN_LENGTH_KEY = "cosmo.event.validation.description.min.length";
    private static final String DESCRIPTION_MAX_LENGTH_KEY = "cosmo.event.validation.description.max.length";

    private static final String ATTENDEES_MAX_LENGTH_KEY = "cosmo.event.validation.attendees.max.length";

    private static final String PROPERTIES_FILE = "/etc/application.properties";

    public Set<String> allowedRecurrenceFrequencies = new HashSet<>(5);

    public int summaryMinLength;
    public int summaryMaxLength;

    public int locationMinLength;
    public int locationMaxLength;

    public int descriptionMinLength;
    public int descriptionMaxLength;

    public int attendeesMaxSize;

    /**
     * Default constructor.
     */
    public ValidationConfig() {

    }

    @PostConstruct
    public void initEventValidator() {
        EventValidator.setValidationConfig(this);
    }

    private static int getIntFromPropsFor(Environment environment, String key, Properties defaultProps) {
        String value = environment.getProperty(key);
        return Integer.parseInt(value == null ? defaultProps.getProperty(key) : value);
    }

    public void setEnvironment(Environment environment) {
        InputStream is = null;

        Properties properties = new Properties();
        try {
            is = EventValidator.class.getResourceAsStream(PROPERTIES_FILE);

            properties.load(is);

            summaryMinLength = getIntFromPropsFor(environment, SUMMARY_MIN_LENGTH_KEY, properties);
            summaryMaxLength = getIntFromPropsFor(environment, SUMMARY_MAX_LENGTH_KEY, properties);

            locationMinLength = getIntFromPropsFor(environment, LOCATION_MIN_LENGTH_KEY, properties);
            locationMaxLength = getIntFromPropsFor(environment, LOCATION_MAX_LENGTH_KEY, properties);

            descriptionMinLength = getIntFromPropsFor(environment, DESCRIPTION_MIN_LENGTH_KEY, properties);
            descriptionMaxLength = getIntFromPropsFor(environment, DESCRIPTION_MAX_LENGTH_KEY, properties);

            attendeesMaxSize = getIntFromPropsFor(environment, ATTENDEES_MAX_LENGTH_KEY, properties);

            String permittedFrequencies = environment.getProperty(ALLOWED_RECURRENCES_FREQUENCIES_KEY);
            String permittedFrequenciesToUse = permittedFrequencies == null
                    ? properties.getProperty(ALLOWED_RECURRENCES_FREQUENCIES_KEY)
                    : permittedFrequencies;

            String[] frequencies = permittedFrequenciesToUse.split(FREQUENCIES_SEPARATOR);

            for (String s : frequencies) {
                allowedRecurrenceFrequencies.add(s.trim());
            }

        } catch (Exception e) {
            LOG.warn("Failed to initialize validation config", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOG.warn("Exception occured while closing the input stream", e);
                }
            }
        }
    }

}
