package org.rp.service;

import org.modelmapper.ModelMapper;
import org.rp.dao.HistoricQuote;
import org.rp.dao.security.Security;
import org.rp.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarketDataService
{
    @Value("${securitymaster.uri}")
    private String securityMasterURI;

    @Autowired
    private ModelMapper marketQuoteMapper;

    @Autowired
    private ModelMapper historicQuoteMapper;

    @Autowired
    private RestTemplate restTemplate;

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
        try {
            URI url = new URI(securityMasterURI+ "/security_service/id=" + securityId);
            Security security = restTemplate.getForObject(url, Security.class);
            if (security == null)
                throw new MarketDataServiceException("The security with id ["+securityId+"] is not found");
            return getClosePriceBySymbol(security.symbol(), date);
        }
        catch (URISyntaxException e)
        {
            throw new MarketDataServiceException(e);
        }
    }
}
