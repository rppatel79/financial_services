package org.rp.financial_services.common.api.security_master.exception;

public class SecurityMasterServiceException extends Exception
{
    public SecurityMasterServiceException() {
    }

    public SecurityMasterServiceException(String message) {
        super(message);
    }

    public SecurityMasterServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityMasterServiceException(Throwable cause) {
        super(cause);
    }

    public SecurityMasterServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
