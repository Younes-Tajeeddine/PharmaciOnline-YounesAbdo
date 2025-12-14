package net.younes.commandeservice.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CommandeRequest {
    private Long customerId;
    private List<ProductItemRequest> productItems;

}
