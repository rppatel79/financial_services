package org.rp.financial_services.market_data.service;

import org.modelmapper.ModelMapper;
import org.rp.financial_services.common.api.interfaces.market_data.MarketDataService;
import org.rp.financial_services.common.api.interfaces.market_data.exception.MarketDataServiceException;
import org.rp.financial_services.common.api.interfaces.security_master.SecurityService;
import org.rp.financial_services.common.api.interfaces.security_master.exception.SecurityMasterServiceException;
import org.rp.financial_services.common.dao.market_data.HistoricQuote;
import org.rp.financial_services.common.dao.security.Security;
import org.rp.financial_services.common.dao.security.options.MarketData;
import org.rp.financial_services.common.dao.security.options.OptionContract;
import org.rp.financial_services.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

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
    @Autowired
    private ModelMapper stockQuoteToMarketDataMapper;
    @Autowired
    private ModelMapper historicQuoteMapper;

    @Autowired
    private SecurityService securityService;

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
        Security security = securityService.getSecurity(securityId);
        return getClosePriceBySymbol(security.getSymbol(), date);
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
        try {
            OptionContract option = securityService.getOption(symbol);
            if (option == null || option.getMarketData() ==null)
                throw new MarketDataServiceException("Unable to find symbol/quote for [" + symbol + "]");

            return option.getMarketData();
        } catch (SecurityMasterServiceException ex)
        {
            throw new MarketDataServiceException("Unable to find symbol/quote for [" + symbol + "]",ex);
        }
    }

    @Override
    public MarketData getEquityLatestQuote(String symbol) throws MarketDataServiceException
    {
        try {
            yahoofinance.Stock stock = YahooFinance.get(symbol);
            StockQuote liveQuote = stock.getQuote();
            return stockQuoteToMarketDataMapper.map(liveQuote, MarketData.class);
        }
        catch(Exception ex)
        {
            throw new MarketDataServiceException(ex);
        }
    }
}
