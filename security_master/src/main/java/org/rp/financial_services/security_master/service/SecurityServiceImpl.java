package org.rp.financial_services.security_master.service;

import org.modelmapper.ModelMapper;

import org.rp.financial_services.common.api.interfaces.security_master.SecurityService;
import org.rp.financial_services.common.dao.security.Security;
import org.rp.financial_services.common.api.interfaces.security_master.exception.SecurityMasterServiceException;
import org.rp.financial_services.common.dao.security.options.MarketData;
import org.rp.financial_services.common.dao.security.options.OptionContract;
import org.rp.financial_services.security_master.repo.dao.SecurityEntity;
import org.rp.financial_services.security_master.repo.SecurityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
public class SecurityServiceImpl implements SecurityService {
    @Autowired
    private SecurityRepo securityRepo;

    @Autowired
    private ModelMapper yahooToSecurityMapper;

    @Autowired
    private ModelMapper entityToSecurityMapper;

    @Autowired
    private ModelMapper entityToOptionMapper;

    @Override
    public Security getSecurity(int id)
    {
        List<Security> security = convertToSecurity(List.of(securityRepo.getReferenceById(id)));
        if (security.size() != 1)
            return null;
        else
            return security.get(0);
    }

    @Override
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
    @Override
    public Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>> getAllOptions(String symbol) throws SecurityMasterServiceException
    {
        try
        {
            Map<LocalDate,
                    Map<yahoofinance.options.dao.OptionContract.OptionType, List<yahoofinance.options.dao.OptionContract>>> yahooMap
                    = YahooFinance.getOptionContracts(symbol);
            Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>> ret = new HashMap<>();
            for (Map.Entry<LocalDate,Map<yahoofinance.options.dao.OptionContract.OptionType, List<yahoofinance.options.dao.OptionContract>>> yahooMapEntry: yahooMap.entrySet())
            {
                ret.put(yahooMapEntry.getKey(),convertMap(yahooMapEntry.getValue()));
            }

            return ret;
        }
        catch (IOException e)
        {
            throw new SecurityMasterServiceException(e);
        }
    }

    private Map<OptionContract.OptionType, List<OptionContract>> convertMap(Map<yahoofinance.options.dao.OptionContract.OptionType, List<yahoofinance.options.dao.OptionContract>> map)
    {
        Map<OptionContract.OptionType, List<OptionContract>> ret = new HashMap<>();
        for (Map.Entry<yahoofinance.options.dao.OptionContract.OptionType, List<yahoofinance.options.dao.OptionContract>> mapEntry : map.entrySet())
        {
            ret.put(convertType(mapEntry.getKey()),convertToOptionContract(mapEntry.getValue()));
        }

        return ret;
    }

    private OptionContract.OptionType convertType(yahoofinance.options.dao.OptionContract.OptionType key) {
        return switch (key) {
            case Call -> OptionContract.OptionType.Call;
            case Put -> OptionContract.OptionType.Put;
            default -> throw new IllegalArgumentException("Unmapped type [" + key + "]");
        };
    }

    @Override
    public OptionContract getOption(String underlyingSymbol, String contract) throws SecurityMasterServiceException
    {
        try {
            Map<LocalDate, Map<yahoofinance.options.dao.OptionContract.OptionType, List<yahoofinance.options.dao.OptionContract>>>  allOptionsMap
                    = YahooFinance.getOptionContracts(underlyingSymbol);
            List<yahoofinance.options.dao.OptionContract> allOptions
                    =allOptionsMap.values().stream().flatMap(i->i.values().stream()).flatMap(Collection::stream).toList();
            List<yahoofinance.options.dao.OptionContract> filteredContracts
                    = allOptions.stream().filter(optionContract -> contract.equals(optionContract.getSymbol())).toList();
            if (filteredContracts.size() == 0)
                throw new SecurityMasterServiceException("Unable to find option ["+contract+"] with underlying ["+underlyingSymbol+"]");
            else if (filteredContracts.size() > 1)
                throw new SecurityMasterServiceException("Found more than one contract ["+contract+"] with underlying ["+underlyingSymbol+"]");
            return entityToOptionMapper.map(filteredContracts.get(0),OptionContract.class);
        }
        catch (IOException e)
        {
            throw new SecurityMasterServiceException(e);
        }
    }

    @Override
    public OptionContract getOption(String contract) throws SecurityMasterServiceException
    {
        return getOption(contract.substring(0,3), contract);
    }


    private List<Security> convertToSecurity(List<SecurityEntity> securityEntities) {
        List<Security> securities = new ArrayList<>(securityEntities.size());
        for (SecurityEntity security : securityEntities)
            securities.add(entityToSecurityMapper.map(security, Security.class));

        return securities;
    }

    private List<OptionContract> convertToOptionContract(List<yahoofinance.options.dao.OptionContract> securityEntities) {
        List<OptionContract> securities = new ArrayList<>(securityEntities.size());
        for (yahoofinance.options.dao.OptionContract yahooContract : securityEntities) {
            OptionContract convertedContract = entityToOptionMapper.map(yahooContract, OptionContract.class);
            convertedContract.setMarketData(entityToOptionMapper.map(yahooContract.getMarketData(), MarketData.class));
            securities.add(convertedContract);
        }

        return securities;
    }

}
