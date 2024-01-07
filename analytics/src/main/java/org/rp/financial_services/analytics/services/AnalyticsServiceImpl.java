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

    public static final int DEFAULT_MAX_DAYS_UNTIL_EXPIRATION = 90;
    public static final int DEFAULT_MIN_DAYS_UNTIL_EXPIRATION = 2;
    public static final double MIN_DELTA = 0.000000001;
    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private SecurityService securityService;


    public static final double MIN_VOL = 0.01;
    public static final double MAX_VOL = 1.0 - MIN_VOL;

    private static final int DEFAULT_CONTRACTS_AWAY_FROM_THE_MONEY = 5;
    private static final BigDecimal interestRate = new BigDecimal("0.05");

    enum DCC {ACT_365,ACT_360}
    private static final DCC DEFAULT_DCC = DCC.ACT_360; // this should be part of the option contract or seperated in some convensions file
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

                    return pricer.getOptionPrice() - optionPrice;
        },
                MIN_VOL,
                MAX_VOL,
                0.5
        );
    }

    private static Option buildAnalyticsOptionModel(LocalDate valueDate, OptionContract contract,double underlyingPrice)
    {
        return new Option(underlyingPrice,
                -1,
                contract.getStrike().doubleValue(),
                interestRate.doubleValue(),
                0,
                ((int) getUntil(valueDate, contract)),
                Collections.emptyList(),
                contract.getOptionStyle() == OptionContract.OptionStyle.American ? Option.OptionStyle.American : Option.OptionStyle.European,
                contract.getOptionType() == OptionContract.OptionType.Call ? Option.OptionType.Call : Option.OptionType.Put
        );
    }

    private static long getUntil(LocalDate valueDate, OptionContract contract) {
        return valueDate.until(contract.getExpiration(), ChronoUnit.DAYS);
    }


    public Map<String,Set<Map<String,String>>> optionsToSellNow(List<String> underlyings,OptionContract.OptionType optionType)
    {
        return optionsToSell(underlyings,optionType, DEFAULT_CONTRACTS_AWAY_FROM_THE_MONEY,LocalDateTime.now());
    }

    public Map<String,Set<Map<String,String>>> optionsToSell(List<String> underlyings,OptionContract.OptionType optionType,int contractsAwayFromTheMoney,LocalDateTime asOfDate)
    {
        Map<String,Set<Map<String,String>>> ret = new HashMap<>();
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
                List<OptionContract> filteredContracts;
                int stepSize;
                if (allContracts.size() >0 && (stepSize = getStepSize(optionType, allContracts)) >0)
                {
                    filteredContracts = filteredContracts(optionType, asOfDate, contractsAwayFromTheMoney, quote, allContracts, stepSize)
                            .collect(Collectors.toList());
                }
                else
                {
                    filteredContracts = Collections.emptyList();
                }
                ret.put(underlying, buildResults(underlying,quote,filteredContracts));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        return ret;
    }

    private static int getStepSize(OptionContract.OptionType optionType,List<OptionContract> allContracts) {
        // finds the nearest expirations, and then guesses the step size by looking at the strike's of the mid and mid+1
        OptionContract contractWithMinExpiration = allContracts.stream().min(Comparator.comparing(OptionContract::getExpiration)).get();

        List<OptionContract> f= allContracts.stream().
                filter(optionContract -> optionType.equals(optionContract.getOptionType())).
                filter(optionContract -> optionContract.getExpiration().equals(contractWithMinExpiration.getExpiration())).toList();
        if (f.size() > 2) {
            int mid = (f.size() / 2)-1;
            return f.get(mid + 1).getStrike().subtract(f.get(mid).getStrike()).intValue();
        }
        else
            return -1;

    }

    private static Stream<OptionContract> filteredContracts(OptionContract.OptionType optionType, LocalDateTime asOfDate, int contractsAwayFromTheMoney, double quote, List<OptionContract> allContracts, int stepSize) {

        return allContracts.stream().filter(optionContract -> optionType.equals(optionContract.getOptionType()))

                .filter(optionContract -> getUntil(asOfDate.toLocalDate(), optionContract) < DEFAULT_MAX_DAYS_UNTIL_EXPIRATION)
                .filter(optionContract -> getUntil(asOfDate.toLocalDate(), optionContract) > DEFAULT_MIN_DAYS_UNTIL_EXPIRATION)//This condition is required, otherwise the delta calculation may break

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
                /*
                .filter(optionContract -> {
                    double expectedLoss =calculateExpectedLoss(quote,optionContract);
                    double returnsAtInterestRate = expectedLoss*(interestRate.doubleValue())*getDcc(asOfDate.toLocalDate(),optionContract);
                    return optionContract.getStrike().doubleValue()*100.0 > (returnsAtInterestRate);
                })*/
                .sorted((c1, c2) ->
                {
                    if (c1.getExpiration().equals(c2.getExpiration()))
                        return c1.getStrike().compareTo(c2.getStrike());
                    else
                        return c1.getExpiration().compareTo(c2.getExpiration());
                });
    }

    private static double getDcc(LocalDate toLocalDate, OptionContract optionContract) {
        if (DCC.ACT_365.equals(DEFAULT_DCC))
            return getUntil(toLocalDate,optionContract)/365.0;
        else if (DCC.ACT_360.equals(DEFAULT_DCC))
            return getUntil(toLocalDate,optionContract)/365.0;
        else
            throw new IllegalArgumentException();
    }

    private Set<Map<String,String>> buildResults(String underlying, double underlyingQuote, List<OptionContract> contractsToBuy) {
        LocalDate asOfDate = LocalDate.now();
        Set<Map<String,String>> results = new TreeSet<>((o1, o2) -> new BigDecimal(o2.get("RiskAdjustedPrice")).compareTo(new BigDecimal(o1.get("RiskAdjustedPrice"))));
        {
            contractsToBuy.stream().forEach(
                    optionContract -> optionContract.getMarketData().setImpliedVol(computeVolatility(asOfDate,optionContract,underlyingQuote,optionContract.getMarketData().getMid().doubleValue()))
                    );
            List<OptionContract> orderedContractsToBuy= contractsToBuy.stream().sorted((optionContract1, optionContract2) ->
            {
                double ratio1;
                {
                    double expectedLoss = calculateExpectedLoss(underlyingQuote, optionContract1);
                    ratio1 = optionContract1.getMarketData().getMid().doubleValue() / expectedLoss;
                }

                double ratio2;
                {
                    double expectedLoss = calculateExpectedLoss(underlyingQuote, optionContract2);
                    ratio2 = optionContract2.getMarketData().getMid().doubleValue() / expectedLoss;
                }
                return Double.compare(ratio2, ratio1);
            }).toList();
            orderedContractsToBuy.stream().forEach(optionContract -> {
                Map<String,String> values = new HashMap<>();

                values.put("Ticker",optionContract.getSymbol());
                values.put("Type",String.valueOf(optionContract.getOptionType()));
                values.put("Strike",String.valueOf(optionContract.getStrike()));
                values.put("Expiration",String.valueOf(optionContract.getExpiration()));
                values.put("Bid",String.valueOf(optionContract.getMarketData().getBid()));
                values.put("Mid",String.valueOf(optionContract.getMarketData().getMid()));
                values.put("Ask",String.valueOf(optionContract.getMarketData().getAsk()));
                values.put("ImpliedVol",String.valueOf(optionContract.getMarketData().getImpliedVol()));
                values.put("OptionInterest",String.valueOf(optionContract.getMarketData().getOpenInterest()));
                values.put("LastPrice",String.valueOf(optionContract.getMarketData().getLastPrice()));
                values.put("LastTradeDate",String.valueOf(optionContract.getMarketData().getLastTradeDate()));

                double delta = calculateDelta(underlyingQuote,optionContract,interestRate.doubleValue());
                // sets the delta to a min value (to avoid div by 0)
                if (delta == 0.0)
                    delta = MIN_DELTA;
                values.put("Delta",String.valueOf(delta));

                Double riskAdjustedPrice=optionContract.getMarketData().getMid().doubleValue() / (delta)*optionContract.getStrike().doubleValue()*100.0;
                values.put("RiskAdjustedPrice",new BigDecimal(riskAdjustedPrice).toPlainString());

                // this logic should be added to a filter
                double expectedLoss =calculateExpectedLoss(underlyingQuote,optionContract);
                double returnsAtInterestRate = expectedLoss*(interestRate.doubleValue())*getDcc(asOfDate,optionContract);
                boolean interestRateReturn= optionContract.getStrike().doubleValue()*100.0 > (returnsAtInterestRate);
                values.put("expectedLoss",new BigDecimal(expectedLoss).toPlainString());
                values.put("returnsAtInterestRate",new BigDecimal(returnsAtInterestRate).toPlainString());
                values.put("interestRateReturn",Boolean.valueOf(interestRateReturn).toString());

                results.add(values);
            });
        }

        return results;
    }

    private static double calculateExpectedLoss(double underlyingQuote, OptionContract optionContract2) {
        double delta = Math.abs(calculateDelta(underlyingQuote, optionContract2, interestRate.doubleValue()));
        double payout = optionContract2.getStrike().doubleValue() * 100.0;
        return payout*delta;
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
