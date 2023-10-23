package org.rp.financial_services.analytics.services;

import com.rp.risk_management.analytics.security.options.CoxRossRubinsteinPricer;
import com.rp.risk_management.analytics.security.options.OptionPricer;
import com.rp.risk_management.model.Option;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.rp.financial_services.common.api.interfaces.market_data.MarketDataService;
import org.rp.financial_services.common.api.interfaces.security_master.SecurityService;
import org.rp.financial_services.common.api.interfaces.analytics.exception.AnalyticsServiceException;
import org.rp.financial_services.common.api.interfaces.analytics.AnalyticsService;
import org.rp.financial_services.common.dao.security.options.MarketData;
import org.rp.financial_services.common.dao.market_data.HistoricQuote;
import org.rp.financial_services.common.dao.security.options.OptionContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private SecurityService securityService;


    @Autowired
    private RestTemplate restTemplate;

    public static final double MIN_VOL = 0.01;
    public static final double MAX_VOL = 1.0 - MIN_VOL;

    private static int DEFAULT_CONTRACTS_AWAY_FROM_THE_MONEY = 5;
    private static final BigDecimal interestRate = new BigDecimal("0.05");

    @Override
    public double getVolatility(LocalDate date, String underlyingSymbol, String optionSymbol, double optionPrice) throws AnalyticsServiceException
    {
        try {
            //get prices
            HistoricQuote underlyingQuote = marketDataService.getClosePriceBySymbol(underlyingSymbol,date);

            OptionContract contract = securityService.getOption(optionSymbol);
            return computeVolatility(date, contract, underlyingQuote.getClose().doubleValue(), optionPrice);
        }
        catch (Exception ex)
        {
            throw new AnalyticsServiceException(ex);
        }
    }

    private static double computeVolatility(LocalDate valueDate, OptionContract contract,double underlyingPrice, double optionPrice) {
        BrentSolver solver = new BrentSolver();

        Option option = buildAnalyticsOptionModel(valueDate, contract, underlyingPrice);

        return solver.solve(1000, v -> {
            OptionPricer pricer = new CoxRossRubinsteinPricer(option,
                    interestRate.doubleValue(),
                    v,
                    0.0
            );
            double ret = pricer.getOptionPrice() - optionPrice;

            return ret;
        },
                MIN_VOL,
                MAX_VOL,
                0.5
        );
    }

    private static Option buildAnalyticsOptionModel(LocalDate valueDate, OptionContract contract,double underlyingPrice)
    {
        Option option = new Option(underlyingPrice,
                -1,
                contract.getStrike().doubleValue(),
                interestRate.doubleValue(),
                0,
                ((int) valueDate.until(contract.getExpiration(), ChronoUnit.DAYS)),
                Collections.emptyList(),
                contract.getOptionStyle() == OptionContract.OptionStyle.American ? Option.OptionStyle.American : Option.OptionStyle.European,
                contract.getOptionType() == OptionContract.OptionType.Call ? Option.OptionType.Call : Option.OptionType.Put
        );
        return option;
    }

    public String optionsToSellNow(List<String> underlyings,OptionContract.OptionType optionType)
    {
        return optionsToSell(underlyings,optionType, DEFAULT_CONTRACTS_AWAY_FROM_THE_MONEY,LocalDateTime.now());
    }

    public String optionsToSell(List<String> underlyings,OptionContract.OptionType optionType,int contractsAwayFromTheMoney,LocalDateTime asOfDate)
    {
        StringBuilder builder = new StringBuilder();
        underlyings.forEach(underlying -> {
            try {
                ParameterizedTypeReference<Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>>> responseType =
                        new ParameterizedTypeReference<>() {
                        };
                Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>> responseEntity
                        =securityService.getAllOptions(underlying);

                double quote;

                MarketData underlytingQuote = marketDataService.getEquityLatestQuote(underlying);
                quote = underlytingQuote.getMid().doubleValue();

                List<OptionContract> allContracts = responseEntity.values().stream().flatMap(map -> map.values().stream()).flatMap(Collection::stream).toList();
                int stepSize=getStepSize(allContracts);
                List<OptionContract> filteredContracts = filteredContracts(optionType, asOfDate, contractsAwayFromTheMoney, quote, allContracts, stepSize)
                        .collect(Collectors.toList());

                builder.append(printResults(underlying,quote,filteredContracts));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        return builder.toString();
    }

    private static int getStepSize(List<OptionContract> allContracts) {
        int mid=allContracts.size()/2;
        return Math.abs(allContracts.get(mid).getStrike().subtract(allContracts.get(mid+1).getStrike()).intValue());
    }

    private static Stream<OptionContract> filteredContracts(OptionContract.OptionType optionType, LocalDateTime asOfDate, int contractsAwayFromTheMoney, double quote, List<OptionContract> allContracts, int stepSize) {
        return allContracts.stream().filter(optionContract -> optionType.equals(optionContract.getOptionType()))

                .filter(optionContract -> asOfDate.toLocalDate().until(optionContract.getExpiration(), ChronoUnit.DAYS) < 90)
                .filter(optionContract -> asOfDate.toLocalDate().until(optionContract.getExpiration(), ChronoUnit.DAYS) > 2)

                .filter(optionContract -> optionContract.getMarketData().getAsk() != null && BigDecimal.ZERO.compareTo(optionContract.getMarketData().getAsk()) != 0)
                .filter(optionContract -> optionContract.getMarketData().getBid() != null && BigDecimal.ZERO.compareTo(optionContract.getMarketData().getBid()) != 0)
                .filter(optionContract -> optionContract.getMarketData().getOpenInterest() > 5)
                .filter(optionContract -> optionContract.getMarketData().getLastTradeDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().until(asOfDate, ChronoUnit.HOURS) < 35)
                .filter(optionContract ->
                        {
                            if (OptionContract.OptionType.Put.equals(optionType)) {
                                return new BigDecimal(quote).subtract(new BigDecimal(stepSize * contractsAwayFromTheMoney)).compareTo(optionContract.getStrike()) > 0;
                            }
                            else if (OptionContract.OptionType.Call.equals(optionType))
                            {
                                return new BigDecimal(quote).add(new BigDecimal(stepSize * contractsAwayFromTheMoney)).compareTo(optionContract.getStrike()) < 0;
                            }
                            else
                                throw new IllegalArgumentException("Unknown type ["+optionType+"]");
                        }
                )
                .sorted((c1, c2) ->
                {
                    if (c1.getExpiration().equals(c2.getExpiration()))
                        return c1.getStrike().compareTo(c2.getStrike());
                    else
                        return c1.getExpiration().compareTo(c2.getExpiration());
                });
    }

    /**
     * TODO:  This needs to return a better construct
     */
    private String printResults(String underlying,double underlyingQuote, List<OptionContract> contractsToBuy) {
        StringBuilder ret = new StringBuilder();
        {
            ret.append(underlying);
            ret.append("Ticker,Type,Strike,Expiration,Bid,Mid,Ask,ImpliedVol,OptionInterest,LastPrice,LastTradeDate,Delta,RiskAdjustedPrice");

            contractsToBuy.stream().forEach(
                    optionContract -> {
                        optionContract.getMarketData().setImpliedVol(computeVolatility(LocalDate.now(),optionContract,underlyingQuote,optionContract.getMarketData().getMid().doubleValue()));
                    }
                    );
            List<OptionContract> orderedContractsToBuy=contractsToBuy.stream().sorted((optionContract1,optionContract2) ->
            {
                double ratio1;
                {
                    double delta1 = Math.abs(calculateDelta(underlyingQuote, optionContract1, interestRate.doubleValue()));
                    double expectedLoss1 = optionContract1.getStrike().doubleValue() * 100.0;
                    ratio1 = optionContract1.getMarketData().getMid().doubleValue() / (delta1 * expectedLoss1);
                }

                double ratio2;
                {
                    double delta2 = Math.abs(calculateDelta(underlyingQuote, optionContract2, interestRate.doubleValue()));
                    double expectedLoss2 = optionContract2.getStrike().doubleValue()*100.0;
                    ratio2 = optionContract2.getMarketData().getMid().doubleValue() / (delta2 * expectedLoss2);
                }
                return Double.compare(ratio2,ratio1);
            }).collect(Collectors.toList());
            orderedContractsToBuy.stream().forEach(optionContract -> {
                ret.append(optionContract.getSymbol()+","+optionContract.getOptionType()+","+optionContract.getStrike()+","+optionContract.getExpiration()
                        +","+optionContract.getMarketData().getBid()+","+optionContract.getMarketData().getMid()+","+optionContract.getMarketData().getAsk()
                        +","+optionContract.getMarketData().getImpliedVol()
                        +","+optionContract.getMarketData().getOpenInterest()
                        +","+optionContract.getMarketData().getLastPrice()+","+optionContract.getMarketData().getLastTradeDate()
                        +","+ calculateDelta(underlyingQuote,optionContract,interestRate.doubleValue())
                        +","+ optionContract.getMarketData().getMid().doubleValue() / (calculateDelta(underlyingQuote,optionContract,interestRate.doubleValue())*optionContract.getStrike().doubleValue()*100.0)
                );
            });
        }

        return ret.toString();
    }


    private static double calculateDelta(double underlyingQuote, OptionContract optionContract, double interestRate) {
        OptionPricer optionPricer = new CoxRossRubinsteinPricer(5
                , buildAnalyticsOptionModel(LocalDate.now(),optionContract,underlyingQuote)
                ,interestRate
                ,optionContract.getMarketData().getImpliedVol()
                ,0.0);
        double optionPrice=optionPricer.getOptionPrice();

        optionPricer = new CoxRossRubinsteinPricer(5

                , buildAnalyticsOptionModel(LocalDate.now(),optionContract,underlyingQuote+1)
                ,interestRate
                ,optionContract.getMarketData().getImpliedVol()
                ,0.0);

        return optionPricer.getOptionPrice()-optionPrice;
    }


}
