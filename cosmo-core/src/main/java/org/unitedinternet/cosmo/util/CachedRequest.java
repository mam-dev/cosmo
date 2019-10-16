package org.unitedinternet.cosmo.util;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

public class CachedRequest extends HttpServletRequestWrapper {

    private BufferedContent content;

    public CachedRequest(HttpServletRequest request) {
        super(request);
        try {
            content = new BufferedContent(request.getInputStream());
        } catch (IOException ex) {
            content = null;
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (content != null)
            return new DelegatingServletInputStream(content.getInputStream());
        else
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return true;
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setReadListener(ReadListener readListener) {

                }

                @Override
                public int read() throws IOException {
                    return 0;
                }
            };

    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

}