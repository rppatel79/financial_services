package org.rp.financial_services.analytics.controller;

import org.rp.financial_services.common.api.interfaces.analytics.exception.AnalyticsServiceException;
import org.rp.financial_services.analytics.services.AnalyticsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;


@RestController
public class AnalyticsController {

    @Autowired
    private AnalyticsServiceImpl analyticsService;

    @GetMapping("/analytics/getVolatility/underlyingSymbol={underlyingSymbol}&optionSymbol={optionSymbol}&optionPrice={optionPrice}&asOfDate={asOfDate}")
    public ResponseEntity<Double> getVolatility(@PathVariable("underlyingSymbol") String underlyingSymbol,
                                @PathVariable("optionSymbol") String optionSymbol,
                                @PathVariable("optionPrice") Double optionPrice,
                                @PathVariable("asOfDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) throws AnalyticsServiceException

    {
        System.out.println("underlyingSymbol ="+underlyingSymbol+" optionSymbol="+optionSymbol+" optionPrice="+optionPrice+" date="+date);
        double ret = analyticsService.getVolatility(date, underlyingSymbol, optionSymbol,optionPrice);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }


}
