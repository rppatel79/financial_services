package org.rp.security_master.exception;

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
