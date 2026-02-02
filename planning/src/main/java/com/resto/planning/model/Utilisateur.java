package com.resto.planning.model;

import jakarta.persistence.*;
import lombok.Data; // Lombok génère les getters/setters tout seul (si installé)
import java.time.LocalDateTime;

@Data // @Getter @Setter @ToString...
@Entity // C'est une table SQL
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-Increment
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role; // "admin" ou "employe"

    @Column(name = "poste_defaut")
    private String posteDefaut;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}