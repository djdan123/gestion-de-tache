package ui;

import model.Tache;
import model.Employe;
import model.Projet;
import dao.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

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
    Vector<Projet> projets = chargerProjets();
    Vector<Employe> employes = chargerEmployes();
    
    if (projets.isEmpty() || employes.isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            "Aucun projet ou employé disponible", 
            "Erreur", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    JComboBox<Projet> cbProjet = new JComboBox<>(projets);
    JComboBox<Employe> cbEmploye = new JComboBox<>(employes);
    JTextField tfLibelle = new JTextField(20);
    
    JPanel panel = new JPanel(new GridLayout(3, 2));
    panel.add(new JLabel("Libellé:"));
    panel.add(tfLibelle);
    panel.add(new JLabel("Projet:"));
    panel.add(cbProjet);
    panel.add(new JLabel("Employé:"));
    panel.add(cbEmploye);
    
    int result = JOptionPane.showConfirmDialog(this, panel, 
        "Nouvelle Tâche", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    
    if (result == JOptionPane.OK_OPTION) {
        String libelle = tfLibelle.getText();
        if (libelle == null || libelle.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Le libellé ne peut pas être vide", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Projet projet = (Projet) cbProjet.getSelectedItem();
        Employe employe = (Employe) cbEmploye.getSelectedItem();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Insertion de la tâche
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Tache (libelle, id_projet, id_employe) VALUES (?, ?, ?)");
            ps.setString(1, libelle);
            ps.setInt(2, projet.getId());
            ps.setInt(3, employe.getId());
            ps.executeUpdate();
            
            // Création de la notification
            PreparedStatement psNotif = conn.prepareStatement(
                "INSERT INTO Notification (message, date, id_employe) VALUES (?, NOW(), ?)");
            psNotif.setString(1, "Nouvelle tâche: " + libelle + " (Projet: " + projet.getNom() + ")");
            psNotif.setInt(2, employe.getId());
            psNotif.executeUpdate();
            
            chargerTaches();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur: " + e.getMessage(), 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

    private void modifierTache() {
        Tache tache = liste.getSelectedValue();
        if (tache != null) {
            String libelle = JOptionPane.showInputDialog(this, "Nouveau libellé:", tache.getLibelle());
            int idProjet = Integer.parseInt(JOptionPane.showInputDialog(this, "ID projet:", tache.getIdProjet()));
            int idEmploye = Integer.parseInt(JOptionPane.showInputDialog(this, "ID employé:", tache.getIdEmploye()));
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
    private Vector<Projet> chargerProjets() {
    Vector<Projet> projets = new Vector<>();
    try (Connection conn = DatabaseConnection.getConnection()) {
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM Projet");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            projets.add(new Projet(rs.getInt("id"), rs.getString("nom")));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return projets;
}

private Vector<Employe> chargerEmployes() {
    Vector<Employe> employes = new Vector<>();
    try (Connection conn = DatabaseConnection.getConnection()) {
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM Employe");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            employes.add(new Employe(rs.getInt("id"), rs.getString("nom"), rs.getString("prenom")));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return employes;
}
}