package org.rp.financial_services.common.api.interfaces.security_master;

import org.rp.financial_services.common.api.interfaces.security_master.exception.SecurityMasterServiceException;
import org.rp.financial_services.common.dao.security.Security;
import org.rp.financial_services.common.dao.security.options.OptionContract;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SecurityService {
    Security getSecurity(int id);

    List<Security> getSecurityBySymbol(String symbol) throws SecurityMasterServiceException;

    Map<LocalDate, Map<OptionContract.OptionType, List<OptionContract>>> getAllOptions(String symbol) throws SecurityMasterServiceException;

    OptionContract getOption(String underlyingSymbol, String contract) throws SecurityMasterServiceException;
    OptionContract getOption(String contract) throws SecurityMasterServiceException;

}
