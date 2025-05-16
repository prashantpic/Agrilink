package com.thesss.platform.crop.application.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class MarketSaleDto {

    private UUID id;
    private String quantitySold; // e.g., "500 kg"
    private BigDecimal salePricePerUnit;
    private String buyerNameOrMarket;
    private LocalDate saleDate;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getQuantitySold() { return quantitySold; }
    public void setQuantitySold(String quantitySold) { this.quantitySold = quantitySold; }

    public BigDecimal getSalePricePerUnit() { return salePricePerUnit; }
    public void setSalePricePerUnit(BigDecimal salePricePerUnit) { this.salePricePerUnit = salePricePerUnit; }

    public String getBuyerNameOrMarket() { return buyerNameOrMarket; }
    public void setBuyerNameOrMarket(String buyerNameOrMarket) { this.buyerNameOrMarket = buyerNameOrMarket; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }
}