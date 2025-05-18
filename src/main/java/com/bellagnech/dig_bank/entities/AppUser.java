package com.bellagnech.dig_bank.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String username;
    
    private String password;
    
    @Column(unique = true)
    private String email;
    
    private boolean enabled = true;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<AppRole> roles = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    private List<Customer> customers = new ArrayList<>();
    
    // Audit fields
    private String createdBy;
    private java.util.Date createdDate;
    private String lastModifiedBy;
    private java.util.Date lastModifiedDate;
}
