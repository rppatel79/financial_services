package org.rp.financial_services.common.api.interfaces.analytics.exception;

public class AnalyticsServiceException extends Exception
{
    public AnalyticsServiceException() {
    }

    public AnalyticsServiceException(String message) {
        super(message);
    }

    public AnalyticsServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnalyticsServiceException(Throwable cause) {
        super(cause);
    }

    public AnalyticsServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
