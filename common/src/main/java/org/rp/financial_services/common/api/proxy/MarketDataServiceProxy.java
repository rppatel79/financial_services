package org.rp.financial_services.common.api.proxy;

import org.rp.financial_services.common.api.interfaces.market_data.MarketDataService;
import org.rp.financial_services.common.api.interfaces.market_data.exception.MarketDataServiceException;
import org.rp.financial_services.common.dao.market_data.HistoricQuote;
import org.rp.financial_services.common.dao.security.options.MarketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketDataServiceProxy implements MarketDataService
{
    @Value("${marketdata.uri}")
    private String marketDataURI;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public HistoricQuote getClosePriceBySymbol(String symbol, LocalDate localDate) throws MarketDataServiceException {
        String urlStr= marketDataURI + "/price/symbol={symbol}&eod_date={eod_date}";
        Map<String,String> params = new HashMap<>();
        {
            params.put("symbol", symbol);
            params.put("eod_date", localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        return restTemplate.getForObject(urlStr,HistoricQuote.class,params);
    }

    @Override
    public HistoricQuote getClosePrice(int securityId, LocalDate localDate) throws MarketDataServiceException {
        String urlStr= marketDataURI + "/price/id={id}&eod_date={eod_date}";
        Map<String,String> params = new HashMap<>();
        {
            params.put("id", String.valueOf(securityId));
            params.put("eod_date", localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        return restTemplate.getForObject(urlStr,HistoricQuote.class,params);
    }

    @Override
    public List<HistoricQuote> getClosePriceBySymbol(String symbol, LocalDate startDate, LocalDate endDate) throws MarketDataServiceException {
        String urlStr= marketDataURI + "/price/symbol={symbol}&start_date={start_date}&end_date={end_date}";
        Map<String,String> params = new HashMap<>();
        {
            params.put("symbol", symbol);
            params.put("start_date", startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            params.put("end_date", endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        return Arrays.asList(restTemplate.getForObject(urlStr,HistoricQuote[].class,params));
    }

    @Override
    public MarketData getOptionLatestQuote(String symbol) throws MarketDataServiceException {
        String url = marketDataURI + "/price/option/latest/symbol={ticker}";
        Map<String, String> params = new HashMap<>();
        params.put("ticker", symbol);
        System.out.println("Making request: ["+url+"] with params: ["+params+"]");
        return restTemplate.getForObject(url, MarketData.class, params);    }

    @Override
    public MarketData getEquityLatestQuote(String symbol) throws MarketDataServiceException {
        String url = marketDataURI + "/price/equity/latest/symbol={ticker}";
        Map<String, String> params = new HashMap<>();
        params.put("ticker", symbol);
        System.out.println("Making request: ["+url+"] with params: ["+params+"]");
        return restTemplate.getForObject(url, MarketData.class, params);
    }
}
