package model;

public class Tache {
    private int id;
    private String libelle;
    private int idProjet;
    private int idEmploye;

    public Tache(int id, String libelle, int idProjet, int idEmploye) {
        this.id = id;
        this.libelle = libelle;
        this.idProjet = idProjet;
        this.idEmploye = idEmploye;
    }

    public int getId() { return id; }
    public String getLibelle() { return libelle; }
    public int getIdProjet() { return idProjet; }
    public int getIdEmploye() { return idEmploye; }

    @Override
    public String toString() {
        return id + " - " + libelle;
    }
}