package org.unitedinternet.cosmo.app;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unitedinternet.cosmo.dav.servlet.StandardRequestHandler;

/**
 * TODO
 * 
 * @author daniel grigore
 *
 */
@RestController
public class PersonController {

    private final StandardRequestHandler requestHandler;

    /**
     * 
     */
    public PersonController(StandardRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @GetMapping("/persons")
    List<String> get() throws ServletException, IOException {
        this.requestHandler.handleRequest(null, null);
        return Collections.emptyList();
    }
}
