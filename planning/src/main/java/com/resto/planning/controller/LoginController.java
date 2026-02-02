package com.resto.planning.controller;

import com.resto.planning.model.Planning;
import com.resto.planning.model.Utilisateur;
import com.resto.planning.repository.PlanningRepository;
import com.resto.planning.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek; // <--- Import important
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter; // Optionnel pour affichage
import java.util.*;

@Controller
public class LoginController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PlanningRepository planningRepository;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model, @RequestParam(required = false) String date) {

        // 1. Quelle date afficher ?
        LocalDate jourAffiche = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();

        // 2. Définir le début (Lundi) et la fin (Dimanche) de la semaine concernée
        LocalDate lundi = jourAffiche.with(DayOfWeek.MONDAY);
        LocalDate dimanche = jourAffiche.with(DayOfWeek.SUNDAY);

        // 3. Récupérer les données
        List<Utilisateur> employes = utilisateurRepository.findAll();

        // A. Shifts du JOUR (Pour le tableau visuel et la liste modifiable)
        List<Planning> shiftsDuJour = planningRepository.findByDateDebutBetween(
                jourAffiche.atStartOfDay(),
                jourAffiche.atTime(LocalTime.MAX)
        );

        // B. Shifts de la SEMAINE (Pour le calcul du total d'heures)
        List<Planning> shiftsSemaine = planningRepository.findByDateDebutBetween(
                lundi.atStartOfDay(),
                dimanche.atTime(LocalTime.MAX)
        );

        // 4. LOGIQUE GRILLE (Visuel du jour)
        Map<Long, Set<Integer>> grille = new HashMap<>();

        // 5. LOGIQUE COMPTEUR SEMAINE (Total Hebdo) ⏱️
        Map<Long, String> compteurSemaine = new HashMap<>();

        for (Utilisateur u : employes) {
            grille.put(u.getId(), new HashSet<>());

            // --- CALCUL TOTAL SEMAINE ---
            long totalMinutesSemaine = 0;
            for (Planning p : shiftsSemaine) { // On boucle sur TOUTE la semaine
                if (p.getUtilisateur().getId().equals(u.getId())) {
                    totalMinutesSemaine += Duration.between(p.getDateDebut(), p.getDateFin()).toMinutes();
                }
            }

            // Formatage "35h00"
            long h = totalMinutesSemaine / 60;
            long m = totalMinutesSemaine % 60;
            String totalStr = h + "h" + (m > 0 ? String.format("%02d", m) : "");
            compteurSemaine.put(u.getId(), totalStr);
        }

        // Remplissage de la grille (Reste basé sur le JOUR)
        for (Planning p : shiftsDuJour) {
            int hDebut = p.getDateDebut().getHour();
            int hFin = p.getDateFin().getHour();
            if (hFin == 0 || p.getDateFin().getDayOfYear() > p.getDateDebut().getDayOfYear()) {
                hFin = 24;
            }
            for (int h = hDebut; h < hFin; h++) {
                Long userId = p.getUtilisateur().getId();
                if (grille.containsKey(userId)) {
                    grille.get(userId).add(h);
                }
            }
        }

        // Horaires Soirée (18h-23h)
        List<Integer> heures = Arrays.asList(18, 19, 20, 21, 22, 23);

        model.addAttribute("employes", employes);
        model.addAttribute("heures", heures);
        model.addAttribute("grille", grille);
        model.addAttribute("shiftsDuJour", shiftsDuJour);
        model.addAttribute("jourActuel", jourAffiche);

        // On envoie le compteur SEMAINE
        model.addAttribute("compteurHeures", compteurSemaine);

        // On envoie aussi les dates de la semaine pour info (optionnel)
        model.addAttribute("dateLundi", lundi);
        model.addAttribute("dateDimanche", dimanche);

        return "index";
    }
}