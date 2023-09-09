package org.rp.security.controller;

import org.rp.security.dao.Security;
import org.rp.security.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import yahoofinance.options.dao.OptionContract;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class SecurityController
{

    @Autowired
    private SecurityService securityService;

    @GetMapping(value="/security_service/symbol={symbol}")
    public ResponseEntity<List<Security>> getSecurity(@PathVariable String symbol) throws IOException
    {
        List<Security> ret = securityService.getSecurityBySymbol(symbol);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping(value = "/security_service/id={secId}")
    public ResponseEntity<Security> getSecurityById(@PathVariable int secId)
    {
        Security s = securityService.getSecurity(secId);
        return new ResponseEntity<>(s,HttpStatus.OK);
    }

    @GetMapping(value = "/security_service/underlying={underlyingSymbol}")
    public Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>  getSecurityOptions(@PathVariable String underlyingSymbol) throws IOException
    {
        return securityService.getSecurityOptions(underlyingSymbol);
    }
}
