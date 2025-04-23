package model;

public class Projet {
    private int id;
    private String nom;

    public Projet(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }

    @Override
    public String toString() {
        return id + " - " + nom;
    }
}