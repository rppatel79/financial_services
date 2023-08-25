package org.rp.service;

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
