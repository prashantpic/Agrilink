package com.thesss.platform.crop.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "market_sales")
public class MarketSale {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_cycle_id", nullable = false)
    private CropCycle cropCycle;

    @Column(name = "quantity_sold_value", precision = 12, scale = 3, nullable = false)
    private BigDecimal quantitySoldValue;

    @Column(name = "quantity_sold_unit_master_id", nullable = false)
    private String quantitySoldUnitMasterId; // References MasterData ID for unit

    @Column(name = "sale_price_per_unit", precision = 12, scale = 4, nullable = false) // Increased precision for price
    private BigDecimal salePricePerUnit;

    @Column(name = "buyer_name_or_market", nullable = false, length = 255)
    private String buyerNameOrMarket; // Text field

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    protected MarketSale() {
        // JPA LOMBOK Requirement
    }

    public MarketSale(CropCycle cropCycle, BigDecimal quantitySoldValue, String quantitySoldUnitMasterId,
                      BigDecimal salePricePerUnit, String buyerNameOrMarket, LocalDate saleDate) {
        if (cropCycle == null) {
            throw new IllegalArgumentException("CropCycle cannot be null for a MarketSale");
        }
        if (quantitySoldValue == null || quantitySoldValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity sold value must be positive");
        }
        if (quantitySoldUnitMasterId == null || quantitySoldUnitMasterId.isBlank()) {
            throw new IllegalArgumentException("Quantity sold unit master ID cannot be null or empty");
        }
        if (salePricePerUnit == null || salePricePerUnit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Sale price per unit cannot be negative");
        }
        if (buyerNameOrMarket == null || buyerNameOrMarket.isBlank()) {
            throw new IllegalArgumentException("Buyer name or market cannot be null or empty");
        }
        if (saleDate == null) {
            throw new IllegalArgumentException("Sale date cannot be null");
        }

        this.cropCycle = cropCycle;
        this.quantitySoldValue = quantitySoldValue;
        this.quantitySoldUnitMasterId = quantitySoldUnitMasterId;
        this.salePricePerUnit = salePricePerUnit;
        this.buyerNameOrMarket = buyerNameOrMarket;
        this.saleDate = saleDate;
    }

    public UUID getId() {
        return id;
    }

    public CropCycle getCropCycle() {
        return cropCycle;
    }

    // Package-private setter for aggregate root to manage relationship
    void setCropCycle(CropCycle cropCycle) {
        this.cropCycle = cropCycle;
    }

    public BigDecimal getQuantitySoldValue() {
        return quantitySoldValue;
    }

    public String getQuantitySoldUnitMasterId() {
        return quantitySoldUnitMasterId;
    }

    public BigDecimal getSalePricePerUnit() {
        return salePricePerUnit;
    }

    public String getBuyerNameOrMarket() {
        return buyerNameOrMarket;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }
}