package org.rp.financial_services.market_data.config;

import org.modelmapper.ModelMapper;
import org.rp.financial_services.common.api.interfaces.security_master.SecurityService;
import org.rp.financial_services.common.api.proxy.SecurityServiceProxy;
import org.rp.financial_services.common.dao.security.options.MarketData;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MarketDataConfig {
    @Bean(name="historicQuoteMapper")
    public ModelMapper createHistoricQuoteMapper()
    {
        return new ModelMapper();
    }

    @Bean(name="stockQuoteToMarketDataMapper")
    public ModelMapper createStockQuoteToMarketDataMapper()
    {
        ModelMapper stockQuoteToMarketDataMapper =  new ModelMapper();
        stockQuoteToMarketDataMapper.createTypeMap(yahoofinance.quotes.stock.StockQuote.class, MarketData.class);

        return stockQuoteToMarketDataMapper;
    }

    @Bean
    public SecurityService securityService()
    {
        return new SecurityServiceProxy();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }


}
