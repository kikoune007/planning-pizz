package com.resto.planning.repository;

import com.resto.planning.model.Planning;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PlanningRepository extends JpaRepository<Planning, Long> {

    // Récupérer les shifts d'un utilisateur
    List<Planning> findByUtilisateurId(Long userId);

    // Récupérer tous les shifts entre deux dates (ex: du matin au soir)
    List<Planning> findByDateDebutBetween(LocalDateTime start, LocalDateTime end);
}