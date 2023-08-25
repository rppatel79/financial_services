package rp.security.controller;

import rp.security.dao.Security;
import rp.security.service.SecurityService;
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
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

}
