package org.rp.analytics.services;

import com.rp.risk_management.analytics.security.options.CoxRossRubinsteinPricer;
import com.rp.risk_management.analytics.security.options.OptionPricer;
import com.rp.risk_management.model.Option;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.rp.analytics.dao.HistoricQuote;
import org.rp.analytics.dao.security.options.OptionContract;
import org.rp.analytics.exception.AnalyticsServiceException;
import org.rp.analytics.utils.DateUtils2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AnalyticsService {
    @Value("${securitymaster.uri}")
    private String securityMasterURI;

    @Value("${marketdata.uri}")
    private String marketDataURI;

    @Autowired
    private RestTemplate restTemplate;

    public static final double MIN_VOL = 0.01;
    public static final double MAX_VOL = 1.0 - MIN_VOL;
    private static final BigDecimal interestRate = new BigDecimal("0.05");

    public double getVolatility(LocalDate date, String underlyingSymbol, String optionSymbol,double optionPrice) throws AnalyticsServiceException
    {
        //get prices
        HistoricQuote underlyingQuote = getClosePriceBySymbol(underlyingSymbol, date);

        OptionContract contract = getOptionsContract(securityMasterURI,restTemplate,optionSymbol);
        return computeVolatility(date, contract, underlyingQuote.getClose().doubleValue(), optionPrice);
    }

    private HistoricQuote getClosePriceBySymbol(String symbol, LocalDate date)
    {
        return getClosePriceBySymbol(marketDataURI,restTemplate,symbol,date);
    }

    private static HistoricQuote getClosePriceBySymbol(String marketDataURI, RestTemplate restTemplate, String symbol, LocalDate date)
    {
        String url = marketDataURI + "/price/symbol={ticker}&eod_date={eod_date}";
        Map<String, String> params = new HashMap<>();
        params.put("ticker", symbol);
        params.put("eod_date", DateTimeFormatter.ofPattern("yyyy-MM-dd").format(DateUtils2.adjustDate(date)));
        return restTemplate.getForObject(url, HistoricQuote.class, params);
    }


    private double computeVolatility(LocalDate valueDate, OptionContract contract, double underlyingPrice, double optionPrice) {
        BrentSolver solver = new BrentSolver();

        Option option = new Option(underlyingPrice,
                -1,
                contract.strike().doubleValue(),
                interestRate.doubleValue(),
                0,
                ((int) valueDate.until(contract.expiration(), ChronoUnit.DAYS)),
                Collections.emptyList(),
                Option.OptionStyle.American,
                contract.optionType() == OptionContract.OptionType.Call ? Option.OptionType.Call : Option.OptionType.Put
        );

        return solver.solve(1000, v -> {
            OptionPricer pricer = new CoxRossRubinsteinPricer(option,
                    interestRate.doubleValue(),
                    v,
                    0.0
            );
            double ret = pricer.getOptionPrice() - optionPrice;

            System.out.println("With the vol=[" + v + "] compute price:" + pricer.getOptionPrice());

            return ret;
        },
                MIN_VOL,
                MAX_VOL,
                0.5
        );
    }


    public static void main(String[] args) {
        LocalDateTime asOfDate = LocalDateTime.now();
        String securityMasterURI = "http://localhost:80";
        String marketDataURI = "http://localhost:90";
        int contractsAwayFromTheMoney= 2;
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate = builder.build();

        System.out.println("securityMasterURI=[" + securityMasterURI + "]");
        // underlying tickers
        Stream<String> underlyings = Stream.of("VOO", "VYM");

        underlyings.forEach(underlying -> {
            try {
                ParameterizedTypeReference<Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>> responseType =
                        new ParameterizedTypeReference<>() {
                        };
                ResponseEntity<Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>> responseEntity
                        = getAllOptionsContracts(securityMasterURI, restTemplate, underlying, responseType);


                HistoricQuote underlytingQuote=getClosePriceBySymbol(marketDataURI,restTemplate,underlying,asOfDate.toLocalDate());

                List<OptionContract> allContracts = responseEntity.getBody().values().stream().flatMap(map -> map.values().stream()).flatMap(Collection::stream).toList();
                int stepSize=Math.abs(allContracts.get(0).strike().subtract(allContracts.get(1).strike()).intValue());
                List<OptionContract> filteredContracts = allContracts.stream().filter(optionContract -> OptionContract.OptionType.Put.equals(optionContract.optionType()))
                        .filter(optionContract -> asOfDate.toLocalDate().until(optionContract.expiration(), ChronoUnit.DAYS) < 90)
                        .filter(optionContract -> !BigDecimal.ZERO.equals(optionContract.marketData().ask()))
                        .filter(optionContract -> !BigDecimal.ZERO.equals(optionContract.marketData().bid()))
                        .filter(optionContract -> optionContract.marketData().openInterest() > 5)
                        .filter(optionContract -> optionContract.marketData().lastTradeDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().until(asOfDate, ChronoUnit.HOURS) < 35)
                        .filter(optionContract -> underlytingQuote.getClose().subtract(new BigDecimal(stepSize*contractsAwayFromTheMoney)).compareTo(optionContract.strike())>0)
                        .collect(Collectors.toList());
                System.out.println(filteredContracts);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });


    }

    private static OptionContract  getOptionsContract(String securityMasterURI, RestTemplate restTemplate,String optionSymbol)
    {
        String url = securityMasterURI + "/security_service/options/optionSymbol={optionSymbol}";
        Map<String,String> params = Collections.singletonMap("optionSymbol",optionSymbol);
        return restTemplate.getForObject(url, OptionContract.class,params);
    }

    private static ResponseEntity<Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>> getAllOptionsContracts(String securityMasterURI, RestTemplate restTemplate, String underlying, ParameterizedTypeReference<Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>> responseType) throws URISyntaxException
    {
        ResponseEntity<Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>> responseEntity;
        URI url = new URI(securityMasterURI + "/security_service/options/underlying=" + underlying);
        responseEntity = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, responseType);
        return responseEntity;
    }

}
