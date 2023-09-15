package org.rp.analytics.dao.security.options;

import java.math.BigDecimal;
import java.util.Date;


public record MarketData(BigDecimal lastPrice,
                         int volume,
                         int openInterest,
                         BigDecimal bid,
                         BigDecimal ask,
                         Date lastTradeDate,
                         double impliedVol)
{
}
