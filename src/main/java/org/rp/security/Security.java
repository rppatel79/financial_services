package org.rp.security;

public class Security {
    private String name;
    private String symbol;

    public Security(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public Security() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
