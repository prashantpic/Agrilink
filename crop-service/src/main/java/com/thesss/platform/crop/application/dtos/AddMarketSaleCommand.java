package com.thesss.platform.crop.application.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AddMarketSaleCommand {

    @NotNull(message = "Quantity sold value cannot be null")
    @Positive(message = "Quantity sold value must be positive")
    private BigDecimal quantitySoldValue;

    @NotNull(message = "Quantity sold unit Master ID cannot be null")
    @Size(min = 1, max = 255, message = "Quantity sold unit Master ID must be between 1 and 255 characters")
    private String quantitySoldUnitMasterId;

    @NotNull(message = "Sale price per unit cannot be null")
    @PositiveOrZero(message = "Sale price per unit must be zero or positive")
    private BigDecimal salePricePerUnit;

    @NotNull(message = "Buyer name or market cannot be null")
    @Size(min = 1, max = 255, message = "Buyer name or market must be between 1 and 255 characters")
    private String buyerNameOrMarket;

    @NotNull(message = "Sale date cannot be null")
    @PastOrPresent(message = "Sale date must be in the past or present")
    private LocalDate saleDate;

    // Getters and Setters
    public BigDecimal getQuantitySoldValue() { return quantitySoldValue; }
    public void setQuantitySoldValue(BigDecimal quantitySoldValue) { this.quantitySoldValue = quantitySoldValue; }

    public String getQuantitySoldUnitMasterId() { return quantitySoldUnitMasterId; }
    public void setQuantitySoldUnitMasterId(String quantitySoldUnitMasterId) { this.quantitySoldUnitMasterId = quantitySoldUnitMasterId; }

    public BigDecimal getSalePricePerUnit() { return salePricePerUnit; }
    public void setSalePricePerUnit(BigDecimal salePricePerUnit) { this.salePricePerUnit = salePricePerUnit; }

    public String getBuyerNameOrMarket() { return buyerNameOrMarket; }
    public void setBuyerNameOrMarket(String buyerNameOrMarket) { this.buyerNameOrMarket = buyerNameOrMarket; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }
}