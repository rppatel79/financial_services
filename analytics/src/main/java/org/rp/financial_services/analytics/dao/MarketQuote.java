package org.rp.financial_services.analytics.dao;

import java.math.BigDecimal;

public class MarketQuote {
    private BigDecimal ask;
    private Long askSize;
    private BigDecimal bid;
    private Long bidSize;
    private BigDecimal price;


    public BigDecimal getAsk() {
        return ask;
    }

    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }

    public Long getAskSize() {
        return askSize;
    }

    public void setAskSize(Long askSize) {
        this.askSize = askSize;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    public Long getBidSize() {
        return bidSize;
    }

    public void setBidSize(Long bidSize) {
        this.bidSize = bidSize;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
