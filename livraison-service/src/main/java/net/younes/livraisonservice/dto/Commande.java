package net.younes.livraisonservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class Commande {
    private Long id;
    private Long customerId;
    private List<ProductItem> productItems;
    
    @Data
    public static class ProductItem {
        private Long id;
        private Long productId;
        private int quantity;
        private double unitPrice;
        private Product product;
    }
    
    @Data
    public static class Product {
        private Long id;
        private String name;
        private double price;
        private int quantity;
    }
}