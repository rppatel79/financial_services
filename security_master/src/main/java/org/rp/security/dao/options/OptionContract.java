package org.rp.security.dao.options;

import yahoofinance.options.dao.MarketData;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OptionContract (OptionType optionType,
                                String symbol,
                                BigDecimal strike,
                                String currency,
                                String contractSize,
                                LocalDate expiration,
                                MarketData marketData)
{
    public enum OptionType {Call,Put};
}
