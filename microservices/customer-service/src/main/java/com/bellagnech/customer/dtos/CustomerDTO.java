package com.bellagnech.customer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String firstName;
    private String lastName;
    private boolean enabled = true;
    private String role;
    private java.util.Date createdAt;
}

