package net.younes.commandeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductItemResponse {
    private String productName;
    private int quantity;
    private double unitPrice;
}