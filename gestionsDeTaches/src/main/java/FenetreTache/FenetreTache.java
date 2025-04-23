package ui;

import model.Tache;
import model.Employe;
import model.Projet;
import dao.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class FenetreTache extends JFrame {
    private DefaultListModel<Tache> model;
    private JList<Tache> liste;
    private JButton ajouter, modifier, supprimer;

    public FenetreTache() {
        setTitle("Gestion des Tâches");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        model = new DefaultListModel<>();
        liste = new JList<>(model);
        chargerTaches();

        ajouter = new JButton("Ajouter");
        modifier = new JButton("Modifier");
        supprimer = new JButton("Supprimer");

        ajouter.addActionListener(e -> ajouterTache());
        modifier.addActionListener(e -> modifierTache());
        supprimer.addActionListener(e -> supprimerTache());

        JPanel boutons = new JPanel();
        boutons.add(ajouter);
        boutons.add(modifier);
        boutons.add(supprimer);

        add(new JScrollPane(liste), BorderLayout.CENTER);
        add(boutons, BorderLayout.SOUTH);
    }

    private void chargerTaches() {
        model.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Tache");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(new Tache(
                    rs.getInt("id"),
                    rs.getString("libelle"),
                    rs.getInt("id_projet"),
                    rs.getInt("id_employe")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ajouterTache() {
        String libelle = JOptionPane.showInputDialog(this, "Libellé de la tâche:");
        if (libelle != null) {
            int idProjet = Integer.parseInt(JOptionPane.showInputDialog(this, "ID du projet:"));
            int idEmploye = Integer.parseInt(JOptionPane.showInputDialog(this, "ID de l'employé:"));
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO Tache (libelle, id_projet, id_employe) VALUES (?, ?, ?)");
                ps.setString(1, libelle);
                ps.setInt(2, idProjet);
                ps.setInt(3, idEmploye);
                ps.executeUpdate();
                chargerTaches();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void modifierTache() {
        Tache tache = liste.getSelectedValue();
        if (tache != null) {
            String libelle = JOptionPane.showInputDialog(this, "Nouveau libellé:", tache.getLibelle());
            int idProjet = Integer.parseInt(JOptionPane.showInputDialog(this, "ID projet:", tache.getId_projet()));
            int idEmploye = Integer.parseInt(JOptionPane.showInputDialog(this, "ID employé:", tache.getId_employe()));
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE Tache SET libelle=?, id_projet=?, id_employe=? WHERE id=?");
                ps.setString(1, libelle);
                ps.setInt(2, idProjet);
                ps.setInt(3, idEmploye);
                ps.setInt(4, tache.getId());
                ps.executeUpdate();
                chargerTaches();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void supprimerTache() {
        Tache tache = liste.getSelectedValue();
        if (tache != null && JOptionPane.showConfirmDialog(this, "Supprimer ?") == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM Tache WHERE id=?");
                ps.setInt(1, tache.getId());
                ps.executeUpdate();
                chargerTaches();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}