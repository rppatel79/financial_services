package org.rp.financial_services.common.api.interfaces.analytics;

import org.rp.financial_services.common.api.interfaces.analytics.exception.AnalyticsServiceException;
import java.time.LocalDate;

public interface AnalyticsService {
    double getVolatility(LocalDate date, String underlyingSymbol, String optionSymbol, double optionPrice) throws AnalyticsServiceException;
}
