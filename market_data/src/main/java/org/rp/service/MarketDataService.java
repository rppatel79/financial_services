package org.rp.service;

import org.modelmapper.ModelMapper;
import org.rp.dao.HistoricQuote;
import org.rp.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarketDataService
{
    @Autowired
    private ModelMapper marketQuoteMapper;

    @Autowired
    private ModelMapper historicQuoteMapper;

    public HistoricQuote getClosePriceBySymbol(String symbol,LocalDate localDate) throws MarketDataServiceException
    {
        try {
            yahoofinance.Stock stock = YahooFinance.get(symbol);

            Calendar calendar = DateUtils.convertLocalDateToCalendar(localDate);

            List<HistoricalQuote> historyQuoteList = stock.getHistory(calendar);
            List<HistoricQuote> quotes = historyQuoteList.stream()
                    .map(historicalQuote -> historicQuoteMapper.map(historicalQuote, HistoricQuote.class)).toList();

            if (quotes.size() != 1)
                throw new MarketDataServiceException("Unable to find [" + symbol + "] on [" + localDate + "]");
            return quotes.get(0);
        }
        catch (IOException exception)
        {
            throw new MarketDataServiceException(exception);
        }
    }

    public HistoricQuote getClosePrice(int securityId, LocalDate date) throws MarketDataServiceException
    {
        //TODO: Need to call the security service to get a ticker
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
