package org.rp.financial_services.market_data.config;

import org.modelmapper.ModelMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

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

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }


}
