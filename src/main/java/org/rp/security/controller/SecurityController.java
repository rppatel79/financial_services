package org.rp.security.controller;

import com.sun.net.httpserver.Authenticator;
import org.rp.security.Security;
import org.rp.security.repo.dao.SecurityEntity;
import org.rp.security.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class SecurityController
{

    @Autowired
    private SecurityService securityService;

    @GetMapping(value="/security_service/{symbol}")
    public ResponseEntity<List<Security>> getSecurity(@PathVariable String symbol) throws IOException
    {
        List<Security> ret = securityService.getSecurityBySymbol(symbol);
        return new ResponseEntity<List<Security>>(ret, HttpStatus.OK);
    }

}
