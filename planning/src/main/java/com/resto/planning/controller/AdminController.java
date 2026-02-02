package com.resto.planning.controller;

import com.resto.planning.model.Planning;
import com.resto.planning.model.Utilisateur;
import com.resto.planning.repository.PlanningRepository;
import com.resto.planning.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PlanningRepository planningRepository;


    // --- GESTION DES EMPLOYÉS ---

    @GetMapping("/admin/employes")
    public String pageEmployes(Model model) {
        List<Utilisateur> list = utilisateurRepository.findAll();
        model.addAttribute("employes", list);
        return "admin_employes"; // On va créer ce fichier HTML
    }

    @PostMapping("/admin/employes/ajouter")
    public String ajouterEmploye(@RequestParam String nom,
                                 @RequestParam String email,
                                 @RequestParam String poste) {
        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setEmail(email);
        u.setPosteDefaut(poste);
        u.setPassword("1234"); // Mot de passe par défaut (à changer plus tard)
        u.setRole("employe");

        utilisateurRepository.save(u);
        return "redirect:/admin/employes";
    }

    @GetMapping("/admin/employes/supprimer/{id}")
    public String supprimerEmploye(@PathVariable Long id) {
        utilisateurRepository.deleteById(id);
        return "redirect:/admin/employes";
    }

    // --- GESTION DU PLANNING (AJOUT & MODIF) ---

    // 1. Afficher le formulaire VIDE (Pour AJOUTER)
    @GetMapping("/ajouter-shift")
    public String pageAjoutShift(Model model) {
        model.addAttribute("employes", utilisateurRepository.findAll());
        model.addAttribute("shift", new Planning()); // On envoie un planning vide
        return "form_shift";
    }

    // 2. Afficher le formulaire REMPLI (Pour MODIFIER)
    // On appelle cette URL quand on clique sur une case du planning
    @GetMapping("/modifier-shift/{id}")
    public String pageModifShift(@PathVariable Long id, Model model) {
        Planning p = planningRepository.findById(id).orElse(null);
        if (p == null) return "redirect:/"; // Sécurité si l'ID n'existe pas

        model.addAttribute("employes", utilisateurRepository.findAll());
        model.addAttribute("shift", p); // On envoie le planning existant
        return "form_shift";
    }

    // 3. ENREGISTRER (Marche pour Ajout ET Modif)
    @PostMapping("/enregistrer-shift")
    public String enregistrerShift(@RequestParam(required = false) Long id,
                                   @RequestParam Long userId,
                                   @RequestParam String dateDebut,
                                   @RequestParam String dateFin,
                                   @RequestParam String poste,
                                   @RequestParam String commentaire) {

        Planning p;

        // Si on a un ID, c'est une modif -> on cherche l'existant
        if (id != null) {
            p = planningRepository.findById(id).orElse(new Planning());
        } else {
            // Sinon, c'est un nouveau
            p = new Planning();
        }

        Utilisateur u = utilisateurRepository.findById(userId).orElse(null);
        if (u != null) {
            p.setUtilisateur(u);
            p.setDateDebut(LocalDateTime.parse(dateDebut));
            p.setDateFin(LocalDateTime.parse(dateFin));
            p.setPoste(poste);
            p.setCommentaire(commentaire);
            planningRepository.save(p);
        }

        return "redirect:/";
    }

    // Supprimer
    @GetMapping("/supprimer-shift/{id}")
    public String supprimerShift(@PathVariable Long id) {
        planningRepository.deleteById(id);
        return "redirect:/";
    }
}