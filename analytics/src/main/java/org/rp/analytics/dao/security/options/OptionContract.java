package org.rp.analytics.dao.security.options;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OptionContract(OptionType optionType,
                             String symbol,
                             BigDecimal strike,
                             String currency,
                             String contractSize,
                             LocalDate expiration,
                             MarketData marketData)
{
    public enum OptionType {Call,Put}
}
