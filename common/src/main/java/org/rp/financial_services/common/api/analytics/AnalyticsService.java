package org.rp.financial_services.common.api.analytics;

import org.rp.financial_services.common.api.analytics.exception.AnalyticsServiceException;
import java.time.LocalDate;

public interface AnalyticsService {
    double getVolatility(LocalDate date, String underlyingSymbol, String optionSymbol, double optionPrice) throws AnalyticsServiceException;
}
