package org.rp.financial_services.security_master.controller;


import org.rp.financial_services.common.dao.security.Security;
import org.rp.financial_services.common.api.interfaces.security_master.exception.SecurityMasterServiceException;
import org.rp.financial_services.common.dao.security.options.OptionContract;
import org.rp.financial_services.security_master.service.SecurityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class SecurityController
{

    @Autowired
    private SecurityServiceImpl securityService;

    @GetMapping(value="/security_service/symbol={symbol}")
    public ResponseEntity<List<Security>> getSecurity(@PathVariable String symbol) throws SecurityMasterServiceException
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

    @GetMapping(value = "/security_service/options/underlying={underlyingSymbol}")
    public Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>  getAllOptionsContracts(@PathVariable String underlyingSymbol) throws SecurityMasterServiceException
    {
        return securityService.getAllOptions(underlyingSymbol);
    }

    @GetMapping(value = "/security_service/options/optionSymbol={optionSymbol}")
    public OptionContract  getOptionsContract(@PathVariable String optionSymbol) throws SecurityMasterServiceException
    {

        return securityService.getOption(optionSymbol.substring(0,3),optionSymbol);
    }

}
