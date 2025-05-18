package com.bellagnech.dig_bank.entities;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<BankAccount> bankAccounts;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    // Audit fields
    private String createdBy;
    private java.util.Date createdDate;
    private String lastModifiedBy;
    private java.util.Date lastModifiedDate;
}
