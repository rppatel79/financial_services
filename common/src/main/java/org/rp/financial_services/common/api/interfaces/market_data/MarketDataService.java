package org.rp.financial_services.common.api.interfaces.market_data;

import org.rp.financial_services.common.api.interfaces.market_data.exception.MarketDataServiceException;
import org.rp.financial_services.common.dao.market_data.HistoricQuote;
import org.rp.financial_services.common.dao.security.options.MarketData;


import java.time.LocalDate;
import java.util.List;

public interface MarketDataService {
    HistoricQuote getClosePriceBySymbol(String symbol, LocalDate localDate) throws MarketDataServiceException;

    HistoricQuote getClosePrice(int securityId, LocalDate date) throws MarketDataServiceException;

    List<HistoricQuote> getClosePriceBySymbol(String symbol, LocalDate startDate, LocalDate endDate) throws MarketDataServiceException;

    MarketData getOptionLatestQuote(String symbol) throws MarketDataServiceException;

    MarketData getEquityLatestQuote(String symbol) throws MarketDataServiceException;
}
