package org.rp.financial_services.analytics.config;

import org.rp.financial_services.common.api.proxy.MarketDataServiceProxy;
import org.rp.financial_services.common.api.proxy.SecurityServiceProxy;
import org.rp.financial_services.common.api.interfaces.market_data.MarketDataService;
import org.rp.financial_services.common.api.interfaces.security_master.SecurityService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AnalyticsConfig
{
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public MarketDataService marketDataService()
    {
        return new MarketDataServiceProxy();
    }

    @Bean
    public SecurityService securityService()
    {
        return new SecurityServiceProxy();
    }

}
