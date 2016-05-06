package org.unitedinternet.cosmo.ext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @author daniel grigore
 *
 */
public class RequestOptions {

    private Map<String, String> headers;
    private Map<String, String> queryParams;

    private RequestOptions() {

    }

    public Map<String, String> headers() {
        return headers;
    }

    public Map<String, String> queryParams() {
        return queryParams;
    }

    private void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    private void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public static RequestOptionsBuilder builder() {
        return new RequestOptionsBuilder();
    }

    public static class RequestOptionsBuilder {

        private Map<String, String> headers = new LinkedHashMap<>();
        private Map<String, String> queryParams = new LinkedHashMap<>();

        private RequestOptionsBuilder() {

        }

        public static RequestOptionsBuilder instance() {
            return new RequestOptionsBuilder();
        }

        public RequestOptionsBuilder withHeader(String headerName, String headerValue) {
            headers.put(headerName, headerValue);
            return this;
        }

        public RequestOptionsBuilder withQueryParam(String headerName, String headerValue) {
            this.queryParams.put(headerName, headerValue);
            return this;
        }

        public RequestOptions build() {
            RequestOptions options = new RequestOptions();
            options.setHeaders(this.headers);
            options.setQueryParams(this.queryParams);
            return options;
        }
    }
}
