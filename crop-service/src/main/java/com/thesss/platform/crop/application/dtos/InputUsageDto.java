package com.thesss.platform.crop.application.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public class InputUsageDto {

    private UUID id;
    private String inputType; // Resolved from MasterData
    private String inputNameBrand; // Resolved or text
    private String quantity; // e.g., "2 kg"
    private String applicationMethod; // Resolved from MasterData
    private BigDecimal cost;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getInputType() { return inputType; }
    public void setInputType(String inputType) { this.inputType = inputType; }

    public String getInputNameBrand() { return inputNameBrand; }
    public void setInputNameBrand(String inputNameBrand) { this.inputNameBrand = inputNameBrand; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getApplicationMethod() { return applicationMethod; }
    public void setApplicationMethod(String applicationMethod) { this.applicationMethod = applicationMethod; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
}