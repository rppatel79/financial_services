package org.rp.security_master.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.rp.financial_services.common.dao.security.Security;
import org.rp.security_master.repo.dao.SecurityEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yahoofinance.Stock;

@Configuration
public class SecurityConfig {
    @Bean(name="yahooToSecurityMapper")
    public ModelMapper createYahooToSecurityMapper(){
        ModelMapper stockMapper =  new ModelMapper();
        TypeMap<Stock,SecurityEntity> typeMap =stockMapper.createTypeMap(Stock.class, SecurityEntity.class);
        typeMap.addMapping(Stock::getName,SecurityEntity::setName);
        typeMap.addMapping(Stock::getSymbol,SecurityEntity::setSymbol);

        return stockMapper;
    }

    @Bean(name="entityToSecurityMapper")
    public ModelMapper createEntityToSecurityMapper()
    {
        ModelMapper stockMapper =  new ModelMapper();
        TypeMap<SecurityEntity, Security> typeMap =stockMapper.createTypeMap(SecurityEntity.class, Security.class);
        typeMap.addMapping(SecurityEntity::getName,Security::setName);
        typeMap.addMapping(SecurityEntity::getSymbol,Security::setSymbol);

        return stockMapper;
    }
}
