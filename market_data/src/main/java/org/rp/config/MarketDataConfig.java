package org.rp.config;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarketDataConfig {
    @Bean(name="marketQuoteMapper")
    public ModelMapper createMarketQuoteMapper()
    {
        return new ModelMapper();
    }

    @Bean(name="historicQuoteMapper")
    public ModelMapper createHistoricQuoteMapper()
    {
        return new ModelMapper();
    }
}
