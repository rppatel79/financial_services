package org.rp.financial_services.common.api.market_data.exception;

public class MarketDataServiceException extends Exception
{
    public MarketDataServiceException() {
    }

    public MarketDataServiceException(String message) {
        super(message);
    }

    public MarketDataServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MarketDataServiceException(Throwable cause) {
        super(cause);
    }

    public MarketDataServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
