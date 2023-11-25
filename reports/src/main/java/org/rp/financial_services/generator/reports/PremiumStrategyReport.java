package org.rp.financial_services.generator.reports;

import org.rp.financial_services.common.dao.security.options.OptionContract;

import java.util.List;

public class PremiumStrategyReport implements ReportGenerator
{
    private final OptionContract.OptionType optionType;
    private final List<String> symbols;

    public PremiumStrategyReport(OptionContract.OptionType optionType, List<String> symbols)
    {
        this.optionType = optionType;
        this.symbols = symbols;
    }

    /**
     *
     * @see ReportGenerator#runReport
     */
    @Override
    public void runReport()
    {
        System.out.println("Generating report"+toString());
    }

    @Override
    public String toString() {
        return "PremiumStrategyReport{" +
                "optionType=" + optionType +
                ", symbols=" + symbols +
                '}';
    }
}
