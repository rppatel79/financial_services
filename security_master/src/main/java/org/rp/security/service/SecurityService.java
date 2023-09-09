package org.rp.security.service;

import org.modelmapper.ModelMapper;
import org.rp.security.dao.Security;
import org.rp.security.repo.dao.SecurityEntity;
import org.rp.security.repo.SecurityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yahoofinance.YahooFinance;
import yahoofinance.options.dao.OptionContract;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public List<Security> getSecurityBySymbol(String symbol) throws IOException {
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
    public Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>  getSecurityOptions(String symbol) throws IOException
    {
        return YahooFinance.getOptionContracts(symbol);
    }

    private List<Security> convertToSecurity(List<SecurityEntity> securityEntities)
    {
        List<Security> securities = new ArrayList<>(securityEntities.size());
        for (SecurityEntity security : securityEntities)
            securities.add(entityToSecurityMapper.map(security,Security.class));

        return securities;
    }
}
