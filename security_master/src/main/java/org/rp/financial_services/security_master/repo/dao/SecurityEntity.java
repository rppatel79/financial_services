package org.rp.financial_services.security_master.repo.dao;

import jakarta.persistence.*;

@Entity
@Table(name = "security_master")
public class SecurityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column()
    private String name;
    @Column(nullable=false)
    private String symbol;

    public SecurityEntity(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public SecurityEntity(int id, String name, String symbol) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
    }

    public SecurityEntity() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return "SecurityEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
