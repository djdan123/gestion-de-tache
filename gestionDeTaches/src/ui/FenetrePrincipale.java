package ui;

import javax.swing.*;
import java.awt.*;

public class FenetrePrincipale extends JFrame {
    private static String currentUserRole;
    private final int idEmploye;
    private final String roleEmploye;

    public FenetrePrincipale(int idEmploye, String roleEmploye) {
        this.idEmploye = idEmploye;
        this.roleEmploye = roleEmploye;

        setTitle("Gestion des Tâches - " + (isAdmin() ? "Administrateur" : "Employé"));
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initUI();
    }
    public static boolean estAdminStatic() {
    // Solution temporaire - À remplacer par un vrai système de session
    return "admin".equals(currentUserRole); // Variable à adapter
}

    private boolean isAdmin() {
        return "admin".equals(roleEmploye);
    }

    private void initUI() {
        JButton btnEmployes = new JButton("Gérer les employés");
        JButton btnProjets = new JButton("Gérer les projets");
        JButton btnTaches = new JButton("Gérer les tâches");
        JButton btnStats = new JButton("Statistiques");

        // Désactiver certaines fonctionnalités pour les non-admins
        if (!isAdmin()) {
            btnEmployes.setEnabled(false);
            btnStats.setEnabled(false);
        }

        // Gestion des événements
        btnEmployes.addActionListener(e -> new FenetreEmploye().setVisible(true));
        btnProjets.addActionListener(e -> new FenetreProjet().setVisible(true));
        btnTaches.addActionListener(e -> new FenetreTache().setVisible(true));
        btnStats.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fonctionnalité à venir"));

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        panel.add(btnEmployes);
        panel.add(btnProjets);
        panel.add(btnTaches);
        panel.add(btnStats);

        add(panel);
    }
}