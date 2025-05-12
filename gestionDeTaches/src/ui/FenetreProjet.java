package ui;

import model.Projet;
import dao.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FenetreProjet extends JFrame {
    private DefaultListModel<Projet> model;
    private JList<Projet> liste;
    private JButton ajouter, modifier, supprimer;

    public FenetreProjet() {
        setTitle("Gestion des Projets");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        model = new DefaultListModel<>();
        liste = new JList<>(model);
        chargerProjets();

        ajouter = new JButton("Ajouter");
        modifier = new JButton("Modifier");
        supprimer = new JButton("Supprimer");

        ajouter.addActionListener(e -> ajouterProjet());
        modifier.addActionListener(e -> modifierProjet());
        supprimer.addActionListener(e -> supprimerProjet());

        JPanel boutons = new JPanel();
        boutons.add(ajouter);
        boutons.add(modifier);
        boutons.add(supprimer);

        add(new JScrollPane(liste), BorderLayout.CENTER);
        add(boutons, BorderLayout.SOUTH);
    }

    private void chargerProjets() {
        model.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Projet");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(new Projet(rs.getInt("id"), rs.getString("nom")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ajouterProjet() {
        String nom = JOptionPane.showInputDialog(this, "Nom du projet:");
        if (nom != null) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO Projet (nom) VALUES (?)");
                ps.setString(1, nom);
                ps.executeUpdate();
                chargerProjets();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void modifierProjet() {
        Projet projet = liste.getSelectedValue();
        if (projet != null) {
            String nom = JOptionPane.showInputDialog(this, "Nouveau nom:", projet.getNom());
            if (nom != null) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("UPDATE Projet SET nom=? WHERE id=?");
                    ps.setString(1, nom);
                    ps.setInt(2, projet.getId());
                    ps.executeUpdate();
                    chargerProjets();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void supprimerProjet() {
        Projet projet = liste.getSelectedValue();
        if (projet != null && JOptionPane.showConfirmDialog(this, "Supprimer ?") == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM Projet WHERE id=?");
                ps.setInt(1, projet.getId());
                ps.executeUpdate();
                chargerProjets();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}