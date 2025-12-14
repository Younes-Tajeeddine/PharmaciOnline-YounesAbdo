package net.younes.commandeservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductItemRequest {
        private Long productId;
        private int quantity;

}
