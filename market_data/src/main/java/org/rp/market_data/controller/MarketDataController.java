package org.rp.market_data.controller;

import org.rp.financial_services.common.dao.market_data.HistoricQuote;
import org.rp.financial_services.common.dao.security.options.MarketData;
import org.rp.market_data.service.MarketDataService;
import org.rp.market_data.exception.MarketDataServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class MarketDataController
{
    @Autowired
    private MarketDataService marketDataService;

    @GetMapping("/price/symbol={symbol}&eod_date={eod_date}")
    public ResponseEntity<HistoricQuote> getClosePriceBySymbol(@PathVariable("symbol") String symbol
            , @PathVariable("eod_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) throws MarketDataServiceException
    {
        HistoricQuote quote = marketDataService.getClosePriceBySymbol(symbol,date);
        return new ResponseEntity<>(quote, HttpStatus.OK);
    }

    @GetMapping("/price/option/latest/symbol={symbol}")
    public ResponseEntity<MarketData> getOptionLatestQuote(@PathVariable("symbol") String symbol) throws MarketDataServiceException
    {
        MarketData quote = marketDataService.getOptionLatestQuote(symbol);
        return new ResponseEntity<>(quote, HttpStatus.OK);
    }

    @GetMapping("/price/id={id}&eod_date={eod_date}")
    public ResponseEntity<HistoricQuote> getClosePrice(@PathVariable("id") int securityId
            ,@PathVariable("eod_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) throws MarketDataServiceException
    {
        HistoricQuote quote = marketDataService.getClosePrice(securityId,date);
        return new ResponseEntity<>(quote,HttpStatus.OK);
    }


}
