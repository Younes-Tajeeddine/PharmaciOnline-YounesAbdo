package net.younes.commandeservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
}