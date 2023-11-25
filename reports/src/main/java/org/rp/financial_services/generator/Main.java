package org.rp.financial_services.generator;

import org.rp.financial_services.common.dao.security.options.OptionContract;
import org.rp.financial_services.generator.reports.PremiumStrategyReport;
import org.rp.financial_services.generator.reports.ReportGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class Main implements CommandLineRunner {
    public static void main(String[] args)
    {
        SpringApplication.run(Main.class,args);
    }

    public List<ReportGenerator> getReportList()
    {
        return Arrays.asList(new PremiumStrategyReport(OptionContract.OptionType.Put,Arrays.asList("VOO","VTV","VBR")),
                new PremiumStrategyReport(OptionContract.OptionType.Call,Arrays.asList("VEA","ESGD","ESGE","VWO","BNDX","EMB","ITOT")));
    }

    public void run(String...args) throws Exception
    {
        getReportList().stream().forEach(ReportGenerator::runReport);
    }
}
