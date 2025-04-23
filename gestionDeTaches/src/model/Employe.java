package model;

public class Employe {
    private int id;
    private String nom;
    private String prenom;

    public Employe(int id, String nom, String prenom) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }

    @Override
    public String toString() {
        return id + " - " + nom + " " + prenom;
    }
}