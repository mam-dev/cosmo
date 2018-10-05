package org.unitedinternet.cosmo.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unitedinternet.cosmo.dav.servlet.StandardRequestHandler;

/**
 * Simple resource to check everything works. XXX - Remove it afterwards.
 * 
 * @author daniel grigore
 *
 */
@RestController()
public class TimeResource {

    @Autowired
    private StandardRequestHandler handler;

    @GetMapping(path = "/time")
    public String get() {
        return Long.toString(System.currentTimeMillis()) + this.handler.toString();
    }
}
