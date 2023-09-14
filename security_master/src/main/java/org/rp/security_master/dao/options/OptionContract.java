package org.rp.security_master.dao.options;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OptionContract (OptionType optionType,
                                String symbol,
                                BigDecimal strike,
                                String currency,
                                String contractSize,
                                LocalDate expiration)
{
    public enum OptionType {Call,Put}
}
