package net.younes.commandeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CommandeResponse {
    private Long id;
    private String customerName;
    private List<ProductItemResponse> products;
    private double totalPrice;
}

