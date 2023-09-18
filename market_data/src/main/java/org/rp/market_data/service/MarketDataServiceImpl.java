package org.rp.market_data.service;

import org.modelmapper.ModelMapper;
import org.rp.financial_services.common.api.market_data.MarketDataService;
import org.rp.financial_services.common.dao.market_data.HistoricQuote;
import org.rp.financial_services.common.dao.security.Security;
import org.rp.financial_services.common.dao.security.options.MarketData;
import org.rp.financial_services.common.dao.security.options.OptionContract;
import org.rp.financial_services.common.api.market_data.exception.MarketDataServiceException;
import org.rp.market_data.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

@Service
public class MarketDataServiceImpl implements MarketDataService {
    @Value("${securitymaster.uri}")
    private String securityMasterURI;

    @Autowired
    private ModelMapper marketQuoteMapper;

    @Autowired
    private ModelMapper historicQuoteMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public HistoricQuote getClosePriceBySymbol(String symbol, LocalDate localDate) throws MarketDataServiceException
    {
        List<HistoricQuote> quotes =getClosePriceBySymbol(symbol,localDate,localDate.plus(1, ChronoUnit.DAYS));
        if (quotes.size() != 1)
            throw new MarketDataServiceException("Unable to find [" + symbol + "] on [" + localDate + "]");
        return quotes.get(0);
    }

    @Override
    public HistoricQuote getClosePrice(int securityId, LocalDate date) throws MarketDataServiceException
    {
        try {
            System.out.println("Connecting to URI ["+securityMasterURI+"]");
            URI url = new URI(securityMasterURI+ "/security_service/id=" + securityId);
            Security security = restTemplate.getForObject(url, Security.class);
            if (security == null)
                throw new MarketDataServiceException("The security with id ["+securityId+"] is not found");
            return getClosePriceBySymbol(security.getSymbol(), date);
        }
        catch (URISyntaxException e)
        {
            throw new MarketDataServiceException(e);
        }
    }


    @Override
    public List<HistoricQuote> getClosePriceBySymbol(String symbol, LocalDate startDate, LocalDate endDate) throws MarketDataServiceException
    {
        try {
            yahoofinance.Stock stock = YahooFinance.get(symbol);

            Calendar startDateCal = DateUtils.convertLocalDateToCalendar(startDate);
            Calendar endDateCal = DateUtils.convertLocalDateToCalendar(endDate);


            List<HistoricalQuote> historyQuoteList = stock.getHistory(startDateCal,endDateCal, Interval.DAILY);
            return historyQuoteList.stream()
                    .map(historicalQuote -> historicQuoteMapper.map(historicalQuote, HistoricQuote.class)).toList();
        } catch (IOException e) {
            throw new MarketDataServiceException(e);
        }

    }

    @Override
    public MarketData getOptionLatestQuote(String symbol) throws MarketDataServiceException
    {
        System.out.println("Connecting to URI ["+securityMasterURI+"]");
        String url = securityMasterURI+ "/security_service/options/optionSymbol={optionSymbol}";
        OptionContract security = restTemplate.getForObject(url, OptionContract.class, Collections.singletonMap("optionSymbol",symbol));
        if (security == null || security.getMarketData() ==null)
            throw new MarketDataServiceException("Unable to find symbol/quote for ["+symbol+"]");
        return security.getMarketData();
    }
}
