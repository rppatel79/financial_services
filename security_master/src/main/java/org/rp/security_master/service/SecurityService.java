package org.rp.security_master.service;

import org.modelmapper.ModelMapper;
import org.rp.security_master.dao.Security;
import org.rp.security_master.exception.SecurityMasterServiceException;
import org.rp.security_master.repo.dao.SecurityEntity;
import org.rp.security_master.repo.SecurityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yahoofinance.YahooFinance;
import yahoofinance.options.dao.OptionContract;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SecurityService {
    @Autowired
    private SecurityRepo securityRepo;

    @Autowired
    private ModelMapper yahooToSecurityMapper;

    @Autowired
    private ModelMapper entityToSecurityMapper;


    public Security getSecurity(int id)
    {
        List<Security> security = convertToSecurity(List.of(securityRepo.getReferenceById(id)));
        if (security.size() != 1)
            return null;
        else
            return security.get(0);
    }

    public List<Security> getSecurityBySymbol(String symbol) throws SecurityMasterServiceException {
        try {
            List<SecurityEntity> securities =securityRepo.findBySymbol(symbol);
            if (securities.size() == 0) {
                yahoofinance.Stock stock = YahooFinance.get(symbol);

                SecurityEntity security=yahooToSecurityMapper.map(stock,SecurityEntity.class);
                securityRepo.save(security);

                securities = List.of(security);
            }

            System.out.println(securities);

            return convertToSecurity(securities);
        }
        catch (IOException e)
        {
            throw new SecurityMasterServiceException(e);
        }
    }
    public Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>> getAllOptions(String symbol) throws SecurityMasterServiceException
    {
        try
        {
            return YahooFinance.getOptionContracts(symbol);
        }
        catch (IOException e)
        {
            throw new SecurityMasterServiceException(e);
        }
    }

    public OptionContract getOption(String underlyingSymbol, String contract) throws SecurityMasterServiceException
    {
        try {
            Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>  allOptionsMap = YahooFinance.getOptionContracts(underlyingSymbol);
            List<OptionContract> allOptions =allOptionsMap.values().stream().flatMap(i->i.values().stream()).flatMap(Collection::stream).toList();
            List<OptionContract> filteredContracts = allOptions.stream().filter(optionContract -> contract.equals(optionContract.getSymbol())).toList();
            if (filteredContracts.size() == 0)
                throw new SecurityMasterServiceException("Unable to find option ["+contract+"] with underlying ["+underlyingSymbol+"]");
            else if (filteredContracts.size() > 1)
                throw new SecurityMasterServiceException("Found more than one contract ["+contract+"] with underlying ["+underlyingSymbol+"]");
            return filteredContracts.get(0);
        }
        catch (IOException e)
        {
            throw new SecurityMasterServiceException(e);
        }
    }


    private List<Security> convertToSecurity(List<SecurityEntity> securityEntities)
    {
        List<Security> securities = new ArrayList<>(securityEntities.size());
        for (SecurityEntity security : securityEntities)
            securities.add(entityToSecurityMapper.map(security,Security.class));

        return securities;
    }
}
