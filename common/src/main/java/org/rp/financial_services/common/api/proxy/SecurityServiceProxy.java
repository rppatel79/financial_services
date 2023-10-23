package org.rp.financial_services.common.api.proxy;

import org.rp.financial_services.common.api.interfaces.security_master.SecurityService;
import org.rp.financial_services.common.api.interfaces.security_master.exception.SecurityMasterServiceException;
import org.rp.financial_services.common.dao.security.Security;
import org.rp.financial_services.common.dao.security.options.OptionContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SecurityServiceProxy implements SecurityService
{
    @Value("${securitymaster.uri}")
    private String securityMasterURI;
    @Autowired
    private RestTemplate restTemplate;

    public SecurityServiceProxy(String securityMasterURI, RestTemplate restTemplate) {
        this.securityMasterURI = securityMasterURI;
        this.restTemplate = restTemplate;
    }

    public SecurityServiceProxy() {
    }

    @Override
    public Security getSecurity(int id)
    {
        String urlStr=securityMasterURI + "/security_service/id={id}";
        Map<String,String> params = Collections.singletonMap("id",String.valueOf(id));

        return restTemplate.getForObject(urlStr, Security.class,params);
    }

    @Override
    public List<Security> getSecurityBySymbol(String symbol) throws SecurityMasterServiceException
    {
        String urlStr=securityMasterURI + "/security_service/symbol={symbol}";
        Map<String,String> params = Collections.singletonMap("symbol",symbol);

        ParameterizedTypeReference<List<Security>> responseType =
                new ParameterizedTypeReference<>() {
                };
//        restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);
//        restTemplate.exchange(urlStr,HttpMethod.GET,HttpEntity.EMPTY,responseType,params);
        return Arrays.asList(restTemplate.getForObject(urlStr, Security[].class,params));
    }

    @Override
    public Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>> getAllOptions(String underlying) throws SecurityMasterServiceException {
        /*
        ParameterizedTypeReference<Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>> responseType =
                new ParameterizedTypeReference<>() {
                };
        String urlStr =securityMasterURI + "/security_service/options/underlying=" + symbol;
        ResponseEntity<Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>> responseEntity;
        responseEntity = restTemplate.exchange(urlStr, HttpMethod.GET, HttpEntity.EMPTY, responseType);

        return responseEntity.getBody();
        */

        ParameterizedTypeReference<Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>> responseType =
                new ParameterizedTypeReference<>() {
                };
        ResponseEntity<Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>> responseEntity
                = getAllOptionsContracts(securityMasterURI, restTemplate, underlying, responseType);

        return responseEntity.getBody();
    }

    private static ResponseEntity<Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>> getAllOptionsContracts(String securityMasterURI, RestTemplate restTemplate, String underlying, ParameterizedTypeReference<Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>> responseType)
    {
        try {
            ResponseEntity<Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>> responseEntity;
            URI url = new URI(securityMasterURI + "/security_service/options/underlying=" + underlying);
            responseEntity = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);
            return responseEntity;
        }
        catch(Exception es)
        {
            throw new RuntimeException(es);
        }
    }

    @Override
    public OptionContract getOption(String underlyingSymbol, String contract) throws SecurityMasterServiceException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public OptionContract getOption(String contract) throws SecurityMasterServiceException
    {
        String url = securityMasterURI + "/security_service/options/optionSymbol={optionSymbol}";
        Map<String,String> params = Collections.singletonMap("optionSymbol",contract);
        System.out.println("Making request: ["+url+"] with params: ["+params+"]");
        return restTemplate.getForObject(url, OptionContract.class,params);

    }
}
