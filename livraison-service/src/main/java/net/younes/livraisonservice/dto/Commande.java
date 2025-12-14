package net.younes.livraisonservice.dto;


import lombok.Data;

@Data
public class Commande {
    private Long id;
    private Long customerId;
    private int quantity;
}