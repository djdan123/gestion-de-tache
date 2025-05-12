package ui;

import model.Employe;
import dao.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;



public class FenetreEmploye extends JFrame {
    private DefaultListModel<Employe> model;
    private JList<Employe> liste;
    private JButton ajouter, modifier, supprimer;
    
      public FenetreEmploye() {
        
         if (!FenetrePrincipale.estAdminStatic()) { // Méthode statique à créer
        JOptionPane.showMessageDialog(null,
            "Accès réservé aux administrateurs",
            "Permission refusée",
            JOptionPane.WARNING_MESSAGE);
        dispose();
        return;
    }
        setTitle("Gestion des Employés");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        model = new DefaultListModel<>();
        liste = new JList<>(model);
        chargerEmployes();

        ajouter = new JButton("Ajouter");
        modifier = new JButton("Modifier");
        supprimer = new JButton("Supprimer");

        ajouter.addActionListener(e -> ajouterEmploye());
        modifier.addActionListener(e -> modifierEmploye());
        supprimer.addActionListener(e -> supprimerEmploye());

        JPanel boutons = new JPanel();
        boutons.add(ajouter);
        boutons.add(modifier);
        boutons.add(supprimer);

        add(new JScrollPane(liste), BorderLayout.CENTER);
        add(boutons, BorderLayout.SOUTH);
    }

    private void chargerEmployes() {
        model.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Employe");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addElement(new Employe(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ajouterEmploye() {
        String nom = JOptionPane.showInputDialog(this, "Nom:");
        String prenom = JOptionPane.showInputDialog(this, "Prénom:");
        if (nom != null && prenom != null) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO Employe (nom, prenom) VALUES (?, ?)");
                ps.setString(1, nom);
                ps.setString(2, prenom);
                ps.executeUpdate();
                chargerEmployes();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void modifierEmploye() {
        Employe emp = liste.getSelectedValue();
        if (emp != null) {
            String nom = JOptionPane.showInputDialog(this, "Nouveau nom:", emp.getNom());
            String prenom = JOptionPane.showInputDialog(this, "Nouveau prénom:", emp.getPrenom());
            if (nom != null && prenom != null) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("UPDATE Employe SET nom=?, prenom=? WHERE id=?");
                    ps.setString(1, nom);
                    ps.setString(2, prenom);
                    ps.setInt(3, emp.getId());
                    ps.executeUpdate();
                    chargerEmployes();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void supprimerEmploye() {
        Employe emp = liste.getSelectedValue();
        if (emp != null && JOptionPane.showConfirmDialog(this, "Supprimer ?") == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM Employe WHERE id=?");
                ps.setInt(1, emp.getId());
                ps.executeUpdate();
                chargerEmployes();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}