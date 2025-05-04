package model;

import java.sql.Timestamp;

public class Tache {
    private int id;
    private String libelle;
    private int idProjet;
    private int idEmploye;
    private String projetNom;  // Nouveau champ
    private String employeNom; // Nouveau champ
    private String statut;
    private Timestamp dateCreation;

    // Constructeur original (maintenu pour compatibilité)
    public Tache(int id, String libelle, int idProjet, int idEmploye) {
        this(id, libelle, idProjet, idEmploye, "", "");
    }

    // Constructeur intermédiaire requis pour compatibilité avec l'ancien code
    public Tache(int id, String libelle, int idProjet, int idEmploye,
                 String projetNom, String employeNom) {
        this(id, libelle, idProjet, idEmploye, projetNom, employeNom, null, "");
    }

    // Nouveau constructeur complet
    public Tache(int id, String libelle, int idProjet, int idEmploye,
                 String projetNom, String employeNom, Timestamp dateCreation, String statut) {
        this.id = id;
        this.libelle = libelle;
        this.idProjet = idProjet;
        this.idEmploye = idEmploye;
        this.projetNom = projetNom;
        this.employeNom = employeNom;
        this.dateCreation = dateCreation;
        this.statut = statut;
    }

    // Getters
    public int getId() { return id; }
    public String getLibelle() { return libelle; }
    public int getIdProjet() { return idProjet; }
    public int getIdEmploye() { return idEmploye; }
    public String getProjetNom() { return projetNom; }
    public String getEmployeNom() { return employeNom; }
    public String getStatut() { return statut; }
    public Timestamp getDateCreation() { return dateCreation; }

    // Setters pour les nouveaux champs
    public void setProjetNom(String projetNom) { this.projetNom = projetNom; }
    public void setEmployeNom(String employeNom) { this.employeNom = employeNom; }

    @Override
    public String toString() {
        if (projetNom.isEmpty() || employeNom.isEmpty()) {
            return String.format("%d - %s (Projet: %d, Employé: %d)",
                                 id, libelle, idProjet, idEmploye);
        }
        return String.format("%d - %s | Projet: %s | Assigné à: %s",
                             id, libelle, projetNom, employeNom);
    }

    // Méthode utilitaire pour le rendu HTML
    public String toHTMLString() {
        return String.format("<html><b>%s</b><br>Projet: %s<br>Assigné à: %s</html>",
                             libelle, projetNom, employeNom);
    }
}
